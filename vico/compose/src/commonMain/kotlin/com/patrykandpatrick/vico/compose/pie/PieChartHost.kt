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

package com.patrykandpatrick.vico.compose.pie

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Animation
import com.patrykandpatrick.vico.compose.common.ChartHostBox
import com.patrykandpatrick.vico.compose.common.Defaults
import com.patrykandpatrick.vico.compose.common.MutableDrawScope
import com.patrykandpatrick.vico.compose.common.ValueWrapper
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel

internal val defaultPieDiffAnimationSpec: AnimationSpec<Float> =
  tween(durationMillis = Animation.PIE_DIFF_DURATION)

/**
 * What a [PieChartHost] displays: [chart] with [drawingModel], the current frame of the animation
 * toward [targetDrawingModel], which is [chart]’s model’s drawing model. [startDrawingModel] is
 * where the animation started. [animation] identifies the animation (drawing models can be equal
 * across data updates, so they can’t), and it’s `null` if nothing is animated.
 */
internal class PieChartDisplay(
  val chart: PieChart,
  val drawingModel: PieChartDrawingModel,
  val targetDrawingModel: PieChartDrawingModel,
  val startDrawingModel: PieChartDrawingModel?,
  val animation: Any?,
  val animationSpec: AnimationSpec<Float>? = null,
)

/**
 * Determines what to display for [chart], given the [previous] display. Returns `null` if
 * [previous] already displays [chart]. A [PieChart] whose model matches [previous]’s is applied
 * without restarting the animation.
 */
internal fun preparePieChartDisplay(
  chart: PieChart,
  previous: PieChartDisplay?,
  animationSpec: AnimationSpec<Float>?,
): PieChartDisplay? {
  if (previous?.chart === chart) return null
  if (previous != null && previous.chart.model == chart.model) {
    return PieChartDisplay(
      chart,
      previous.drawingModel,
      previous.targetDrawingModel,
      previous.startDrawingModel,
      previous.animation,
      previous.animationSpec,
    )
  }
  val target = chart.createDrawingModel()
  // The slices on screen are restyled via the chart that knows them, so that slices that exit keep
  // their latest style.
  val start = previous?.drawingModel?.withStylesFrom(previous.chart)
  return if (animationSpec != null) {
    PieChartDisplay(chart, target.transform(start, 0f), target, start, Any(), animationSpec)
  } else {
    PieChartDisplay(chart, target, target, null, animation = null)
  }
}

/**
 * Displays a [PieChart]. The [PieChart] carries the data, so to update the data, pass a new
 * [PieChart] (e.g., via [rememberPieChart] with a new model). The first [PieChart] is drawn in the
 * first frame. A [PieChart] whose model has no entries takes up space but draws nothing (once any
 * exiting slices are gone).
 *
 * @param chart the [PieChart].
 * @param modifier the modifier to be applied to the chart.
 * @param rotationState houses information on the [PieChart]’s rotation. Passing a
 *   [PieRotationState] with rotation enabled turns on drag-to-rotate.
 * @param animationSpec the [AnimationSpec] for difference animations. Slices are matched by
 *   [PieChartModel.Entry.key]: remaining slices animate to their new angles, new ones grow in at
 *   their positions, and removed ones shrink and fade out at theirs, drawn with the style they had
 *   (so the pie stays a full circle). An outside label’s room is reserved in full while its slice
 *   is drawn: from the first frame of its entry until its exit ends. Going to a model without
 *   entries animates every slice out. Set to `null` to apply data updates without animation.
 * @param initialAnimationSpec the [AnimationSpec] for the initial animation. Defaults to
 *   [animationSpec]. Set to `null` to skip the initial animation. The initial animation is also
 *   skipped in previews and after configuration changes.
 * @param chartAreaHeight the default diameter of the pie, to which the heights of the legend and
 *   other components are added. Used only when the height isn’t otherwise constrained (e.g., via
 *   [Modifier.height]).
 */
@Composable
public fun PieChartHost(
  chart: PieChart,
  modifier: Modifier = Modifier,
  rotationState: PieRotationState = rememberPieRotationState(),
  animationSpec: AnimationSpec<Float>? = defaultPieDiffAnimationSpec,
  initialAnimationSpec: AnimationSpec<Float>? = animationSpec,
  chartAreaHeight: Dp = Defaults.PIE_CHART_AREA_HEIGHT.dp,
) {
  val isInPreview = LocalInspectionMode.current
  // `true` only when the host is restored after showing data (e.g., after a configuration change).
  var hasShownData by rememberSaveable { mutableStateOf(false) }
  // Whether the next reveal is skipped. Only the first chart after a restoration skips it.
  val skipReveal = remember { ValueWrapper(hasShownData) }
  val displayState = remember { mutableStateOf<PieChartDisplay?>(null) }
  // What the layout depends on (the set of slices and their labels). This is published when a chart
  // is applied and when an animation ends (when exiting slices are dropped), not with every frame.
  val layoutState = remember { mutableStateOf<PieChartDisplay?>(null) }

  // A reveal (from no slices) uses `initialAnimationSpec`; other updates use `animationSpec`.
  fun getAnimationSpec(previous: PieChartDisplay?): AnimationSpec<Float>? =
    when {
      isInPreview -> null
      previous == null || previous.drawingModel.slices.isEmpty() ->
        initialAnimationSpec.takeUnless { skipReveal.value }
      else -> animationSpec
    }

  // `displayState` changes with every animation frame, so it’s read without subscribing this scope.
  val previous = Snapshot.withoutReadObservation { displayState.value }
  val prepared = preparePieChartDisplay(chart, previous, getAnimationSpec(previous))
  val display = prepared ?: previous!!
  SideEffect {
    if (prepared != null) {
      // If the display changed since this composition read it (e.g., an animation frame), the
      // display is prepared again from what’s on screen, so no stale frame is applied.
      val current = displayState.value
      val applied =
        if (current === previous) prepared
        else preparePieChartDisplay(chart, current, getAnimationSpec(current))
      if (applied != null) {
        displayState.value = applied
        layoutState.value = applied
      }
      skipReveal.value = false
    }
    if (chart.model.entries.isNotEmpty()) hasShownData = true
  }

  // This reads the layout state, which changes on commits and animation ends, not every frame.
  val animatedDisplay = layoutState.value?.takeIf { it.animation != null }
  LaunchedEffect(animatedDisplay?.animation) {
    val animatedDisplay = animatedDisplay ?: return@LaunchedEffect
    val animation = animatedDisplay.animation
    val spec = animatedDisplay.animationSpec ?: return@LaunchedEffect
    val target = animatedDisplay.targetDrawingModel
    val start = animatedDisplay.startDrawingModel
    animate(initialValue = 0f, targetValue = 1f, animationSpec = spec) { fraction, _ ->
      val current = displayState.value ?: return@animate
      if (current.animation !== animation) return@animate
      displayState.value =
        PieChartDisplay(
          current.chart,
          target.transform(start, fraction),
          target,
          start,
          animation,
          spec,
        )
    }
    // Finalize once the animation is over, whatever the last callback’s fraction was: the exited
    // slices and the transition’s start are dropped.
    val current = displayState.value ?: return@LaunchedEffect
    if (current.animation !== animation) return@LaunchedEffect
    val final = PieChartDisplay(current.chart, target, target, null, animation = null)
    displayState.value = final
    layoutState.value = final
  }

  val displayedChart = display.chart
  val measuringContext = rememberPieChartMeasuringContext(displayedChart.model)
  // The room for outside labels depends only on the layout state and the width. It’s computed by
  // whichever phase needs it first (the layout phase doesn’t run when the height is fixed) and
  // cached, so neither phase depends on a side effect of the other.
  val labelInsetsCache = remember { ValueWrapper<Pair<Any, PieInsets>?>(null) }
  fun getLabelInsets(layoutDisplay: PieChartDisplay, width: Float): PieInsets {
    val context = measuringContext.value
    val chartAreaHeightPx = with(context) { chartAreaHeight.pixels }
    val key = Triple(layoutDisplay, width, chartAreaHeightPx)
    labelInsetsCache.value?.let { (cachedKey, insets) -> if (cachedKey == key) return insets }
    context.canvasWidth = width
    val insets =
      if (layoutDisplay.drawingModel.slices.isEmpty()) {
        PieInsets()
      } else {
        layoutDisplay.chart.getLabelInsets(
          context,
          Rect(0f, 0f, width, chartAreaHeightPx),
          layoutDisplay.drawingModel,
        )
      }
    labelInsetsCache.value = key to insets
    return insets
  }
  val measureExtras =
    remember(measuringContext, chartAreaHeight) {
      { widthPx: Int ->
        // This reads the layout state, not the per-frame display, so animations don’t cause layout
        // passes. Its slices match the drawn ones throughout a transition.
        val layoutDisplay = layoutState.value
        if (layoutDisplay == null || layoutDisplay.drawingModel.slices.isEmpty()) {
          0f
        } else {
          val width = widthPx.toFloat()
          val insets = getLabelInsets(layoutDisplay, width)
          measuringContext.value.canvasWidth = width
          layoutDisplay.chart.getLegendHeight(measuringContext.value) + insets.top + insets.bottom
        }
      }
    }
  val currentChart by rememberUpdatedState(displayedChart)

  ChartHostBox(modifier, chartAreaHeight, measureExtras) {
    Canvas(
      modifier =
        Modifier.fillMaxSize().pointerInput(rotationState) {
          detectPieRotationGestures(rotationState) { currentChart.gestureGeometry }
        },
    ) {
      if (size.isEmpty()) return@Canvas
      val currentDisplay = displayState.value ?: display
      val chart = currentDisplay.chart
      if (currentDisplay.drawingModel.slices.isEmpty()) return@Canvas
      val labelInsets = getLabelInsets(layoutState.value ?: currentDisplay, size.width)
      measuringContext.value.canvasWidth = size.width
      val mutableDrawScope = MutableDrawScope(this)
      val legendHeight = chart.getLegendHeight(measuringContext.value)
      val chartBounds =
        Rect(
          left = 0f,
          top = labelInsets.top,
          right = size.width,
          bottom = size.height - legendHeight - labelInsets.bottom,
        )
      if (chartBounds.isEmpty) return@Canvas
      chart.bounds = chartBounds
      chart.legend?.setBounds(0f, size.height - legendHeight, size.width, size.height)
      val drawingContext =
        PieChartDrawingContext(
          measuringContext.value,
          size,
          drawContext.canvas,
          chartBounds,
          mutableDrawScope,
        )
      chart.draw(
        drawingContext,
        currentDisplay.drawingModel,
        rotationState.angle,
        fitLabelsInBounds = false,
      )
      measuringContext.value.cacheStore.purge()
    }
  }
}
