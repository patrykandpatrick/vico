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

package com.patrykandpatrick.vico.compose.chart

import android.annotation.SuppressLint
import android.graphics.RectF
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
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
import com.patrykandpatrick.vico.compose.chart.layout.segmented
import com.patrykandpatrick.vico.compose.chart.scroll.VicoScrollState
import com.patrykandpatrick.vico.compose.chart.scroll.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.chart.zoom.VicoZoomState
import com.patrykandpatrick.vico.compose.chart.zoom.rememberDefaultVicoZoomState
import com.patrykandpatrick.vico.compose.extension.chartTouchEvent
import com.patrykandpatrick.vico.compose.layout.rememberMutableMeasureContext
import com.patrykandpatrick.vico.compose.model.collectAsState
import com.patrykandpatrick.vico.compose.model.defaultDiffAnimationSpec
import com.patrykandpatrick.vico.compose.state.component1
import com.patrykandpatrick.vico.compose.state.component2
import com.patrykandpatrick.vico.compose.state.component3
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.Defaults.CHART_HEIGHT
import com.patrykandpatrick.vico.core.chart.CartesianChart
import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.chartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.drawMarker
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.MutableChartValues
import com.patrykandpatrick.vico.core.chart.values.toImmutable
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.core.extension.spToPx
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerVisibilityChangeListener
import com.patrykandpatrick.vico.core.model.CartesianChartModel
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.util.Point
import com.patrykandpatrick.vico.core.util.ValueWrapper
import com.patrykandpatrick.vico.core.util.getValue
import com.patrykandpatrick.vico.core.util.setValue
import kotlinx.coroutines.launch

/**
 * Displays a [CartesianChart].
 *
 * @param chart the [CartesianChart].
 * @param modelProducer creates and updates the [CartesianChartModel].
 * @param modifier the modifier to be applied to the chart.
 * @param marker appears when the chart is touched, highlighting the entry or entries nearest to the touch point.
 * @param markerVisibilityChangeListener allows for listening to [marker] visibility changes.
 * @param scrollState houses information on the [CartesianChart]’s scroll value. Allows for scroll customization and
 * programmatic scrolling.
 * @param zoomState houses information on the [CartesianChart]’s zoom factor. Allows for zoom customization.
 * @param diffAnimationSpec the animation spec used for difference animations.
 * @param runInitialAnimation whether to display an animation when the chart is created. In this animation, the value
 * of each chart entry is animated from zero to the actual value. This animation isn’t run in previews.
 * @param horizontalLayout defines how the chart’s content is positioned horizontally.
 * @param getXStep overrides the _x_ step (the difference between the _x_ values of neighboring major entries). If this
 * is null, the output of [CartesianChartModel.getXDeltaGcd] is used.
 * @param placeholder shown when no [CartesianChartModel] is available.
 */
@Composable
public fun CartesianChartHost(
    chart: CartesianChart,
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    marker: Marker? = null,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener? = null,
    scrollState: VicoScrollState = rememberVicoScrollState(),
    zoomState: VicoZoomState = rememberDefaultVicoZoomState(scrollState.scrollEnabled),
    diffAnimationSpec: AnimationSpec<Float>? = defaultDiffAnimationSpec,
    runInitialAnimation: Boolean = true,
    horizontalLayout: HorizontalLayout = HorizontalLayout.segmented(),
    getXStep: ((CartesianChartModel) -> Float)? = null,
    placeholder: @Composable BoxScope.() -> Unit = {},
) {
    val mutableChartValues = remember(chart) { MutableChartValues() }
    val modelWrapper by modelProducer
        .collectAsState(chart, modelProducer, diffAnimationSpec, runInitialAnimation, mutableChartValues, getXStep)
    val (model, previousModel, chartValues) = modelWrapper

    CartesianChartHostBox(modifier = modifier) {
        if (model != null) {
            CartesianChartHostImpl(
                chart = chart,
                model = model,
                oldModel = previousModel,
                marker = marker,
                markerVisibilityChangeListener = markerVisibilityChangeListener,
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
 * Displays a [CartesianChart]. This function accepts a [CartesianChartModel]. For dynamic data, use the function
 * overload that accepts a [CartesianChartModelProducer] instance.
 *
 * @param chart the [CartesianChart].
 * @param model the [CartesianChartModel].
 * @param modifier the modifier to be applied to the chart.
 * @param marker appears when the chart is touched, highlighting the entry or entries nearest to the touch point.
 * @param markerVisibilityChangeListener allows for listening to [marker] visibility changes.
 * @param scrollState houses information on the [CartesianChart]’s scroll value. Allows for scroll customization and
 * programmatic scrolling.
 * @param zoomState houses information on the [CartesianChart]’s zoom factor. Allows for zoom customization.
 * @param oldModel the chart’s previous [CartesianChartModel]. This is used to determine whether to perform an automatic
 * scroll.
 * @param horizontalLayout defines how the chart’s content is positioned horizontally.
 * @param getXStep overrides the _x_ step (the difference between the _x_ values of neighboring major entries). If this
 * is null, the output of [CartesianChartModel.getXDeltaGcd] is used.
 */
@Composable
@SuppressLint("RememberReturnType")
public fun CartesianChartHost(
    chart: CartesianChart,
    model: CartesianChartModel,
    modifier: Modifier = Modifier,
    marker: Marker? = null,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener? = null,
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
    CartesianChartHostBox(modifier = modifier) {
        CartesianChartHostImpl(
            chart = chart,
            model = model,
            marker = marker,
            markerVisibilityChangeListener = markerVisibilityChangeListener,
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
    marker: Marker?,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener?,
    scrollState: VicoScrollState,
    zoomState: VicoZoomState,
    oldModel: CartesianChartModel?,
    horizontalLayout: HorizontalLayout,
    chartValues: ChartValues,
) {
    val bounds = remember { RectF() }
    val markerTouchPoint = remember { mutableStateOf<Point?>(null) }
    val measureContext =
        rememberMutableMeasureContext(
            scrollState.scrollEnabled,
            bounds,
            horizontalLayout,
            with(LocalContext.current) { ::spToPx },
            chartValues,
        )
    val lastMarkerEntryModels = remember { mutableStateOf(emptyList<Marker.EntryModel>()) }

    val elevationOverlayColor = currentChartStyle.elevationOverlayColor.toArgb()
    val (wasMarkerVisible, setWasMarkerVisible) = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var previousModelID by remember { ValueWrapper(model.id) }
    val horizontalDimensions = remember { MutableHorizontalDimensions() }

    LaunchedEffect(scrollState.pointerXDeltas) {
        scrollState
            .pointerXDeltas
            .collect { delta ->
                markerTouchPoint.value?.let { point -> markerTouchPoint.value = point.copy(point.x + delta) }
            }
    }

    Canvas(
        modifier =
            Modifier
                .fillMaxSize()
                .chartTouchEvent(
                    setTouchPoint =
                        remember(marker == null) { if (marker != null) markerTouchPoint.component2() else null },
                    isScrollEnabled = scrollState.scrollEnabled,
                    scrollState = scrollState,
                    onZoom =
                        remember(zoomState, scrollState, chart, coroutineScope) {
                            if (zoomState.zoomEnabled) {
                                { factor, centroid ->
                                    zoomState
                                        .zoom(factor, centroid.x, scrollState.value, chart.bounds)
                                        .let { delta -> coroutineScope.launch { scrollState.scrollBy(delta) } }
                                }
                            } else {
                                null
                            }
                        },
                ),
    ) {
        bounds.set(left = 0, top = 0, right = size.width, bottom = size.height)

        horizontalDimensions.clear()
        chart.prepare(measureContext, model, horizontalDimensions, bounds, marker)

        if (chart.bounds.isEmpty) return@Canvas

        zoomState.update(measureContext, horizontalDimensions, chart.bounds)
        scrollState.update(measureContext, chart.bounds, horizontalDimensions, zoomState.value)

        if (model.id != previousModelID) {
            coroutineScope.launch { scrollState.autoScroll(model, oldModel) }
            previousModelID = model.id
        }

        scrollState.handleInitialScroll()

        val chartDrawContext =
            chartDrawContext(
                canvas = drawContext.canvas.nativeCanvas,
                elevationOverlayColor = elevationOverlayColor,
                measureContext = measureContext,
                markerTouchPoint = markerTouchPoint.value,
                horizontalDimensions = horizontalDimensions,
                chartBounds = chart.bounds,
                horizontalScroll = scrollState.value,
                zoom = zoomState.value,
            )

        chart.draw(chartDrawContext, model)

        if (marker != null) {
            chartDrawContext.drawMarker(
                marker = marker,
                markerTouchPoint = markerTouchPoint.value,
                chart = chart,
                markerVisibilityChangeListener = markerVisibilityChangeListener,
                wasMarkerVisible = wasMarkerVisible,
                setWasMarkerVisible = setWasMarkerVisible,
                lastMarkerEntryModels = lastMarkerEntryModels.value,
                onMarkerEntryModelsChange = lastMarkerEntryModels.component2(),
            )
        }

        measureContext.reset()
    }
}

@Composable
internal fun CartesianChartHostBox(
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.height(CHART_HEIGHT.dp).fillMaxWidth(),
        content = content,
    )
}
