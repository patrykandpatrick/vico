/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

@file:OptIn(ExperimentalTestApi::class)

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test

/**
 * Checks that exiting series never reach the new layer’s providers: each provider here knows only
 * its own model and throws for anything else.
 */
class ExitingSeriesProviderTest {
  private val spec = tween<Float>(durationMillis = 400, easing = LinearEasing)

  private fun runExit(
    first: Any,
    second: Any,
    layer: @Composable (Any) -> CartesianLayer<*>,
  ) = runComposeUiTest {
    mainClock.autoAdvance = false
    var model by mutableStateOf(first)
    setContent {
      CompositionLocalProvider(
        LocalCartesianChartBackgroundContext provides EmptyCoroutineContext
      ) {
        CartesianChartHost(
          rememberCartesianChart(layer(model)),
          Modifier.size(300.dp, 200.dp),
          animationSpec = spec,
          initialAnimationSpec = null,
        )
      }
    }
    mainClock.advanceTimeByFrame()
    model = second
    repeat(5) { mainClock.advanceTimeBy(60) }
    mainClock.advanceTimeBy(1_000)
  }

  @Test
  fun `An exiting line series isn’t passed to the new layer’s line provider`() =
    runExit(
      LineCartesianLayerModel.build {
        series(1, 2, 3, key = "a")
        series(3, 2, 1, key = "b")
      },
      LineCartesianLayerModel.build { series(1, 2, 3, key = "a") },
    ) { model ->
      model as LineCartesianLayerModel
      val lineProvider =
        remember(model) {
          val line = LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color.Red)))
          LineCartesianLayer.LineProvider { seriesKey, _ ->
            check(seriesKey in model.seriesKeys) { "Asked about the removed series $seriesKey." }
            line
          }
        }
      rememberLineCartesianLayer(model, lineProvider)
    }

  @Test
  fun `An exiting column series isn’t passed to the new layer’s column or stack key provider`() =
    runExit(
      ColumnCartesianLayerModel.build {
        series(1, 2, 3, key = "a")
        series(3, 2, 1, key = "b")
        series(2, 2, 2, key = "c")
      },
      ColumnCartesianLayerModel.build {
        series(1, 2, 3, key = "a")
        series(2, 2, 2, key = "c")
      },
    ) { model ->
      model as ColumnCartesianLayerModel
      val columnProvider =
        remember(model) {
          val column = LineComponent(Fill(Color.Red), 8.dp)
          ColumnCartesianLayer.ColumnProvider { entry ->
            check(entry.seriesKey in model.seriesKeys) {
              "Asked about the removed series ${entry.seriesKey}."
            }
            column
          }
        }
      val stackKeyProvider =
        remember(model) {
          ColumnCartesianLayer.StackKeyProvider { seriesKey, _ ->
            check(seriesKey in model.seriesKeys) { "Asked about the removed series $seriesKey." }
            if (seriesKey == "c") "c" else "ab"
          }
        }
      rememberColumnCartesianLayer(
        model,
        columnProvider = columnProvider,
        stackKeyProvider = stackKeyProvider,
      )
    }

  @Test
  fun `An exiting candlestick series isn’t passed to the new layer’s candle provider`() =
    runExit(
      CandlestickCartesianLayerModel.build(
        listOf(1, 2),
        listOf(2, 1),
        listOf(0, 0),
        listOf(3, 3),
        seriesKey = "old",
      ),
      CandlestickCartesianLayerModel.build(
        listOf(2, 3),
        listOf(3, 2),
        listOf(1, 1),
        listOf(4, 4),
        seriesKey = "new",
      ),
    ) { model ->
      model as CandlestickCartesianLayerModel
      val candleProvider =
        remember(model) {
          val candle = CandlestickCartesianLayer.Candle(LineComponent(Fill(Color.Red), 8.dp))
          CandlestickCartesianLayer.CandleProvider { entry ->
            check(entry in model.series) { "Asked about an entry of the removed series." }
            candle
          }
        }
      rememberCandlestickCartesianLayer(model, candleProvider)
    }
}
