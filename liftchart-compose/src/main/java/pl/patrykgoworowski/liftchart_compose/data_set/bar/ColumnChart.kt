package pl.patrykgoworowski.liftchart_compose.data_set.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.constants.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.constants.DEF_MERGED_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.data_set.bar.ColumnDataSet
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_compose.extension.pixels

@Composable
fun columnDataSet(
    columns: List<LineComponent>,
    spacing: Dp = DEF_MERGED_BAR_SPACING.dp,
    innerSpacing: Dp = DEF_MERGED_BAR_INNER_SPACING.dp,
    mergeMode: MergeMode = MergeMode.Grouped,
    minX: Float? = null,
    maxX: Float? = null,
    minY: Float? = null,
    maxY: Float? = null,
): ColumnDataSet {
    val dataSet = remember { ColumnDataSet() }
    return dataSet.apply {
        this.columns = columns
        this.spacing = spacing.pixels
        this.innerSpacing = innerSpacing.pixels
        this.mergeMode = mergeMode
        this.minX = minX
        this.maxX = maxX
        this.minY = minY
        this.maxY = maxY
    }
}

@Composable
fun columnDataSet(
    column: LineComponent,
    spacing: Dp = DEF_MERGED_BAR_SPACING.dp,
    minX: Float? = null,
    maxX: Float? = null,
    minY: Float? = null,
    maxY: Float? = null,
): ColumnDataSet = columnDataSet(
    columns = listOf(column),
    spacing = spacing,
    minX = minX,
    maxX = maxX,
    minY = minY,
    maxY = maxY,
)