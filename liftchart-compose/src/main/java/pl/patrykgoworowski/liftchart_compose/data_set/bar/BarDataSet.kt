package pl.patrykgoworowski.liftchart_compose.data_set.bar

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection
import pl.patrykgoworowski.liftchart_compose.extension.colorInt
import pl.patrykgoworowski.liftchart_compose.extension.colorInts
import pl.patrykgoworowski.liftchart_compose.extension.pixels
import pl.patrykgoworowski.liftchart_core.data_set.bar.BarPathCreator
import pl.patrykgoworowski.liftchart_core.data_set.bar.CoreBarDataSet
import pl.patrykgoworowski.liftchart_core.data_set.bar.CoreMergedBarDataSet
import pl.patrykgoworowski.liftchart_core.data_set.bar.DefaultBarPath
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_core.extension.setAll

@Composable
fun BarDataSet(
    entryCollection: EntryCollection<AnyEntry>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.secondary,
    barPathCreator: BarPathCreator = DefaultBarPath(),
    barWidth: Dp = DEF_BAR_WIDTH.dp,
    barSpacing: Dp = DEF_BAR_SPACING.dp,
) {
    val dataSet = remember { CoreBarDataSet(entryCollection) }
    val bounds = remember { RectF() }

    dataSet.barPathCreator = barPathCreator
    dataSet.color = color.colorInt
    dataSet.barWidth = barWidth.pixels
    dataSet.barSpacing = barSpacing.pixels

    Canvas(modifier) {
        bounds.set(0f, 0f, size.width, size.height)
        dataSet.setBounds(bounds)

        this.drawIntoCanvas { canvas ->
            dataSet.draw(canvas.nativeCanvas, 0f)
        }
    }
}

@Composable
fun MergedBarDataSet(
    entryCollections: List<EntryCollection<AnyEntry>>,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(MaterialTheme.colors.secondary),
    groupMode: CoreMergedBarDataSet.GroupMode = CoreMergedBarDataSet.GroupMode.Stack,
    barPathCreators: List<BarPathCreator> = emptyList(),
    barWidth: Dp = DEF_BAR_WIDTH.dp,
    barSpacing: Dp = DEF_BAR_SPACING.dp,
    barInnerSpacing: Dp = DEF_BAR_INNER_SPACING.dp,
) {
    val dataSet = remember { CoreMergedBarDataSet(entryCollections) }
    val bounds = remember { RectF() }

    dataSet.barPathCreators.setAll(barPathCreators)
    dataSet.setColors(colors.colorInts)
    dataSet.groupMode = groupMode
    dataSet.barWidth = barWidth.pixels
    dataSet.barSpacing = barSpacing.pixels
    dataSet.barInnerSpacing = barInnerSpacing.pixels

    Canvas(modifier) {
        bounds.set(0f, 0f, size.width, size.height)
        dataSet.setBounds(bounds)

        this.drawIntoCanvas { canvas ->
            dataSet.draw(canvas.nativeCanvas, 0f)
        }
    }
}