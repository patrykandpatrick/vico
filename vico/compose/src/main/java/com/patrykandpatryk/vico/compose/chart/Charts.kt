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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.chart.entry.collect
import com.patrykandpatryk.vico.compose.chart.entry.defaultDiffAnimationSpec
import com.patrykandpatryk.vico.compose.extension.chartTouchEvent
import com.patrykandpatryk.vico.compose.gesture.OnZoom
import com.patrykandpatryk.vico.compose.layout.getMeasureContext
import com.patrykandpatryk.vico.compose.style.currentChartStyle
import com.patrykandpatryk.vico.core.DEF_MAX_ZOOM
import com.patrykandpatryk.vico.core.DEF_MIN_ZOOM
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.axis.Axis
import com.patrykandpatryk.vico.core.axis.AxisManager
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.AxisRenderer
import com.patrykandpatryk.vico.core.chart.Chart
import com.patrykandpatryk.vico.core.chart.draw.chartDrawContext
import com.patrykandpatryk.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.ChartModelProducer
import com.patrykandpatryk.vico.core.extension.getClosestMarkerEntryModel
import com.patrykandpatryk.vico.core.extension.ifNotNull
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
 * @param isHorizontalScrollEnabled whether horizontal scroll is enabled.
 * @param isZoomEnabled whether zooming in and out is enabled.
 * @param diffAnimationSpec the animation spec used to animate differences between entry sets ([ChartEntryModel]
 * instances).
 * @param runInitialAnimation whether to display an animation when the chart is created. In this animation, the value
 * of each chart entry is animated from zero to the actual value.
 * @param axisValuesOverrider overrides minimum and maximum values on x-axis and y-axis displayed in [Chart] and [Axis].
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
    isHorizontalScrollEnabled: Boolean = true,
    isZoomEnabled: Boolean = true,
    diffAnimationSpec: AnimationSpec<Float> = defaultDiffAnimationSpec,
    runInitialAnimation: Boolean = true,
    axisValuesOverrider: AxisValuesOverrider<Model>? = null,
) {
    val model = chartModelProducer.collect(
        key = chart,
        animationSpec = diffAnimationSpec,
        runInitialAnimation = runInitialAnimation,
    )

    if (model != null) {
        Chart(
            modifier = modifier,
            chart = chart,
            model = model,
            startAxis = startAxis,
            topAxis = topAxis,
            endAxis = endAxis,
            bottomAxis = bottomAxis,
            marker = marker,
            markerVisibilityChangeListener = markerVisibilityChangeListener,
            legend = legend,
            isHorizontalScrollEnabled = isHorizontalScrollEnabled,
            isZoomEnabled = isZoomEnabled,
            axisValuesOverrider = axisValuesOverrider,
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
 * @param axisValuesOverrider overrides minimum and maximum values on x-axis and y-axis displayed in [Chart] and [Axis].
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
    isHorizontalScrollEnabled: Boolean = true,
    isZoomEnabled: Boolean = true,
    axisValuesOverrider: AxisValuesOverrider<Model>? = null,
) {
    val axisManager = remember { AxisManager() }
    val bounds = remember { RectF() }
    val markerTouchPoint = remember { mutableStateOf<Point?>(null) }
    val horizontalScroll = remember { mutableStateOf(0f) }
    val zoom = remember { mutableStateOf(1f) }
    val measureContext = getMeasureContext(
        isHorizontalScrollEnabled = isHorizontalScrollEnabled,
        horizontalScroll = horizontalScroll.value,
        chartScale = zoom.value,
        canvasBounds = bounds,
    )
    val interactionSource = remember { MutableInteractionSource() }
    val interaction = interactionSource.interactions.collectAsState(initial = null)

    axisManager.setAxes(startAxis, topAxis, endAxis, bottomAxis)

    val setHorizontalScroll = rememberSetHorizontalScroll(
        scroll = horizontalScroll,
        touchPoint = markerTouchPoint,
        interaction = interaction,
    )

    val scrollHandler = remember { ScrollHandler(setHorizontalScroll) }
    val scrollableState = rememberScrollableState(scrollHandler::handleScrollDelta)
    val onZoom = rememberZoomState(zoom, scrollHandler, chart.bounds)
    val virtualLayout = remember { VirtualLayout(axisManager) }
    val elevationOverlayColor = currentChartStyle.elevationOverlayColor.toArgb()

    var wasMarkerVisible: Boolean by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .height(DefaultDimens.CHART_HEIGHT.dp)
            .fillMaxWidth()
            .chartTouchEvent(
                setTouchPoint = if (marker != null) markerTouchPoint.component2() else null,
                scrollableState = if (isHorizontalScrollEnabled) scrollableState else null,
                onZoom = if (isZoomEnabled) onZoom else null,
                interactionSource = interactionSource,
            ),
    ) {
        bounds.set(left = 0, top = 0, right = size.width, bottom = size.height)

        axisValuesOverrider?.also { overrider ->
            measureContext.chartValuesManager.update(overrider, model)
        } ?: chart.updateChartValues(measureContext.chartValuesManager, model)

        val segmentProperties = chart.getSegmentProperties(measureContext, model)

        virtualLayout.setBounds(
            context = measureContext,
            contentBounds = bounds,
            chart = chart,
            legend = legend,
            segmentProperties = segmentProperties,
            marker,
        )

        val chartDrawContext = chartDrawContext(
            canvas = drawContext.canvas.nativeCanvas,
            elevationOverlayColor = elevationOverlayColor,
            measureContext = measureContext,
            markerTouchPoint = markerTouchPoint.value,
            segmentProperties = segmentProperties,
            chartBounds = chart.bounds,
        )

        axisManager.drawBehindChart(chartDrawContext)
        chart.draw(chartDrawContext, model)
        axisManager.drawAboveChart(chartDrawContext)
        legend?.draw(chartDrawContext)

        ifNotNull(
            t1 = marker,
            t2 = markerTouchPoint.value?.let(chart.entryLocationMap::getClosestMarkerEntryModel),
        ) { marker, markerModel ->
            marker.draw(
                context = chartDrawContext,
                bounds = chart.bounds,
                markedEntries = markerModel,
            )
            if (wasMarkerVisible.not()) {
                markerVisibilityChangeListener?.onMarkerVisibilityChanged(true, marker)
                wasMarkerVisible = true
            }
        } ?: marker
            .takeIf { wasMarkerVisible }
            ?.also { marker ->
                markerVisibilityChangeListener?.onMarkerVisibilityChanged(false, marker)
                wasMarkerVisible = false
            }

        scrollHandler.maxScrollDistance = chartDrawContext.maxScrollDistance
        measureContext.chartValuesManager.resetChartValues()
        measureContext.clearExtras()
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
