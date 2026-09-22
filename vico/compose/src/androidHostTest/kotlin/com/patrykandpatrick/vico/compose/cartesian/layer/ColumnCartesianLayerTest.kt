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

package com.patrykandpatrick.vico.compose.cartesian.layer

import android.graphics.BlendMode
import android.graphics.Paint
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColumnCartesianLayerHostTest {
  private class CompatibleLayer :
    ColumnCartesianLayer(ColumnProvider.series(LineComponent(Fill.Black))) {
    override fun CartesianDrawingContext.updateMarkerTargets(
      entry: ColumnCartesianLayerModel.Entry,
      seriesKey: Any,
      canvasX: Float,
      canvasY: Float,
      columnHeight: Float,
      column: LineComponent,
      mergeMode: MergeMode,
    ) {}
  }

  @BeforeTest
  fun setUp() {
    mockkConstructor(Paint::class)
    justRun { anyConstructed<Paint>().setColor(any()) }
    justRun { anyConstructed<Paint>().setAlpha(any()) }
    justRun { anyConstructed<Paint>().setStrokeWidth(any()) }
    justRun { anyConstructed<Paint>().setStyle(any()) }
    justRun { anyConstructed<Paint>().setStrokeCap(any()) }
    justRun { anyConstructed<Paint>().setStrokeJoin(any()) }
    justRun { anyConstructed<Paint>().setAntiAlias(any()) }
    every { anyConstructed<Paint>().setXfermode(any()) } returns null
    justRun { anyConstructed<Paint>().setBlendMode(any<BlendMode>()) }
  }

  @Test
  fun `GroupedStacked shares centers stacks members and groups marker targets`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val layer =
      ColumnCartesianLayer(
        columnProvider = ColumnCartesianLayer.ColumnProvider.series(columns),
        mergeMode = {
          ColumnCartesianLayer.MergeMode.GroupedStacked(
            groupKeySelector = { seriesKey ->
              if (seriesKey == "a" || seriesKey == "b") "income" else "expenses"
            },
            columnSpacing = 10.dp,
          )
        },
      )
    val model =
      ColumnCartesianLayerModel(
        series =
          listOf(
            listOf(ColumnCartesianLayerModel.Entry(0.0, 2.0)),
            listOf(ColumnCartesianLayerModel.Entry(0.0, 3.0)),
            listOf(ColumnCartesianLayerModel.Entry(0.0, -4.0)),
          ),
        seriesKeys = listOf("a", "b", "c"),
      )
    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) }
    val dimensions = MutableCartesianLayerDimensions(xSpacing = 100f, scalableStartPadding = 20f)
    val baseContext = mockk<CartesianDrawingContext>(relaxed = true)
    every { baseContext.model } returns CartesianChartModel(model)
    every { baseContext.ranges } returns ranges
    every { baseContext.layerBounds } returns Rect(0f, 0f, 200f, 100f)
    every { baseContext.layerDimensions } returns dimensions
    every { baseContext.scroll } returns 0f
    every { baseContext.zoom } returns 1f
    val context =
      object : CartesianDrawingContext by baseContext {
        override val extraStore = ExtraStore.Empty
        override val density = Density(1f)
        override val androidx.compose.ui.unit.Dp.pixels: Float
          get() = value

        override val layoutDirection = LayoutDirection.Ltr
        override val isLtr = true
        override val layoutDirectionMultiplier = 1
      }

    layer.draw(context, model)

    assertEquals(columns[0].centers.single(), columns[1].centers.single())
    assertEquals(
      17f,
      columns[2].centers.single() - columns[0].centers.single(),
      "centers: ${columns.map { it.centers.single() }}",
    )
    assertEquals(columns[0].tops.single(), columns[1].bottoms.single())
    val targets = layer.markerTargets.getValue(0.0).map { it as ColumnCartesianLayerMarkerTarget }
    assertEquals(2, targets.size)
    assertTrue(
      targets.any { target -> target.columns.map { it.entry.seriesKey } == listOf("a", "b") }
    )
    assertTrue(targets.any { target -> target.columns.map { it.entry.seriesKey } == listOf("c") })
  }

  @Test
  fun `GroupedStacked lays out signed group members independently around zero`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(4.dp))
    val layer =
      ColumnCartesianLayer(
        columnProvider = ColumnCartesianLayer.ColumnProvider.series(columns),
        mergeMode = {
          ColumnCartesianLayer.MergeMode.GroupedStacked(groupKeySelector = { "group" })
        },
      )
    val model =
      ColumnCartesianLayerModel(
        series =
          listOf(
            listOf(ColumnCartesianLayerModel.Entry(0.0, 2.0)),
            listOf(ColumnCartesianLayerModel.Entry(0.0, -3.0)),
          )
      )

    layer.draw(createContext(layer, model), model)

    assertEquals(columns[0].centers.single(), columns[1].centers.single())
    assertEquals(columns[0].bottoms.single(), columns[1].tops.single())
    assertTrue(columns[0].tops.single() < columns[0].bottoms.single())
    assertTrue(columns[1].tops.single() < columns[1].bottoms.single())
    val targets = layer.markerTargets.getValue(0.0).single() as ColumnCartesianLayerMarkerTarget
    assertEquals(listOf(2.0, -3.0), targets.columns.map { it.entry.y })
  }

  @Test
  fun `GroupedStacked preserves marker suppression by legacy overrides`() {
    val layer =
      TrackingLayer(
        columnProvider =
          ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp), RecordingColumn(4.dp)),
        mergeMode = {
          ColumnCartesianLayer.MergeMode.GroupedStacked(groupKeySelector = { "group" })
        },
      )
    val model =
      ColumnCartesianLayerModel(
        series =
          listOf(
            listOf(ColumnCartesianLayerModel.Entry(0.0, 2.0)),
            listOf(ColumnCartesianLayerModel.Entry(0.0, 3.0)),
          )
      )

    layer.draw(createContext(layer, model), model)

    assertEquals(2, layer.markerTargetUpdates)
    assertTrue(layer.markerTargets.isEmpty())
  }

  private class RecordingColumn(thickness: androidx.compose.ui.unit.Dp) :
    LineComponent(Fill.Black, thickness) {
    val centers = mutableListOf<Float>()
    val tops = mutableListOf<Float>()
    val bottoms = mutableListOf<Float>()

    override fun drawVertical(
      context: com.patrykandpatrick.vico.compose.common.DrawingContext,
      x: Float,
      top: Float,
      bottom: Float,
      thicknessFactor: Float,
    ) {
      centers += x
      tops += top
      bottoms += bottom
    }
  }

  private class TrackingLayer(
    columnProvider: ColumnCartesianLayer.ColumnProvider,
    mergeMode: (ExtraStore) -> ColumnCartesianLayer.MergeMode,
  ) : ColumnCartesianLayer(columnProvider, mergeMode = mergeMode) {
    var markerTargetUpdates = 0

    override fun CartesianDrawingContext.updateMarkerTargets(
      entry: ColumnCartesianLayerModel.Entry,
      seriesKey: Any,
      canvasX: Float,
      canvasY: Float,
      columnHeight: Float,
      column: LineComponent,
      mergeMode: MergeMode,
    ) {
      markerTargetUpdates++
    }
  }

  private fun createContext(
    layer: ColumnCartesianLayer,
    model: ColumnCartesianLayerModel,
  ): CartesianDrawingContext {
    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) }
    val dimensions = MutableCartesianLayerDimensions(xSpacing = 100f, scalableStartPadding = 20f)
    val baseContext = mockk<CartesianDrawingContext>(relaxed = true)
    every { baseContext.model } returns CartesianChartModel(model)
    every { baseContext.ranges } returns ranges
    every { baseContext.layerBounds } returns Rect(0f, 0f, 200f, 100f)
    every { baseContext.layerDimensions } returns dimensions
    every { baseContext.scroll } returns 0f
    every { baseContext.zoom } returns 1f
    return object : CartesianDrawingContext by baseContext {
      override val extraStore = ExtraStore.Empty
      override val density = Density(1f)
      override val androidx.compose.ui.unit.Dp.pixels: Float
        get() = value

      override val layoutDirection = LayoutDirection.Ltr
      override val isLtr = true
      override val layoutDirectionMultiplier = 1
    }
  }
}
