package pl.patrykgoworowski.liftchart_compose.data_set.bar

import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.component.MarkerComponent
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.constants.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.constants.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.constants.DEF_CHART_WIDTH
import pl.patrykgoworowski.liftchart_common.constants.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.bar.ColumnDataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergedColumnDataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.layout.VirtualLayout
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_common.path.cutCornerShape
import pl.patrykgoworowski.liftchart_compose.data_set.entry.collectAsState
import pl.patrykgoworowski.liftchart_compose.extension.chartTouchEvent
import pl.patrykgoworowski.liftchart_compose.extension.colorInt
import pl.patrykgoworowski.liftchart_compose.extension.pixels
import pl.patrykgoworowski.liftchart_compose.extension.runIf


val defaultColumnComponent: RectComponent
    @Composable
    get() = RectComponent(
        color = MaterialTheme.colors.secondary.colorInt,
        thickness = DEF_BAR_WIDTH.dp.pixels,
        shape = cutCornerShape(topLeft = 8f.dp.pixels)
    )

@Composable
fun ColumnChart(
    singleEntryCollection: SingleEntryCollection,
    modifier: Modifier = Modifier,
    column: RectComponent = defaultColumnComponent,
    spacing: Dp = DEF_BAR_SPACING.dp,
    axisManager: AxisManager = AxisManager(),
    marker: Marker? = MarkerComponent(),
) {
    val dataSet = remember {
        ColumnDataSetRenderer(
            column = column,
            spacing = 0f,
        )
    }
    dataSet.spacing = spacing.pixels
    val model = singleEntryCollection.collectAsState()

    DataSet(
        modifier = modifier,
        dataSet = dataSet,
        model = model.value,
        axisManager = axisManager,
        marker = marker,
    )
}

@Composable
fun MergedColumnChart(
    multiEntryCollection: MultiEntryCollection,
    modifier: Modifier = Modifier,
    columns: List<RectComponent> = listOf(defaultColumnComponent),
    mergeMode: MergeMode = MergeMode.Stack,
    spacing: Dp = DEF_BAR_SPACING.dp,
    innerSpacing: Dp = DEF_MERGED_BAR_INNER_SPACING.dp,
    axisManager: AxisManager = AxisManager(),
    marker: Marker? = MarkerComponent(),
) {
    val dataSet = remember { MergedColumnDataSetRenderer(columns, mergeMode = mergeMode) }
    val model = multiEntryCollection.collectAsState()

    dataSet.spacing = spacing.pixels
    dataSet.innerSpacing = innerSpacing.pixels

    DataSet(
        modifier = modifier,
        dataSet = dataSet,
        model = model.value,
        axisManager = axisManager,
        marker = marker,
    )
}

@Composable
fun <Model : EntriesModel> DataSet(
    modifier: Modifier,
    dataSet: DataSetRenderer<Model>,
    model: Model,
    axisManager: AxisManager,
    marker: Marker?,
) {
    val bounds = remember { RectF() }

    val (touchPoint, setTouchPoint) = remember { mutableStateOf<PointF?>(null) }
    val virtualLayout = remember { VirtualLayout(true) }
    virtualLayout.isLTR = LocalLayoutDirection.current == LayoutDirection.Ltr

    Canvas(
        modifier = modifier
            .height(DEF_CHART_WIDTH.dp)
            .fillMaxWidth()
            .runIf(marker != null) { chartTouchEvent(setTouchPoint) }
    ) {
        bounds.set(0f, 0f, size.width, size.height)
        virtualLayout.setBounds(bounds, dataSet, model, axisManager, marker)
        val canvas = drawContext.canvas.nativeCanvas
        val segmentProperties = dataSet.getSegmentProperties(model)
        axisManager.draw(canvas, model, segmentProperties)
        dataSet.draw(canvas, model, touchPoint, marker)
    }
}