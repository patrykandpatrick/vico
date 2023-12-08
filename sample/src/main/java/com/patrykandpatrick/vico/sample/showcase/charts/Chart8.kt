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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.axis.vertical.rememberEndAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.chart.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.databinding.Chart8Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberChartStyle
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

@Composable
internal fun Chart8(
    uiSystem: UISystem,
    modelProducer: CartesianChartModelProducer,
) {
    when (uiSystem) {
        UISystem.Compose -> ComposeChart8(modelProducer)
        UISystem.Views -> ViewChart8(modelProducer)
    }
}

@Composable
private fun ComposeChart8(modelProducer: CartesianChartModelProducer) {
    ProvideChartStyle(rememberChartStyle(columnChartColors, lineChartColors)) {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
                        verticalAxisPosition = AxisPosition.Vertical.Start,
                    ),
                    rememberLineCartesianLayer(verticalAxisPosition = AxisPosition.Vertical.End),
                ),
            modelProducer = modelProducer,
            startAxis = rememberStartAxis(guideline = null),
            endAxis = rememberEndAxis(),
            marker = rememberMarker(),
            runInitialAnimation = false,
        )
    }
}

@Composable
private fun ViewChart8(modelProducer: CartesianChartModelProducer) {
    val marker = rememberMarker()
    AndroidViewBinding(Chart8Binding::inflate) {
        with(chartView) {
            (chart?.layers?.get(0) as ColumnCartesianLayer).verticalAxisPosition = AxisPosition.Vertical.Start
            (chart?.layers?.get(1) as LineCartesianLayer).verticalAxisPosition = AxisPosition.Vertical.End
            runInitialAnimation = false
            this.modelProducer = modelProducer
            (startAxis as Axis).guideline = null
            this.marker = marker
        }
    }
}

private const val COLOR_1_CODE = 0xffa55a5a
private const val COLOR_2_CODE = 0xffd3756b
private const val COLOR_3_CODE = 0xfff09b7d
private const val COLOR_4_CODE = 0xffffc3a1

private val color1 = Color(COLOR_1_CODE)
private val color2 = Color(COLOR_2_CODE)
private val color3 = Color(COLOR_3_CODE)
private val color4 = Color(COLOR_4_CODE)
private val columnChartColors = listOf(color1, color2, color3)
private val lineChartColors = listOf(color4)
