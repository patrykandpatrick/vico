package pl.patrykgoworowski.liftchart_compose.data_set.bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.axis.VerticalAxis
import pl.patrykgoworowski.liftchart_common.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryList
import pl.patrykgoworowski.liftchart_common.entry.entriesOf

private val entryList = SingleEntryList(entriesOf(0 to 1, 1 to 2, 2 to 3, 3 to 4))

private val topAxis = HorizontalAxis()
private val startAxis = VerticalAxis()
private val bottomAxis = HorizontalAxis()
private val endAxis = VerticalAxis()

@Preview("Column Chart Left", heightDp = 100, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewColumnChartLeft() {
    ColumnChart(
        singleEntryCollection = entryList,
        axisManager = AxisManager(
            startAxis = startAxis,
            topAxis = null,
            endAxis = null,
            bottomAxis = null
        )
    )
}

@Preview("Column Chart Top", heightDp = 100, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewColumnChartTop() {
    ColumnChart(
        singleEntryCollection = entryList,
        axisManager = AxisManager(
            startAxis = null,
            topAxis = topAxis,
            endAxis = null,
            bottomAxis = null
        )
    )
}

@Preview("Column Chart Right", heightDp = 100, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewColumnChartRight() {
    ColumnChart(
        singleEntryCollection = entryList,
        axisManager = AxisManager(
            startAxis = null,
            topAxis = null,
            endAxis = endAxis,
            bottomAxis = null
        )
    )
}

@Preview("Column Chart Bottom", heightDp = 100, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewColumnChartBottom() {
    ColumnChart(
        singleEntryCollection = entryList,
        axisManager = AxisManager(
            startAxis = null,
            topAxis = null,
            endAxis = null,
            bottomAxis = bottomAxis
        )
    )
}

@Preview("Column Chart Bottom-Left", heightDp = 100, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewColumnChartBottomLeft() {
    ColumnChart(
        singleEntryCollection = entryList,
        axisManager = AxisManager(
            startAxis = startAxis,
            topAxis = null,
            endAxis = null,
            bottomAxis = bottomAxis
        )
    )
}

@Preview("Column Chart Top-Right", heightDp = 100, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewColumnChartTopRight() {
    ColumnChart(
        singleEntryCollection = entryList,
        axisManager = AxisManager(
            startAxis = null,
            topAxis = topAxis,
            endAxis = endAxis,
            bottomAxis = null
        )
    )
}

@Preview("Column Chart All", heightDp = 100, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewColumnChartAll() {
    ColumnChart(
        singleEntryCollection = entryList,
        axisManager = AxisManager(
            startAxis = startAxis,
            topAxis = topAxis,
            endAxis = endAxis,
            bottomAxis = bottomAxis
        )
    )
}

