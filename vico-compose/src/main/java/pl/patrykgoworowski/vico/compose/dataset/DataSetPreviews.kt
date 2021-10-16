/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.compose.dataset

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import pl.patrykgoworowski.vico.compose.component.columnComponent
import pl.patrykgoworowski.vico.compose.dataset.column.columnDataSet
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.AxisRenderer
import pl.patrykgoworowski.vico.core.axis.horizontal.createHorizontalAxis
import pl.patrykgoworowski.vico.core.axis.vertical.createVerticalAxis
import pl.patrykgoworowski.vico.core.dataset.entry.collection.entryModelOf

@Suppress("MagicNumber")
private val model = entryModelOf(1, 2, 3, 4)

private val topAxis = createHorizontalAxis<AxisPosition.Horizontal.Top>()
private val startAxis = createVerticalAxis<AxisPosition.Vertical.Start>()
private val bottomAxis = createHorizontalAxis<AxisPosition.Horizontal.Bottom>()
private val endAxis = createVerticalAxis<AxisPosition.Vertical.End>()

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
        dataSet = columnDataSet(listOf(columnComponent(color = Color.Blue))),
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
