/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.compose.chart

import android.graphics.RectF
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.patrykandpatryk.vico.compose.chart.entry.collectAsState
import com.patrykandpatryk.vico.compose.chart.entry.defaultDiffAnimationSpec
import com.patrykandpatryk.vico.compose.chart.scroll.ChartScrollSpec
import com.patrykandpatryk.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatryk.vico.compose.extension.chartTouchEvent
import com.patrykandpatryk.vico.compose.gesture.OnZoom
import com.patrykandpatryk.vico.compose.layout.getMeasureContext
import com.patrykandpatryk.vico.compose.state.MutableSharedState
import com.patrykandpatryk.vico.compose.style.currentChartStyle
import com.patrykandpatryk.vico.core.DEF_MAX_ZOOM
import com.patrykandpatryk.vico.core.DEF_MIN_ZOOM
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.axis.AxisManager
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.AxisRenderer
import com.patrykandpatryk.vico.core.chart.Chart
import com.patrykandpatryk.vico.core.chart.draw.chartDrawContext
import com.patrykandpatryk.vico.core.chart.draw.drawMarker
import com.patrykandpatryk.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatryk.vico.core.chart.edges.FadingEdges
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.ChartModelProducer
import com.patrykandpatryk.vico.core.extension.set
import com.patrykandpatryk.vico.core.layout.VirtualLayout
import com.patrykandpatryk.vico.core.legend.Legend
import com.patrykandpatryk.vico.core.marker.Marker
import com.patrykandpatryk.vico.core.marker.MarkerVisibilityChangeListener
import com.patrykandpatryk.vico.core.model.Point
import com.patrykandpatryk.vico.core.scroll.ScrollHandler

/**
 * Displays a chart.
 *
 * @param chart the chart used to display sets of entries (e.g.,
 * [com.patrykandpatryk.vico.core.chart.column.ColumnChart] for a column chart or
 * [com.patrykandpatryk.vico.core.chart.line.LineChart] for a line chart).
 * @param chartModelProducer produces the [ChartEntryModel]s displayed by the [chart].
 * @param modifier an optional modifier.
 * @param startAxis an axis displayed on the start of the chart.
 * @param topAxis an axis displayed on the top of the chart.
 * @param endAxis an axis displayed on the end of the chart.
 * @param bottomAxis an axis displayed on the bottom of the chart.
 * @param marker an optional marker that will appear when the chart is touched, highlighting the entry or entries
 * nearest to the touch point.
 * @param markerVisibilityChangeListener an optional listener for [marker] visibility changes.
 * @param legend an optional legend for the chart.
 * @param chartScrollSpec houses scrolling-related settings.
 * @param isZoomEnabled whether zooming in and out is enabled.
 * @param diffAnimationSpec the animation spec used to animate differences between entry sets ([ChartEntryModel]
 * instances).
 * @param runInitialAnimation whether to display an animation when the chart is created. In this animation, the value
 * of each chart entry is animated from zero to the actual value.
 * @param fadingEdges applies a horizontal fade to the edges of the chart area for scrollable charts.
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
) {
    val modelState: MutableSharedState<Model?, Model?> = chartModelProducer.collectAsState(
        chartKey = chart,
        producerKey = chartModelProducer,
        animationSpec = diffAnimationSpec,
        runInitialAnimation = runInitialAnimation,
    )

    modelState.value?.also { model ->
        Chart(
            modifier = modifier,
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
        )
    }
}

/**
 * Displays a chart.
 *
 * This function accepts a [ChartEntryModel]. For regular usage it’s advised to use the function overload that accepts a
 * [ChartModelProducer] instance.
 *
 * @param chart the chart used to display sets of entries (e.g.,
 * [com.patrykandpatryk.vico.core.chart.column.ColumnChart] for a column chart or
 * [com.patrykandpatryk.vico.core.chart.line.LineChart] for a line chart).
 * @param model the [ChartEntryModel]s displayed by the [chart].
 * @param modifier an optional modifier.
 * @param startAxis an axis displayed on the start of the chart.
 * @param topAxis an axis displayed on the top of the chart.
 * @param endAxis an axis displayed on the end of the chart.
 * @param bottomAxis an axis displayed on the bottom of the chart.
 * @param marker an optional marker that will appear when the chart is touched, highlighting the entry or entries
 * nearest to the touch point.
 * @param markerVisibilityChangeListener an optional listener for [marker] visibility changes.
 * @param legend an optional legend for the chart.
 * @param isHorizontalScrollEnabled whether horizontal scroll is enabled.
 * @param isZoomEnabled whether zooming in and out is enabled.
 */
@Deprecated("Use `chartScrollSpec` to enable or disable scrolling.")
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
) {
    Chart(
        chart = chart,
        model = model,
        modifier = modifier,
        startAxis = startAxis,
        topAxis = topAxis,
        endAxis = endAxis,
        bottomAxis = bottomAxis,
        marker = marker,
        markerVisibilityChangeListener = markerVisibilityChangeListener,
        legend = legend,
        isZoomEnabled = isZoomEnabled,
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = isHorizontalScrollEnabled),
    )
}

/**
 * Displays a chart.
 *
 * This function accepts a [ChartEntryModel]. For regular usage it’s advised to use the function overload that accepts a
 * [ChartModelProducer] instance.
 *
 * @param chart the chart used to display sets of entries (e.g.,
 * [com.patrykandpatryk.vico.core.chart.column.ColumnChart] for a column chart or
 * [com.patrykandpatryk.vico.core.chart.line.LineChart] for a line chart).
 * @param model the [ChartEntryModel]s displayed by the [chart].
 * @param modifier an optional modifier.
 * @param startAxis an axis displayed on the start of the chart.
 * @param topAxis an axis displayed on the top of the chart.
 * @param endAxis an axis displayed on the end of the chart.
 * @param bottomAxis an axis displayed on the bottom of the chart.
 * @param marker an optional marker that will appear when the chart is touched, highlighting the entry or entries
 * nearest to the touch point.
 * @param markerVisibilityChangeListener an optional listener for [marker] visibility changes.
 * @param legend an optional legend for the chart.
 * @param chartScrollSpec houses scrolling-related settings.
 * @param isZoomEnabled whether zooming in and out is enabled.
 * @param oldModel the chart’s previous model. This is used to determine whether to perform an automatic scroll.
 * @param fadingEdges applies a horizontal fade to the edges of the chart area for scrollable charts.
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
) {
    val axisManager = remember { AxisManager() }
    val bounds = remember { RectF() }
    val markerTouchPoint = remember { mutableStateOf<Point?>(null) }
    val horizontalScroll = remember { mutableStateOf(0f) }
    val zoom = remember { mutableStateOf(1f) }
    val measureContext = getMeasureContext(chartScrollSpec.isScrollEnabled, zoom.value, bounds)
    val interactionSource = remember { MutableInteractionSource() }
    val interaction = interactionSource.interactions.collectAsState(initial = null)

    axisManager.setAxes(startAxis, topAxis, endAxis, bottomAxis)

    val setHorizontalScroll = rememberSetHorizontalScroll(horizontalScroll, markerTouchPoint, interaction)
    val scrollHandler = remember { ScrollHandler(setHorizontalScroll) }
    val scrollableState = rememberScrollableState(scrollHandler::handleScrollDelta)
    val onZoom = rememberZoomState(zoom, scrollHandler, chart.bounds)
    val virtualLayout = remember { VirtualLayout(axisManager) }
    val elevationOverlayColor = currentChartStyle.elevationOverlayColor.toArgb()

    val (wasMarkerVisible, setWasMarkerVisible) = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = model.id) {
        chartScrollSpec.performAutoScroll(
            model = model,
            oldModel = oldModel,
            currentScroll = horizontalScroll.value,
            maxScrollDistance = scrollHandler.maxScrollDistance,
            scrollableState = scrollableState,
        )
    }

    Canvas(
        modifier = modifier
            .height(DefaultDimens.CHART_HEIGHT.dp)
            .fillMaxWidth()
            .chartTouchEvent(
                setTouchPoint = markerTouchPoint
                    .component2()
                    .takeIf { marker != null },
                scrollableState = scrollableState.takeIf { chartScrollSpec.isScrollEnabled },
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

        scrollHandler.maxScrollDistance = measureContext.getMaxScrollDistance(
            chartWidth = chart.bounds.width(),
            segmentProperties = segmentProperties,
        )

        scrollHandler.handleInitialScroll(initialScroll = chartScrollSpec.initialScroll)

        val chartDrawContext = chartDrawContext(
            canvas = drawContext.canvas.nativeCanvas,
            elevationOverlayColor = elevationOverlayColor,
            measureContext = measureContext,
            markerTouchPoint = markerTouchPoint.value,
            segmentProperties = segmentProperties,
            chartBounds = chart.bounds,
            horizontalScroll = horizontalScroll.value,
        )

        val count = if (fadingEdges != null) chartDrawContext.saveLayer() else -1

        axisManager.drawBehindChart(chartDrawContext)
        chart.drawScrollableContent(chartDrawContext, model)

        fadingEdges?.apply {
            applyFadingEdges(chartDrawContext, chart.bounds)
            chartDrawContext.restoreCanvasToCount(count)
        }

        chart.drawNonScrollableContent(chartDrawContext, model)
        axisManager.drawAboveChart(chartDrawContext)
        legend?.draw(chartDrawContext)

        if (marker != null) {
            chartDrawContext.drawMarker(
                marker = marker,
                markerTouchPoint = markerTouchPoint.value,
                chart = chart,
                markerVisibilityChangeListener = markerVisibilityChangeListener,
                wasMarkerVisible = wasMarkerVisible,
                setWasMarkerVisible = setWasMarkerVisible,
            )
        }

        measureContext.reset()
    }
}

@Composable
internal fun rememberSetHorizontalScroll(
    scroll: MutableState<Float>,
    touchPoint: MutableState<Point?>,
    interaction: State<Interaction?>,
): (Float) -> Unit = remember {
    var canClearTouchPoint = false
    return@remember { newScroll: Float ->
        touchPoint.value?.let { point ->
            if (interaction.value is DragInteraction.Stop && canClearTouchPoint) {
                touchPoint.value = null
                canClearTouchPoint = false
            } else {
                touchPoint.value = point.copy(x = point.x + scroll.value - newScroll)
                canClearTouchPoint = true
            }
        }
        scroll.value = newScroll
    }
}

@Composable
internal fun rememberZoomState(
    zoom: MutableState<Float>,
    scrollHandler: ScrollHandler,
    chartBounds: RectF,
): OnZoom = remember {
    onZoom@{ centroid, zoomChange ->
        val newZoom = zoom.value * zoomChange
        if (newZoom !in DEF_MIN_ZOOM..DEF_MAX_ZOOM) return@onZoom
        val transformationAxisX = scrollHandler.currentScroll + centroid.x - chartBounds.left
        val zoomedTransformationAxisX = transformationAxisX * zoomChange
        zoom.value = newZoom
        scrollHandler.currentScroll += zoomedTransformationAxisX - transformationAxisX
    }
}
