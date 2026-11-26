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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.AnimatedCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.BaseCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCartesianLayerKey
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

// The snippets below document the intended API shape; they’re compiled but not run.

@Composable
internal fun SimpleLineChart() {
  val model = remember { LineCartesianLayerModel.build { series(13, 8, 7, 12) } }
  CartesianChartHost(
    rememberCartesianChart(
      rememberLineCartesianLayer(model),
      startAxis = VerticalAxis.rememberStart(),
      bottomAxis = HorizontalAxis.rememberBottom(),
    ),
  )
}

@Composable
internal fun ColorByData(
  model: ColumnCartesianLayerModel,
  threshold: Double,
  base: LineComponent,
  hot: LineComponent,
) {
  rememberColumnCartesianLayer(
    model,
    columnProvider =
      remember(threshold, base, hot) {
        ColumnCartesianLayer.ColumnProvider { entry -> if (entry.y > threshold) hot else base }
      },
  )
}

internal class SalesUi(
  val sales: ColumnCartesianLayerModel,
  val target: LineCartesianLayerModel?,
  val trend: LineCartesianLayerModel,
)

@Composable
internal fun InsertedLayer(ui: SalesUi) {
  CartesianChartHost(
    rememberCartesianChart(
      rememberColumnCartesianLayer(ui.sales),
      ui.target?.let { rememberLineCartesianLayer(it) },
      rememberLineCartesianLayer(ui.trend),
    ),
  )
}

/** A minimal custom model: one bar per value, at _x_ = 0, 1, 2, …. */
internal class BarModel(val values: List<Double>) : CartesianLayerModel {
  override val minX: Double = 0.0
  override val maxX: Double = (values.size - 1).toDouble()
  override val minY: Double = values.min()
  override val maxY: Double = values.max()

  override fun getXDeltaGcd(): Double = 1.0

  override fun equals(other: Any?): Boolean = other is BarModel && values == other.values

  override fun hashCode(): Int = values.hashCode()
}

/**
 * A minimal custom layer with difference, enter, and exit animations. Its drawing model is the list
 * of bar heights (as fractions of the layer height).
 */
internal class BarLayer(
  override val model: BarModel,
  override val key: Any,
  private val bar: LineComponent = LineComponent(Fill(Color.Red)),
) : BaseCartesianLayer<BarModel>(), AnimatedCartesianLayer<BarModel, List<Float>> {
  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = emptyMap()

  override val drawingModelInterpolator =
    CartesianLayerDrawingModelInterpolator<List<Float>> { old, new ->
      CartesianLayerDrawingModelInterpolator.Transition { fraction ->
        new.mapIndexed { index, height ->
          val oldHeight = old?.getOrNull(index) ?: 0f
          oldHeight + (height - oldHeight) * fraction
        }
      }
    }

  override fun createDrawingModel(ranges: CartesianChartRanges): List<Float> {
    val yRange = ranges.getYRange(null)
    return model.values.map { ((it - yRange.minY) / yRange.length).toFloat() }
  }

  override fun updateChartRanges(chartRanges: MutableCartesianChartRanges) {
    chartRanges.tryUpdate(model.minX, model.maxX, 0.0, model.maxY, null)
  }

  override fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
  ) {
    with(context) { dimensions.ensureValuesAtLeast(xSpacing = bar.thickness.pixels * 2) }
  }

  override fun drawInternal(context: CartesianDrawingContext) {
    with(context) {
      getDrawingModel(this@BarLayer).forEachIndexed { index, height ->
        val x =
          layerBounds.left + layerDimensions.startPadding + index * layerDimensions.xSpacing -
            scroll
        bar.drawVertical(
          this,
          x,
          layerBounds.bottom - height * layerBounds.height,
          layerBounds.bottom,
        )
      }
    }
  }
}

@Composable
internal fun rememberBarLayer(model: BarModel): BarLayer {
  val key = rememberCartesianLayerKey()
  return remember(model) { BarLayer(model, key) }
}

class CustomLayerTest {
  @Test
  fun `A custom layer animates in, updates, and exits through the same contract`() = runBlocking {
    val clock = BroadcastFrameClock()
    val scope = CoroutineScope(coroutineContext + clock + Job())
    val pipeline = CartesianChartPipeline(scope, backgroundContext = EmptyCoroutineContext)
    pipeline.animationSpec = tween(durationMillis = 100, easing = LinearEasing)
    pipeline.initialAnimationSpec = pipeline.animationSpec
    var time = 0L
    suspend fun frame(millis: Long) {
      repeat(20) { yield() }
      time += millis * 1_000_000
      clock.sendFrame(time)
      repeat(20) { yield() }
    }
    fun submit(chart: CartesianChart) = pipeline.apply(pipeline.prepare(chart))
    fun heights() = pipeline.display!!.drawingModels["bars"] as List<*>?
    try {
      submit(testCartesianChart(BarLayer(BarModel(listOf(2.0, 4.0)), "bars")))
      assertEquals(listOf(0f, 0f), heights())
      frame(16)
      frame(200)
      assertEquals(listOf(0.5f, 1f), heights())
      submit(testCartesianChart(BarLayer(BarModel(listOf(4.0, 4.0)), "bars")))
      frame(16)
      frame(50)
      assertEquals(listOf(0.75f, 1f), heights())
      frame(100)
      val other = BarLayer(BarModel(listOf(1.0)), "other")
      submit(testCartesianChart(other))
      frame(16)
      frame(50)
      assertEquals(listOf(0.5f, 0.5f), heights())
      frame(100)
      assertEquals(null, heights())
      assertEquals(listOf<Any>(other), pipeline.display!!.drawList.map { it.layer })
    } finally {
      scope.cancel()
    }
  }
}
