package pl.patrykgoworowski.liftchart_compose.data_set.bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import pl.patrykgoworowski.liftchart_common.axis.AxisPosition
import pl.patrykgoworowski.liftchart_common.axis.AxisRenderer
import pl.patrykgoworowski.liftchart_common.axis.horizontal.bottomAxis
import pl.patrykgoworowski.liftchart_common.axis.horizontal.topAxis
import pl.patrykgoworowski.liftchart_common.axis.vertical.endAxis
import pl.patrykgoworowski.liftchart_common.axis.vertical.startAxis
import pl.patrykgoworowski.liftchart_compose.component.rectComponent
import pl.patrykgoworowski.liftchart_compose.data_set.entry.multiEntryModelOf

private val model = multiEntryModelOf(1, 2, 3, 4)

private val topAxis = topAxis()
private val startAxis = startAxis()
private val bottomAxis = bottomAxis()
private val endAxis = endAxis()

@Composable
private fun PreviewColumnChart(
    modifier: Modifier = Modifier,
    startAxis: AxisRenderer<AxisPosition.Vertical.Start>? = null,
    topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? = null,
    endAxis: AxisRenderer<AxisPosition.Vertical.End>? = null,
    bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? = null,
) {
    DataSet(
        modifier = modifier,
        dataSet = columnDataSet(column = rectComponent(color = Color.Blue)),
        model = model,
        startAxis = startAxis,
        topAxis = topAxis,
        endAxis = endAxis,
        bottomAxis = bottomAxis,
    )
}

@Preview("Column Chart Left", heightDp = 100, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewColumnChartLeft() {
    PreviewColumnChart(startAxis = startAxis)
}

@Preview(
    "Column Chart Top",
    heightDp = 100,
    widthDp = 100,
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun PreviewColumnChartTop() {
    PreviewColumnChart(topAxis = topAxis)
}

@Preview(
    "Column Chart Right",
    heightDp = 100,
    widthDp = 100,
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun PreviewColumnChartRight() {
    PreviewColumnChart(endAxis = endAxis)
}

@Preview(
    "Column Chart Bottom",
    heightDp = 100,
    widthDp = 100,
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun PreviewColumnChartBottom() {
    PreviewColumnChart(bottomAxis = bottomAxis)
}

@Preview(
    "Column Chart Bottom-Left",
    heightDp = 100,
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun PreviewColumnChartBottomLeft() {
    PreviewColumnChart(startAxis = startAxis, bottomAxis = bottomAxis)
}

@Preview(
    "Column Chart Top-Right",
    heightDp = 100,
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun PreviewColumnChartTopRight() {
    PreviewColumnChart(topAxis = topAxis, endAxis = endAxis)
}

@Preview(
    "Column Chart All",
    heightDp = 100,
    widthDp = 100,
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun PreviewColumnChartAll() {
    PreviewColumnChart(
        startAxis = startAxis,
        topAxis = topAxis,
        endAxis = endAxis,
        bottomAxis = bottomAxis,
    )
}

