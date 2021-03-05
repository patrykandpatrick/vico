package pl.patrykgoworowski.liftchart_compose.data_set.bar

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.AmbientLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
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
import pl.patrykgoworowski.liftchart_common.path.CutCornerBarPath
import pl.patrykgoworowski.liftchart_compose.data_set.entry.collectAsState
import pl.patrykgoworowski.liftchart_compose.extension.colorInt
import pl.patrykgoworowski.liftchart_compose.extension.pixels
import pl.patrykgoworowski.liftchart_compose.extension.pxToDp

@Composable
val defaultColumnComponent: RectComponent
    get() = RectComponent(
        color = MaterialTheme.colors.secondary.colorInt,
        thickness = DEF_BAR_WIDTH.dp.pixels,
        shape = CutCornerBarPath(topLeft = 8f.dp.pixels)
    )

@Composable
fun ColumnChart(
    singleEntryCollection: SingleEntryCollection<AnyEntry>,
    modifier: Modifier = Modifier,
    column: RectComponent = defaultColumnComponent,
    spacing: Dp = DEF_BAR_SPACING.dp,
    axisManager: AxisManager = AxisManager(),
) {
    val dataSet = remember {
        ColumnDataSetRenderer<AnyEntry>(
            column = column,
            spacing = 0f,
        )
    }
    dataSet.spacing = spacing.pixels
    val model = singleEntryCollection.collectAsState

    DataSet(
        modifier = modifier,
        dataSet = dataSet,
        model = model.value,
        axisManager = axisManager,
    )
}

@Composable
fun MergedColumnChart(
    multiEntryCollection: MultiEntryCollection<AnyEntry>,
    modifier: Modifier = Modifier,
    columns: List<RectComponent> = listOf(defaultColumnComponent),
    mergeMode: MergeMode = MergeMode.Stack,
    spacing: Dp = DEF_BAR_SPACING.dp,
    innerSpacing: Dp = DEF_MERGED_BAR_INNER_SPACING.dp,
    axisManager: AxisManager = AxisManager(),
) {
    val dataSet = remember { MergedColumnDataSetRenderer<AnyEntry>(columns, mergeMode = mergeMode) }
    val model = multiEntryCollection.collectAsState

    dataSet.spacing = spacing.pixels
    dataSet.innerSpacing = innerSpacing.pixels

    DataSet(
        modifier = modifier,
        dataSet = dataSet,
        model = model.value,
        axisManager = axisManager,
    )
}

@Composable
fun <Model : EntriesModel> DataSet(
    modifier: Modifier,
    dataSet: DataSetRenderer<Model>,
    model: Model,
    axisManager: AxisManager = AxisManager(),
) {
    val bounds = remember { RectF() }

    val virtualLayout = remember { VirtualLayout(true) }
    virtualLayout.isLTR = AmbientLayoutDirection.current == LayoutDirection.Ltr

    Canvas(
        modifier = modifier
            .preferredWidth(
                virtualLayout.getMeasuredWidth(dataSet, model, axisManager).pxToDp
            )
            .preferredHeight(DEF_CHART_WIDTH.dp)
    ) {

        bounds.set(0f, 0f, size.width, size.height)
        virtualLayout.setBounds(bounds, dataSet, model, axisManager)
        val canvas = drawContext.canvas.nativeCanvas
        dataSet.getAxisModel(model).let { axisModel ->
            axisManager.draw(canvas, axisModel)
        }
        dataSet.draw(canvas, model)
    }
}