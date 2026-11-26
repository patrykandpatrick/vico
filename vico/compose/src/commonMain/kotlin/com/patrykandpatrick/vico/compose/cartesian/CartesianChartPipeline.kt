/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.withFrameNanos
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerDrawingModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerDrawingModelInterpolator.Transition
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.isSameDataAs
import com.patrykandpatrick.vico.compose.cartesian.data.mergeKeys
import com.patrykandpatrick.vico.compose.cartesian.data.toImmutable
import com.patrykandpatrick.vico.compose.cartesian.layer.AnimatedCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.DerivationInputs
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A [CartesianChart] together with everything derived from it. [drawingModels] holds the final
 * drawing models of the [CartesianChart]’s [AnimatedCartesianLayer]s, by key.
 */
internal class CartesianChartFrame(
  val chart: CartesianChart,
  val ranges: CartesianChartRanges,
  val drawingModels: Map<Any, Any>,
  val generation: Long,
)

/**
 * What’s on screen: a [frame], the [drawList] (the [frame]’s layers plus any exiting layers, in
 * draw order), and the [drawingModels] of the current animation frame, by layer key.
 */
internal class CartesianChartDisplay(
  val frame: CartesianChartFrame,
  val drawList: List<CartesianLayerDrawItem>,
  val drawingModels: Map<Any, Any>,
) {
  /** Checks that every [AnimatedCartesianLayer] in [drawList] has a drawing model. */
  fun check() {
    drawList.forEach { item ->
      val layer = item.layer as? AnimatedCartesianLayer<*, *> ?: return@forEach
      check(layer.key in drawingModels) { "No drawing model for the layer with key ${layer.key}." }
    }
  }
}

/**
 * What the layout depends on: a [frame], the [drawList], and drawing models whose series keys match
 * those on screen. This changes only when a [CartesianChart] is applied and when an animation ends
 * (when layers and series that were exiting are dropped), not with every animation frame.
 */
internal class CartesianChartLayout(
  val frame: CartesianChartFrame,
  val drawList: List<CartesianLayerDrawItem>,
  val drawingModels: Map<Any, Any>,
)

/**
 * The asynchronous pipeline behind [CartesianChartHost]. It turns [CartesianChart]s into
 * [CartesianChartDisplay]s and owns the difference animations.
 * - A [CartesianChart] is applied in one step, together with its ranges and drawing models. Until a
 *   [CartesianChart] is ready, the previous [CartesianChartDisplay] stays on screen.
 * - The first [CartesianChart] with layers, [CartesianChart]s whose layer models are unchanged
 *   (settings-only updates), and, in previews, all [CartesianChart]s are applied synchronously.
 * - Removed layers (including the last one) exit; a [CartesianChart] without layers keeps the
 *   previous ranges, so the exiting layers can be drawn, and draws nothing once they’re gone.
 * - Other [CartesianChart]s are processed on [backgroundContext]. Pending [CartesianChart]s are
 *   conflated: only the latest is processed next, and each processed one is applied.
 * - Each [AnimatedCartesianLayer] (including exiting ones) has its own animation timeline. Frames
 *   are interpolated on [backgroundContext], skipping frames while a computation is in progress.
 *   While a derivation reads the display, frames are held and the timelines are paused.
 *
 * All functions must be called on the main thread.
 */
internal class CartesianChartPipeline(
  private val scope: CoroutineScope,
  private val backgroundContext: CoroutineContext = Dispatchers.Default,
) {
  /** The current [CartesianChartDisplay]. This changes with every animation frame. */
  var display: CartesianChartDisplay? by mutableStateOf(null)
    private set

  /** The current [CartesianChartLayout]. This doesn’t change with every animation frame. */
  var layout: CartesianChartLayout? by mutableStateOf(null)
    private set

  /** The current [CartesianChartFrame]. */
  val frame: CartesianChartFrame?
    get() = layout?.frame

  /** The host’s settings, as of the last [apply]. */
  data class Settings(
    val animationSpec: AnimationSpec<Float>?,
    val initialAnimationSpec: AnimationSpec<Float>?,
    val isInPreview: Boolean,
  )

  private var currentSettings = Settings(null, null, false)

  var animationSpec: AnimationSpec<Float>?
    get() = currentSettings.animationSpec
    set(value) {
      currentSettings = currentSettings.copy(animationSpec = value)
    }

  var initialAnimationSpec: AnimationSpec<Float>?
    get() = currentSettings.initialAnimationSpec
    set(value) {
      currentSettings = currentSettings.copy(initialAnimationSpec = value)
    }

  var isInPreview: Boolean
    get() = currentSettings.isInPreview
    set(value) {
      currentSettings = currentSettings.copy(isInPreview = value)
    }

  /**
   * Whether to skip the initial animation of the first [CartesianChart] that this pipeline applies
   * (e.g., because the host was restored after a configuration change). This is cleared once a
   * [CartesianChart] is applied, so later reveals (after the chart has been emptied) animate.
   */
  var skipInitialAnimation: Boolean = false

  private var lastSubmittedChart: CartesianChart? = null
  private var generation = 0L
  private val pendingCharts = Channel<PendingChart>(Channel.CONFLATED)
  private var workerJob: Job? = null
  private var animationJob: Job? = null
  private var derivationCount = 0
  private val animations = mutableMapOf<Any, LayerAnimation>()

  /**
   * Prepares [chart] for display. Returns a [Commit] to be applied (synchronously, before the next
   * frame is drawn) via [apply], or `null` if [chart] is processed asynchronously or is already
   * applied. This doesn’t change the pipeline’s state, so it’s safe to call during composition.
   */
  fun prepare(chart: CartesianChart, settings: Settings = currentSettings): Commit? {
    if (chart === lastSubmittedChart) return null
    // `display` changes with every animation frame, so it’s read without subscribing the caller.
    val display = Snapshot.withoutReadObservation { display }
    return when {
      display == null || display.drawList.isEmpty() ->
        if (chart.layers.isEmpty()) {
          Commit(chart, display, null)
        } else {
          Commit(chart, display, derive(chart, null, getAnimationSpec(display, settings)))
        }
      hasSameData(chart, display.frame.chart) ->
        Commit(chart, display, deriveSettingsOnly(chart, display, settings))
      settings.isInPreview -> Commit(chart, display, derive(chart, display, animationSpec = null))
      else -> Commit(chart, display, null, isAsync = true)
    }
  }

  /**
   * Applies [settings] and [commit] (returned by [prepare]). If the display changed since [commit]
   * was prepared (e.g., an asynchronous commit landed in between), [commit] is prepared again from
   * the current display, so that no stale derivation is applied. This is to be called after
   * composition (e.g., in a `SideEffect`), so a composition that’s never applied changes nothing.
   */
  fun apply(commit: Commit?, settings: Settings = currentSettings) {
    currentSettings = settings
    if (commit == null || commit.chart === lastSubmittedChart) return
    if (!commit.isAsync && commit.basedOn !== display) {
      apply(prepare(commit.chart, settings), settings)
      return
    }
    lastSubmittedChart = commit.chart
    skipInitialAnimation = false
    val generation = ++generation
    when {
      commit.isAsync -> {
        ensureWorker()
        pendingCharts.trySend(PendingChart(commit.chart, generation))
      }
      commit.derived == null -> {
        animations.clear()
        publish(
          CartesianChartDisplay(emptyFrame(commit.chart, generation), emptyList(), emptyMap()),
          updateLayout = true,
        )
      }
      else -> commitDerived(commit.derived, generation)
    }
  }

  /**
   * Returns the [AnimationSpec] for a transition from [previous], or `null` for no animation. A
   * transition from nothing (no display or an empty draw list) is a reveal, which uses
   * [initialAnimationSpec] unless [skipInitialAnimation] is `true`.
   */
  private fun getAnimationSpec(
    previous: CartesianChartDisplay?,
    settings: Settings = currentSettings,
  ): AnimationSpec<Float>? =
    when {
      settings.isInPreview -> null
      previous == null || previous.drawList.isEmpty() ->
        settings.initialAnimationSpec.takeUnless { skipInitialAnimation }
      else -> settings.animationSpec
    }

  private fun emptyFrame(chart: CartesianChart, generation: Long) =
    CartesianChartFrame(chart, MutableCartesianChartRanges().toImmutable(), emptyMap(), generation)

  private fun hasSameData(new: CartesianChart, old: CartesianChart): Boolean {
    if (new.layers.size != old.layers.size) return false
    for (index in new.layers.indices) {
      if (!new.layers[index].model.isSameDataAs(old.layers[index].model)) return false
    }
    return true
  }

  private fun ensureWorker() {
    if (workerJob != null) return
    workerJob = scope.launch {
      for (received in pendingCharts) {
        var pending = received
        while (true) {
          pendingCharts.tryReceive().getOrNull()?.let { pending = it }
          val previous = display
          derivationCount++
          val derived =
            try {
              val chart = pending.chart
              val spec = getAnimationSpec(previous)
              withContext(backgroundContext) { derive(chart, previous, spec) }
            } finally {
              derivationCount--
            }
          val current = display
          // A newer `CartesianChart` was applied synchronously.
          if (current != null && current.frame.generation >= pending.generation) break
          // The display changed during the derivation, so rederive from what’s now on screen.
          if (current !== previous) continue
          commitDerived(derived, pending.generation)
          break
        }
      }
    }
  }

  private fun commitDerived(derived: Derived, generation: Long) {
    val previous = display
    val frame =
      CartesianChartFrame(derived.chart, derived.ranges, derived.finalDrawingModels, generation)
    val spec = derived.animationSpec
    if (derived.isSettingsOnly) {
      derived.restartedKeys.forEach { key ->
        val transition = derived.transitions[key]
        if (transition != null && spec != null) {
          animations[key] = LayerAnimation(transition, spec, isExit = false)
        } else {
          animations.remove(key)
        }
      }
    } else {
      frame.drawingModels.keys.forEach { key ->
        val transition = derived.transitions[key]
        if (transition != null && spec != null) {
          animations[key] = LayerAnimation(transition, spec, isExit = false)
        } else {
          animations.remove(key)
        }
      }
    }
    // `derive` and `deriveSettingsOnly` guarantee that every exiting item in the draw list has a
    // drawing model and an exit transition. A layer that’s already exiting keeps its timeline.
    derived.exitTransitions.forEach { (key, transition) ->
      if (spec != null && animations[key]?.isExit != true) {
        animations[key] = LayerAnimation(transition, spec, isExit = true)
      }
    }
    val exitingKeys = derived.exitTransitions.keys
    animations.keys.retainAll { key -> key in frame.drawingModels || key in exitingKeys }
    val display =
      CartesianChartDisplay(frame, derived.drawList, derived.startDrawingModels).also { it.check() }
    publish(display, updateLayout = true)
    if (animations.isNotEmpty()) ensureAnimationLoop()
  }

  private fun publish(display: CartesianChartDisplay, updateLayout: Boolean) {
    if (updateLayout) {
      layout = CartesianChartLayout(display.frame, display.drawList, display.drawingModels)
    }
    this.display = display
  }

  private fun ensureAnimationLoop() {
    if (animationJob?.isActive == true) return
    animationJob = scope.launch { runAnimations() }
  }

  private suspend fun runAnimations() {
    var previousFrameTimeNanos: Long? = null
    while (animations.isNotEmpty()) {
      val frameTimeNanos = withFrameNanos { it }
      val elapsedNanos = previousFrameTimeNanos?.let { frameTimeNanos - it } ?: 0L
      previousFrameTimeNanos = frameTimeNanos
      // Hold the current frame while a derivation reads it, so that the derived transitions start
      // from what’s on screen. The running timelines are paused meanwhile.
      if (derivationCount > 0) {
        animations.values.forEach { it.delay(elapsedNanos) }
        continue
      }
      val frameAnimations = animations.values.toList()
      frameAnimations.forEach { it.start(frameTimeNanos) }
      val fractions = frameAnimations.map { it.getFraction(frameTimeNanos) }
      val values =
        withContext(backgroundContext) {
          frameAnimations.mapIndexed { index, animation ->
            animation.transition.interpolate(fractions[index])
          }
        }
      val display = display ?: return
      val drawingModels = display.drawingModels.toMutableMap()
      val finishedExitKeys = mutableSetOf<Any>()
      var hasFinishedAnimations = false
      animations.entries.toList().forEach { (key, animation) ->
        val index = frameAnimations.indexOf(animation)
        if (index == -1) return@forEach
        drawingModels[key] = values[index]
        if (animation.isFinished(frameTimeNanos)) {
          animations.remove(key)
          hasFinishedAnimations = true
          if (animation.isExit) finishedExitKeys += key
        }
      }
      finishedExitKeys.forEach(drawingModels::remove)
      val drawList =
        if (finishedExitKeys.isEmpty()) {
          display.drawList
        } else {
          display.drawList.filterNot { item ->
            item.exitRanges != null && item.layer.drawItemKey() in finishedExitKeys
          }
        }
      publish(
        CartesianChartDisplay(display.frame, drawList, drawingModels),
        updateLayout = hasFinishedAnimations,
      )
    }
  }

  /**
   * Updates the pipeline for a settings-only change: the layer models are unchanged, so the running
   * animations continue unless the final drawing models changed (e.g., because the ranges changed).
   * The affected layers animate from their drawing models on screen to the new final ones (or, if
   * animations are disabled, jump to the latter). A pure style change thus applies at once.
   */
  private fun deriveSettingsOnly(
    chart: CartesianChart,
    previous: CartesianChartDisplay,
    settings: Settings,
  ): Derived {
    // Fast path: for each layer whose model and derivation-relevant settings are unchanged (e.g.,
    // after a pure restyle), the previous drawing model is reused; if that holds for every layer,
    // and the chart-level inputs are unchanged, so are the ranges.
    val previousLayers = previous.frame.chart.layers
    val unchangedLayers =
      chart.layers.indices.filter { index ->
        val layer = chart.layers[index]
        layer is DerivationInputs && layer.hasSameDerivationInputs(previousLayers[index])
      }
    val ranges =
      if (unchangedLayers.size == chart.layers.size && chart.xStep == previous.frame.chart.xStep) {
        previous.frame.ranges
      } else {
        getRanges(chart, previous)
      }
    val reuseDrawingModels =
      ranges === previous.frame.ranges || ranges.isSameAs(previous.frame.ranges)
    val finalDrawingModels = buildMap {
      chart.layers.forEachIndexed { index, layer ->
        if (layer !is AnimatedCartesianLayer<*, *>) return@forEachIndexed
        val previousDrawingModel =
          previous.frame.drawingModels[layer.key].takeIf {
            reuseDrawingModels && index in unchangedLayers
          }
        put(layer.key, previousDrawingModel ?: layer.createDrawingModel(ranges))
      }
    }
    val startDrawingModels = previous.drawingModels.toMutableMap()
    val restartedKeys = mutableSetOf<Any>()
    val transitions = mutableMapOf<Any, Transition<Any>>()
    val spec = if (settings.isInPreview) null else settings.animationSpec
    val animate = spec != null
    chart.layers.forEach { layer ->
      if (layer !is AnimatedCartesianLayer<*, *>) return@forEach
      @Suppress("UNCHECKED_CAST") layer as AnimatedCartesianLayer<*, Any>
      val final = finalDrawingModels.getValue(layer.key)
      val previousFinal = previous.frame.drawingModels[layer.key]
      if (previousFinal === final || previousFinal == final) return@forEach
      restartedKeys += layer.key
      val current = previous.drawingModels[layer.key]?.takeIf { it::class == final::class }
      if (animate && current != null) {
        val transition = layer.drawingModelInterpolator.createTransition(current, final)
        transitions[layer.key] = transition
        startDrawingModels[layer.key] = transition.interpolate(0f)
      } else {
        startDrawingModels[layer.key] = final
      }
    }
    // Running exits continue (with their own transitions) if updates are animated; otherwise,
    // the exiting layers are dropped.
    val exitTransitions = mutableMapOf<Any, Transition<Any>>()
    val exitRanges = mutableMapOf<Any, CartesianChartRanges>()
    if (animate) {
      previous.drawList.forEach { item ->
        val itemExitRanges = item.exitRanges ?: return@forEach
        val key = item.layer.drawItemKey()
        if (chart.layers.any { it.drawItemKey() == key }) return@forEach
        val current = previous.drawingModels[key] ?: return@forEach
        @Suppress("UNCHECKED_CAST") val layer = item.layer as AnimatedCartesianLayer<*, Any>
        exitTransitions[key] = layer.createExitTransition(current)
        exitRanges[key] = itemExitRanges
      }
    }
    val keptKeys = finalDrawingModels.keys + exitTransitions.keys
    return Derived(
      chart = chart,
      ranges = ranges,
      finalDrawingModels = finalDrawingModels,
      drawList = getDrawList(chart, previous, exitRanges),
      startDrawingModels = startDrawingModels.filterKeys { it in keptKeys },
      transitions = transitions,
      exitTransitions = exitTransitions,
      animationSpec = spec,
      isSettingsOnly = true,
      restartedKeys = restartedKeys,
    )
  }

  private class PendingChart(val chart: CartesianChart, val generation: Long)

  /** [derived] was prepared from [basedOn], the display at the time. */
  class Commit(
    val chart: CartesianChart,
    val basedOn: CartesianChartDisplay?,
    val derived: Derived?,
    val isAsync: Boolean = false,
  )

  class Derived(
    val chart: CartesianChart,
    val ranges: CartesianChartRanges,
    val finalDrawingModels: Map<Any, Any>,
    val drawList: List<CartesianLayerDrawItem>,
    val startDrawingModels: Map<Any, Any>,
    val transitions: Map<Any, Transition<Any>>,
    val exitTransitions: Map<Any, Transition<Any>>,
    /** The [AnimationSpec] for [transitions] and [exitTransitions], chosen with them. */
    val animationSpec: AnimationSpec<Float>?,
    val isSettingsOnly: Boolean = false,
    val restartedKeys: Set<Any> = emptySet(),
  )

  private class LayerAnimation(
    val transition: Transition<Any>,
    spec: AnimationSpec<Float>,
    val isExit: Boolean,
  ) {
    private val animation = TargetBasedAnimation(spec, Float.VectorConverter, 0f, 1f)
    private var startTimeNanos = Long.MIN_VALUE

    fun start(frameTimeNanos: Long) {
      if (startTimeNanos == Long.MIN_VALUE) startTimeNanos = frameTimeNanos
    }

    /** Pauses the timeline for [nanos], if it has started. */
    fun delay(nanos: Long) {
      if (startTimeNanos != Long.MIN_VALUE) startTimeNanos += nanos
    }

    fun getFraction(frameTimeNanos: Long): Float =
      if (isFinished(frameTimeNanos)) {
        1f
      } else {
        animation.getValueFromNanos(frameTimeNanos - startTimeNanos)
      }

    fun isFinished(frameTimeNanos: Long): Boolean =
      animation.isFinishedFromNanos(frameTimeNanos - startTimeNanos)
  }

  internal companion object {
    fun getRanges(chart: CartesianChart): CartesianChartRanges {
      val ranges = MutableCartesianChartRanges()
      chart.updateRanges(ranges)
      return ranges.toImmutable()
    }

    /**
     * Returns [chart]’s ranges. A [CartesianChart] without layers keeps [previous]’s ranges, which
     * the exiting layers are drawn with.
     */
    fun getRanges(chart: CartesianChart, previous: CartesianChartDisplay?): CartesianChartRanges =
      if (chart.layers.isEmpty()) {
        previous?.frame?.ranges ?: MutableCartesianChartRanges().toImmutable()
      } else {
        getRanges(chart)
      }

    /**
     * Returns this display’s drawing models, with each series attributed to the latest layer that
     * has it in its model (the layer in [CartesianChartDisplay.drawList]), so that series exiting
     * after this update are styled as they were most recently (e.g., after a restyle).
     */
    fun CartesianChartDisplay.getDrawingModelsWithLatestSources(): Map<Any, Any> {
      val layers = drawList.associate { it.layer.drawItemKey() to it.layer }
      return drawingModels.mapValues { (key, drawingModel) ->
        val layer = layers[key] ?: return@mapValues drawingModel
        if (drawingModel !is CartesianLayerDrawingModel<*>) return@mapValues drawingModel
        val layerSeriesKeys = layer.model.getSeriesKeys() ?: return@mapValues drawingModel
        drawingModel.withSeriesSources(
          drawingModel.seriesKeys.mapIndexed { index, seriesKey ->
            if (seriesKey in layerSeriesKeys) layer else drawingModel.getSeriesSource(index)
          },
        )
      }
    }

    private fun CartesianLayerModel.getSeriesKeys(): Collection<Any>? =
      when (this) {
        is LineCartesianLayerModel -> seriesKeys.toSet()
        is ColumnCartesianLayerModel -> seriesKeys.toSet()
        is CandlestickCartesianLayerModel -> setOf(seriesKey)
        else -> null
      }

    /** Whether these ranges and [other] have the same values. */
    fun CartesianChartRanges.isSameAs(other: CartesianChartRanges): Boolean {
      if (minX != other.minX || maxX != other.maxX || xStep != other.xStep) return false
      return listOf(null, Axis.Position.Vertical.Start, Axis.Position.Vertical.End).all { position
        ->
        val a = getYRangeOrNull(position)
        val b = other.getYRangeOrNull(position)
        a?.minY == b?.minY && a?.maxY == b?.maxY
      }
    }

    private fun CartesianChartRanges.getYRangeOrNull(
      position: Axis.Position.Vertical?,
    ): CartesianChartRanges.YRange? = runCatching { getYRange(position) }.getOrNull()

    fun getFinalDrawingModels(chart: CartesianChart, ranges: CartesianChartRanges): Map<Any, Any> =
      buildMap {
        chart.layers.forEach { layer ->
          if (layer is AnimatedCartesianLayer<*, *>)
            put(layer.key, layer.createDrawingModel(ranges))
        }
      }

    fun CartesianLayer<*>.drawItemKey(): Any =
      if (this is AnimatedCartesianLayer<*, *>) key else this

    /** Returns a transition that plays this layer’s entry backward, starting from [current]. */
    fun AnimatedCartesianLayer<*, Any>.createExitTransition(current: Any): Transition<Any> {
      val enter = drawingModelInterpolator.createTransition(null, current)
      return Transition { fraction -> enter.interpolate(1 - fraction) }
    }

    /**
     * Returns the draw list for [chart]: its layers, plus the layers of [previous]’s draw list that
     * are absent from [chart] and are in [exitRanges] (the exiting layers, which keep their
     * positions relative to their neighbors).
     */
    fun getDrawList(
      chart: CartesianChart,
      previous: CartesianChartDisplay?,
      exitRanges: Map<Any, CartesianChartRanges>,
    ): List<CartesianLayerDrawItem> {
      val newItems = chart.layers.associate { it.drawItemKey() to CartesianLayerDrawItem(it) }
      if (previous == null) return newItems.values.toList()
      val previousItems = previous.drawList.associateBy { it.layer.drawItemKey() }
      val exitingItems =
        previousItems
          .mapNotNull { (key, item) ->
            when {
              key in newItems -> null
              key in exitRanges ->
                key to CartesianLayerDrawItem(item.layer, exitRanges.getValue(key))
              else -> null
            }
          }
          .toMap()
      if (exitingItems.isEmpty()) return newItems.values.toList()
      return mergeKeys(
          previous.drawList
            .map { it.layer.drawItemKey() }
            .filter { it in newItems || it in exitingItems },
          newItems.keys.toList(),
        )
        .map { key -> newItems[key] ?: exitingItems.getValue(key) }
    }

    /**
     * Derives a [Derived] for [chart] from [previous]. This doesn’t touch any mutable state, so it
     * can run off the main thread. If [animationSpec] isn’t `null`, [Derived.transitions] and
     * [Derived.exitTransitions] are populated, and [Derived.startDrawingModels] holds the first
     * animation frame. Otherwise, [Derived.startDrawingModels] holds the final drawing models.
     */
    fun derive(
      chart: CartesianChart,
      previous: CartesianChartDisplay?,
      animationSpec: AnimationSpec<Float>?,
    ): Derived {
      val animate = animationSpec != null
      val ranges = getRanges(chart, previous)
      val finalDrawingModels = getFinalDrawingModels(chart, ranges)
      val previousDrawingModels = previous?.getDrawingModelsWithLatestSources().orEmpty()
      val startDrawingModels = mutableMapOf<Any, Any>()
      val transitions = mutableMapOf<Any, Transition<Any>>()
      chart.layers.forEach { layer ->
        if (layer !is AnimatedCartesianLayer<*, *>) return@forEach
        @Suppress("UNCHECKED_CAST") layer as AnimatedCartesianLayer<*, Any>
        val final = finalDrawingModels.getValue(layer.key)
        if (!animate) {
          startDrawingModels[layer.key] = final
          return@forEach
        }
        val old = previousDrawingModels[layer.key]?.takeIf { it::class == final::class }
        val transition = layer.drawingModelInterpolator.createTransition(old, final)
        transitions[layer.key] = transition
        startDrawingModels[layer.key] = transition.interpolate(0f)
      }
      val exitTransitions = mutableMapOf<Any, Transition<Any>>()
      val exitRanges = mutableMapOf<Any, CartesianChartRanges>()
      if (animate && previous != null) {
        previous.drawList.forEach { item ->
          val layer = item.layer as? AnimatedCartesianLayer<*, *> ?: return@forEach
          if (layer.key in finalDrawingModels) return@forEach
          @Suppress("UNCHECKED_CAST") layer as AnimatedCartesianLayer<*, Any>
          val current = previousDrawingModels[layer.key] ?: return@forEach
          exitTransitions[layer.key] = layer.createExitTransition(current)
          exitRanges[layer.key] = item.exitRanges ?: previous.frame.ranges
          startDrawingModels[layer.key] = current
        }
      }
      return Derived(
        chart = chart,
        ranges = ranges,
        finalDrawingModels = finalDrawingModels,
        drawList = getDrawList(chart, previous, exitRanges),
        startDrawingModels = startDrawingModels,
        transitions = transitions,
        exitTransitions = exitTransitions,
        animationSpec = animationSpec,
      )
    }
  }
}
