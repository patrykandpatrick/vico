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

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.data.CacheStore
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class DefaultCartesianChartDescriptionProviderTest {

  private fun createLineLayer(verticalAxisPosition: Axis.Position.Vertical? = null) =
    LineCartesianLayer(
      lineProvider =
        LineCartesianLayer.LineProvider.series(
          LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill.Black))
        ),
      verticalAxisPosition = verticalAxisPosition,
    )

  private fun createColumnLayer(verticalAxisPosition: Axis.Position.Vertical? = null) =
    ColumnCartesianLayer(
      columnProvider = ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill.Black)),
      verticalAxisPosition = verticalAxisPosition,
    )

  private fun createCandlestickLayer(
    verticalAxisPosition: Axis.Position.Vertical? = null
  ): CandlestickCartesianLayer {
    val candle = CandlestickCartesianLayer.Candle(LineComponent(Fill.Black))
    return CandlestickCartesianLayer(
      candleProvider =
        object : CandlestickCartesianLayer.CandleProvider {
          override fun getCandle(
            entry: CandlestickCartesianLayerModel.Entry,
            key: Any,
            extraStore: ExtraStore,
          ) = candle

          override fun getWidestCandle(key: Any, extraStore: ExtraStore) = candle
        },
      verticalAxisPosition = verticalAxisPosition,
    )
  }

  private val wholeNumberFormatter = CartesianValueFormatter { _, value, _ ->
    value.toInt().toString()
  }

  private val provider =
    CartesianChartDescriptionProvider.default(
      xValueFormatter = wholeNumberFormatter,
      yValueFormatter = wholeNumberFormatter,
    )

  private fun createContext(model: CartesianChartModel): CartesianMeasuringContext =
    object : CartesianMeasuringContext {
      override val model: CartesianChartModel = model
      override val ranges: CartesianChartRanges = CartesianChartRanges.Empty
      override val scrollEnabled: Boolean = false
      override val zoomEnabled: Boolean = false
      override val layerPadding: CartesianLayerPadding = CartesianLayerPadding()
      override val markerX: Double? = null
      override val markerSeriesIndex: Int? = null
      override val canvasWidth: Float = 800f
      @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
      override val canvasSize: Size = Size(800f, 400f)
      override val fontFamilyResolver: FontFamily.Resolver
        get() = error("Unused in test")

      override val density: Density = Density(1f)
      override val extraStore: ExtraStore = ExtraStore.Empty
      override val layoutDirection: LayoutDirection = LayoutDirection.Ltr
      override val cacheStore: CacheStore = CacheStore()
    }

  @Test
  fun lineLayerAggregationProducesCorrectSummaryForSingleSeries() {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(10, 30, 20, 50)
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..3.0)

    val summary = provider.getSummary(context, aggregation).toString()

    assertEquals(
      "Line chart. X from 0 to 3. Min: 10 at 0, max: 50 at 3, first: 10, last: 50 (change: +40).",
      summary,
    )
  }

  @Test
  fun lineLayerAggregationProducesCorrectSummaryForMultipleSeries() {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(10, 50)
          series(20, 15)
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..1.0)

    val summary = provider.getSummary(context, aggregation).toString()

    assertEquals(
      "Line chart. X from 0 to 1. Series 1: Min: 10 at 0, max: 50 at 1, first: 10, last: 50 (change: +40). Series 2: Min: 15 at 1, max: 20 at 0, first: 20, last: 15 (change: -5).",
      summary,
    )
  }

  @Test
  fun lineLayerProducesCorrectIntervalDescriptionForSinglePoint() {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(10, 30, 20, 50)
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 1.0..1.0)

    val intervalDesc = provider.getIntervalDescription(context, aggregation).toString()

    assertEquals("X: 1. Y: 30.", intervalDesc)
  }

  @Test
  fun lineLayerProducesCorrectIntervalDescriptionForRange() {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(10, 30, 20, 50)
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..2.0)

    val intervalDesc = provider.getIntervalDescription(context, aggregation).toString()

    assertEquals("X: 0 to 2. Min: 10, max: 30, first: 10, last: 20.", intervalDesc)
  }

  @Test
  fun columnLayerAggregationProducesCorrectSummaryForSingleSeries() {
    val model =
      CartesianChartModel(
        ColumnCartesianLayerModel.build {
          series(10, 20, 30)
        }
      )
    val chart = CartesianChart(createColumnLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..2.0)

    val summary = provider.getSummary(context, aggregation).toString()

    assertEquals(
      "Column chart. X from 0 to 2. Min: 10 at 0, max: 30 at 2, first: 10, last: 30 (change: +20), sum: 60, mean: 20.",
      summary,
    )
  }

  @Test
  fun columnLayerAggregationProducesCorrectSummaryForMultipleSeries() {
    val model =
      CartesianChartModel(
        ColumnCartesianLayerModel.build {
          series(10, 20)
          series(30, 40)
        }
      )
    val chart = CartesianChart(createColumnLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..1.0)

    val summary = provider.getSummary(context, aggregation).toString()

    assertEquals(
      "Column chart. X from 0 to 1. Series 1: Min: 10 at 0, max: 20 at 1, first: 10, last: 20 (change: +10), sum: 30, mean: 15. Series 2: Min: 30 at 0, max: 40 at 1, first: 30, last: 40 (change: +10), sum: 70, mean: 35.",
      summary,
    )
  }

  @Test
  fun columnLayerProducesCorrectIntervalDescriptionForSingleItem() {
    val model =
      CartesianChartModel(
        ColumnCartesianLayerModel.build {
          series(10, 20, 30)
        }
      )
    val chart = CartesianChart(createColumnLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 1.0..1.0)

    val intervalDesc = provider.getIntervalDescription(context, aggregation).toString()

    assertEquals("X: 1. Y: 20.", intervalDesc)
  }

  @Test
  fun columnLayerProducesCorrectIntervalDescriptionForMultipleItems() {
    val model =
      CartesianChartModel(
        ColumnCartesianLayerModel.build {
          series(10, 20, 30)
        }
      )
    val chart = CartesianChart(createColumnLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..1.0)

    val intervalDesc = provider.getIntervalDescription(context, aggregation).toString()

    assertEquals("X: 0 to 1. Min: 10, max: 20, sum: 30, mean: 15.", intervalDesc)
  }

  @Test
  fun candlestickLayerAggregationProducesCorrectSummary() {
    val model =
      CartesianChartModel(
        CandlestickCartesianLayerModel.build(
          opening = listOf(100, 110),
          closing = listOf(110, 105),
          low = listOf(90, 95),
          high = listOf(120, 115),
        )
      )
    val chart = CartesianChart(createCandlestickLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..1.0)

    val summary = provider.getSummary(context, aggregation).toString()

    assertEquals(
      "Candlestick chart. X from 0 to 1. Open: 100, high: 120, low: 90, close: 105 (change: +5).",
      summary,
    )
  }

  @Test
  fun candlestickLayerProducesCorrectIntervalDescription() {
    val model =
      CartesianChartModel(
        CandlestickCartesianLayerModel.build(
          opening = listOf(100, 110),
          closing = listOf(110, 105),
          low = listOf(90, 95),
          high = listOf(120, 115),
        )
      )
    val chart = CartesianChart(createCandlestickLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..0.0)

    val intervalDesc = provider.getIntervalDescription(context, aggregation).toString()

    assertEquals("X: 0. Open: 100, high: 120, low: 90, close: 110.", intervalDesc)
  }

  @Test
  fun multiLayerChartProducesCartesianChartSummary() {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build { series(10, 20) },
        ColumnCartesianLayerModel.build { series(30, 40) },
      )
    val chart =
      CartesianChart(createLineLayer(), createColumnLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..1.0)

    val summary = provider.getSummary(context, aggregation).toString()

    assertEquals(
      "Cartesian chart. X from 0 to 1. Series 1: Min: 10 at 0, max: 20 at 1, first: 10, last: 20 (change: +10). Series 2: Min: 30 at 0, max: 40 at 1, first: 30, last: 40 (change: +10), sum: 70, mean: 35.",
      summary,
    )
  }

  @Test
  fun customSeriesLabelIsApplied() {
    val customProvider =
      CartesianChartDescriptionProvider.default(
        xValueFormatter = wholeNumberFormatter,
        yValueFormatter = wholeNumberFormatter,
        seriesLabel = { _, layerIndex, key -> "Layer $layerIndex Data $key" },
      )
    val model = CartesianChartModel(LineCartesianLayerModel.build { series(10, 20) })
    val chart = CartesianChart(createLineLayer(), descriptionProvider = customProvider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..1.0)

    val summary = customProvider.getSummary(context, aggregation).toString()

    assertEquals(
      "Line chart. X from 0 to 1. Layer 0 Data 0: Min: 10 at 0, max: 20 at 1, first: 10, last: 20 (change: +10).",
      summary,
    )
  }

  @Test
  fun customAxisFormattersAndVerticalAxisPositionAreRespected() {
    var receivedPosition: Axis.Position.Vertical? = null
    val customYFormatter = CartesianValueFormatter { _, value, verticalAxisPosition ->
      receivedPosition = verticalAxisPosition
      "$$value"
    }
    val customXFormatter = CartesianValueFormatter { _, value, _ -> "Point $value" }

    val customProvider =
      CartesianChartDescriptionProvider.default(
        xValueFormatter = customXFormatter,
        yValueFormatter = customYFormatter,
      )

    val model = CartesianChartModel(LineCartesianLayerModel.build { series(10.0, 20.0) })
    val configuredLineLayer = createLineLayer(verticalAxisPosition = Axis.Position.Vertical.End)
    val chart = CartesianChart(configuredLineLayer, descriptionProvider = customProvider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..1.0)

    val summary = customProvider.getSummary(context, aggregation).toString()

    assertEquals(
      "Line chart. X from Point 0.0 to Point 1.0. Min: $10.0 at Point 0.0, max: $20.0 at Point 1.0, first: $10.0, last: $20.0 (change: +$10.0).",
      summary,
    )
    assertEquals(Axis.Position.Vertical.End, receivedPosition)
  }

  @Test
  fun unknownLayerReturnsNullAggregationAndIsIgnored() {
    val unknownLayer =
      object : CartesianLayer<LineCartesianLayerModel> {
        override val markerTargets: Map<Double, List<CartesianMarker.Target>> = emptyMap()

        override fun draw(
          context: com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext,
          model: LineCartesianLayerModel,
        ) {}

        override fun updateDimensions(
          context: CartesianMeasuringContext,
          dimensions: MutableCartesianLayerDimensions,
          model: LineCartesianLayerModel,
        ) {}

        override fun updateChartRanges(
          chartRanges: com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges,
          model: LineCartesianLayerModel,
        ) {}

        override fun prepareForTransformation(
          model: LineCartesianLayerModel?,
          ranges: CartesianChartRanges,
          extraStore: MutableExtraStore,
        ) {}

        override suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {}
      }

    val model = LineCartesianLayerModel.build { series(10, 20) }
    assertNull(unknownLayer.getAggregation(model, 0.0..1.0, 0))
  }

  @Test
  fun providerEqualityAndHashCodeCheck() {
    val provider1 =
      CartesianChartDescriptionProvider.default(
        xValueFormatter = wholeNumberFormatter,
        yValueFormatter = wholeNumberFormatter,
      )
    val provider2 =
      CartesianChartDescriptionProvider.default(
        xValueFormatter = wholeNumberFormatter,
        yValueFormatter = wholeNumberFormatter,
      )
    val provider3 =
      CartesianChartDescriptionProvider.default(
        xValueFormatter = CartesianValueFormatter.decimal(decimalCount = 1),
        yValueFormatter = wholeNumberFormatter,
      )

    assertEquals(provider1, provider2)
    assertEquals(provider1.hashCode(), provider2.hashCode())
    assertNotEquals(provider1, provider3)
  }

  @Test
  fun intervalDescriptionWithMultiSeriesUsesConsistentSeriesNumber() {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(listOf(0, 1), listOf(10, 20))
          series(listOf(2, 3), listOf(30, 40))
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 2.0..3.0)

    val intervalDesc = provider.getIntervalDescription(context, aggregation).toString()

    assertEquals("X: 2 to 3. Series 2: Min: 30, max: 40, first: 30, last: 40.", intervalDesc)
  }

  @Test
  fun duplicateXForSingleSeriesIsDeduplicatedAndDescribedAsMinMax() {
    val model =
      CartesianChartModel(
        LineCartesianLayerModel.build {
          series(listOf(0.0, 1.0, 1.0, 2.0), listOf(10, 20, 25, 30))
        }
      )
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 1.0..1.0)

    val intervalDesc = provider.getIntervalDescription(context, aggregation).toString()

    assertEquals("X: 1. Min: 20, max: 25.", intervalDesc)
  }

  @Test
  fun seriesNumberingMatchesLayerOrderWhenModelOrderDiffers() {
    val columnModel =
      ColumnCartesianLayerModel.build {
        series(10, 20)
        series(30, 40)
      }
    val lineModel = LineCartesianLayerModel.build { series(50, 60) }
    val model = CartesianChartModel(lineModel, columnModel)
    val chart =
      CartesianChart(createColumnLayer(), createLineLayer(), descriptionProvider = provider)
    val context = createContext(model)
    val aggregation = chart.getAggregation(model, 0.0..1.0)

    val summary = provider.getSummary(context, aggregation).toString()

    assertEquals(
      "Cartesian chart. X from 0 to 1. Series 1: Min: 10 at 0, max: 20 at 1, first: 10, last: 20 (change: +10), sum: 30, mean: 15. Series 2: Min: 30 at 0, max: 40 at 1, first: 30, last: 40 (change: +10), sum: 70, mean: 35. Series 3: Min: 50 at 0, max: 60 at 1, first: 50, last: 60 (change: +10).",
      summary,
    )
  }

  @Test
  fun cartesianChartEqualityIncludesDescriptionProvider() {
    val chart = CartesianChart(createLineLayer(), descriptionProvider = provider)
    val copy = chart.copy()
    val copyWithDifferentProvider = chart.copy(descriptionProvider = null)

    assertEquals(chart, copy)
    assertEquals(chart.hashCode(), copy.hashCode())
    assertNotEquals(chart, copyWithDifferentProvider)
  }

  @Test
  fun unknownLayerWithoutForEachWithLayerOverrideThrowsException() {
    val unknownLayer =
      object : CartesianLayer<LineCartesianLayerModel> {
        override val markerTargets: Map<Double, List<CartesianMarker.Target>> = emptyMap()

        override fun draw(
          context: com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext,
          model: LineCartesianLayerModel,
        ) {}

        override fun updateDimensions(
          context: CartesianMeasuringContext,
          dimensions: MutableCartesianLayerDimensions,
          model: LineCartesianLayerModel,
        ) {}

        override fun updateChartRanges(
          chartRanges: com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges,
          model: LineCartesianLayerModel,
        ) {}

        override fun prepareForTransformation(
          model: LineCartesianLayerModel?,
          ranges: CartesianChartRanges,
          extraStore: MutableExtraStore,
        ) {}

        override suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {}
      }

    val model = CartesianChartModel(LineCartesianLayerModel.build { series(10, 20) })
    val chart = CartesianChart(unknownLayer, descriptionProvider = provider)

    assertFailsWith<IllegalArgumentException> {
      chart.getAggregation(model, 0.0..1.0)
    }
  }

  @Test
  fun layerSwapUpdatesSeriesNumbering() {
    val columnModel =
      ColumnCartesianLayerModel.build {
        series(10, 20)
        series(30, 40)
      }
    val lineModel = LineCartesianLayerModel.build { series(50, 60) }
    val model = CartesianChartModel(lineModel, columnModel)
    val chart =
      CartesianChart(createColumnLayer(), createLineLayer(), descriptionProvider = provider)
    val swappedChart = chart.copy(layers = arrayOf(createLineLayer(), createColumnLayer()))
    val context = createContext(model)
    val aggregation = swappedChart.getAggregation(model, 0.0..1.0)

    val summary = provider.getSummary(context, aggregation).toString()

    assertEquals(
      "Cartesian chart. X from 0 to 1. Series 1: Min: 50 at 0, max: 60 at 1, first: 50, last: 60 (change: +10). Series 2: Min: 10 at 0, max: 20 at 1, first: 10, last: 20 (change: +10), sum: 30, mean: 15. Series 3: Min: 30 at 0, max: 40 at 1, first: 30, last: 40 (change: +10), sum: 70, mean: 35.",
      summary,
    )
  }
}
