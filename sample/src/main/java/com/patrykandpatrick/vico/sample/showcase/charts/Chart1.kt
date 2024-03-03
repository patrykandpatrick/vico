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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.chart.zoom.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.component.shape.shader.color
import com.patrykandpatrick.vico.core.axis.BaseAxis
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.lineSeries
import com.patrykandpatrick.vico.databinding.Chart1Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

@Composable
internal fun Chart1(
    uiSystem: UISystem,
    modifier: Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer.build() }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            modelProducer.tryRunTransaction { lineSeries { series(x, x.map { Random.nextFloat() * 15 }) } }
        }
    }
    when (uiSystem) {
        UISystem.Compose -> ComposeChart1(modelProducer, modifier)
        UISystem.Views -> ViewChart1(modelProducer, modifier)
    }
}

@Composable
private fun ComposeChart1(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier,
) {
    val marker = rememberMarker()
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(listOf(rememberLineSpec(DynamicShaders.color(Color(0xffa485e0))))),
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(guideline = null),
                persistentMarkers = mapOf(PERSISTENT_MARKER_X to marker),
            ),
        modelProducer = modelProducer,
        modifier = modifier,
        marker = marker,
        zoomState = rememberVicoZoomState(zoomEnabled = false),
    )
}

@Composable
private fun ViewChart1(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier,
) {
    val marker = rememberMarker()
    AndroidViewBinding(
        { inflater, parent, attachToParent ->
            Chart1Binding
                .inflate(inflater, parent, attachToParent)
                .apply {
                    with(chartView) {
                        chart?.addPersistentMarker(PERSISTENT_MARKER_X, marker)
                        this.modelProducer = modelProducer
                        (chart?.bottomAxis as BaseAxis).guideline = null
                        this.marker = marker
                    }
                }
        },
        modifier,
    )
}

private const val PERSISTENT_MARKER_X = 7f

private val x = (1..50).toList()
