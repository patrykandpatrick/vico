package pl.patrykgoworowski.liftchart_compose

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.axis.VerticalAxis
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryList
import pl.patrykgoworowski.liftchart_common.entry.entriesOf
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_compose.data_set.bar.ColumnChart
import pl.patrykgoworowski.liftchart_compose.data_set.bar.path.chartShape
import pl.patrykgoworowski.liftchart_compose.extension.colorInt

@Preview("Sample DataSet")
@Composable
fun SampleDataSet(modifier: Modifier = Modifier) {
    ColumnChart(
        modifier = modifier,
        singleEntryCollection = SingleEntryList(entriesOf(1 to 1, 2 to 2, 3 to 3, 4 to 2)),
        axisManager = AxisManager(
            startAxis = VerticalAxis(
                label = TextComponent(
                    textColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium).colorInt,
                    color = Color.TRANSPARENT,
                    textSize = 10f.dp,
                ),
                axis = null,
                tick = null,
                guideline = null,
            ),
            topAxis = null,
            endAxis = null,
            bottomAxis = null),
        column = RectComponent(
            LocalContentColor.current.colorInt,
            thickness = 8f.dp,
            shape = CutCornerShape(Dp(4f)).chartShape()
        )
    )
}

@Preview("Sample Card", widthDp = 200)
@Composable
fun SampleCard() {
    Card(
        modifier = Modifier,
        shape = RoundedCornerShape(Dp(8f))
    ) {
        Column(
            modifier = Modifier.padding(Dp(16f))
        ) {
            SampleDataSet(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(Dp(16f)))

            Divider()

            Spacer(modifier = Modifier.height(Dp(8f)))

            Text(
                text = "Title",
                style = MaterialTheme.typography.h6,
            )
            Text(
                text = "This is a subtitle. It may be long",
                style = MaterialTheme.typography.subtitle1,
            )

        }
    }
}