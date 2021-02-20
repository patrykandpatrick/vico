package pl.patrykgoworowski.liftchart_compose.data_set.bar

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.AmbientLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.axis.AxisRenderer
import pl.patrykgoworowski.liftchart_common.data_set.axis.Position
import pl.patrykgoworowski.liftchart_common.data_set.bar.BarDataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergedBarDataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.layout.VirtualLayout
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.defaults.DEF_CHART_WIDTH
import pl.patrykgoworowski.liftchart_common.defaults.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.extension.setAll
import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_common.path.Shape
import pl.patrykgoworowski.liftchart_compose.data_set.entry.collectAsState
import pl.patrykgoworowski.liftchart_compose.extension.colorInt
import pl.patrykgoworowski.liftchart_compose.extension.colorInts
import pl.patrykgoworowski.liftchart_compose.extension.pixels
import pl.patrykgoworowski.liftchart_compose.extension.pxToDp

@Composable
fun BarDataSet(
    singleEntryCollection: SingleEntryCollection<AnyEntry>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.secondary,
    shape: Shape = RectShape(),
    barWidth: Dp = DEF_BAR_WIDTH.dp,
    barSpacing: Dp = DEF_BAR_SPACING.dp,
    axisMap: Map<Position, AxisRenderer> = emptyMap(),
    ) {
    val dataSet = remember { BarDataSetRenderer<AnyEntry>() }
    val model = singleEntryCollection.collectAsState

    dataSet.shape = shape
    dataSet.color = color.colorInt
    dataSet.barWidth = barWidth.pixels
    dataSet.barSpacing = barSpacing.pixels

    DataSet(
        modifier = modifier,
        dataSet = dataSet,
        model = model.value,
        axisMap = axisMap,
        )
}

@Composable
fun MergedBarDataSet(
    multiEntryCollection: MultiEntryCollection<AnyEntry>,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(MaterialTheme.colors.secondary),
    mergeMode: MergeMode = MergeMode.Stack,
    shapes: List<Shape> = emptyList(),
    barWidth: Dp = DEF_BAR_WIDTH.dp,
    barSpacing: Dp = DEF_BAR_SPACING.dp,
    barInnerSpacing: Dp = DEF_MERGED_BAR_INNER_SPACING.dp,
    axisMap: Map<Position, AxisRenderer> = emptyMap(),
    ) {
    val dataSet = remember { MergedBarDataSetRenderer<AnyEntry>() }
    val model = multiEntryCollection.collectAsState

    dataSet.barPathCreators.setAll(shapes)
    dataSet.setColors(colors.colorInts)
    dataSet.groupMode = mergeMode
    dataSet.barWidth = barWidth.pixels
    dataSet.barSpacing = barSpacing.pixels
    dataSet.barInnerSpacing = barInnerSpacing.pixels

    DataSet(
        modifier = modifier,
        dataSet = dataSet,
        model = model.value,
        axisMap = axisMap,
    )
}

@Composable
fun <Model : EntriesModel> DataSet(
    modifier: Modifier,
    dataSet: DataSetRenderer<Model>,
    model: Model,
    axisMap: Map<Position, AxisRenderer>
) {
    val bounds = remember { RectF() }

    val virtualLayout = remember { VirtualLayout(true) }
    virtualLayout.isLTR = AmbientLayoutDirection.current == LayoutDirection.Ltr

    Canvas(
        modifier = modifier
            .preferredWidth(
                virtualLayout.getMeasuredWidth(
                    dataSet.getMeasuredWidth(model),
                    model,
                    axisMap
                ).pxToDp
            )
            .preferredHeight(DEF_CHART_WIDTH.dp)
    ) {

        bounds.set(0f, 0f, size.width, size.height)
        virtualLayout.setBounds(bounds, dataSet, model, axisMap)
        dataSet.getAxisModel(model).let { axisModel ->
            axisMap.forEach { (_, axis) ->
                axis.isLTR = virtualLayout.isLTR
                axis.draw(drawContext.canvas.nativeCanvas, axisModel)
            }
        }
        dataSet.draw(drawContext.canvas.nativeCanvas, model)
    }
}