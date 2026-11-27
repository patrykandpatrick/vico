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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartPipeline.Companion.drawItemKey
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.AxisManager
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.cartesian.marker.CandlestickCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.data.CacheStore
import com.patrykandpatrick.vico.compose.common.gcdWith
import com.patrykandpatrick.vico.compose.common.orZero
import kotlin.math.abs

private fun getDefaultXStep(model: CartesianChartModel, minX: Double): Double {
  var gcd = model.getXDeltaGcd()
  val nonEmptyModels = model.models.filterNot { it.isEmpty }
  if (nonEmptyModels.isEmpty()) return gcd
  val minXOffset = nonEmptyModels.minOf { it.minX } - minX
  if (minXOffset != 0.0) {
    gcd = gcd.gcdWith(abs(minXOffset))
    require(gcd != 0.0) {
      "The x-values are too precise. The maximum precision is four decimal places."
    }
  }
  return gcd
}

/**
 * Returns a key identifying [target] across data updates: its layer ([layerKey]) and, for the
 * built-in targets, its series (for a column stack, its first series). Other targets are identified
 * by their position among their layer’s targets ([index]).
 */
private fun getMarkerTargetKey(layerKey: Any, target: CartesianMarker.Target, index: Int): Any =
  layerKey to
    when (target) {
      is ColumnCartesianLayerMarkerTarget -> target.columns.firstOrNull()?.entry?.seriesKey
      is CandlestickCartesianLayerMarkerTarget -> target.seriesKey
      is LineCartesianLayerMarkerTarget -> LineCartesianLayerMarkerTarget::class
      else -> index
    }

/**
 * A [CartesianLayer] to draw. [exitRanges] is non-`null` for exiting [CartesianLayer]s, which are
 * drawn with the _y_-ranges they had before they were removed.
 */
internal class CartesianLayerDrawItem(
  val layer: CartesianLayer<*>,
  val exitRanges: CartesianChartRanges? = null,
)

/** Uses the _x_-ranges of [xRanges] and the _y_-ranges of [yRanges]. */
internal class ExitingLayerRanges(
  private val xRanges: CartesianChartRanges,
  private val yRanges: CartesianChartRanges,
) : CartesianChartRanges {
  override val minX: Double
    get() = xRanges.minX

  override val maxX: Double
    get() = xRanges.maxX

  override val xStep: Double
    get() = xRanges.xStep

  override fun getYRange(axisPosition: Axis.Position.Vertical?): CartesianChartRanges.YRange =
    yRanges.getYRange(axisPosition)
}

/** Tracks marker visibility across the [CartesianChart]s drawn by a host. */
internal class MarkerVisibilityTracker {
  var previousTargetHashCode: Int? = null

  /**
   * Identifies the marked target when a single one is selected (see `markerSeriesIndex`), by its
   * layer and series rather than by its position among the targets, so that the selection follows
   * the target when layers, series, or stacks are added or removed. `null` means the selection
   * hasn’t been resolved yet, in which case the positional index is used.
   */
  var selectedTargetKey: Any? = null
}

/**
 * A chart based on a Cartesian coordinate plane, composed of [CartesianLayer]s. Each
 * [CartesianLayer] carries its own data. Use [rememberCartesianChart] to create [CartesianChart]s.
 *
 * A [CartesianChart] is opaque: its configuration is set via [rememberCartesianChart] and read only
 * by the library. It holds draw-time state (e.g., its bounds and marker targets), so an instance
 * must be displayed by at most one host at a time. To show the same data twice, create two
 * [CartesianChart]s (they can share the same layer models).
 */
@Stable
public class CartesianChart
internal constructor(
  layers: List<CartesianLayer<*>>,
  startAxis: Axis<Axis.Position.Vertical.Start>?,
  topAxis: Axis<Axis.Position.Horizontal.Top>?,
  endAxis: Axis<Axis.Position.Vertical.End>?,
  bottomAxis: Axis<Axis.Position.Horizontal.Bottom>?,
  internal val marker: CartesianMarker?,
  internal val markerVisibilityListener: CartesianMarkerVisibilityListener?,
  internal val layerPadding: CartesianLayerPadding,
  internal val legend: Legend<CartesianMeasuringContext, CartesianDrawingContext>?,
  internal val fadingEdges: FadingEdges?,
  internal val decorations: List<Decoration>,
  persistentMarkers: Map<out Number, CartesianMarker>,
  internal val xStep: Double?,
  internal val markerController: CartesianMarkerController,
) : CartesianLayerMarginUpdater {
  private val persistentMarkers: Map<Double, CartesianMarker> =
    persistentMarkers.mapKeys { (x, _) ->
      x.toDouble()
    }
  private val layerMargins = CartesianLayerMargins()
  private val axisManager = AxisManager()
  private val _markerTargets = mutableMapOf<Double, MutableList<CartesianMarker.Target>>()
  private val markerTargetKeys = mutableMapOf<Double, MutableList<Any>>()

  internal var layerBounds: Rect = Rect.Zero

  /**
   * Whether this [CartesianChart] has been drawn. Until then, its [layerBounds] and marker targets
   * aren’t known, so interactions aren’t resolved against it.
   */
  internal var isDrawn: Boolean = false
    private set

  /** The [CartesianLayer]s of which this [CartesianChart] is composed. */
  internal val layers: List<CartesianLayer<*>> = layers.toList()

  private val defaultDrawList = this.layers.map(::CartesianLayerDrawItem)

  private var measuredLayers: List<CartesianLayer<*>> = this.layers

  /** Aggregates the [CartesianLayer]s’ data. */
  internal val model: CartesianChartModel = CartesianChartModel(this.layers.map { it.model })

  /** Links _x_-values to [CartesianMarker.Target]s. */
  internal val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

  /** The start [Axis]. */
  internal val startAxis: Axis<Axis.Position.Vertical.Start>? by axisManager::startAxis

  /** The top [Axis]. */
  internal val topAxis: Axis<Axis.Position.Horizontal.Top>? by axisManager::topAxis

  /** The end [Axis]. */
  internal val endAxis: Axis<Axis.Position.Vertical.End>? by axisManager::endAxis

  /** The bottom [Axis]. */
  internal val bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? by axisManager::bottomAxis

  init {
    val drawItemKeys = mutableSetOf<Any>()
    this.layers.forEach { layer ->
      require(drawItemKeys.add(layer.drawItemKey())) {
        if (layer is AnimatedCartesianLayer<*, *>) {
          "Two `AnimatedCartesianLayer`s have the same key (${layer.key}). Keys must be unique " +
            "among a `CartesianChart`’s layers; use `rememberCartesianLayerKey` for each layer."
        } else {
          "The same `CartesianLayer` (or two equal ones) was passed twice. Each layer may appear " +
            "only once in a `CartesianChart`."
        }
      }
    }
    axisManager.startAxis = startAxis
    axisManager.topAxis = topAxis
    axisManager.endAxis = endAxis
    axisManager.bottomAxis = bottomAxis
  }

  private fun setLayerBounds(left: Float, top: Float, right: Float, bottom: Float) {
    layerBounds = Rect(left, top, right, bottom)
  }

  private fun marginUpdaters(): List<CartesianLayerMarginUpdater> = buildList {
    add(this@CartesianChart)
    addAll(axisManager.axisCache)
    marker?.let(::add)
    addAll(persistentMarkers.values)
  }

  /**
   * Populates [layerMargins] (and [layerDimensions]) and returns the legend height. The result
   * depends on [CartesianMeasuringContext.canvasWidth] but not the canvas height, so it can be used
   * to measure the chart before its height is known.
   */
  private fun measure(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
    drawList: List<CartesianLayerDrawItem>,
  ): Float =
    with(context) {
      layerMargins.clear()
      // Exiting layers are measured too, so that the layout doesn’t change until they’re dropped.
      measuredLayers = drawList.map { it.layer }
      measuredLayers.forEach { it.updateDimensions(context, layerDimensions) }
      startAxis?.updateLayerDimensions(context, layerDimensions)
      topAxis?.updateLayerDimensions(context, layerDimensions)
      endAxis?.updateLayerDimensions(context, layerDimensions)
      bottomAxis?.updateLayerDimensions(context, layerDimensions)
      marginUpdaters().forEach { updater ->
        updater.updateLayerMargins(context, layerMargins, layerDimensions)
      }
      legend?.getHeight(context, canvasWidth).orZero
    }

  /**
   * Returns the total vertical space taken up by the layer margins and the legend—i.e., the height
   * the chart needs in addition to the coordinate system. This is height-independent, so it can be
   * used to derive the chart’s height from a desired coordinate-system height.
   */
  internal fun getVerticalExtras(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
    drawList: List<CartesianLayerDrawItem> = defaultDrawList,
  ): Float {
    val legendHeight = measure(context, layerDimensions, drawList)
    return layerMargins.vertical + legendHeight
  }

  internal fun prepare(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
    canvasSize: Size,
    drawList: List<CartesianLayerDrawItem> = defaultDrawList,
  ) {
    with(context) {
      val legendHeight = measure(context, layerDimensions, drawList)
      val freeHeight = canvasSize.height - layerMargins.vertical - legendHeight
      marginUpdaters().forEach { updater ->
        updater.updateHorizontalLayerMargins(context, layerMargins, freeHeight)
      }
      setLayerBounds(
        layerMargins.getLeft(isLtr),
        layerMargins.top,
        canvasSize.width - layerMargins.getRight(isLtr),
        canvasSize.height - layerMargins.bottom - legendHeight,
      )
      axisManager.setAxesBounds(context, canvasSize, layerBounds, layerMargins)
      legend?.setBounds(
        left = 0,
        top = layerBounds.bottom + layerMargins.bottom,
        right = canvasSize.width,
        bottom = layerBounds.bottom + layerMargins.bottom + legendHeight,
      )
    }
  }

  internal fun draw(
    context: CartesianDrawingContext,
    markerVisibilityTracker: MarkerVisibilityTracker = MarkerVisibilityTracker(),
    drawList: List<CartesianLayerDrawItem> = defaultDrawList,
  ) {
    with(context) {
      // The marker targets are rebuilt here (and only here), so measuring, which also happens in
      // the layout phase, doesn’t clear them.
      _markerTargets.clear()
      markerTargetKeys.clear()
      if (fadingEdges != null) canvas.saveLayer(Rect(Offset.Zero, canvasSize), EmptyPaint)
      decorations.forEach { it.drawUnderLayers(context) }
      axisManager.drawUnderLayers(context)
      val (layerBitmap, layerCanvas) = getBitmap(cacheKeyNamespace)
      withCanvas(layerCanvas) {
        drawList.forEach { item ->
          val exitRanges = item.exitRanges
          if (exitRanges != null) {
            item.layer.draw(context.withRanges(ExitingLayerRanges(context.ranges, exitRanges)))
          } else {
            item.layer.draw(context)
            val layerKey = item.layer.drawItemKey()
            item.layer.markerTargets.forEach { (x, targets) ->
              _markerTargets.getOrPut(x) { mutableListOf() } += targets
              val keys = markerTargetKeys.getOrPut(x) { mutableListOf() }
              targets.forEachIndexed { index, target ->
                keys += getMarkerTargetKey(layerKey, target, index)
              }
            }
          }
        }
      }
      val sortedMarkerTargetPairs = _markerTargets.toList().sortedBy { it.first }
      _markerTargets.clear()
      _markerTargets.putAll(sortedMarkerTargetPairs)
      forEachPersistentMarker { marker, targets -> marker.drawUnderLayers(context, targets) }
      val markerTargets = getMarkerTargets(markerX, markerSeriesIndex, markerVisibilityTracker)
      val drawMarker = markerTargets.isNotEmpty()
      if (drawMarker) marker?.drawUnderLayers(context, markerTargets)
      canvas.drawImage(layerBitmap, Offset.Zero, EmptyPaint)
      fadingEdges?.run {
        draw(context)
        canvas.restore()
      }
      axisManager.drawOverLayers(context)
      decorations.forEach { it.drawOverLayers(context) }
      forEachPersistentMarker { marker, targets -> marker.drawOverLayers(context, targets) }
      legend?.draw(context)
      if (drawMarker) marker?.drawOverLayers(context, markerTargets)
    }
    isDrawn = true
  }

  /**
   * Updates [ranges] in accordance with the [CartesianLayer]s’ data. This may be called off the
   * main thread.
   */
  internal fun updateRanges(ranges: MutableCartesianChartRanges) {
    // Layers with empty models come last, so they can follow the ranges of the others.
    layers.forEach { if (!it.model.isEmpty) it.updateChartRanges(ranges) }
    layers.forEach { if (it.model.isEmpty) it.updateChartRanges(ranges) }
    ranges.ensureNonZeroYLengths()
    ranges.xStep = xStep ?: getDefaultXStep(model, ranges.minX)
  }

  override fun updateLayerMargins(
    context: CartesianMeasuringContext,
    layerMargins: CartesianLayerMargins,
    layerDimensions: CartesianLayerDimensions,
  ) {
    measuredLayers.forEach { it.updateLayerMargins(context, layerMargins, layerDimensions) }
  }

  override fun updateHorizontalLayerMargins(
    context: CartesianMeasuringContext,
    horizontalLayerMargins: HorizontalCartesianLayerMargins,
    layerHeight: Float,
  ) {
    measuredLayers.forEach {
      it.updateHorizontalLayerMargins(context, horizontalLayerMargins, layerHeight)
    }
  }

  private inline fun forEachPersistentMarker(
    block: (CartesianMarker, List<CartesianMarker.Target>) -> Unit
  ) {
    persistentMarkers.forEach { (x, marker) ->
      markerTargets[x]?.also { targets -> block(marker, targets) }
    }
  }

  /** Returns the [CartesianMarker.Target]s closest to [x]. */
  internal fun getMarkerTargets(
    x: Double?,
    visibleXRange: ClosedFloatingPointRange<Double>,
  ): List<CartesianMarker.Target> =
    if (x == null || x !in visibleXRange || markerTargets.isEmpty()) {
      emptyList()
    } else {
      var targets = emptyList<CartesianMarker.Target>()
      var previousDelta = Double.POSITIVE_INFINITY
      for ((key, keyTargets) in markerTargets) {
        val delta = abs(key - x)
        if (delta > previousDelta) break
        targets = keyTargets
        previousDelta = delta
      }
      targets
    }

  private fun getMarkerTargets(
    x: Double?,
    seriesIndex: Int?,
    tracker: MarkerVisibilityTracker,
  ): List<CartesianMarker.Target> {
    val marker = marker ?: return emptyList()
    fun hide(): List<CartesianMarker.Target> {
      if (tracker.previousTargetHashCode != null) markerVisibilityListener?.onHidden(marker)
      tracker.previousTargetHashCode = null
      return emptyList()
    }
    if (x == null || markerTargets.isEmpty()) return hide()
    val allTargets = markerTargets[x] ?: return emptyList()
    val targets =
      if (seriesIndex != null) {
        val keys = markerTargetKeys[x].orEmpty()
        val index = tracker.selectedTargetKey?.let(keys::indexOf) ?: seriesIndex
        val target = allTargets.getOrNull(index) ?: return hide()
        tracker.selectedTargetKey = keys.getOrNull(index)
        listOf(target)
      } else {
        allTargets
      }
    val targetHashCode = targets.hashCode()
    if (tracker.previousTargetHashCode == null) {
      markerVisibilityListener?.onShown(marker, targets)
    } else if (targetHashCode != tracker.previousTargetHashCode) {
      markerVisibilityListener?.onUpdated(marker, targets)
    }
    tracker.previousTargetHashCode = targetHashCode
    return targets
  }

  private companion object {
    val cacheKeyNamespace: CacheStore.KeyNamespace = CacheStore.KeyNamespace()
  }
}

/**
 * Creates and remembers a [CartesianChart]. A new [CartesianChart] is created whenever a layer or
 * another argument changes—e.g., because of a data update—and the [CartesianChartHost] applies it.
 *
 * @param layers the [CartesianLayer]s. `null`s are ignored, so layers can be added and removed
 *   conditionally (e.g., `model?.let { rememberLineCartesianLayer(it) }`).
 * @param startAxis the start [Axis].
 * @param topAxis the top [Axis].
 * @param endAxis the end [Axis].
 * @param bottomAxis the bottom [Axis].
 * @param marker appears when the [CartesianChart] is tapped.
 * @param markerVisibilityListener allows for listening to [marker] visibility changes.
 * @param layerPadding the [CartesianLayerPadding].
 * @param legend the legend.
 * @param fadingEdges applies a horizontal fade to the edges of the [CartesianChart], provided that
 *   it’s scrollable.
 * @param decorations the [Decoration]s.
 * @param persistentMarkers maps _x_-values to persistent [CartesianMarker]s, which are shown at
 *   those _x_-values regardless of interaction.
 * @param xStep the _x_-step (the difference between neighboring major _x_-values). If this is
 *   `null`, the greatest common divisor of the _x_-values’ differences is used.
 * @param markerController controls [marker] visibility.
 *
 * The returned [CartesianChart] holds draw-time state, so display it in at most one host at a time;
 * to show the same data twice, call [rememberCartesianChart] twice.
 *
 * @see rememberCandlestickCartesianLayer
 * @see rememberColumnCartesianLayer
 * @see rememberLineCartesianLayer
 */
@Composable
public fun rememberCartesianChart(
  vararg layers: CartesianLayer<*>?,
  startAxis: Axis<Axis.Position.Vertical.Start>? = null,
  topAxis: Axis<Axis.Position.Horizontal.Top>? = null,
  endAxis: Axis<Axis.Position.Vertical.End>? = null,
  bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? = null,
  marker: CartesianMarker? = null,
  markerVisibilityListener: CartesianMarkerVisibilityListener? = null,
  layerPadding: CartesianLayerPadding = CartesianLayerPadding(),
  legend: Legend<CartesianMeasuringContext, CartesianDrawingContext>? = null,
  fadingEdges: FadingEdges? = null,
  decorations: List<Decoration> = emptyList(),
  persistentMarkers: Map<out Number, CartesianMarker> = emptyMap(),
  xStep: Double? = null,
  markerController: CartesianMarkerController = CartesianMarkerController.rememberShowOnPress(),
): CartesianChart =
  remember(
    *layers,
    startAxis,
    topAxis,
    endAxis,
    bottomAxis,
    marker,
    markerVisibilityListener,
    layerPadding,
    legend,
    fadingEdges,
    decorations,
    persistentMarkers,
    xStep,
    markerController,
  ) {
    CartesianChart(
      layers = layers.filterNotNull(),
      startAxis = startAxis,
      topAxis = topAxis,
      endAxis = endAxis,
      bottomAxis = bottomAxis,
      marker = marker,
      markerVisibilityListener = markerVisibilityListener,
      layerPadding = layerPadding,
      legend = legend,
      fadingEdges = fadingEdges,
      decorations = decorations,
      persistentMarkers = persistentMarkers,
      xStep = xStep,
      markerController = markerController,
    )
  }
