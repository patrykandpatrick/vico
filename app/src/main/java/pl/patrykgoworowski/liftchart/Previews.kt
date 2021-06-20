package pl.patrykgoworowski.liftchart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.axis.VerticalAxis
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryList
import pl.patrykgoworowski.liftchart_common.entry.entriesOf
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_compose.component.shapeComponent
import pl.patrykgoworowski.liftchart_compose.component.textComponent
import pl.patrykgoworowski.liftchart_compose.data_set.bar.ColumnChart
import pl.patrykgoworowski.liftchart_compose.extension.colorInt
import pl.patrykgoworowski.liftchart_compose.path.chartShape

@Preview("Sample DataSet", widthDp = 200)
@Composable
fun SampleDataSet(modifier: Modifier = Modifier) {
    ColumnChart(
        modifier = modifier,
        singleEntryCollection = SingleEntryList(entriesOf(1 to 1, 2 to 2, 3 to 3, 4 to 2)),
        axisManager = AxisManager(
            startAxis = VerticalAxis(
                label = textComponent(
                    color = MaterialTheme.colors.primary,
                    textSize = 10f.sp,
                    background = shapeComponent(
                        shape = CutCornerShape(
                            CornerSize(25),
                            CornerSize(50),
                            CornerSize(50),
                            CornerSize(25)
                        ),
                        color = MaterialTheme.colors.primary.copy(0.1f),
                    )
                ).apply {
                    padding.apply {
                        end = 8f.dp
                        start = 4f.dp
                    }
                },
                axis = null,
                tick = null,
                guideline = GuidelineComponent(
                    MaterialTheme.colors.primary.copy(0.1f).colorInt,
                    1f.dp
                ),
            ),
            topAxis = null,
            endAxis = null,
            bottomAxis = null
        ),
        column = RectComponent(
            MaterialTheme.colors.primary.colorInt,
            thickness = 8f.dp,
            shape = CutCornerShape(Dp(4f)).chartShape()
        )
    )
}

@Preview("Sample Card", widthDp = 200)
@Composable
fun SampleCard() {
    Card(
        modifier = Modifier
            .padding(Dp(8f)),
        shape = RoundedCornerShape(Dp(8f)),
        elevation = Dp(4f)
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