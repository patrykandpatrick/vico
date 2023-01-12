/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

import android.graphics.RectF
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.entry.collectAsState
import com.patrykandpatrick.vico.compose.chart.entry.defaultDiffAnimationSpec
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.ChartScrollSpec
import com.patrykandpatrick.vico.compose.chart.scroll.ChartScrollState
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.extension.chartTouchEvent
import com.patrykandpatrick.vico.compose.gesture.OnZoom
import com.patrykandpatrick.vico.compose.layout.getMeasureContext
import com.patrykandpatrick.vico.compose.state.MutableSharedState
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DEF_MAX_ZOOM
import com.patrykandpatrick.vico.core.DEF_MIN_ZOOM
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatrick.vico.core.axis.AxisManager
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.chartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.drawMarker
import com.patrykandpatrick.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatrick.vico.core.chart.edges.FadingEdges
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.core.layout.VirtualLayout
import com.patrykandpatrick.vico.core.legend.Legend
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerVisibilityChangeListener
import com.patrykandpatrick.vico.core.model.Point
import com.patrykandpatrick.vico.core.scroll.ScrollListener

/**
 * Displays a chart.
 *
 * @param chart the chart itself (excluding axes, markers, etc.). You can use [lineChart] or [columnChart], or provide a
 * custom [Chart] implementation.
 * @param chartModelProducer creates and updates the [ChartEntryModel] for the chart.
 * @param modifier the modifier to be applied to the chart.
 * @param startAxis the axis displayed at the start of the chart.
 * @param topAxis the axis displayed at the top of the chart.
 * @param endAxis the axis displayed at the end of the chart.
 * @param bottomAxis the axis displayed at the bottom of the chart.
 * @param marker appears when the chart is touched, highlighting the entry or entries nearest to the touch point.
 * @param markerVisibilityChangeListener allows for listening to [marker] visibility changes.
 * @param legend an optional legend for the chart.
 * @param chartScrollSpec houses scrolling-related settings.
 * @param isZoomEnabled whether zooming in and out is enabled.
 * @param diffAnimationSpec the animation spec used for difference animations.
 * @param runInitialAnimation whether to display an animation when the chart is created. In this animation, the value
 * of each chart entry is animated from zero to the actual value.
 * @param fadingEdges applies a horizontal fade to the edges of the chart area for scrollable charts.
 * @param autoScaleUp defines whether the content of a scrollable chart should be scaled up when the entry count and
 * intrinsic segment width are such that, at a scale factor of 1, an empty space would be visible near the end edge of
 * the chart.
 * @param chartScrollState houses information on the chart’s scroll state. Allows for programmatic scrolling.
 */
@Composable
public fun <Model : ChartEntryModel> Chart(
    chart: Chart<Model>,
    chartModelProducer: ChartModelProducer<Model>,
    modifier: Modifier = Modifier,
    startAxis: AxisRenderer<AxisPosition.Vertical.Start>? = null,
    topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? = null,
    endAxis: AxisRenderer<AxisPosition.Vertical.End>? = null,
    bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? = null,
    marker: Marker? = null,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener? = null,
    legend: Legend? = null,
    chartScrollSpec: ChartScrollSpec<Model> = rememberChartScrollSpec(),
    isZoomEnabled: Boolean = true,
    diffAnimationSpec: AnimationSpec<Float> = defaultDiffAnimationSpec,
    runInitialAnimation: Boolean = true,
    fadingEdges: FadingEdges? = null,
    autoScaleUp: AutoScaleUp = AutoScaleUp.Full,
    chartScrollState: ChartScrollState = rememberChartScrollState(),
) {
    val modelState: MutableSharedState<Model?, Model?> = chartModelProducer.collectAsState(
        chartKey = chart,
        producerKey = chartModelProducer,
        animationSpec = diffAnimationSpec,
        runInitialAnimation = runInitialAnimation,
    )

    ChartBox(modifier = modifier) {
        modelState.value?.also { model ->
            ChartImpl(
                chart = chart,
                model = model,
                oldModel = modelState.previousValue,
                startAxis = startAxis,
                topAxis = topAxis,
                endAxis = endAxis,
                bottomAxis = bottomAxis,
                marker = marker,
                markerVisibilityChangeListener = markerVisibilityChangeListener,
                legend = legend,
                chartScrollSpec = chartScrollSpec,
                isZoomEnabled = isZoomEnabled,
                fadingEdges = fadingEdges,
                autoScaleUp = autoScaleUp,
                chartScrollState = chartScrollState,
            )
        }
    }
}

/**
 * Displays a chart.
 *
 * This function accepts a [ChartEntryModel]. For dynamic data, use the function overload that accepts a
 * [ChartModelProducer] instance.
 *
 * @param chart the chart itself (excluding axes, markers, etc.). You can use [lineChart] or [columnChart], or provide a
 * custom [Chart] implementation.
 * @param model the [ChartEntryModel] for the chart.
 * @param modifier the modifier to be applied to the chart.
 * @param startAxis the axis displayed at the start of the chart.
 * @param topAxis the axis displayed at the top of the chart.
 * @param endAxis the axis displayed at the end of the chart.
 * @param bottomAxis the axis displayed at the bottom of the chart.
 * @param marker appears when the chart is touched, highlighting the entry or entries nearest to the touch point.
 * @param markerVisibilityChangeListener allows for listening to [marker] visibility changes.
 * @param legend an optional legend for the chart.
 * @param isZoomEnabled whether zooming in and out is enabled.
 * @param fadingEdges applies a horizontal fade to the edges of the chart area for scrollable charts.
 * @param autoScaleUp defines whether the content of a scrollable chart should be scaled up when the entry count and
 * intrinsic segment width are such that, at a scale factor of 1, an empty space would be visible near the end edge of
 * the chart.
 * @param chartScrollState houses information on the chart’s scroll state. Allows for programmatic scrolling.
 */
@Deprecated(message = "Use `chartScrollSpec` to enable or disable scrolling.")
@Composable
public fun <Model : ChartEntryModel> Chart(
    chart: Chart<Model>,
    model: Model,
    modifier: Modifier = Modifier,
    startAxis: AxisRenderer<AxisPosition.Vertical.Start>? = null,
    topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? = null,
    endAxis: AxisRenderer<AxisPosition.Vertical.End>? = null,
    bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? = null,
    marker: Marker? = null,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener? = null,
    legend: Legend? = null,
    isHorizontalScrollEnabled: Boolean,
    isZoomEnabled: Boolean = true,
    fadingEdges: FadingEdges? = null,
    autoScaleUp: AutoScaleUp = AutoScaleUp.Full,
    chartScrollState: ChartScrollState = rememberChartScrollState(),
) {
    ChartBox(modifier = modifier) {
        ChartImpl(
            chart = chart,
            model = model,
            startAxis = startAxis,
            topAxis = topAxis,
            endAxis = endAxis,
            bottomAxis = bottomAxis,
            marker = marker,
            markerVisibilityChangeListener = markerVisibilityChangeListener,
            legend = legend,
            isZoomEnabled = isZoomEnabled,
            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = isHorizontalScrollEnabled),
            fadingEdges = fadingEdges,
            autoScaleUp = autoScaleUp,
            chartScrollState = chartScrollState,
        )
    }
}

/**
 * Displays a chart.
 *
 * This function accepts a [ChartEntryModel]. For dynamic data, use the function overload that accepts a
 * [ChartModelProducer] instance.
 *
 * @param chart the chart itself (excluding axes, markers, etc.). You can use [lineChart] or [columnChart], or provide a
 * custom [Chart] implementation.
 * @param model the [ChartEntryModel] for the chart.
 * @param modifier the modifier to be applied to the chart.
 * @param startAxis the axis displayed at the start of the chart.
 * @param topAxis the axis displayed at the top of the chart.
 * @param endAxis the axis displayed at the end of the chart.
 * @param bottomAxis the axis displayed at the bottom of the chart.
 * @param marker appears when the chart is touched, highlighting the entry or entries nearest to the touch point.
 * @param markerVisibilityChangeListener allows for listening to [marker] visibility changes.
 * @param legend an optional legend for the chart.
 * @param chartScrollSpec houses scrolling-related settings.
 * @param isZoomEnabled whether zooming in and out is enabled.
 * @param oldModel the chart’s previous [ChartEntryModel]. This is used to determine whether to perform an automatic
 * scroll.
 * @param fadingEdges applies a horizontal fade to the edges of the chart area for scrollable charts.
 * @param autoScaleUp defines whether the content of a scrollable chart should be scaled up when the entry count and
 * intrinsic segment width are such that, at a scale factor of 1, an empty space would be visible near the end edge of
 * the chart.
 * @param chartScrollState houses information on the chart’s scroll state. Allows for programmatic scrolling.
 */
@Composable
public fun <Model : ChartEntryModel> Chart(
    chart: Chart<Model>,
    model: Model,
    modifier: Modifier = Modifier,
    startAxis: AxisRenderer<AxisPosition.Vertical.Start>? = null,
    topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? = null,
    endAxis: AxisRenderer<AxisPosition.Vertical.End>? = null,
    bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? = null,
    marker: Marker? = null,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener? = null,
    legend: Legend? = null,
    chartScrollSpec: ChartScrollSpec<Model> = rememberChartScrollSpec(),
    isZoomEnabled: Boolean = true,
    oldModel: Model? = null,
    fadingEdges: FadingEdges? = null,
    autoScaleUp: AutoScaleUp = AutoScaleUp.Full,
    chartScrollState: ChartScrollState = rememberChartScrollState(),
) {
    ChartBox(modifier = modifier) {
        ChartImpl(
            chart = chart,
            model = model,
            startAxis = startAxis,
            topAxis = topAxis,
            endAxis = endAxis,
            bottomAxis = bottomAxis,
            marker = marker,
            markerVisibilityChangeListener = markerVisibilityChangeListener,
            legend = legend,
            chartScrollSpec = chartScrollSpec,
            isZoomEnabled = isZoomEnabled,
            oldModel = oldModel,
            fadingEdges = fadingEdges,
            autoScaleUp = autoScaleUp,
            chartScrollState = chartScrollState,
        )
    }
}

@Composable
internal fun <Model : ChartEntryModel> ChartImpl(
    chart: Chart<Model>,
    model: Model,
    startAxis: AxisRenderer<AxisPosition.Vertical.Start>?,
    topAxis: AxisRenderer<AxisPosition.Horizontal.Top>?,
    endAxis: AxisRenderer<AxisPosition.Vertical.End>?,
    bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>?,
    marker: Marker?,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener?,
    legend: Legend?,
    chartScrollSpec: ChartScrollSpec<Model>,
    isZoomEnabled: Boolean,
    oldModel: Model? = null,
    fadingEdges: FadingEdges?,
    autoScaleUp: AutoScaleUp,
    chartScrollState: ChartScrollState = rememberChartScrollState(),
) {
    val axisManager = remember { AxisManager() }
    val bounds = remember { RectF() }
    val markerTouchPoint = remember { mutableStateOf<Point?>(null) }
    val zoom = remember { mutableStateOf(1f) }
    val measureContext = getMeasureContext(chartScrollSpec.isScrollEnabled, zoom.value, bounds)
    val interactionSource = remember { MutableInteractionSource() }
    val interaction = interactionSource.interactions.collectAsState(initial = null)
    val scrollListener = rememberScrollListener(markerTouchPoint, interaction)
    val lastMarkerEntryModels = remember { mutableStateOf<List<Marker.EntryModel>>(emptyList()) }

    axisManager.setAxes(startAxis, topAxis, endAxis, bottomAxis)
    chartScrollState.registerScrollListener(scrollListener)

    val virtualLayout = remember { VirtualLayout(axisManager) }
    val elevationOverlayColor = currentChartStyle.elevationOverlayColor.toArgb()
    val (wasMarkerVisible, setWasMarkerVisible) = remember { mutableStateOf(false) }

    val onZoom = rememberZoomState(
        zoom = zoom,
        getScroll = { chartScrollState.value },
        setScroll = { value -> chartScrollState.value = value },
        chartBounds = chart.bounds,
    )

    LaunchedEffect(key1 = model.id) {
        chartScrollSpec.performAutoScroll(
            model = model,
            oldModel = oldModel,
            chartScrollState = chartScrollState,
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .chartTouchEvent(
                setTouchPoint = markerTouchPoint
                    .component2()
                    .takeIf { marker != null },
                scrollableState = chartScrollState.takeIf { chartScrollSpec.isScrollEnabled },
                onZoom = onZoom.takeIf { isZoomEnabled },
                interactionSource = interactionSource,
            ),
    ) {
        bounds.set(left = 0, top = 0, right = size.width, bottom = size.height)
        chart.updateChartValues(measureContext.chartValuesManager, model)

        val segmentProperties = chart.getSegmentProperties(measureContext, model)

        virtualLayout.setBounds(
            context = measureContext,
            contentBounds = bounds,
            chart = chart,
            legend = legend,
            segmentProperties = segmentProperties,
            marker,
        )

        chartScrollState.maxValue = measureContext.getMaxScrollDistance(
            chartWidth = chart.bounds.width(),
            segmentProperties = segmentProperties,
        )

        chartScrollState.handleInitialScroll(initialScroll = chartScrollSpec.initialScroll)

        val chartDrawContext = chartDrawContext(
            canvas = drawContext.canvas.nativeCanvas,
            elevationOverlayColor = elevationOverlayColor,
            measureContext = measureContext,
            markerTouchPoint = markerTouchPoint.value,
            segmentProperties = segmentProperties,
            chartBounds = chart.bounds,
            horizontalScroll = chartScrollState.value,
            autoScaleUp = autoScaleUp,
        )

        val count = if (fadingEdges != null) chartDrawContext.saveLayer() else -1

        axisManager.drawBehindChart(chartDrawContext)
        chart.drawScrollableContent(chartDrawContext, model)

        fadingEdges?.applyFadingEdges(chartDrawContext, chart.bounds)
        if (fadingEdges != null) chartDrawContext.restoreCanvasToCount(count)

        axisManager.drawAboveChart(chartDrawContext)
        chart.drawNonScrollableContent(chartDrawContext, model)
        legend?.draw(chartDrawContext)

        drawMarkerIfNecessary(
            chartDrawContext = chartDrawContext,
            marker = marker,
            markerTouchPoint = markerTouchPoint.value,
            chart = chart,
            markerVisibilityChangeListener = markerVisibilityChangeListener,
            wasMarkerVisible = wasMarkerVisible,
            setWasMarkerVisible = setWasMarkerVisible,
            lastMarkerEntryModels = lastMarkerEntryModels.value,
            onMarkerEntryModelsChange = lastMarkerEntryModels.component2(),
        )

        measureContext.reset()
    }
}

@Composable
internal fun ChartBox(
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.height(DefaultDimens.CHART_HEIGHT.dp),
        content = content,
    )
}

@Composable
internal fun rememberScrollListener(
    touchPoint: MutableState<Point?>,
    interaction: State<Interaction?>,
): ScrollListener = remember {
    object : ScrollListener {
        var shouldClearTouchPoint = false

        override fun onValueChanged(oldValue: Float, newValue: Float) {
            touchPoint.value?.let { point ->
                if (interaction.value is DragInteraction.Stop && shouldClearTouchPoint) {
                    touchPoint.value = null
                    shouldClearTouchPoint = false
                } else {
                    touchPoint.value = point.copy(x = point.x + oldValue - newValue)
                    shouldClearTouchPoint = true
                }
            }
        }
    }
}

@Composable
internal fun rememberZoomState(
    zoom: MutableState<Float>,
    getScroll: () -> Float,
    setScroll: (value: Float) -> Unit,
    chartBounds: RectF,
): OnZoom = remember {
    onZoom@{ centroid, zoomChange ->
        val newZoom = zoom.value * zoomChange
        if (newZoom !in DEF_MIN_ZOOM..DEF_MAX_ZOOM) return@onZoom
        val transformationAxisX = getScroll() + centroid.x - chartBounds.left
        val zoomedTransformationAxisX = transformationAxisX * zoomChange
        zoom.value = newZoom
        setScroll(getScroll() + zoomedTransformationAxisX - transformationAxisX)
    }
}

@LongParameterListDrawFunction
private fun <Model : ChartEntryModel> drawMarkerIfNecessary(
    chartDrawContext: ChartDrawContext,
    marker: Marker?,
    markerTouchPoint: Point?,
    chart: Chart<Model>,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener?,
    wasMarkerVisible: Boolean,
    setWasMarkerVisible: ((Boolean) -> Unit),
    lastMarkerEntryModels: List<Marker.EntryModel>,
    onMarkerEntryModelsChange: (List<Marker.EntryModel>) -> Unit,
) {
    if (marker != null) {
        chartDrawContext.drawMarker(
            marker = marker,
            markerTouchPoint = markerTouchPoint,
            chart = chart,
            markerVisibilityChangeListener = markerVisibilityChangeListener,
            wasMarkerVisible = wasMarkerVisible,
            setWasMarkerVisible = setWasMarkerVisible,
            lastMarkerEntryModels = lastMarkerEntryModels,
            onMarkerEntryModelsChange = onMarkerEntryModelsChange,
        )
    }
}
