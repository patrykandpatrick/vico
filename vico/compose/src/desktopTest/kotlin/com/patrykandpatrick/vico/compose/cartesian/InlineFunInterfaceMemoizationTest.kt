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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * Checks, empirically, whether lambdas converted to Vico’s `fun interface`s inside a composable are
 * memoized by the Compose compiler as configured in this build (strong skipping), so that inline
 * providers and formatters don’t recreate layers and charts on every recomposition.
 */
class InlineFunInterfaceMemoizationTest {
  private class Instances(
    val recompositionTrigger: Int,
    val columnProvider: ColumnCartesianLayer.ColumnProvider,
    val valueFormatter: CartesianValueFormatter,
    val layer: ColumnCartesianLayer,
    val chart: CartesianChart,
  )

  @Test
  fun `Inline fun-interface lambdas are memoized while their captures are unchanged`() =
    runComposeUiTest {
      val model = ColumnCartesianLayerModel.build { series(1, 2, 3) }
      val base = LineComponent(Fill(Color.Blue))
      val hot = LineComponent(Fill(Color.Red))
      val labels = listOf("a", "b", "c")
      var recompositionTrigger by mutableIntStateOf(0)
      var threshold by mutableStateOf(2.0)
      val instances = mutableListOf<Instances>()
      setContent {
        Content(recompositionTrigger, model, base, hot, labels, threshold) { instances += it }
      }
      waitForIdle()
      runOnIdle { recompositionTrigger++ }
      waitForIdle()
      check(instances.size == 2) { "Expected a recomposition; got ${instances.size} records." }
      val (first, second) = instances.takeLast(2)
      assertSame(first.columnProvider, second.columnProvider)
      assertSame(first.valueFormatter, second.valueFormatter)
      assertSame(first.layer, second.layer)
      assertSame(first.chart, second.chart)

      runOnIdle { threshold = 3.0 }
      waitForIdle()
      val third = instances.last()
      assertNotSame(second.columnProvider, third.columnProvider)
      assertSame(second.valueFormatter, third.valueFormatter)
      assertNotSame(second.layer, third.layer)
      assertNotSame(second.chart, third.chart)
    }

  @Composable
  private fun Content(
    recompositionTrigger: Int,
    model: ColumnCartesianLayerModel,
    base: LineComponent,
    hot: LineComponent,
    labels: List<String>,
    threshold: Double,
    record: (Instances) -> Unit,
  ) {
    // `recompositionTrigger` changes between recompositions, so this body re-executes.
    val columnProvider = ColumnCartesianLayer.ColumnProvider { entry ->
      if (entry.y > threshold) hot else base
    }
    val valueFormatter = CartesianValueFormatter { _, x, _ -> labels[x.toInt()] }
    val layer =
      rememberColumnCartesianLayer(
        model,
        columnProvider = columnProvider,
        dataLabelValueFormatter = valueFormatter,
      )
    val chart = rememberCartesianChart(layer)
    SideEffect {
      record(Instances(recompositionTrigger, columnProvider, valueFormatter, layer, chart))
    }
  }
}
