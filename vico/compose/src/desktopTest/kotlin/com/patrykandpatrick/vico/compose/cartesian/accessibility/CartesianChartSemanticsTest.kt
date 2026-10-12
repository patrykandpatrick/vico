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

package com.patrykandpatrick.vico.compose.cartesian.accessibility

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@Suppress("DEPRECATION")
class CartesianChartSemanticsTest {

  private val wholeNumberFormatter = CartesianValueFormatter { _, value, _ ->
    value.toInt().toString()
  }

  private val provider =
    CartesianChartDescriptionProvider.default(
      xValueFormatter = wholeNumberFormatter,
      yValueFormatter = wholeNumberFormatter,
    )

  private fun createLineLayer() =
    LineCartesianLayer(
      lineProvider =
        LineCartesianLayer.LineProvider.series(
          LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill.Black))
        )
    )

  @Test
  fun initialDisplayPopulatesChartSummaryAndIntervalNodes() = runComposeUiTest {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(10, 20, 30, 40, 50)
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)

    setContent {
      CartesianChartHost(
        chart = chart,
        model = model,
        modifier = Modifier.size(600.dp, 300.dp),
      )
    }

    onNodeWithContentDescription(
        "Line chart. X from 0 to 4. Min: 10 at 0, max: 50 at 4, first: 10, last: 50 (change: +40)."
      )
      .assertExists()

    onNodeWithContentDescription("X: 0. Y: 10.").assertExists()
    onNodeWithContentDescription("X: 1. Y: 20.").assertExists()
    onNodeWithContentDescription("X: 2. Y: 30.").assertExists()
    onNodeWithContentDescription("X: 3. Y: 40.").assertExists()
    onNodeWithContentDescription("X: 4. Y: 50.").assertExists()
  }

  @Test
  fun modelUpdateUpdatesSummaryAndIntervalNodes() = runComposeUiTest {
    var model by
      mutableStateOf(
        CartesianChartModel(
          LineCartesianLayerModel.build {
            series(10, 20, 30)
          }
        )
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)

    setContent {
      CartesianChartHost(
        chart = chart,
        model = model,
        modifier = Modifier.size(600.dp, 300.dp),
      )
    }

    onNodeWithContentDescription(
        "Line chart. X from 0 to 2. Min: 10 at 0, max: 30 at 2, first: 10, last: 30 (change: +20)."
      )
      .assertExists()
    onNodeWithContentDescription("X: 0. Y: 10.").assertExists()
    onNodeWithContentDescription("X: 1. Y: 20.").assertExists()
    onNodeWithContentDescription("X: 2. Y: 30.").assertExists()

    model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(15, 25, 35, 45)
        }
      )

    waitForIdle()

    onNodeWithContentDescription(
        "Line chart. X from 0 to 3. Min: 15 at 0, max: 45 at 3, first: 15, last: 45 (change: +30)."
      )
      .assertExists()
    onNodeWithContentDescription("X: 0. Y: 15.").assertExists()
    onNodeWithContentDescription("X: 1. Y: 25.").assertExists()
    onNodeWithContentDescription("X: 2. Y: 35.").assertExists()
    onNodeWithContentDescription("X: 3. Y: 45.").assertExists()
  }

  @Test
  fun scrollingUpdatesVisibleIntervalNodes() = runComposeUiTest {
    val values = (0..50).map { it * 10 }
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(values)
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)
    val scrollState = VicoScrollState(scrollEnabled = true)

    setContent {
      CartesianChartHost(
        chart = chart,
        model = model,
        scrollState = scrollState,
        modifier = Modifier.size(300.dp, 300.dp),
      )
    }

    waitForIdle()

    onNodeWithContentDescription("X: 0. Y: 0.", substring = true).assertExists()

    kotlinx.coroutines.runBlocking {
      scrollState.scroll(Scroll.Absolute.End)
    }
    waitForIdle()

    val initialNodeExists =
      onAllNodes(hasContentDescription("X: 0. Y: 0.", substring = true))
        .fetchSemanticsNodes()
        .isNotEmpty()

    assertTrue(
      !initialNodeExists,
      "Initial point should no longer be in visible interval nodes after scrolling",
    )
  }

  @Test
  fun whenDescriptionProviderIsNullNoAccessibilityNodesAreProduced() = runComposeUiTest {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(10, 20, 30)
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = null)

    setContent {
      CartesianChartHost(
        chart = chart,
        model = model,
        modifier = Modifier.size(600.dp, 300.dp),
      )
    }

    onAllNodes(hasContentDescription("Line chart", substring = true)).assertCountEquals(0)
    onAllNodes(hasContentDescription("X:", substring = true)).assertCountEquals(0)
  }

  @Test
  fun zoomingUpdatesVisibleIntervalNodes() = runComposeUiTest {
    val values = (0..50).map { it * 10 }
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(values)
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)
    val zoomState =
      VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(1f),
        minZoom = Zoom.fixed(0.5f),
        maxZoom = Zoom.fixed(10f),
      )

    setContent {
      CartesianChartHost(
        chart = chart,
        model = model,
        zoomState = zoomState,
        modifier = Modifier.size(300.dp, 300.dp),
      )
    }

    waitForIdle()

    val initialCount =
      onAllNodes(hasContentDescription("X:", substring = true)).fetchSemanticsNodes().size

    kotlinx.coroutines.runBlocking {
      zoomState.zoom(Zoom.fixed(4f))
    }
    waitForIdle()

    val zoomedCount =
      onAllNodes(hasContentDescription("X:", substring = true)).fetchSemanticsNodes().size

    assertTrue(zoomedCount > 0)
    assertTrue(zoomedCount <= initialCount)
  }

  @Test
  fun intervalNodeCountDoesNotExceedMaxIntervals() = runComposeUiTest {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series((0..50).toList())
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)

    setContent {
      CartesianChartHost(
        chart = chart,
        model = model,
        modifier = Modifier.size(200.dp, 200.dp),
      )
    }

    waitForIdle()
    val nodeCount =
      onAllNodes(hasContentDescription("X:", substring = true)).fetchSemanticsNodes().size

    assertTrue(nodeCount in 1..4)
  }

  @Test
  fun intervalNodesHaveDistinctAndAccurateBounds() = runComposeUiTest {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(10, 20, 30)
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)

    setContent {
      CartesianChartHost(
        chart = chart,
        model = model,
        modifier = Modifier.size(600.dp, 300.dp),
      )
    }

    waitForIdle()

    val node0 = onNodeWithContentDescription("X: 0. Y: 10.").fetchSemanticsNode()
    val node1 = onNodeWithContentDescription("X: 1. Y: 20.").fetchSemanticsNode()
    val node2 = onNodeWithContentDescription("X: 2. Y: 30.").fetchSemanticsNode()

    assertTrue(node0.boundsInRoot.left < node1.boundsInRoot.left)
    assertTrue(node1.boundsInRoot.left < node2.boundsInRoot.left)
    assertEquals(node0.boundsInRoot.right, node1.boundsInRoot.left, 1f)
    assertEquals(node1.boundsInRoot.right, node2.boundsInRoot.left, 1f)
    assertTrue(node0.boundsInRoot.width > 0f)
    assertTrue(node1.boundsInRoot.width > 0f)
    assertTrue(node2.boundsInRoot.width > 0f)
    assertTrue(node0.boundsInRoot.width < node2.boundsInRoot.right - node0.boundsInRoot.left)
    assertTrue(node1.boundsInRoot.width < node2.boundsInRoot.right - node0.boundsInRoot.left)
    assertTrue(node2.boundsInRoot.width < node2.boundsInRoot.right - node0.boundsInRoot.left)
    assertEquals(node0.boundsInRoot.width, node2.boundsInRoot.width, 1f)
  }

  @Test
  fun intervalNodesHaveDistinctWidthsForNonUniformSpacing() = runComposeUiTest {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(listOf(0.0, 1.0, 5.0), listOf(10, 20, 30))
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)

    setContent {
      CartesianChartHost(
        chart = chart,
        model = model,
        modifier = Modifier.size(600.dp, 300.dp),
      )
    }

    waitForIdle()

    val node0 = onNodeWithContentDescription("X: 0. Y: 10.").fetchSemanticsNode()
    val node1 = onNodeWithContentDescription("X: 1. Y: 20.").fetchSemanticsNode()
    val node2 = onNodeWithContentDescription("X: 5. Y: 30.").fetchSemanticsNode()

    assertTrue(node0.boundsInRoot.left < node1.boundsInRoot.left)
    assertTrue(node1.boundsInRoot.left < node2.boundsInRoot.left)
    assertEquals(node0.boundsInRoot.right, node1.boundsInRoot.left, 1f)
    assertEquals(node1.boundsInRoot.right, node2.boundsInRoot.left, 1f)
    assertTrue(node0.boundsInRoot.width < node1.boundsInRoot.width)
  }

  @Test
  fun rtlLayoutMirrorsIntervalNodePositions() = runComposeUiTest {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(10, 20, 30)
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)

    setContent {
      CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        CartesianChartHost(
          chart = chart,
          model = model,
          modifier = Modifier.size(600.dp, 300.dp),
        )
      }
    }

    waitForIdle()

    val node0 = onNodeWithContentDescription("X: 0. Y: 10.").fetchSemanticsNode()
    val node1 = onNodeWithContentDescription("X: 1. Y: 20.").fetchSemanticsNode()
    val node2 = onNodeWithContentDescription("X: 2. Y: 30.").fetchSemanticsNode()

    assertTrue(node0.boundsInRoot.left > node1.boundsInRoot.left)
    assertTrue(node1.boundsInRoot.left > node2.boundsInRoot.left)
    assertTrue(node0.boundsInRoot.width > 0f)
    assertTrue(node1.boundsInRoot.width > 0f)
    assertTrue(node2.boundsInRoot.width > 0f)
  }
}
