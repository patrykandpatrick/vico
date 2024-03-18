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
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.decoration.rememberHorizontalBox
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.dimension.dimensionsOf
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.axis.AxisValueFormatter
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.model.columnSeries
import com.patrykandpatrick.vico.core.common.shape.Shapes
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
    val horizontalBox = rememberHorizontalBox()
    val shape = remember { Shapes.cutCornerShape(topLeftPercent = 50) }
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnColors.map { rememberLineComponent(color = it, thickness = 8.dp, shape = shape) },
                ),
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter),
                decorations = remember(horizontalBox) { listOf(horizontalBox) },
            ),
        modelProducer = modelProducer,
        modifier = modifier,
        marker = rememberMarker(),
        runInitialAnimation = false,
        zoomState = rememberVicoZoomState(zoomEnabled = false),
    )
}

@Composable
private fun ViewChart6(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier,
) {
    val horizontalBox = rememberHorizontalBox()
    val decorations = remember(horizontalBox) { listOf(horizontalBox) }
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
private fun rememberHorizontalBox() =
    rememberHorizontalBox(
        y = { 7f..14f },
        box = rememberShapeComponent(color = horizontalBoxColor.copy(.36f)),
        labelComponent =
            rememberTextComponent(
                color = Color.Black,
                background = rememberShapeComponent(Shapes.rectShape, horizontalBoxColor),
                padding = dimensionsOf(8.dp, 2.dp),
                margins = dimensionsOf(4.dp),
                typeface = Typeface.MONOSPACE,
            ),
    )

private val columnColors = listOf(Color(0xff3e6558), Color(0xff5e836a), Color(0xffa5ba8e))
private val horizontalBoxColor = Color(0xffe9e5af)
private val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val bottomAxisValueFormatter =
    AxisValueFormatter<AxisPosition.Horizontal.Bottom> { x, _, _ -> daysOfWeek[x.toInt() % daysOfWeek.size] }
