package pl.patrykgoworowski.liftchart_compose.data_set.bar

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
import pl.patrykgoworowski.liftchart_common.MAX_ZOOM
import pl.patrykgoworowski.liftchart_common.MIN_ZOOM
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.axis.AxisPosition
import pl.patrykgoworowski.liftchart_common.axis.AxisRenderer
import pl.patrykgoworowski.liftchart_common.axis.model.MutableDataSetModel
import pl.patrykgoworowski.liftchart_common.constants.DEF_CHART_WIDTH
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.layout.VirtualLayout
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSet
import pl.patrykgoworowski.liftchart_common.data_set.renderer.MutableRendererViewState
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_common.scroll.ScrollHandler
import pl.patrykgoworowski.liftchart_compose.data_set.entry.collectAsState
import pl.patrykgoworowski.liftchart_compose.extension.chartTouchEvent
import pl.patrykgoworowski.liftchart_compose.gesture.rememberOnZoom

@Composable
fun <Model : EntriesModel> DataSet(
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

@Composable
fun <Model : EntriesModel> DataSet(
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
        dataSet.draw(canvas, model, rendererViewState.value, marker)
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