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

package pl.patrykgoworowski.vico.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.patrykgoworowski.vico.app.ui.MainTheme
import pl.patrykgoworowski.vico.compose.component.dimension.setPadding
import pl.patrykgoworowski.vico.compose.component.rectComponent
import pl.patrykgoworowski.vico.compose.component.shape.shader.verticalGradient
import pl.patrykgoworowski.vico.compose.component.shapeComponent
import pl.patrykgoworowski.vico.compose.component.textComponent
import pl.patrykgoworowski.vico.compose.dataset.bar.DataSet
import pl.patrykgoworowski.vico.compose.dataset.bar.columnDataSet
import pl.patrykgoworowski.vico.compose.dataset.bar.lineDataSet
import pl.patrykgoworowski.vico.compose.extension.pixelSize
import pl.patrykgoworowski.vico.compose.extension.pixels
import pl.patrykgoworowski.vico.core.axis.horizontal.bottomAxis
import pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis
import pl.patrykgoworowski.vico.core.axis.vertical.startAxis
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.shader.componentShader
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.dataset.entry.collection.entryModelOf
import pl.patrykgoworowski.vico.core.path.DashedShape
import pl.patrykgoworowski.vico.core.path.Shapes.pillShape
import pl.patrykgoworowski.vico.core.path.Shapes.rectShape

private val chartModifier = Modifier.height(100.dp)

@Preview("Sample Card With Column Chart", widthDp = 200)
@Composable
fun ColumnChartCard() = MainTheme {
    val colors = MaterialTheme.colors

    SampleCard {
        DataSet(
            modifier = chartModifier,
            dataSet = columnDataSet(
                column = rectComponent(
                    colors.primary,
                    thickness = 8f.dp,
                    shape = RoundedCornerShape(4.dp),
                    dynamicShader = verticalGradient(arrayOf(colors.primary, colors.secondary)),
                )
            ),
            startAxis = startAxis(
                label = textComponent(
                    color = colors.primary,
                    textSize = 10.sp,
                    background = shapeComponent(
                        shape = CutCornerShape(
                            CornerSize(percent = 25),
                            CornerSize(percent = 50),
                            CornerSize(percent = 50),
                            CornerSize(percent = 25)
                        ),
                        color = colors.primary.copy(alpha = 0.1f),
                    )
                ).apply {
                    setPadding(end = 8.dp, start = 4.dp)
                },
                axis = null,
                tick = null,
                guideline = LineComponent(
                    colors.primary.copy(alpha = 0.1f).toArgb(),
                    1.dp.pixels,
                ),
            ),
            model = @Suppress("MagicNumber") entryModelOf(1, 2, 3, 2)
        )
    }
}

@Preview("Sample Card With Line Chart", widthDp = 200)
@Composable
fun LineChartCard() = MainTheme {
    val colors = MaterialTheme.colors

    SampleCard {
        DataSet(
            modifier = Modifier.height(100.dp),
            dataSet = lineDataSet(
                point = null,
                lineColor = colors.primary,
                lineBackgroundShader = componentShader(
                    componentSize = 4.dp.pixels,
                    component = shapeComponent(shape = pillShape, color = colors.primary).apply {
                        setMargins(0.5.dp.pixels)
                    },
                ),
                minX = 0f,
                maxY = 3f,
            ),
            model = @Suppress("MagicNumber") entryModelOf(
                -1 to 0, 0 to 0, 1 to 1, 2 to 2, 3 to 0, 4 to 2, 5 to 1
            ),
            startAxis = startAxis(
                label = TextComponent(
                    color = colors.onSurface.toArgb(),
                    textSize = 10.sp.pixelSize(),
                    background = ShapeComponent(shape = rectShape, color = Color.LightGray.toArgb())
                ).apply {
                    setPadding(horizontal = 4.dp, vertical = 2.dp)
                },
                axis = null,
                tick = null,
                guideline = LineComponent(
                    color = Color.LightGray.toArgb(),
                    thickness = 1.dp.pixels,
                    shape = DashedShape(
                        shape = pillShape,
                        dashLength = 2.dp.pixels,
                        gapLength = 4.dp.pixels,
                    ),
                ),
            ).apply {
                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
            },
            bottomAxis = bottomAxis(
                label = null,
                tick = null,
                guideline = null,
                axis = rectComponent(color = Color.LightGray, thickness = 1.dp),
            ),
        )
    }
}

@Composable
fun SampleCard(
    chart: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            chart()
            Spacer(modifier = Modifier.height(8.dp))
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
