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

package pl.patrykgoworowski.vico.compose.dataset.bar

import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.compose.dataset.entry.collectAsState
import pl.patrykgoworowski.vico.compose.extension.chartTouchEvent
import pl.patrykgoworowski.vico.compose.gesture.rememberOnZoom
import pl.patrykgoworowski.vico.core.MAX_ZOOM
import pl.patrykgoworowski.vico.core.MIN_ZOOM
import pl.patrykgoworowski.vico.core.axis.AxisManager
import pl.patrykgoworowski.vico.core.axis.AxisRenderer
import pl.patrykgoworowski.vico.core.axis.model.MutableDataSetModel
import pl.patrykgoworowski.vico.core.constants.DEF_CHART_WIDTH
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryCollection
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.layout.VirtualLayout
import pl.patrykgoworowski.vico.core.dataset.renderer.DataSet
import pl.patrykgoworowski.vico.core.dataset.renderer.MutableRendererViewState
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.scroll.ScrollHandler

@Composable
fun <Model : EntryModel> DataSet(
    modifier: Modifier,
    dataSet: DataSet<Model>,
    entryCollection: EntryCollection<Model>,
    startAxis: AxisRenderer<pl.patrykgoworowski.vico.core.axis.AxisPosition.Vertical.Start>? = null,
    topAxis: AxisRenderer<pl.patrykgoworowski.vico.core.axis.AxisPosition.Horizontal.Top>? = null,
    endAxis: AxisRenderer<pl.patrykgoworowski.vico.core.axis.AxisPosition.Vertical.End>? = null,
    bottomAxis: AxisRenderer<pl.patrykgoworowski.vico.core.axis.AxisPosition.Horizontal.Bottom>? = null,
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

@Composable
fun <Model : EntryModel> DataSet(
    modifier: Modifier,
    dataSet: DataSet<Model>,
    model: Model,
    startAxis: AxisRenderer<pl.patrykgoworowski.vico.core.axis.AxisPosition.Vertical.Start>? = null,
    topAxis: AxisRenderer<pl.patrykgoworowski.vico.core.axis.AxisPosition.Horizontal.Top>? = null,
    endAxis: AxisRenderer<pl.patrykgoworowski.vico.core.axis.AxisPosition.Vertical.End>? = null,
    bottomAxis: AxisRenderer<pl.patrykgoworowski.vico.core.axis.AxisPosition.Horizontal.Bottom>? = null,
    marker: Marker? = null,
    isHorizontalScrollEnabled: Boolean = true,
    isZoomEnabled: Boolean = true,
) {
    val axisManager = remember { AxisManager() }
    val bounds = remember { RectF() }
    val dataSetModel = remember { MutableDataSetModel() }
    val rendererViewState = remember { mutableStateOf(MutableRendererViewState()) }
    val zoom = remember { mutableStateOf(1f) }
    val setRendererViewState = rendererViewState.component2()
    val setTouchPoint = { pointF: PointF? ->
        setRendererViewState(rendererViewState.value.copy(markerTouchPoint = pointF))
    }

    axisManager.apply {
        this.startAxis = startAxis
        this.topAxis = topAxis
        this.endAxis = endAxis
        this.bottomAxis = bottomAxis
    }

    val setHorizontalScroll = { scrollX: Float ->
        setRendererViewState(
            rendererViewState.value.copy(
                markerTouchPoint = null,
                horizontalScroll = scrollX,
            )
        )
    }

    val scrollHandler = remember { ScrollHandler(setHorizontalScroll) }
    val scrollableState = remember { ScrollableState(scrollHandler::handleScrollDelta) }

    val onZoom = rememberOnZoom { centroid, zoomChange ->
        val newZoom = zoom.value * zoomChange
        if (newZoom !in MIN_ZOOM..MAX_ZOOM) return@rememberOnZoom
        val centerX = scrollHandler.currentScroll + centroid.x - dataSet.bounds.left
        val zoomedCenterX = centerX * zoomChange
        zoom.value = newZoom
        scrollHandler.currentScroll += zoomedCenterX - centerX
    }

    val virtualLayout = remember { VirtualLayout(true) }
    virtualLayout.isLTR = LocalLayoutDirection.current == LayoutDirection.Ltr

    Canvas(
        modifier = modifier
            .height(DEF_CHART_WIDTH.dp)
            .fillMaxWidth()
            .then(
                if (marker != null) {
                    Modifier.chartTouchEvent(
                        setTouchPoint = setTouchPoint,
                        scrollableState = if (isHorizontalScrollEnabled) scrollableState else null,
                        onZoom = if (isZoomEnabled) onZoom else null,
                    )
                } else Modifier
            )
    ) {
        bounds.set(0f, 0f, size.width, size.height)
        dataSet.setToAxisModel(dataSetModel, model)
        dataSet.isHorizontalScrollEnabled = isHorizontalScrollEnabled || isZoomEnabled
        dataSet.zoom = zoom.value
        virtualLayout.setBounds(bounds, dataSet, model, dataSetModel, axisManager, marker)
        val canvas = drawContext.canvas.nativeCanvas
        val segmentProperties = dataSet.getSegmentProperties(model)
        axisManager.drawBehindDataSet(
            canvas,
            model,
            dataSetModel,
            segmentProperties,
            rendererViewState.value
        )
        dataSet.draw(canvas, model, segmentProperties, rendererViewState.value, marker)
        axisManager.drawAboveDataSet(
            canvas,
            model,
            dataSetModel,
            segmentProperties,
            rendererViewState.value
        )
        scrollHandler.maxScrollDistance = dataSet.maxScrollAmount
    }
}
