/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

import android.annotation.SuppressLint
import android.graphics.RectF
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.component1
import com.patrykandpatrick.vico.compose.cartesian.data.component2
import com.patrykandpatrick.vico.compose.cartesian.data.component3
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.data.toImmutable
import com.patrykandpatrick.vico.core.common.Defaults.CHART_HEIGHT
import com.patrykandpatrick.vico.core.common.Point
import com.patrykandpatrick.vico.core.common.ValueWrapper
import com.patrykandpatrick.vico.core.common.getValue
import com.patrykandpatrick.vico.core.common.set
import com.patrykandpatrick.vico.core.common.setValue
import com.patrykandpatrick.vico.core.common.spToPx
import kotlinx.coroutines.launch

/**
 * Displays a [CartesianChart].
 *
 * @param chart the [CartesianChart].
 * @param modelProducer creates and updates the [CartesianChartModel].
 * @param modifier the modifier to be applied to the chart.
 * @param scrollState houses information on the [CartesianChart]’s scroll value. Allows for scroll
 *   customization and programmatic scrolling.
 * @param zoomState houses information on the [CartesianChart]’s zoom factor. Allows for zoom
 *   customization.
 * @param animationSpec the [AnimationSpec] for difference animations.
 * @param runInitialAnimation whether to display an animation when the chart is created. In this
 *   animation, the value of each chart entry is animated from zero to the actual value. This
 *   animation isn’t run in previews.
 * @param placeholder shown when no [CartesianChartModel] is available.
 */
@Composable
public fun CartesianChartHost(
  chart: CartesianChart,
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
  scrollState: VicoScrollState = rememberVicoScrollState(),
  zoomState: VicoZoomState = rememberDefaultVicoZoomState(scrollState.scrollEnabled),
  animationSpec: AnimationSpec<Float>? = defaultCartesianDiffAnimationSpec,
  runInitialAnimation: Boolean = true,
  placeholder: @Composable BoxScope.() -> Unit = {},
) {
  val mutableRanges = remember(chart) { MutableCartesianChartRanges() }
  val modelWrapper by
    modelProducer.collectAsState(chart, animationSpec, runInitialAnimation, mutableRanges)
  val (model, previousModel, ranges) = modelWrapper

  CartesianChartHostBox(modifier) {
    if (model != null) {
      CartesianChartHostImpl(chart, model, scrollState, zoomState, ranges, previousModel)
    } else {
      placeholder()
    }
  }
}

/**
 * Displays a [CartesianChart]. This function accepts a [CartesianChartModel]. For dynamic data, use
 * the function overload that accepts a [CartesianChartModelProducer] instance.
 *
 * @param chart the [CartesianChart].
 * @param model the [CartesianChartModel].
 * @param modifier the modifier to be applied to the chart.
 * @param scrollState houses information on the [CartesianChart]’s scroll value. Allows for scroll
 *   customization and programmatic scrolling.
 * @param zoomState houses information on the [CartesianChart]’s zoom factor. Allows for zoom
 *   customization.
 */
@Composable
@SuppressLint("RememberReturnType")
public fun CartesianChartHost(
  chart: CartesianChart,
  model: CartesianChartModel,
  modifier: Modifier = Modifier,
  scrollState: VicoScrollState = rememberVicoScrollState(),
  zoomState: VicoZoomState = rememberDefaultVicoZoomState(scrollState.scrollEnabled),
) {
  val ranges = remember(chart) { MutableCartesianChartRanges() }
  remember(ranges, chart, model) {
    ranges.reset()
    chart.updateRanges(ranges, model)
  }
  CartesianChartHostBox(modifier) {
    CartesianChartHostImpl(chart, model, scrollState, zoomState, ranges.toImmutable())
  }
}

@Composable
internal fun CartesianChartHostImpl(
  chart: CartesianChart,
  model: CartesianChartModel,
  scrollState: VicoScrollState,
  zoomState: VicoZoomState,
  ranges: CartesianChartRanges,
  previousModel: CartesianChartModel? = null,
) {
  val canvasBounds = remember { RectF() }
  val markerTouchPoint = remember { mutableStateOf<Point?>(null) }
  val measuringContext =
    rememberCartesianMeasuringContext(
      canvasBounds = canvasBounds,
      model = model,
      ranges = ranges,
      scrollEnabled = scrollState.scrollEnabled,
      zoomEnabled = scrollState.scrollEnabled && zoomState.zoomEnabled,
      layerPadding = chart.layerPadding,
      spToPx = with(LocalContext.current) { ::spToPx },
    )

  val coroutineScope = rememberCoroutineScope()
  var previousModelID by remember { ValueWrapper(model.id) }
  val horizontalDimensions = remember { MutableHorizontalDimensions() }

  LaunchedEffect(scrollState.pointerXDeltas) {
    scrollState.pointerXDeltas.collect { delta ->
      markerTouchPoint.value?.let { point -> markerTouchPoint.value = point.copy(point.x + delta) }
    }
  }

  DisposableEffect(scrollState) { onDispose { scrollState.clearUpdated() } }

  Canvas(
    modifier =
      Modifier.fillMaxSize()
        .chartTouchEvent(
          setTouchPoint =
            remember(chart.marker == null) {
              if (chart.marker != null) markerTouchPoint.component2() else null
            },
          isScrollEnabled = scrollState.scrollEnabled,
          scrollState = scrollState,
          onZoom =
            remember(zoomState, scrollState, chart, coroutineScope) {
              if (zoomState.zoomEnabled) {
                { factor, centroid ->
                  zoomState.zoom(factor, centroid.x, scrollState.value, chart.layerBounds).let {
                    scroll ->
                    coroutineScope.launch { scrollState.scroll(scroll) }
                  }
                }
              } else {
                null
              }
            },
        )
  ) {
    val canvas = drawContext.canvas.nativeCanvas
    if (canvas.width == 0 || canvas.height == 0) return@Canvas
    canvasBounds.set(left = 0, top = 0, right = size.width, bottom = size.height)

    horizontalDimensions.clear()
    chart.prepare(measuringContext, horizontalDimensions, canvasBounds)

    if (chart.layerBounds.isEmpty) return@Canvas

    zoomState.update(measuringContext, horizontalDimensions, chart.layerBounds)
    scrollState.update(measuringContext, chart.layerBounds, horizontalDimensions)

    if (model.id != previousModelID) {
      coroutineScope.launch { scrollState.autoScroll(model, previousModel) }
      previousModelID = model.id
    }

    val drawingContext =
      CartesianDrawingContext(
        measuringContext = measuringContext,
        canvas = canvas,
        markerTouchPoint = markerTouchPoint.value,
        horizontalDimensions = horizontalDimensions,
        layerBounds = chart.layerBounds,
        scroll = scrollState.value,
        zoom = zoomState.value,
      )

    chart.draw(drawingContext, markerTouchPoint.value)
    measuringContext.reset()
  }
}

@Composable
private fun CartesianChartHostBox(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
  Box(modifier = modifier.height(CHART_HEIGHT.dp).fillMaxWidth(), content = content)
}
