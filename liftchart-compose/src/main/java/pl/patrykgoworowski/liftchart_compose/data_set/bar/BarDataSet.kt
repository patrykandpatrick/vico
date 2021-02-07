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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.bar.BarDataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergedBarDataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.BarPathCreator
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.DefaultBarPath
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryCollection
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.defaults.DEF_CHART_WIDTH
import pl.patrykgoworowski.liftchart_common.defaults.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.extension.setAll
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
    barPathCreator: BarPathCreator = DefaultBarPath(),
    barWidth: Dp = DEF_BAR_WIDTH.dp,
    barSpacing: Dp = DEF_BAR_SPACING.dp,
) {
    val dataSet = remember { BarDataSetRenderer<AnyEntry>() }
    val bounds = remember { RectF() }
    val model = singleEntryCollection.collectAsState

    dataSet.barPathCreator = barPathCreator
    dataSet.color = color.colorInt
    dataSet.barWidth = barWidth.pixels
    dataSet.barSpacing = barSpacing.pixels

    Canvas(
        modifier = modifier
            .preferredWidth(dataSet.getMeasuredWidth(model.value).pxToDp)
            .preferredHeight(DEF_CHART_WIDTH.dp)
    ) {
        bounds.set(0f, 0f, size.width, size.height)
        dataSet.setBounds(bounds, model.value)
        dataSet.draw(drawContext.canvas.nativeCanvas, model.value)
    }

   // DrawDataSet(modifier = modifier, bounds = bounds, dataSet = dataSet)
}

@Composable
fun MergedBarDataSet(
    multiEntryCollection: MultiEntryCollection<AnyEntry>,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(MaterialTheme.colors.secondary),
    mergeMode: MergeMode = MergeMode.Stack,
    barPathCreators: List<BarPathCreator> = emptyList(),
    barWidth: Dp = DEF_BAR_WIDTH.dp,
    barSpacing: Dp = DEF_BAR_SPACING.dp,
    barInnerSpacing: Dp = DEF_MERGED_BAR_INNER_SPACING.dp,
) {
    val dataSet = remember { MergedBarDataSetRenderer<AnyEntry>() }
    val bounds = remember { RectF() }
    val model = multiEntryCollection.collectAsState

    dataSet.barPathCreators.setAll(barPathCreators)
    dataSet.setColors(colors.colorInts)
    dataSet.groupMode = mergeMode
    dataSet.barWidth = barWidth.pixels
    dataSet.barSpacing = barSpacing.pixels
    dataSet.barInnerSpacing = barInnerSpacing.pixels

    Canvas(
        modifier = modifier
            .preferredWidth(dataSet.getMeasuredWidth(model.value).pxToDp)
            .preferredHeight(DEF_CHART_WIDTH.dp)
    ) {
        bounds.set(0f, 0f, size.width, size.height)
        dataSet.setBounds(bounds, model.value)
        dataSet.draw(drawContext.canvas.nativeCanvas, model.value)
    }
}