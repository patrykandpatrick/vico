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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerDrawingModel
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerDrawingModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Custom interpolators, built only from public API, that animate removed series out. The host must
 * draw those series with the data and style they had, without asking the new layer’s providers.
 */
class CustomInterpolatorExitTest {
  private val tag = "chart"
  private val spec = tween<Float>(durationMillis = 400, easing = LinearEasing)
  private val colors = mapOf<Any, Color>("a" to Color.Blue, "b" to Color.Green)

  /** Keeps the new series and appends the removed ones, shrinking them to zero. */
  private val lineInterpolator =
    CartesianLayerDrawingModelInterpolator<LineCartesianLayerDrawingModel> { old, new ->
      CartesianLayerDrawingModelInterpolator.Transition { fraction ->
        if (old == null || fraction == 1f) return@Transition new
        val removedKeys = old.seriesKeys.filter { it !in new.seriesKeys }
        LineCartesianLayerDrawingModel(
          new.toList() +
            removedKeys.map { key ->
              old[old.seriesKeys.indexOf(key)].mapValues { (_, entry) ->
                LineCartesianLayerDrawingModel.Entry(entry.y * (1 - fraction), entry.entry)
              }
            },
          new.seriesKeys + removedKeys,
        )
      }
    }

  private val columnInterpolator =
    CartesianLayerDrawingModelInterpolator<ColumnCartesianLayerDrawingModel> { old, new ->
      CartesianLayerDrawingModelInterpolator.Transition { fraction ->
        if (old == null || fraction == 1f) return@Transition new
        val removedKeys = old.seriesKeys.filter { it !in new.seriesKeys }
        ColumnCartesianLayerDrawingModel(
          new.toList() +
            removedKeys.map { key ->
              old[old.seriesKeys.indexOf(key)].mapValues { (_, entry) ->
                ColumnCartesianLayerDrawingModel.Entry(entry.height * (1 - fraction), entry.entry)
              }
            },
          new.seriesKeys + removedKeys,
        )
      }
    }

  private fun ComposeUiTest.greenPixels(): Int {
    val pixels = onNodeWithTag(tag).captureToImage().toPixelMap()
    var count = 0
    for (x in 0..<pixels.width) {
      for (y in 0..<pixels.height) {
        val pixel = pixels[x, y]
        if (pixel.green > pixel.red + 0.2f && pixel.green > pixel.blue + 0.2f) count++
      }
    }
    return count
  }

  private fun <M : Any> runExit(first: M, second: M, layer: @Composable (M) -> CartesianLayer<*>) =
    runComposeUiTest {
      mainClock.autoAdvance = false
      var model by mutableStateOf(first)
      setContent {
        CompositionLocalProvider(
          LocalCartesianChartBackgroundContext provides EmptyCoroutineContext
        ) {
          CartesianChartHost(
            rememberCartesianChart(layer(model)),
            Modifier.size(300.dp, 200.dp).background(Color.White).testTag(tag),
            animationSpec = spec,
            initialAnimationSpec = null,
          )
        }
      }
      mainClock.advanceTimeByFrame()
      val before = greenPixels()
      assertTrue(before > 0)
      model = second
      mainClock.advanceTimeBy(150)
      assertTrue(greenPixels() > 0, "The removed series should be drawn mid-transition.")
      mainClock.advanceTimeBy(1_000)
      assertEquals(0, greenPixels(), "The removed series should be gone.")
    }

  @Test
  fun `A custom line interpolator can animate a removed series out`() =
    runExit(
      LineCartesianLayerModel.build {
        series(1, 2, 3, key = "a")
        series(3, 3, 3, key = "b")
      },
      LineCartesianLayerModel.build { series(1, 2, 3, key = "a") },
    ) { model ->
      val lineProvider =
        remember(model) {
          LineCartesianLayer.LineProvider { seriesKey, _ ->
            check(seriesKey in model.seriesKeys) { "Asked about the removed series $seriesKey." }
            LineCartesianLayer.Line(
              LineCartesianLayer.LineFill.single(Fill(colors.getValue(seriesKey))),
              LineCartesianLayer.LineStroke.Continuous(8.dp),
            )
          }
        }
      rememberLineCartesianLayer(
        model,
        lineProvider,
        drawingModelInterpolator = lineInterpolator,
      )
    }

  @Test
  fun `A custom column interpolator can animate a removed series out`() =
    runExit(
      ColumnCartesianLayerModel.build {
        series(1, 2, 3, key = "a")
        series(3, 3, 3, key = "b")
      },
      ColumnCartesianLayerModel.build { series(1, 2, 3, key = "a") },
    ) { model ->
      val columnProvider =
        remember(model) {
          ColumnCartesianLayer.ColumnProvider { entry ->
            check(entry.seriesKey in model.seriesKeys) {
              "Asked about the removed series ${entry.seriesKey}."
            }
            LineComponent(Fill(colors.getValue(entry.seriesKey)), 16.dp)
          }
        }
      val stackKeyProvider =
        remember(model) {
          ColumnCartesianLayer.StackKeyProvider { seriesKey, seriesIndex ->
            check(seriesKey in model.seriesKeys) { "Asked about the removed series $seriesKey." }
            seriesIndex
          }
        }
      rememberColumnCartesianLayer(
        model,
        columnProvider = columnProvider,
        stackKeyProvider = stackKeyProvider,
        drawingModelInterpolator = columnInterpolator,
      )
    }
}
