/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.compose.chart

import android.graphics.RectF
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.compose.chart.entry.collectAsState
import pl.patrykgoworowski.vico.compose.extension.addIf
import pl.patrykgoworowski.vico.compose.extension.chartTouchEvent
import pl.patrykgoworowski.vico.compose.gesture.OnZoom
import pl.patrykgoworowski.vico.compose.layout.getMeasureContext
import pl.patrykgoworowski.vico.compose.style.currentChartColors
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.MAX_ZOOM
import pl.patrykgoworowski.vico.core.MIN_ZOOM
import pl.patrykgoworowski.vico.core.axis.AxisManager
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.AxisRenderer
import pl.patrykgoworowski.vico.core.axis.model.MutableChartModel
import pl.patrykgoworowski.vico.core.chart.Chart
import pl.patrykgoworowski.vico.core.chart.draw.chartDrawContext
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.entry.ChartModelProducer
import pl.patrykgoworowski.vico.core.extension.set
import pl.patrykgoworowski.vico.core.layout.VirtualLayout
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.model.Point
import pl.patrykgoworowski.vico.core.scroll.ScrollHandler

@Composable
public fun <Model : ChartEntryModel> Chart(
    modifier: Modifier,
    chart: Chart<Model>,
    chartModelProducer: ChartModelProducer<Model>,
    startAxis: AxisRenderer<AxisPosition.Vertical.Start>? = null,
    topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? = null,
    endAxis: AxisRenderer<AxisPosition.Vertical.End>? = null,
    bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? = null,
    marker: Marker? = null,
    isHorizontalScrollEnabled: Boolean = true,
    isZoomEnabled: Boolean = true,
) {
    val model = chartModelProducer.collectAsState()

    Chart(
        modifier = modifier,
        chart = chart,
        model = model.value,
        startAxis = startAxis,
        topAxis = topAxis,
        endAxis = endAxis,
        bottomAxis = bottomAxis,
        marker = marker,
        isHorizontalScrollEnabled = isHorizontalScrollEnabled,
        isZoomEnabled = isZoomEnabled
    )
}

@Suppress("LongMethod")
@Composable
public fun <Model : ChartEntryModel> Chart(
    modifier: Modifier,
    chart: Chart<Model>,
    model: Model,
    startAxis: AxisRenderer<AxisPosition.Vertical.Start>? = null,
    topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? = null,
    endAxis: AxisRenderer<AxisPosition.Vertical.End>? = null,
    bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? = null,
    marker: Marker? = null,
    isHorizontalScrollEnabled: Boolean = true,
    isZoomEnabled: Boolean = true,
) {
    val axisManager = remember { AxisManager() }
    val chartModel = remember { MutableChartModel() }
    chart.setToAxisModel(chartModel, model)
    val bounds = remember { RectF() }
    val markerTouchPoint = remember { mutableStateOf<Point?>(null) }
    val horizontalScroll = remember { mutableStateOf(0f) }
    val zoom = remember { mutableStateOf(1f) }
    val measureContext = getMeasureContext(
        isHorizontalScrollEnabled = isHorizontalScrollEnabled,
        horizontalScroll = horizontalScroll.value,
        zoom = zoom.value,
        chartModel = chartModel,
    )
    val interactionSource = remember { MutableInteractionSource() }
    val interaction = interactionSource.interactions.collectAsState(initial = null)

    axisManager.setAxes(startAxis, topAxis, endAxis, bottomAxis)

    val setHorizontalScroll = rememberSetHorizontalScroll(
        scroll = horizontalScroll,
        touchPoint = markerTouchPoint,
        interaction = interaction
    )

    val scrollHandler = remember { ScrollHandler(setHorizontalScroll) }
    val scrollableState = rememberScrollableState(scrollHandler::handleScrollDelta)
    val onZoom = rememberZoomState(zoom, scrollHandler, chart.bounds)
    val virtualLayout = remember { VirtualLayout() }
    val chartColors = currentChartColors

    Canvas(
        modifier = modifier
            .height(Dimens.CHART_HEIGHT.dp)
            .fillMaxWidth()
            .addIf(marker != null) {
                chartTouchEvent(
                    setTouchPoint = markerTouchPoint.component2(),
                    scrollableState = if (isHorizontalScrollEnabled) scrollableState else null,
                    onZoom = if (isZoomEnabled) onZoom else null,
                    interactionSource = interactionSource,
                )
            }
    ) {
        bounds.set(0, 0, size.width, size.height)
        virtualLayout.setBounds(
            context = measureContext,
            contentBounds = bounds,
            chart = chart,
            chartModel = chartModel,
            axisManager = axisManager,
            marker
        )
        val chartDrawContext = chartDrawContext(
            canvas = drawContext.canvas.nativeCanvas,
            colors = chartColors,
            measureContext = measureContext,
            markerTouchPoint = markerTouchPoint.value,
            segmentProperties = chart.getSegmentProperties(measureContext, model),
            chartModel = chartModel,
        )
        axisManager.drawBehindChart(chartDrawContext)
        chart.draw(chartDrawContext, model, marker)
        axisManager.drawAboveChart(chartDrawContext)
        scrollHandler.maxScrollDistance = chart.maxScrollAmount
        measureContext.clearExtras()
    }
}

@Composable
public fun rememberSetHorizontalScroll(
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
public fun rememberZoomState(
    zoom: MutableState<Float>,
    scrollHandler: ScrollHandler,
    chartBounds: RectF
): OnZoom = remember {
    onZoom@{ centroid, zoomChange ->
        val newZoom = zoom.value * zoomChange
        if (newZoom !in MIN_ZOOM..MAX_ZOOM) return@onZoom
        val transformationAxisX = scrollHandler.currentScroll + centroid.x - chartBounds.left
        val zoomedTransformationAxisX = transformationAxisX * zoomChange
        zoom.value = newZoom
        scrollHandler.currentScroll += zoomedTransformationAxisX - transformationAxisX
    }
}
