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

package pl.patrykgoworowski.vico.compose.dataset

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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.compose.dataset.entry.collectAsState
import pl.patrykgoworowski.vico.compose.extension.addIf
import pl.patrykgoworowski.vico.compose.extension.chartTouchEvent
import pl.patrykgoworowski.vico.compose.gesture.OnZoom
import pl.patrykgoworowski.vico.compose.layout.getMeasureContext
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.MAX_ZOOM
import pl.patrykgoworowski.vico.core.MIN_ZOOM
import pl.patrykgoworowski.vico.core.axis.AxisManager
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.AxisRenderer
import pl.patrykgoworowski.vico.core.axis.model.MutableDataSetModel
import pl.patrykgoworowski.vico.core.dataset.draw.chartDrawContext
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryCollection
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.renderer.DataSet
import pl.patrykgoworowski.vico.core.extension.set
import pl.patrykgoworowski.vico.core.layout.VirtualLayout
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.model.Point
import pl.patrykgoworowski.vico.core.scroll.ScrollHandler

@Composable
fun <Model : EntryModel> DataSet(
    modifier: Modifier,
    dataSet: DataSet<Model>,
    entryCollection: EntryCollection<Model>,
    startAxis: AxisRenderer<AxisPosition.Vertical.Start>? = null,
    topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? = null,
    endAxis: AxisRenderer<AxisPosition.Vertical.End>? = null,
    bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? = null,
    marker: Marker? = null,
    isHorizontalScrollEnabled: Boolean = true,
    isZoomEnabled: Boolean = true,
) {
    val model = entryCollection.collectAsState()

    DataSet(
        modifier = modifier,
        dataSet = dataSet,
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
fun <Model : EntryModel> DataSet(
    modifier: Modifier,
    dataSet: DataSet<Model>,
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
    val dataSetModel = remember { MutableDataSetModel() }
    val bounds = remember { RectF() }
    val markerTouchPoint = remember { mutableStateOf<Point?>(null) }
    val horizontalScroll = remember { mutableStateOf(0f) }
    val zoom = remember { mutableStateOf(1f) }
    val measureContext = getMeasureContext(
        isHorizontalScrollEnabled = isHorizontalScrollEnabled,
        zoom = zoom.value
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
    val onZoom = rememberZoomState(zoom, scrollHandler, dataSet.bounds)
    val virtualLayout = remember { VirtualLayout() }

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
            .onSizeChanged { size ->
                bounds.set(0, 0, size.width, size.height)
                virtualLayout.setBounds(
                    context = measureContext,
                    contentBounds = bounds,
                    dataSet = dataSet,
                    dataSetModel = dataSetModel,
                    axisManager = axisManager,
                    marker
                )
            }
    ) {
        dataSet.setToAxisModel(dataSetModel, model)

        val chartDrawContext = chartDrawContext(
            canvas = drawContext.canvas.nativeCanvas,
            measureContext = measureContext,
            horizontalScroll = horizontalScroll.value,
            markerTouchPoint = markerTouchPoint.value,
            segmentProperties = dataSet.getSegmentProperties(measureContext, model),
            dataSetModel = dataSetModel,
        )
        axisManager.drawBehindDataSet(chartDrawContext)
        dataSet.draw(chartDrawContext, model, marker)
        axisManager.drawAboveDataSet(chartDrawContext)
        scrollHandler.maxScrollDistance = dataSet.maxScrollAmount
    }
}

@Composable
fun rememberSetHorizontalScroll(
    scroll: MutableState<Float>,
    touchPoint: MutableState<Point?>,
    interaction: State<Interaction?>,
) = remember {
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
fun rememberZoomState(
    zoom: MutableState<Float>,
    scrollHandler: ScrollHandler,
    dataSetBounds: RectF
): OnZoom = remember {
    onZoom@{ centroid, zoomChange ->
        val newZoom = zoom.value * zoomChange
        if (newZoom !in MIN_ZOOM..MAX_ZOOM) return@onZoom
        val centerX = scrollHandler.currentScroll + centroid.x - dataSetBounds.left
        val zoomedCenterX = centerX * zoomChange
        zoom.value = newZoom
        scrollHandler.currentScroll += zoomedCenterX - centerX
    }
}
