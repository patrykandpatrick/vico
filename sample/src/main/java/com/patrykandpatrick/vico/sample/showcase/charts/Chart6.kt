/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.sample.showcase.charts

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.columnSeries
import com.patrykandpatrick.vico.databinding.Chart6Binding
import com.patrykandpatrick.vico.sample.showcase.Defaults
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
internal fun Chart6(
    uiSystem: UISystem,
    modifier: Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer.build() }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            while (isActive) {
                modelProducer.tryRunTransaction {
                    columnSeries {
                        repeat(Defaults.MULTI_SERIES_COUNT) {
                            series(
                                List(Defaults.ENTRY_COUNT) {
                                    Defaults.COLUMN_LAYER_MIN_Y +
                                        Random.nextFloat() * Defaults.COLUMN_LAYER_RELATIVE_MAX_Y
                                },
                            )
                        }
                    }
                }
                delay(Defaults.TRANSACTION_INTERVAL_MS)
            }
        }
    }

    when (uiSystem) {
        UISystem.Compose -> ComposeChart6(modelProducer, modifier)
        UISystem.Views -> ViewChart6(modelProducer, modifier)
    }
}

@Composable
private fun ComposeChart6(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier,
) {
    val thresholdLine = rememberThresholdLine()
    val shape = remember { Shapes.cutCornerShape(topLeftPercent = 50) }
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columns =
                        listOf(
                            rememberLineComponent(color = color1, thickness = 8.dp, shape = shape),
                            rememberLineComponent(color = color2, thickness = 12.dp, shape = shape),
                            rememberLineComponent(color = color3, thickness = 10.dp, shape = shape),
                        ),
                    mergeMode = { ColumnCartesianLayer.MergeMode.Grouped },
                ),
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter),
                decorations = remember(thresholdLine) { listOf(thresholdLine) },
            ),
        modelProducer = modelProducer,
        modifier = modifier,
        marker = rememberMarker(),
        runInitialAnimation = false,
    )
}

@Composable
private fun ViewChart6(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier,
) {
    val thresholdLine = rememberThresholdLine()
    val decorations = remember(thresholdLine) { listOf(thresholdLine) }
    val marker = rememberMarker()
    AndroidViewBinding(Chart6Binding::inflate, modifier) {
        with(chartView) {
            chart?.setDecorations(decorations)
            runInitialAnimation = false
            this.modelProducer = modelProducer
            (chart?.bottomAxis as HorizontalAxis<AxisPosition.Horizontal.Bottom>).valueFormatter =
                bottomAxisValueFormatter
            this.marker = marker
        }
    }
}

@Composable
private fun rememberThresholdLine(): ThresholdLine {
    val label =
        rememberTextComponent(
            color = Color.Black,
            background = rememberShapeComponent(Shapes.rectShape, color4),
            padding = dimensionsOf(8.dp, 2.dp),
            margins = dimensionsOf(4.dp),
            typeface = Typeface.MONOSPACE,
        )
    val line = rememberShapeComponent(color = color4.copy(.36f))
    return remember(label, line) {
        ThresholdLine(thresholdRange = 7f..14f, labelComponent = label, lineComponent = line)
    }
}

private val color1 = Color(0xff3e6558)
private val color2 = Color(0xff5e836a)
private val color3 = Color(0xffa5ba8e)
private val color4 = Color(0xffe9e5af)
private val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val bottomAxisValueFormatter =
    AxisValueFormatter<AxisPosition.Horizontal.Bottom> { x, _, _ -> daysOfWeek[x.toInt() % daysOfWeek.size] }
