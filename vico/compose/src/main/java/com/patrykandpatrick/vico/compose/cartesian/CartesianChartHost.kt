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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.component1
import com.patrykandpatrick.vico.compose.cartesian.data.component2
import com.patrykandpatrick.vico.compose.cartesian.data.component3
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.ChartValues
import com.patrykandpatrick.vico.core.cartesian.data.MutableChartValues
import com.patrykandpatrick.vico.core.cartesian.data.toImmutable
import com.patrykandpatrick.vico.core.cartesian.drawMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.common.Defaults.CHART_HEIGHT
import com.patrykandpatrick.vico.core.common.Point
import com.patrykandpatrick.vico.core.common.ValueWrapper
import com.patrykandpatrick.vico.core.common.getValue
import com.patrykandpatrick.vico.core.common.set
import com.patrykandpatrick.vico.core.common.setValue
import com.patrykandpatrick.vico.core.common.spToPx
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Displays a [CartesianChart].
 *
 * @param chart the [CartesianChart].
 * @param modelProducer creates and updates the [CartesianChartModel].
 * @param modifier the modifier to be applied to the chart.
 * @param marker appears when the chart is touched, highlighting the entry or entries nearest to the
 *   touch point.
 * @param markerVisibilityListener allows for listening to [marker] visibility changes.
 * @param scrollState houses information on the [CartesianChart]’s scroll value. Allows for scroll
 *   customization and programmatic scrolling.
 * @param zoomState houses information on the [CartesianChart]’s zoom factor. Allows for zoom
 *   customization.
 * @param diffAnimationSpec the animation spec used for difference animations.
 * @param runInitialAnimation whether to display an animation when the chart is created. In this
 *   animation, the value of each chart entry is animated from zero to the actual value. This
 *   animation isn’t run in previews.
 * @param horizontalLayout defines how the chart’s content is positioned horizontally.
 * @param getXStep overrides the _x_ step (the difference between the _x_ values of neighboring
 *   major entries). If this is null, the output of [CartesianChartModel.getXDeltaGcd] is used.
 * @param dispatcher used for handling [CartesianChartModel] updates.
 * @param placeholder shown when no [CartesianChartModel] is available.
 */
@Composable
public fun CartesianChartHost(
  chart: CartesianChart,
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
  marker: CartesianMarker? = null,
  markerVisibilityListener: CartesianMarkerVisibilityListener? = null,
  scrollState: VicoScrollState = rememberVicoScrollState(),
  zoomState: VicoZoomState = rememberDefaultVicoZoomState(scrollState.scrollEnabled),
  diffAnimationSpec: AnimationSpec<Float>? = defaultCartesianDiffAnimationSpec,
  runInitialAnimation: Boolean = true,
  horizontalLayout: HorizontalLayout = HorizontalLayout.segmented(),
  getXStep: ((CartesianChartModel) -> Float)? = null,
  dispatcher: CoroutineDispatcher = Dispatchers.Default,
  placeholder: @Composable BoxScope.() -> Unit = {},
) {
  val mutableChartValues = remember(chart) { MutableChartValues() }
  val modelWrapper by
    modelProducer.collectAsState(
      chart,
      diffAnimationSpec,
      runInitialAnimation,
      mutableChartValues,
      getXStep,
      dispatcher,
    )
  val (model, previousModel, chartValues) = modelWrapper

  CartesianChartHostBox(modifier) {
    if (model != null) {
      CartesianChartHostImpl(
        chart = chart,
        model = model,
        oldModel = previousModel,
        marker = marker,
        markerVisibilityListener = markerVisibilityListener,
        scrollState = scrollState,
        zoomState = zoomState,
        horizontalLayout = horizontalLayout,
        chartValues = chartValues,
      )
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
 * @param marker appears when the chart is touched, highlighting the entry or entries nearest to the
 *   touch point.
 * @param markerVisibilityListener allows for listening to [marker] visibility changes.
 * @param scrollState houses information on the [CartesianChart]’s scroll value. Allows for scroll
 *   customization and programmatic scrolling.
 * @param zoomState houses information on the [CartesianChart]’s zoom factor. Allows for zoom
 *   customization.
 * @param oldModel the chart’s previous [CartesianChartModel]. This is used to determine whether to
 *   perform an automatic scroll.
 * @param horizontalLayout defines how the chart’s content is positioned horizontally.
 * @param getXStep overrides the _x_ step (the difference between the _x_ values of neighboring
 *   major entries). If this is null, the output of [CartesianChartModel.getXDeltaGcd] is used.
 */
@Composable
@SuppressLint("RememberReturnType")
public fun CartesianChartHost(
  chart: CartesianChart,
  model: CartesianChartModel,
  modifier: Modifier = Modifier,
  marker: CartesianMarker? = null,
  markerVisibilityListener: CartesianMarkerVisibilityListener? = null,
  scrollState: VicoScrollState = rememberVicoScrollState(),
  zoomState: VicoZoomState = rememberDefaultVicoZoomState(scrollState.scrollEnabled),
  oldModel: CartesianChartModel? = null,
  horizontalLayout: HorizontalLayout = HorizontalLayout.segmented(),
  getXStep: ((CartesianChartModel) -> Float)? = null,
) {
  val chartValues = remember(chart) { MutableChartValues() }
  remember(chartValues, model, getXStep) {
    chartValues.reset()
    chart.updateChartValues(chartValues, model, getXStep?.invoke(model))
  }
  CartesianChartHostBox(modifier) {
    CartesianChartHostImpl(
      chart = chart,
      model = model,
      marker = marker,
      markerVisibilityListener = markerVisibilityListener,
      scrollState = scrollState,
      zoomState = zoomState,
      oldModel = oldModel,
      horizontalLayout = horizontalLayout,
      chartValues = chartValues.toImmutable(),
    )
  }
}

@Composable
internal fun CartesianChartHostImpl(
  chart: CartesianChart,
  model: CartesianChartModel,
  marker: CartesianMarker?,
  markerVisibilityListener: CartesianMarkerVisibilityListener?,
  scrollState: VicoScrollState,
  zoomState: VicoZoomState,
  oldModel: CartesianChartModel?,
  horizontalLayout: HorizontalLayout,
  chartValues: ChartValues,
) {
  val canvasBounds = remember { RectF() }
  val markerTouchPoint = remember { mutableStateOf<Point?>(null) }
  val measureContext =
    rememberCartesianMeasureContext(
      scrollEnabled = scrollState.scrollEnabled,
      zoomEnabled = scrollState.scrollEnabled && zoomState.zoomEnabled,
      canvasBounds = canvasBounds,
      horizontalLayout = horizontalLayout,
      spToPx = with(LocalContext.current) { ::spToPx },
      chartValues = chartValues,
    )
  val previousMarkerTargetHashCode = remember { ValueWrapper<Int?>(null) }

  val elevationOverlayColor = vicoTheme.elevationOverlayColor.toArgb()
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
            remember(marker == null) {
              if (marker != null) markerTouchPoint.component2() else null
            },
          isScrollEnabled = scrollState.scrollEnabled,
          scrollState = scrollState,
          onZoom =
            remember(zoomState, scrollState, chart, coroutineScope) {
              if (zoomState.zoomEnabled) {
                { factor, centroid ->
                  zoomState.zoom(factor, centroid.x, scrollState.value, chart.bounds).let { scroll
                    ->
                    coroutineScope.launch { scrollState.scroll(scroll) }
                  }
                }
              } else {
                null
              }
            },
        )
  ) {
    canvasBounds.set(left = 0, top = 0, right = size.width, bottom = size.height)

    horizontalDimensions.clear()
    chart.prepare(measureContext, model, horizontalDimensions, canvasBounds, marker)

    if (chart.bounds.isEmpty) return@Canvas

    zoomState.update(measureContext, horizontalDimensions, chart.bounds)
    scrollState.update(measureContext, chart.bounds, horizontalDimensions)

    if (model.id != previousModelID) {
      coroutineScope.launch { scrollState.autoScroll(model, oldModel) }
      previousModelID = model.id
    }

    val cartesianDrawContext =
      CartesianDrawContext(
        canvas = drawContext.canvas.nativeCanvas,
        elevationOverlayColor = elevationOverlayColor,
        measureContext = measureContext,
        markerTouchPoint = markerTouchPoint.value,
        horizontalDimensions = horizontalDimensions,
        chartBounds = chart.bounds,
        scroll = scrollState.value,
        zoom = zoomState.value,
      )

    chart.draw(cartesianDrawContext, model)

    if (marker != null) {
      previousMarkerTargetHashCode.value =
        cartesianDrawContext.drawMarker(
          marker,
          markerTouchPoint.value,
          chart,
          markerVisibilityListener,
          previousMarkerTargetHashCode.value,
        )
    }

    measureContext.reset()
  }
}

@Composable
private fun CartesianChartHostBox(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
  Box(modifier = modifier.height(CHART_HEIGHT.dp).fillMaxWidth(), content = content)
}
