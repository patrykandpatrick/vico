package pl.patrykgoworowski.liftchart_compose.data_set.bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.liftchart_common.axis.vertical.VerticalAxis
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryList
import pl.patrykgoworowski.liftchart_common.entry.entriesOf

private val entryList = MultiEntryList(
    entriesOf(0 to 1, 1 to 2, 2 to 3, 3 to 4),
    animateChanges = false,
)

private val topAxis = HorizontalAxis.top()
private val startAxis = VerticalAxis.start()
private val bottomAxis = HorizontalAxis.bottom()
private val endAxis = VerticalAxis.end()

@Preview("Column Chart Left", heightDp = 100, showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewColumnChartLeft() {
    ColumnChart(
        entryCollection = entryList,
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
        entryCollection = entryList,
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
        entryCollection = entryList,
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
        entryCollection = entryList,
        axisManager = AxisManager(
            startAxis = null,
            topAxis = null,
            endAxis = null,
            bottomAxis = bottomAxis
        )
    )
}

@Preview(
    "Column Chart Bottom-Left",
    heightDp = 100,
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun PreviewColumnChartBottomLeft() {
    ColumnChart(
        entryCollection = entryList,
        axisManager = AxisManager(
            startAxis = startAxis,
            topAxis = null,
            endAxis = null,
            bottomAxis = bottomAxis
        )
    )
}

@Preview(
    "Column Chart Top-Right",
    heightDp = 100,
    showBackground = true,
    backgroundColor = 0xFFFFFF
)
@Composable
fun PreviewColumnChartTopRight() {
    ColumnChart(
        entryCollection = entryList,
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
        entryCollection = entryList,
        axisManager = AxisManager(
            startAxis = startAxis,
            topAxis = topAxis,
            endAxis = endAxis,
            bottomAxis = bottomAxis
        )
    )
}

