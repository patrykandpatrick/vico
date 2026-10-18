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
import androidx.compose.ui.semantics.SemanticsProperties
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore
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

  private fun createColumnLayer() =
    ColumnCartesianLayer(
      columnProvider = ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill.Black)),
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
  fun zoomOutMatchesFreshComposition() = runComposeUiTest {
    val values = (0..59).map { it * 10 }
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(values)
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)
    val zoomState1 =
      VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(1f),
        minZoom = Zoom.fixed(0.1f),
        maxZoom = Zoom.fixed(10f),
      )
    val zoomState2 =
      VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(0.4f),
        minZoom = Zoom.fixed(0.1f),
        maxZoom = Zoom.fixed(10f),
      )

    var showFresh by mutableStateOf(false)

    setContent {
      if (!showFresh) {
        CartesianChartHost(
          chart = chart,
          model = model,
          zoomState = zoomState1,
          modifier = Modifier.size(400.dp, 220.dp),
        )
      } else {
        CartesianChartHost(
          chart = chart,
          model = model,
          zoomState = zoomState2,
          modifier = Modifier.size(400.dp, 220.dp),
        )
      }
    }

    waitForIdle()

    kotlinx.coroutines.runBlocking {
      zoomState1.zoom(Zoom.fixed(0.4f))
    }
    waitForIdle()

    val zoomedNodes = onAllNodes(hasContentDescription("X:", substring = true)).fetchSemanticsNodes()
    val zoomedDescriptions =
      zoomedNodes.map { it.config[SemanticsProperties.ContentDescription].first() }
    val zoomedBounds = zoomedNodes.map { it.boundsInRoot.left to it.boundsInRoot.width }

    showFresh = true
    waitForIdle()

    val freshNodes = onAllNodes(hasContentDescription("X:", substring = true)).fetchSemanticsNodes()
    val freshDescriptions =
      freshNodes.map { it.config[SemanticsProperties.ContentDescription].first() }
    val freshBounds = freshNodes.map { it.boundsInRoot.left to it.boundsInRoot.width }

    assertTrue(freshDescriptions.isNotEmpty())
    assertEquals(freshDescriptions, zoomedDescriptions)
    assertEquals(freshBounds.size, zoomedBounds.size)
    for (i in freshBounds.indices) {
      assertEquals(freshBounds[i].first, zoomedBounds[i].first, 1f)
      assertEquals(freshBounds[i].second, zoomedBounds[i].second, 1f)
    }
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

  @Test
  fun modelUpdateChangingXSpacingMatchesFreshComposition() = runComposeUiTest {
    val model1 =
      CartesianChartModel(
        ColumnCartesianLayerModel.build {
          series(10, 20, 30, 40)
        }
      )
    val model2 =
      CartesianChartModel(
        ColumnCartesianLayerModel.build {
          series(10, 20, 30, 40)
          series(20, 30, 40, 50)
          series(30, 40, 50, 60)
        }
      )
    val chart = CartesianChart(createColumnLayer(), descriptionProvider = provider)

    var currentModel by mutableStateOf(model1)
    var showFresh by mutableStateOf(false)

    setContent {
      if (!showFresh) {
        CartesianChartHost(
          chart = chart,
          model = currentModel,
          modifier = Modifier.size(400.dp, 220.dp),
        )
      } else {
        CartesianChartHost(
          chart = chart,
          model = model2,
          modifier = Modifier.size(400.dp, 220.dp),
        )
      }
    }

    waitForIdle()
    currentModel = model2
    waitForIdle()

    val updatedNodes = onAllNodes(hasContentDescription("X:", substring = true)).fetchSemanticsNodes()
    val updatedDescriptions =
      updatedNodes.map { it.config[SemanticsProperties.ContentDescription].first() }
    val updatedBounds = updatedNodes.map { it.boundsInRoot.left to it.boundsInRoot.width }

    showFresh = true
    waitForIdle()

    val freshNodes = onAllNodes(hasContentDescription("X:", substring = true)).fetchSemanticsNodes()
    val freshDescriptions =
      freshNodes.map { it.config[SemanticsProperties.ContentDescription].first() }
    val freshBounds = freshNodes.map { it.boundsInRoot.left to it.boundsInRoot.width }

    assertTrue(freshDescriptions.isNotEmpty())
    assertEquals(freshDescriptions, updatedDescriptions)
    assertEquals(freshBounds.size, updatedBounds.size)
    for (i in freshBounds.indices) {
      assertEquals(freshBounds[i].first, updatedBounds[i].first, 1f)
      assertEquals(freshBounds[i].second, updatedBounds[i].second, 1f)
    }
  }

  @Test
  fun providerChangeUpdatesDescriptions() = runComposeUiTest {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(10, 20, 30)
        }
      )
    val customProvider =
      CartesianChartDescriptionProvider.default(
        xValueFormatter = wholeNumberFormatter,
        yValueFormatter = wholeNumberFormatter,
        seriesLabel = { _, _, _ -> "Custom series" },
      )
    var chart by
      mutableStateOf(CartesianChart(createLineLayer(), descriptionProvider = provider))

    setContent {
      CartesianChartHost(
        chart = chart,
        model = model,
        modifier = Modifier.size(600.dp, 300.dp),
      )
    }

    waitForIdle()
    onNodeWithContentDescription(
        "Line chart. X from 0 to 2. Min: 10 at 0, max: 30 at 2, first: 10, last: 30 (change: +20)."
      )
      .assertExists()

    chart = chart.copy(descriptionProvider = customProvider)
    waitForIdle()

    onNodeWithContentDescription(
        "Line chart. X from 0 to 2. Custom series: Min: 10 at 0, max: 30 at 2, first: 10, last: 30 (change: +20)."
      )
      .assertExists()
    onNodeWithContentDescription("X: 0. Custom series: Y: 10.").assertExists()
    onNodeWithContentDescription("X: 1. Custom series: Y: 20.").assertExists()
    onNodeWithContentDescription("X: 2. Custom series: Y: 30.").assertExists()

    chart = chart.copy(descriptionProvider = null)
    waitForIdle()

    onAllNodes(hasContentDescription("Line chart", substring = true)).assertCountEquals(0)
    onAllNodes(hasContentDescription("X:", substring = true)).assertCountEquals(0)
  }

  @Test
  fun multiLayerModelOrderDiffersFromLayerOrder() = runComposeUiTest {
    val columnLayer = createColumnLayer()
    val lineLayer = createLineLayer()
    val chart = CartesianChart(columnLayer, lineLayer, descriptionProvider = provider)

    val lineModel = LineCartesianLayerModel.build { series(10, 20) }
    val columnModel =
      ColumnCartesianLayerModel.build {
        series(30, 40)
        series(50, 60)
      }
    val model = CartesianChartModel(listOf(lineModel, columnModel))

    setContent {
      CartesianChartHost(
        chart = chart,
        model = model,
        modifier = Modifier.size(600.dp, 300.dp),
      )
    }

    waitForIdle()

    onNodeWithContentDescription("Series 1: Min: 30 at 0, max: 40 at 1", substring = true)
      .assertExists()
    onNodeWithContentDescription("Series 2: Min: 50 at 0, max: 60 at 1", substring = true)
      .assertExists()
    onNodeWithContentDescription("Series 3: Min: 10 at 0, max: 20 at 1", substring = true)
      .assertExists()

    onNodeWithContentDescription("X: 0. Series 1: Y: 30. Series 2: Y: 50. Series 3: Y: 10.")
      .assertExists()
    onNodeWithContentDescription("X: 1. Series 1: Y: 40. Series 2: Y: 60. Series 3: Y: 20.")
      .assertExists()
  }

  @Test
  fun customLayerContributesIntervals() = runComposeUiTest {
    val customLayer = CustomCartesianLayer()
    val lineLayer = createLineLayer()
    val chart = CustomCartesianChart(customLayer, lineLayer, descriptionProvider = provider)

    val customModel = CustomLayerModel()
    val lineModel = LineCartesianLayerModel.build { series(20, 30, 40) }
    val model = CartesianChartModel(customModel, lineModel)

    setContent {
      CartesianChartHost(
        chart = chart,
        model = model,
        modifier = Modifier.size(600.dp, 300.dp),
      )
    }

    waitForIdle()

    onNodeWithContentDescription(
        "Series 1: Min: 5 at 0, max: 15 at 2, first: 5, last: 15 (change: +10).",
        substring = true,
      )
      .assertExists()
    onNodeWithContentDescription(
        "Series 2: Min: 20 at 0, max: 40 at 2, first: 20, last: 40 (change: +20).",
        substring = true,
      )
      .assertExists()

    onNodeWithContentDescription("X: 0. Series 1: Y: 5. Series 2: Y: 20.").assertExists()
    onNodeWithContentDescription("X: 1. Series 1: Y: 10. Series 2: Y: 30.").assertExists()
    onNodeWithContentDescription("X: 2. Series 1: Y: 15. Series 2: Y: 40.").assertExists()
  }

  private class CustomLayerModel(
    override val minX: Double = 0.0,
    override val maxX: Double = 2.0,
    override val minY: Double = 5.0,
    override val maxY: Double = 15.0,
    override val extraStore: ExtraStore = ExtraStore.Empty,
  ) : CartesianLayerModel {
    override fun getXDeltaGcd(): Double = 1.0
    override fun copy(extraStore: ExtraStore): CartesianLayerModel = this
    override fun equals(other: Any?): Boolean = other is CustomLayerModel
    override fun hashCode(): Int = javaClass.hashCode()
  }

  private class CustomCartesianLayer : CartesianLayer<CustomLayerModel> {
    override val markerTargets: Map<Double, List<CartesianMarker.Target>> = emptyMap()

    override fun draw(
      context: CartesianDrawingContext,
      model: CustomLayerModel,
    ) {}

    override fun updateDimensions(
      context: CartesianMeasuringContext,
      dimensions: MutableCartesianLayerDimensions,
      model: CustomLayerModel,
    ) {
      dimensions.ensureValuesAtLeast(xSpacing = 50f)
    }

    override fun updateChartRanges(
      chartRanges: MutableCartesianChartRanges,
      model: CustomLayerModel,
    ) {
      chartRanges.tryUpdate(model.minX, model.maxX, model.minY, model.maxY, null)
    }

    override fun prepareForTransformation(
      model: CustomLayerModel?,
      ranges: CartesianChartRanges,
      extraStore: MutableExtraStore,
    ) {}

    override suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {}

    override fun getSeriesKeys(model: CustomLayerModel): List<Any> = listOf("customKey")

    override fun getXValues(model: CustomLayerModel): List<Double> = listOf(0.0, 1.0, 2.0)

    override fun getAggregation(
      model: CustomLayerModel,
      x: ClosedFloatingPointRange<Double>,
      layerIndex: Int,
    ): CartesianLayerAggregation? {
      val entries =
        listOf(
          LineCartesianLayerModel.Entry(0.0, 5.0),
          LineCartesianLayerModel.Entry(1.0, 10.0),
          LineCartesianLayerModel.Entry(2.0, 15.0),
        ).filter { it.x in x }
      if (entries.isEmpty()) return null
      val min = entries.minBy { it.y }
      val max = entries.maxBy { it.y }
      val first = entries.first()
      val last = entries.last()
      return LineCartesianLayerAggregation(
        series =
          listOf(
            LineCartesianLayerAggregation.Series(
              key = "customKey",
              index = 0,
              min = min,
              max = max,
              first = first,
              last = last,
            )
          ),
        layerIndex = layerIndex,
      )
    }
  }

  private class CustomCartesianChart(
    customLayer: CustomCartesianLayer,
    lineLayer: LineCartesianLayer,
    descriptionProvider: CartesianChartDescriptionProvider?,
  ) : CartesianChart(customLayer, lineLayer, descriptionProvider = descriptionProvider) {
    override fun CartesianChartModel.forEachWithLayer(consumer: ModelAndLayerConsumer) {
      val freeModels = models.toMutableList()
      layers.forEach { layer ->
        when (layer) {
          is CustomCartesianLayer -> freeModels.consume(layer, consumer)
          is LineCartesianLayer -> freeModels.consume(layer, consumer)
          else -> throw IllegalArgumentException("Unexpected layer")
        }
      }
    }
  }
}
