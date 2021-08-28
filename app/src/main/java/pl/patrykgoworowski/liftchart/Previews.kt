package pl.patrykgoworowski.liftchart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.patrykgoworowski.liftchart.ui.MainTheme
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.liftchart_common.axis.vertical.VerticalAxis
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.shape.ShapeComponent
import pl.patrykgoworowski.liftchart_common.component.shape.shader.componentShader
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryList
import pl.patrykgoworowski.liftchart_common.entry.entriesOf
import pl.patrykgoworowski.liftchart_common.path.DashedShape
import pl.patrykgoworowski.liftchart_common.path.pillShape
import pl.patrykgoworowski.liftchart_common.path.rectShape
import pl.patrykgoworowski.liftchart_compose.component.dimension.setPadding
import pl.patrykgoworowski.liftchart_compose.component.rectComponent
import pl.patrykgoworowski.liftchart_compose.component.shape.shader.verticalGradient
import pl.patrykgoworowski.liftchart_compose.component.shapeComponent
import pl.patrykgoworowski.liftchart_compose.component.textComponent
import pl.patrykgoworowski.liftchart_compose.data_set.bar.ColumnChart
import pl.patrykgoworowski.liftchart_compose.data_set.bar.DataSet
import pl.patrykgoworowski.liftchart_compose.data_set.bar.lineDataSet
import pl.patrykgoworowski.liftchart_compose.extension.pixelSize
import pl.patrykgoworowski.liftchart_compose.extension.pixels
import pl.patrykgoworowski.liftchart_compose.path.chartShape

@Preview("Sample Card With Column Chart", widthDp = 200)
@Composable
fun ColumnChartCard() = MainTheme {
    SampleCard {
        val colors = MaterialTheme.colors
        ColumnChart(
            modifier = Modifier
                .height(100.dp),
            entryCollection = MultiEntryList(
                entriesOf(1 to 1, 2 to 2, 3 to 3, 4 to 2),
                animateChanges = false,
            ),
            axisManager = AxisManager(
                startAxis = VerticalAxis.start(
                    label = textComponent(
                        color = colors.primary,
                        textSize = 10.sp,
                        background = shapeComponent(
                            shape = CutCornerShape(
                                CornerSize(25),
                                CornerSize(50),
                                CornerSize(50),
                                CornerSize(25)
                            ),
                            color = colors.primary.copy(0.1f),
                        )
                    ).apply {
                        setPadding(end = 8.dp, start = 4.dp)
                    },
                    axis = null,
                    tick = null,
                    guideline = LineComponent(
                        colors.primary.copy(0.1f).toArgb(),
                        1.dp.pixels,
                    ),
                ),
                topAxis = null,
                endAxis = null,
                bottomAxis = null
            ),
            column = rectComponent(
                colors.primary,
                thickness = 8f.dp,
                shape = RoundedCornerShape(4.dp).chartShape(),
                dynamicShader = verticalGradient(arrayOf(colors.primary, colors.secondary)),
            )
        )
    }
}

@Preview("Sample Card With Line Chart", widthDp = 200)
@Composable
fun LineChartCard() = MainTheme {
    SampleCard {
        val colors = MaterialTheme.colors

        DataSet(
            modifier = Modifier
                .height(100.dp),
            dataSet = lineDataSet(
                point = null,
                lineColor = colors.primary,
                lineBackgroundShader = componentShader(
                    component = shapeComponent(
                        shape = pillShape(),
                        color = colors.primary,
                    ).apply {
                        setMargins(0.5f.dp.pixels)
                    },
                    componentSize = 4.dp.pixels,
                ),
                minX = 0f,
                maxY = 3f,
            ),
            model = MultiEntryList(
                entriesOf(-1 to 0, 0 to 0, 1 to 1, 2 to 2, 3 to 0, 4 to 2, 5 to 1),
                animateChanges = false,
            ).model,
            axisManager = AxisManager(
                startAxis = VerticalAxis.start(
                    label = TextComponent(
                        color = colors.onSurface.toArgb(),
                        textSize = 10.sp.pixelSize(),
                        background = ShapeComponent(
                            shape = rectShape(),
                            color = Color.LightGray.toArgb(),
                        )
                    ).apply {
                        setPadding(horizontal = 4.dp, vertical = 2.dp)
                    },
                    axis = null,
                    tick = null,
                    guideline = LineComponent(
                        shape = DashedShape(
                            shape = pillShape(),
                            dashLength = 2.dp.pixels,
                            gapLength = 4.dp.pixels,
                        ),
                        color = Color.LightGray.toArgb(),
                        thickness = 1.dp.pixels,
                    ),
                ).apply {
                    horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
                },
                topAxis = null,
                endAxis = null,
                bottomAxis = HorizontalAxis.bottom(
                    label = null,
                    axis = LineComponent(Color.LightGray.toArgb()),
                    tick = null,
                    guideline = null,
                )
            ),
            marker = null,
        )
    }
}

@Composable
fun SampleCard(
    chart: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(Dp(8f)),
        shape = RoundedCornerShape(Dp(8f)),
        elevation = Dp(4f)
    ) {
        Column(
            modifier = Modifier.padding(Dp(16f))
        ) {
            chart()

            Spacer(modifier = Modifier.height(Dp(8f)))

            Text(
                text = "Title",
                style = MaterialTheme.typography.h6,
            )
            Text(
                text = "This is a subtitle. It may be long.",
                style = MaterialTheme.typography.subtitle1,
            )

        }
    }
}