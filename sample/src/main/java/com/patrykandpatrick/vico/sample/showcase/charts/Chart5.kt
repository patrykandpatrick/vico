/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.databinding.Chart5Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberChartStyle
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

@Composable
internal fun Chart5(
    uiSystem: UISystem,
    modelProducer: CartesianChartModelProducer,
) {
    when (uiSystem) {
        UISystem.Compose -> ComposeChart5(modelProducer)
        UISystem.Views -> ViewChart5(modelProducer)
    }
}

@Composable
private fun ComposeChart5(modelProducer: CartesianChartModelProducer) {
    ProvideChartStyle(rememberChartStyle(chartColors)) {
        val defaultColumns = currentChartStyle.columnLayer.columns
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columns =
                            remember(defaultColumns) {
                                defaultColumns.mapIndexed { index, defaultColumn ->
                                    val topCornerRadiusPercent =
                                        if (index == defaultColumns.lastIndex) {
                                            DefaultDimens.COLUMN_ROUNDNESS_PERCENT
                                        } else {
                                            0
                                        }
                                    val bottomCornerRadiusPercent =
                                        if (index == 0) DefaultDimens.COLUMN_ROUNDNESS_PERCENT else 0
                                    LineComponent(
                                        defaultColumn.color,
                                        defaultColumn.thicknessDp,
                                        Shapes.roundedCornerShape(
                                            topCornerRadiusPercent,
                                            topCornerRadiusPercent,
                                            bottomCornerRadiusPercent,
                                            bottomCornerRadiusPercent,
                                        ),
                                    )
                                }
                            },
                        mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
                    ),
                    startAxis =
                        rememberStartAxis(
                            itemPlacer = startAxisItemPlacer,
                            labelRotationDegrees = AXIS_LABEL_ROTATION_DEGREES,
                        ),
                    bottomAxis = rememberBottomAxis(labelRotationDegrees = AXIS_LABEL_ROTATION_DEGREES),
                ),
            modelProducer = modelProducer,
            marker = rememberMarker(),
            runInitialAnimation = false,
        )
    }
}

@Composable
private fun ViewChart5(modelProducer: CartesianChartModelProducer) {
    val marker = rememberMarker()
    AndroidViewBinding(Chart5Binding::inflate) {
        with(chartView) {
            runInitialAnimation = false
            this.modelProducer = modelProducer
            (chart?.startAxis as VerticalAxis).itemPlacer = startAxisItemPlacer
            this.marker = marker
        }
    }
}

private const val COLOR_1_CODE = 0xff6438a7
private const val COLOR_2_CODE = 0xff3490de
private const val COLOR_3_CODE = 0xff73e8dc
private const val MAX_START_AXIS_ITEM_COUNT = 3
private const val AXIS_LABEL_ROTATION_DEGREES = 45f

private val color1 = Color(COLOR_1_CODE)
private val color2 = Color(COLOR_2_CODE)
private val color3 = Color(COLOR_3_CODE)
private val chartColors = listOf(color1, color2, color3)
private val startAxisItemPlacer = AxisItemPlacer.Vertical.default(MAX_START_AXIS_ITEM_COUNT)
