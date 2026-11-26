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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerDrawingModel
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer.StackKeyProvider
import com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.common.Defaults
import com.patrykandpatrick.vico.compose.common.DrawingContext
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlin.math.abs
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColumnCartesianLayerTest {
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

  // Layout.

  @Test
  fun `grouped places the columns of each group side by side`() {
    for (direction in LayoutDirection.entries) {
      val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
      val model =
        ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2), entries(0 to 3)))
      val layer = layer(model, ColumnCartesianLayer.ColumnProvider.series(columns))
      val context = createContext(layer, layoutDirection = direction)

      layer.draw(context)

      val sign = if (direction == LayoutDirection.Ltr) 1f else -1f
      val centers = columns.map { it.centers.single() }
      assertEquals(sign * 14f, centers[1] - centers[0], "$direction")
      assertEquals(sign * 15f, centers[2] - centers[1], "$direction")
      assertEquals(3, layer.columnTargets(0.0).size)
      val dimensions = MutableCartesianLayerDimensions()
      layer.updateDimensions(context, dimensions)
      assertEquals(34f + Defaults.GROUP_SPACING, dimensions.xSpacing)
    }
  }

  @Test
  fun `stacked centers every column in one stack`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp))
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to 3)))
    val layer =
      layer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(columns),
        stackKeyProvider = StackKeyProvider.Stacked,
      )
    val context = createContext(layer)

    layer.draw(context)

    assertEquals(columns[0].centers.single(), columns[1].centers.single())
    assertEquals(columns[0].tops.single(), columns[1].bottoms.single())
    assertEquals(listOf(listOf<Any>(0, 1)), layer.columnTargets(0.0).map { it.seriesKeys() })
    val dimensions = MutableCartesianLayerDimensions()
    layer.updateDimensions(context, dimensions)
    assertEquals(8f + Defaults.GROUP_SPACING, dimensions.xSpacing)
  }

  @Test
  fun `stacks are placed stackSpacing apart and are as wide as their widest columns`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val model =
      ColumnCartesianLayerModel(
        listOf(entries(0 to 2), entries(0 to 3), entries(0 to -4)),
        listOf("a", "b", "c"),
      )
    val layer =
      layer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(columns),
        stackKeyProvider = { seriesKey, _ -> if (seriesKey == "c") seriesKey else "ab" },
        stackSpacing = 10.dp,
      )

    layer.draw(createContext(layer))

    assertEquals(columns[0].centers.single(), columns[1].centers.single())
    assertEquals(17f, columns[2].centers.single() - columns[0].centers.single())
    assertEquals(columns[0].tops.single(), columns[1].bottoms.single())
    assertEquals(
      listOf(listOf("a", "b"), listOf("c")),
      layer.columnTargets(0.0).map { it.seriesKeys() },
    )
  }

  @Test
  fun `positive and negative values accumulate separately around zero`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(4.dp), RecordingColumn(4.dp))
    val model =
      ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to -3), entries(0 to 4)))
    val layer =
      layer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(columns),
        stackKeyProvider = StackKeyProvider.Stacked,
      )

    layer.draw(createContext(layer))

    assertEquals(columns[0].bottoms.single(), columns[1].tops.single())
    assertEquals(columns[0].tops.single(), columns[2].bottoms.single())
  }

  @Test
  fun `layout scales with density and zoom and follows the layout direction`() {
    for (direction in LayoutDirection.entries) {
      for ((density, zoom) in listOf(1f to 1f, 2f to 1f, 1f to 2f, 2f to 2f)) {
        val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
        val model =
          ColumnCartesianLayerModel(
            listOf(entries(0 to 1), entries(0 to 2), entries(0 to 3)),
            listOf("a", "b", "c"),
          )
        val layer =
          layer(
            model,
            ColumnCartesianLayer.ColumnProvider.series(columns),
            stackKeyProvider = { seriesKey, _ -> if (seriesKey == "c") "B" else "A" },
            stackSpacing = 10.dp,
          )
        val context =
          createContext(layer, layoutDirection = direction, zoom = zoom, density = density)

        layer.draw(context)

        val sign = if (direction == LayoutDirection.Ltr) 1f else -1f
        val message = "$direction density=$density zoom=$zoom"
        assertEquals(columns[0].centers.single(), columns[1].centers.single(), message)
        assertClose(
          sign * 17f * density * zoom,
          columns[2].centers.single() - columns[0].centers.single(),
          message,
        )
        val dimensions = MutableCartesianLayerDimensions()
        layer.updateDimensions(context, dimensions)
        assertClose(
          24f * density,
          dimensions.xSpacing - Defaults.GROUP_SPACING * density,
          message,
        )
      }
    }
  }

  // Ranges.

  @Test
  fun `the range of single-series stacks is the model range`() {
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2, 1 to 5), entries(0 to 3)))

    val range = intrinsicYRange(StackKeyProvider.Grouped, model)

    assertEquals(2.0, range.minY)
    assertEquals(5.0, range.maxY)
  }

  @Test
  fun `the range of a shared stack is the aggregate model range`() {
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2, 1 to 5), entries(0 to 3)))

    val range = intrinsicYRange(StackKeyProvider.Stacked, model)

    assertEquals(0.0, range.minY)
    assertEquals(5.0, range.maxY)
  }

  // Hooks and caching.

  @Test
  fun `the stack layout is computed once per layer`() {
    var stackKeyProviderCalls = 0
    val model =
      ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)), listOf("a", "b"))
    val layer =
      layer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp), RecordingColumn(8.dp)),
        stackKeyProvider = { _, _ ->
          stackKeyProviderCalls++
          "stack"
        },
      )
    val ranges = MutableCartesianChartRanges().also(layer::updateChartRanges)
    stackKeyProviderCalls = 0
    fun draw(density: Float = 1f) {
      val context = createContext(layer, density = density, ranges = ranges)
      layer.updateDimensions(context, MutableCartesianLayerDimensions())
      layer.draw(context)
    }

    repeat(3) { draw() }
    draw(density = 2f)
    // The layout was computed when the ranges were (off the main thread, for asynchronous updates).
    assertEquals(0, stackKeyProviderCalls)
  }

  @Test
  fun `the widest column of each series is determined once per layer`() {
    var calls = 0
    val column = RecordingColumn(4.dp)
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 1, 1 to 2, 2 to 3)))
    val layer =
      layer(
        model,
        { _ ->
          calls++
          column
        },
      )
    val context = createContext(layer)
    repeat(3) { layer.updateDimensions(context, MutableCartesianLayerDimensions()) }
    assertEquals(3, calls)
  }

  // Exit animations.

  @Test
  fun `exiting series are drawn at their former stack positions without marker targets`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(4.dp), RecordingColumn(4.dp))
    val model =
      ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 3)), listOf("a", "c"))
    val columnsByKey =
      mapOf<Any, RecordingColumn>("a" to columns[0], "b" to columns[1], "c" to columns[2])
    val provider = ColumnCartesianLayer.ColumnProvider { entry ->
      columnsByKey.getValue(entry.seriesKey)
    }
    val layer = layer(model, provider)
    // The layer that `b` came from.
    val previousModel =
      ColumnCartesianLayerModel(
        listOf(entries(0 to 1), entries(0 to 2), entries(0 to 3)),
        listOf("a", "b", "c"),
      )
    val previousLayer = layer(previousModel, provider)
    val exitingEntry = previousModel.series[1][0]
    val drawingModel =
      ColumnCartesianLayerDrawingModel(
        listOf(
          mapOf(0.0 to ColumnCartesianLayerDrawingModel.Entry(0.1f, model.series[0][0])),
          mapOf(0.0 to ColumnCartesianLayerDrawingModel.Entry(0.2f, exitingEntry)),
          mapOf(0.0 to ColumnCartesianLayerDrawingModel.Entry(0.3f, model.series[1][0])),
        ),
        listOf("a", "b", "c"),
        1f,
        listOf(layer, previousLayer, layer),
      )

    layer.draw(createContext(layer, drawingModel = drawingModel))

    // `b` keeps its slot between `a` and `c`.
    assertEquals(1, columns[1].centers.size)
    assertTrue(columns[0].centers.single() < columns[1].centers.single())
    assertTrue(columns[1].centers.single() < columns[2].centers.single())
    assertEquals(
      listOf(listOf<Any>("a"), listOf<Any>("c")),
      layer.columnTargets(0.0).map { it.seriesKeys() },
    )
  }

  // Data labels.

  @Test
  fun `stack totals are drawn after the stack's columns`() {
    val events = mutableListOf<String>()
    val model = sparseModel()
    val layer =
      RecordingLayer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(List(3) { RecordingColumn(4.dp, events) }),
        sparseStackKeyProvider,
        events,
      )

    layer.draw(createContext(layer))

    assertEquals(
      listOf("column", "column", "column", "label", "label", "column", "column", "label", "label"),
      events,
    )
    assertEquals(
      listOf("A" to 5.0, "A" to 11.0, "B" to 5.0, "B" to 7.0),
      layer.labels.map { it.stack to it.topY },
    )
    assertTrue(layer.labels.all { it.stackCount == 2 })
  }

  @Test
  fun `stack labels report the stack width, center, and range edges`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val model = sparseModel()
    val layer =
      RecordingLayer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(columns),
        sparseStackKeyProvider,
        mutableListOf(),
      )

    layer.draw(createContext(layer))

    val byKey = layer.labels.associateBy { it.stack to it.x }
    val a0 = byKey.getValue("A" to 0.0)
    val b1 = byKey.getValue("B" to 1.0)
    assertEquals(6.dp, a0.columnThickness)
    assertEquals(8.dp, b1.columnThickness)
    assertEquals(columns[0].centers.single(), a0.canvasX)
    assertTrue(a0.isFirst && !a0.isLast)
    assertTrue(b1.isLast && !b1.isFirst)
    assertTrue(layer.labels.none { it.stack == "A" && it.isLast })
  }

  @Test
  fun `labels show zero totals and keep positive and negative totals separate`() {
    val model =
      ColumnCartesianLayerModel(
        listOf(entries(0 to 2), entries(0 to -3), entries(0 to 4), entries(0 to 0)),
      )
    val layer =
      DataLabelRecordingLayer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        StackKeyProvider { seriesKey, _ -> if (seriesKey == 3) seriesKey else "s" },
      )

    layer.draw(createContext(layer))

    assertEquals(listOf(6.0, -3.0, 0.0), layer.values)
  }

  @Test
  fun `stack labels follow animated column heights`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(4.dp))
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to 3)))
    val layer =
      RecordingLayer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(columns),
        StackKeyProvider.Stacked,
        mutableListOf(),
      )
    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it) }
    val drawingModel =
      ColumnCartesianLayerDrawingModel(
        listOf(
          mapOf(0.0 to ColumnCartesianLayerDrawingModel.Entry(0.1f, model.series[0][0])),
          mapOf(0.0 to ColumnCartesianLayerDrawingModel.Entry(0.2f, model.series[1][0])),
        ),
        model.seriesKeys,
      )

    layer.draw(createContext(layer, ranges = ranges, drawingModel = drawingModel))

    val label = layer.labels.single()
    assertClose(30f, label.topHeight, "animated height")
    assertEquals(5.0, label.topY)
  }

  // Marker targets.

  @Test
  fun `marker targets follow stack order for sparse interleaved stacks`() {
    val model = sparseModel(withUnstackedSeries = true)
    val layer =
      layer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        stackKeyProvider = sparseStackKeyProvider,
      )

    layer.draw(createContext(layer))

    assertEquals(
      listOf(listOf("A1", "A2"), listOf("B1"), listOf("N")),
      layer.columnTargets(0.0).map { it.seriesKeys() },
    )
    assertEquals(
      listOf(listOf("A2"), listOf("B1"), listOf("N")),
      layer.columnTargets(1.0).map { it.seriesKeys() },
    )
  }

  @Test
  fun `the marker hook uses the coordinates it receives and keeps repeated columns`() {
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)))
    val layer =
      CustomMarkerLayer(model) { context, model ->
        mark(context, model.series[0][0], stackIndex = 0, canvasX = 53f)
        mark(context, model.series[0][0], stackIndex = 0, canvasX = 60f)
        mark(context, model.series[1][0], stackIndex = 1, canvasX = 70f)
      }

    layer.draw(createContext(layer))

    val targets = layer.columnTargets(0.0)
    assertEquals(listOf(53f, 70f), targets.map { it.canvasX })
    assertEquals(listOf(2, 1), targets.map { it.columns.size })
  }

  @Test
  fun `an override that doesn't call the marker hook suppresses targets`() {
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to 3)))
    val layer =
      TrackingLayer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp), RecordingColumn(4.dp)),
      )

    layer.draw(createContext(layer))

    assertEquals(2, layer.markerTargetUpdates)
    assertTrue(layer.markerTargets.isEmpty())
  }

  @Test
  fun `marker targets keep the horizontal cutoff and vertical clamping`() {
    val model =
      ColumnCartesianLayerModel(
        listOf(entries(0 to 5), entries(0 to 2), entries(0 to 1)),
        listOf("a", "b", "c"),
      )
    val layer =
      layer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(
          RecordingColumn(4.dp),
          RecordingColumn(8.dp),
          RecordingColumn(6.dp),
        ),
        stackKeyProvider = { seriesKey, _ -> if (seriesKey == "c") "B" else "A" },
        stackSpacing = 10.dp,
      )
    val ranges =
      MutableCartesianChartRanges().apply {
        tryUpdate(minX = 0.0, maxX = 0.0, minY = 0.0, maxY = 1.0, axisPosition = null)
      }

    layer.draw(createContext(layer, ranges = ranges, layerBounds = Rect(0f, 0f, 20f, 100f)))

    val target = layer.columnTargets(0.0).single()
    assertEquals(listOf("a", "b"), target.seriesKeys())
    assertEquals(0f, target.columns.last().canvasY)
  }

  @Test
  fun `marker targets are rebuilt on every frame`() {
    val model = sparseModel()
    val layer =
      layer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        stackKeyProvider = sparseStackKeyProvider,
      )

    layer.draw(createContext(layer))
    layer.draw(createContext(layer))

    assertEquals(
      listOf(listOf("A1", "A2"), listOf("B1")),
      layer.columnTargets(0.0).map { it.seriesKeys() },
    )
  }

  // Fixtures.

  private val sparseStackKeyProvider = StackKeyProvider { seriesKey, _ ->
    when (seriesKey) {
      "A1",
      "A2" -> "A"
      "B1" -> "B"
      else -> seriesKey
    }
  }

  private fun sparseModel(withUnstackedSeries: Boolean = false): ColumnCartesianLayerModel {
    val series = mutableListOf(entries(0 to 2), entries(0 to 5, 1 to 7), entries(0 to 3, 1 to 11))
    val keys = mutableListOf<Any>("A1", "B1", "A2")
    if (withUnstackedSeries) {
      series += entries(0 to 1, 1 to 1)
      keys += "N"
    }
    return ColumnCartesianLayerModel(series, keys)
  }

  private fun entries(vararg points: Pair<Number, Number>) = points.map { (x, y) ->
    ColumnCartesianLayerModel.Entry(x, y)
  }

  private fun intrinsicYRange(
    stackKeyProvider: StackKeyProvider,
    model: ColumnCartesianLayerModel,
  ) =
    MutableCartesianChartRanges()
      .also {
        layer(
            model,
            ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
            stackKeyProvider = stackKeyProvider,
            rangeProvider = CartesianLayerRangeProvider.Intrinsic,
          )
          .updateChartRanges(it)
      }
      .getYRange(null)

  private fun assertClose(expected: Float, actual: Float, message: String) {
    assertTrue(abs(expected - actual) < 0.001f, "$message: expected $expected, was $actual")
  }

  private fun ColumnCartesianLayer.columnTargets(x: Double) =
    markerTargets.getValue(x).map { it as ColumnCartesianLayerMarkerTarget }

  private fun ColumnCartesianLayerMarkerTarget.seriesKeys() = columns.map { it.entry.seriesKey }

  private class RecordingColumn(thickness: Dp, private val events: MutableList<String>? = null) :
    LineComponent(Fill.Black, thickness) {
    val centers = mutableListOf<Float>()
    val tops = mutableListOf<Float>()
    val bottoms = mutableListOf<Float>()

    override fun drawVertical(
      context: DrawingContext,
      x: Float,
      top: Float,
      bottom: Float,
      thicknessFactor: Float,
    ) {
      centers += x
      tops += top
      bottoms += bottom
      events?.add("column")
    }
  }

  private data class LabelCall(
    val stack: String?,
    val x: Double,
    val canvasX: Float,
    val stackCount: Int,
    val columnThickness: Dp,
    val topY: Double,
    val topHeight: Float,
    val isFirst: Boolean,
    val isLast: Boolean,
  )

  private class RecordingLayer(
    model: ColumnCartesianLayerModel,
    columnProvider: ColumnProvider,
    stackKeyProvider: StackKeyProvider,
    private val events: MutableList<String>,
  ) : ColumnCartesianLayer(model, columnProvider, stackKeyProvider = stackKeyProvider, key = 0) {
    val labels = mutableListOf<LabelCall>()

    override fun CartesianDrawingContext.drawStackedDataLabel(
      stackCount: Int,
      columnThickness: Dp,
      stackInfo: StackInfo,
      x: Float,
      zeroLinePosition: Float,
      heightMultiplier: Float,
      isFirst: Boolean,
      isLast: Boolean,
    ) {
      events += "label"
      val target =
        markerTargets.entries
          .flatMap { (targetX, targets) -> targets.map { targetX to it } }
          .firstOrNull { (_, target) -> abs(target.canvasX - x) < 0.01f }
      labels +=
        LabelCall(
          stack =
            (target?.second as? ColumnCartesianLayerMarkerTarget)
              ?.columns
              ?.firstOrNull()
              ?.entry
              ?.seriesKey
              ?.toString()
              ?.take(1),
          x = target?.first ?: Double.NaN,
          canvasX = x,
          stackCount = stackCount,
          columnThickness = columnThickness,
          topY = stackInfo.topY,
          topHeight = stackInfo.topHeight,
          isFirst = isFirst,
          isLast = isLast,
        )
    }
  }

  private class DataLabelRecordingLayer(
    model: ColumnCartesianLayerModel,
    columnProvider: ColumnProvider,
    stackKeyProvider: StackKeyProvider,
  ) : ColumnCartesianLayer(model, columnProvider, stackKeyProvider = stackKeyProvider, key = 0) {
    val values = mutableListOf<Double>()

    override fun CartesianDrawingContext.drawDataLabel(
      stackCount: Int,
      columnThickness: Dp,
      dataLabelValue: Double,
      x: Float,
      y: Float,
      isFirst: Boolean,
      isLast: Boolean,
    ) {
      values += dataLabelValue
    }
  }

  private class CustomMarkerLayer(
    model: ColumnCartesianLayerModel,
    private val action:
      CustomMarkerLayer.(CartesianDrawingContext, ColumnCartesianLayerModel) -> Unit,
  ) : ColumnCartesianLayer(model, ColumnProvider.series(LineComponent(Fill.Black, 4.dp)), key = 0) {
    fun mark(
      context: CartesianDrawingContext,
      entry: ColumnCartesianLayerModel.Entry,
      stackIndex: Int,
      canvasX: Float,
    ) {
      with(context) {
        updateMarkerTargets(entry, stackIndex, canvasX, 50f, 10f, LineComponent(Fill.Black, 4.dp))
      }
    }

    override fun CartesianDrawingContext.drawChartInternal(
      drawingModel: ColumnCartesianLayerDrawingModel,
    ) {
      action(this, this@CustomMarkerLayer.model)
    }
  }

  private class TrackingLayer(model: ColumnCartesianLayerModel, columnProvider: ColumnProvider) :
    ColumnCartesianLayer(model, columnProvider, key = 0) {
    var markerTargetUpdates = 0

    override fun CartesianDrawingContext.updateMarkerTargets(
      entry: ColumnCartesianLayerModel.Entry,
      stackIndex: Int,
      canvasX: Float,
      canvasY: Float,
      columnHeight: Float,
      column: LineComponent,
    ) {
      markerTargetUpdates++
    }
  }

  private fun layer(
    model: ColumnCartesianLayerModel,
    columnProvider: ColumnCartesianLayer.ColumnProvider,
    stackKeyProvider: StackKeyProvider = StackKeyProvider.Grouped,
    stackSpacing: Dp = Defaults.STACK_SPACING.dp,
    rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
  ) =
    ColumnCartesianLayer(
      model,
      columnProvider,
      stackKeyProvider = stackKeyProvider,
      stackSpacing = stackSpacing,
      rangeProvider = rangeProvider,
      key = 0,
    )

  private fun createContext(
    layer: ColumnCartesianLayer,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    zoom: Float = 1f,
    density: Float = 1f,
    layerBounds: Rect = Rect(0f, 0f, 200f, 100f),
    dimensions: MutableCartesianLayerDimensions =
      MutableCartesianLayerDimensions(xSpacing = 100f, scalableStartPadding = 20f),
    drawingModel: ColumnCartesianLayerDrawingModel? = null,
    ranges: CartesianChartRanges = MutableCartesianChartRanges().also(layer::updateChartRanges),
  ): CartesianDrawingContext {
    val scaledDimensions = dimensions.copyScaled(zoom)
    val baseContext = mockk<CartesianDrawingContext>(relaxed = true)
    every { baseContext.chartModel } returns CartesianChartModel(listOf(layer.model))
    every { baseContext.ranges } returns ranges
    every { baseContext.layerBounds } returns layerBounds
    every { baseContext.layerDimensions } returns scaledDimensions
    every { baseContext.scroll } returns 0f
    every { baseContext.zoom } returns zoom
    every { baseContext.layerPadding } returns CartesianLayerPadding()
    val ltr = layoutDirection == LayoutDirection.Ltr
    val contextDensity = Density(density)
    val contextLayoutDirection = layoutDirection
    return object : CartesianDrawingContext by baseContext {
      override val density = contextDensity
      override val Dp.pixels: Float
        get() = value * contextDensity.density

      override val layoutDirection = contextLayoutDirection
      override val isLtr = ltr
      override val layoutDirectionMultiplier = if (ltr) 1 else -1

      @Suppress("UNCHECKED_CAST")
      override fun <D : Any> getDrawingModel(layer: AnimatedCartesianLayer<*, D>): D =
        drawingModel as D? ?: layer.createDrawingModel(ranges)
    }
  }
}
