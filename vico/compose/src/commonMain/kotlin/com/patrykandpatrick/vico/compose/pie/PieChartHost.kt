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
)

/**
 * Determines what to display for [chart], given the [previous] display. Returns `null` if
 * [previous] already displays [chart]. A [PieChart] whose model matches [previous]’s is applied
 * without restarting the animation.
 */
internal fun preparePieChartDisplay(
  chart: PieChart,
  previous: PieChartDisplay?,
  animate: Boolean,
): PieChartDisplay? {
  if (previous?.chart === chart) return null
  if (previous != null && previous.chart.model == chart.model) {
    return PieChartDisplay(
      chart,
      previous.drawingModel,
      previous.targetDrawingModel,
      previous.startDrawingModel,
      previous.animation,
    )
  }
  val target = chart.createDrawingModel()
  // The slices on screen are restyled via the chart that knows them, so that slices that exit keep
  // their latest style.
  val start = previous?.drawingModel?.withStylesFrom(previous.chart)
  return if (animate) {
    PieChartDisplay(chart, target.transform(start, 0f), target, start, animation = Any())
  } else {
    PieChartDisplay(chart, target, target, start, animation = null)
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
  var hasShownData by rememberSaveable { mutableStateOf(false) }
  val displayState = remember { mutableStateOf<PieChartDisplay?>(null) }
  // What the layout depends on (the set of slices and their labels). This is published when a chart
  // is applied and when an animation ends (when exiting slices are dropped), not with every frame.
  val layoutState = remember { mutableStateOf<PieChartDisplay?>(null) }
  // `displayState` changes with every animation frame, so it’s read without subscribing this scope.
  val previous = Snapshot.withoutReadObservation { displayState.value }
  val spec =
    if (previous == null) initialAnimationSpec.takeUnless { hasShownData } else animationSpec
  val prepared = preparePieChartDisplay(chart, previous, animate = spec != null && !isInPreview)
  val display = prepared ?: previous!!
  SideEffect {
    if (prepared != null) {
      displayState.value = prepared
      layoutState.value = prepared
    }
    hasShownData = true
  }

  LaunchedEffect(display.animation) {
    val animation = display.animation
    if (animation == null || spec == null) return@LaunchedEffect
    val target = display.targetDrawingModel
    val start = display.startDrawingModel
    animate(initialValue = 0f, targetValue = 1f, animationSpec = spec) { fraction, _ ->
      val current = displayState.value ?: return@animate
      if (current.animation !== animation) return@animate
      val next =
        PieChartDisplay(
          current.chart,
          target.transform(start, fraction),
          target,
          start,
          animation,
        )
      displayState.value = next
      if (fraction == 1f) layoutState.value = next
    }
  }

  val displayedChart = display.chart
  val measuringContext = rememberPieChartMeasuringContext(displayedChart.model)
  val labelInsets = remember { ValueWrapper(PieInsets()) }
  val measureExtras =
    remember(measuringContext, chartAreaHeight) {
      { widthPx: Int ->
        // This reads the layout state, not the per-frame display, so animations don’t cause layout
        // passes. Its slices match the drawn ones throughout a transition.
        val currentDisplay = layoutState.value
        val context = measuringContext.value
        val width = widthPx.toFloat()
        val chartAreaHeightPx = with(context) { chartAreaHeight.pixels }
        val chartAreaBounds = Rect(0f, 0f, width, chartAreaHeightPx)
        context.canvasWidth = width
        if (currentDisplay == null || currentDisplay.drawingModel.slices.isEmpty()) {
          labelInsets.value = PieInsets()
          0f
        } else {
          val chart = currentDisplay.chart
          labelInsets.value =
            chart.getLabelInsets(context, chartAreaBounds, currentDisplay.drawingModel)
          chart.getLegendHeight(context) + labelInsets.value.top + labelInsets.value.bottom
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
      measuringContext.value.canvasWidth = size.width
      val mutableDrawScope = MutableDrawScope(this)
      val legendHeight = chart.getLegendHeight(measuringContext.value)
      val chartBounds =
        Rect(
          left = 0f,
          top = labelInsets.value.top,
          right = size.width,
          bottom = size.height - legendHeight - labelInsets.value.bottom,
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
