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
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
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
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore
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
      val layer = ColumnCartesianLayer(ColumnCartesianLayer.ColumnProvider.series(columns))
      val model =
        ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2), entries(0 to 3)))
      val context = createContext(layer, model, layoutDirection = direction)

      layer.draw(context, model)

      val sign = if (direction == LayoutDirection.Ltr) 1f else -1f
      val centers = columns.map { it.centers.single() }
      assertEquals(sign * 14f, centers[1] - centers[0], "$direction")
      assertEquals(sign * 15f, centers[2] - centers[1], "$direction")
      assertEquals(3, layer.columnTargets(0.0).size)
      val dimensions = MutableCartesianLayerDimensions()
      layer.updateDimensions(context, dimensions, model)
      assertEquals(34f + Defaults.GROUP_SPACING, dimensions.xSpacing)
    }
  }

  @Test
  fun `stacked centers every column in one stack`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp))
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        stackKeyProvider = StackKeyProvider.Stacked,
      )
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to 3)))
    val context = createContext(layer, model)

    layer.draw(context, model)

    assertEquals(columns[0].centers.single(), columns[1].centers.single())
    assertEquals(columns[0].tops.single(), columns[1].bottoms.single())
    assertEquals(listOf(listOf<Any>(0, 1)), layer.columnTargets(0.0).map { it.seriesKeys() })
    val dimensions = MutableCartesianLayerDimensions()
    layer.updateDimensions(context, dimensions, model)
    assertEquals(8f + Defaults.GROUP_SPACING, dimensions.xSpacing)
  }

  @Test
  fun `stacks are placed stackSpacing apart and are as wide as their widest columns`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        stackKeyProvider = { seriesKey, _, _ -> if (seriesKey == "c") seriesKey else "ab" },
        stackSpacing = 10.dp,
      )
    val model =
      ColumnCartesianLayerModel(
        listOf(entries(0 to 2), entries(0 to 3), entries(0 to -4)),
        listOf("a", "b", "c"),
      )

    layer.draw(createContext(layer, model), model)

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
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        stackKeyProvider = StackKeyProvider.Stacked,
      )
    val model =
      ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to -3), entries(0 to 4)))

    layer.draw(createContext(layer, model), model)

    assertEquals(columns[0].bottoms.single(), columns[1].tops.single())
    assertEquals(columns[0].tops.single(), columns[2].bottoms.single())
  }

  @Test
  fun `layout scales with density and zoom and follows the layout direction`() {
    for (direction in LayoutDirection.entries) {
      for ((density, zoom) in listOf(1f to 1f, 2f to 1f, 1f to 2f, 2f to 2f)) {
        val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
        val layer =
          ColumnCartesianLayer(
            ColumnCartesianLayer.ColumnProvider.series(columns),
            stackKeyProvider = { seriesKey, _, _ -> if (seriesKey == "c") "B" else "A" },
            stackSpacing = 10.dp,
          )
        val model =
          ColumnCartesianLayerModel(
            listOf(entries(0 to 1), entries(0 to 2), entries(0 to 3)),
            listOf("a", "b", "c"),
          )
        val context =
          createContext(layer, model, layoutDirection = direction, zoom = zoom, density = density)

        layer.draw(context, model)

        val sign = if (direction == LayoutDirection.Ltr) 1f else -1f
        val message = "$direction density=$density zoom=$zoom"
        assertEquals(columns[0].centers.single(), columns[1].centers.single(), message)
        assertClose(
          sign * 17f * density * zoom,
          columns[2].centers.single() - columns[0].centers.single(),
          message,
        )
        val dimensions = MutableCartesianLayerDimensions()
        layer.updateDimensions(context, dimensions, model)
        assertClose(
          24f * density,
          dimensions.xSpacing - Defaults.GROUP_SPACING * density,
          message,
        )
      }
    }
  }

  @Test
  fun `the stack provider receives the series keys, the series indices, and the extras`() {
    val calls = mutableListOf<Pair<Any, Int>>()
    val extrasKey = ExtraStore.Key<String>()
    val extras = MutableExtraStore().also { it[extrasKey] = "extras" }
    val receivedExtras = mutableSetOf<String?>()
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        stackKeyProvider = { seriesKey, seriesIndex, extraStore ->
          calls += seriesKey to seriesIndex
          receivedExtras += extraStore.getOrNull(extrasKey)
          seriesKey
        },
      )
    val model =
      ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)), listOf("a", "b"))
        .copy(extras) as ColumnCartesianLayerModel

    layer.updateChartRanges(MutableCartesianChartRanges(), model)

    assertEquals(listOf<Pair<Any, Int>>("a" to 0, "b" to 1), calls)
    assertEquals(setOf<String?>("extras"), receivedExtras)
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

  // Deprecated API.

  @Test
  @Suppress("DEPRECATION")
  fun `merge modes map to stack providers`() {
    val widths = listOf(4.dp, 8.dp, 6.dp)
    val model =
      ColumnCartesianLayerModel(
        listOf(entries(0 to 2, 1 to -1), entries(0 to 3, 1 to 4), entries(0 to -2, 1 to 1))
      )
    for (direction in LayoutDirection.entries) {
      assertEquals(
        renderCenters(widths, model, direction) {
          ColumnCartesianLayer(
            it,
            stackKeyProvider = StackKeyProvider.Grouped,
            stackSpacing = 10.dp,
          )
        },
        renderCenters(widths, model, direction) {
          ColumnCartesianLayer(it, mergeMode = { ColumnCartesianLayer.MergeMode.Grouped(10.dp) })
        },
      )
      assertEquals(
        renderCenters(widths, model, direction) {
          ColumnCartesianLayer(it, stackKeyProvider = StackKeyProvider.Stacked)
        },
        renderCenters(widths, model, direction) {
          ColumnCartesianLayer(it, mergeMode = { ColumnCartesianLayer.MergeMode.Stacked })
        },
      )
    }
    assertEquals(
      intrinsicYRange(StackKeyProvider.Grouped, model).let { it.minY to it.maxY },
      intrinsicYRange(mergeMode = ColumnCartesianLayer.MergeMode.Grouped(), model = model).let {
        it.minY to it.maxY
      },
    )
    assertEquals(
      intrinsicYRange(StackKeyProvider.Stacked, model).let { it.minY to it.maxY },
      intrinsicYRange(mergeMode = ColumnCartesianLayer.MergeMode.Stacked, model = model).let {
        it.minY to it.maxY
      },
    )
  }

  @Test
  @Suppress("DEPRECATION")
  fun `columnCollectionSpacing maps to groupSpacing`() {
    val columnProvider = ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp))
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 1)))
    fun ColumnCartesianLayer.groupSpacing(): Float {
      val dimensions = MutableCartesianLayerDimensions()
      updateDimensions(createContext(this, model), dimensions, model)
      return dimensions.xSpacing - 4f
    }
    val layer = ColumnCartesianLayer(columnProvider, columnCollectionSpacing = 10.dp)

    assertEquals(10f, layer.groupSpacing())
    assertEquals(
      Defaults.GROUP_SPACING,
      ColumnCartesianLayer(columnProvider, columnCollectionSpacing = null).groupSpacing(),
    )
    assertEquals(10f, layer.copy(columnCollectionSpacing = null).groupSpacing())
    assertEquals(20f, layer.copy(columnCollectionSpacing = 20.dp).groupSpacing())
  }

  // Hooks and caching.

  @Test
  fun `a getDrawingStart override moves the drawn columns`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val layer =
      object :
        ColumnCartesianLayer(
          ColumnProvider.series(columns),
          stackKeyProvider = { _, seriesIndex, _ -> if (seriesIndex == 2) "B" else "A" },
        ) {
        override fun CartesianDrawingContext.getDrawingStart(
          seriesIndex: Int,
          seriesKeys: List<Any>,
          extraStore: ExtraStore,
        ): Float = 100f + 30f * seriesIndex
      }
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2), entries(0 to 3)))

    layer.draw(createContext(layer, model), model)

    assertEquals(listOf(104f, 134f, 163f), columns.map { it.centers.single() })
  }

  @Test
  fun `a getGroupWidth override affects measurement and drawing`() {
    val stackKeyProvider = StackKeyProvider { _, seriesIndex, _ ->
      if (seriesIndex == 2) "B" else "A"
    }
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2), entries(0 to 3)))
    val plainColumns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val plain =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(plainColumns),
        stackKeyProvider = stackKeyProvider,
        stackSpacing = 10.dp,
      )
    val overriddenColumns =
      listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val overridden =
      object :
        ColumnCartesianLayer(
          ColumnProvider.series(overriddenColumns),
          stackKeyProvider = stackKeyProvider,
          stackSpacing = 10.dp,
        ) {
        override fun CartesianMeasuringContext.getGroupWidth(
          seriesKeys: List<Any>,
          extraStore: ExtraStore,
        ): Float = 100f
      }
    val dimensions = MutableCartesianLayerDimensions()

    overridden.updateDimensions(createContext(overridden, model), dimensions, model)
    plain.draw(createContext(plain, model), model)
    overridden.draw(createContext(overridden, model), model)

    assertEquals(100f + Defaults.GROUP_SPACING, dimensions.xSpacing)
    assertEquals(
      plainColumns.map { it.centers.single() - 38f },
      overriddenColumns.map { it.centers.single() },
    )
  }

  @Test
  fun `the stack geometry is reused across frames and rebuilt when its inputs change`() {
    var stackKeyProviderCalls = 0
    val provider = CountingProvider(listOf(RecordingColumn(4.dp), RecordingColumn(8.dp)))
    val layer =
      ColumnCartesianLayer(
        provider,
        stackKeyProvider = { _, _, _ ->
          stackKeyProviderCalls++
          "stack"
        },
      )
    val model =
      ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)), listOf("a", "b"))
    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) }
    stackKeyProviderCalls = 0
    fun draw(model: ColumnCartesianLayerModel, density: Float = 1f) {
      val context = createContext(layer, model, density = density, ranges = ranges)
      layer.updateDimensions(context, MutableCartesianLayerDimensions(), model)
      layer.draw(context, model)
    }

    repeat(3) { draw(model) }
    assertEquals(2, provider.widestCalls)
    assertEquals(2, stackKeyProviderCalls)
    draw(model, density = 2f)
    assertEquals(4, provider.widestCalls)
    draw(ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)), listOf("b", "a")))
    assertEquals(6, provider.widestCalls)
    assertEquals(6, stackKeyProviderCalls)
  }

  // Data labels.

  @Test
  fun `stack totals are drawn after the stack's columns`() {
    val events = mutableListOf<String>()
    val layer =
      RecordingLayer(
        ColumnCartesianLayer.ColumnProvider.series(List(3) { RecordingColumn(4.dp, events) }),
        sparseStackKeyProvider,
        events,
      )
    val model = sparseModel()

    layer.draw(createContext(layer, model), model)

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
    val layer =
      RecordingLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        sparseStackKeyProvider,
        mutableListOf(),
      )
    val model = sparseModel()

    layer.draw(createContext(layer, model), model)

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
    val layer =
      DataLabelRecordingLayer(
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        { seriesKey, _, _ -> if (seriesKey == 3) seriesKey else "s" },
      )
    val model =
      ColumnCartesianLayerModel(
        listOf(entries(0 to 2), entries(0 to -3), entries(0 to 4), entries(0 to 0))
      )

    layer.draw(createContext(layer, model), model)

    assertEquals(listOf(6.0, -3.0, 0.0), layer.values)
  }

  @Test
  fun `stack labels follow animated column heights`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(4.dp))
    val layer =
      RecordingLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        StackKeyProvider.Stacked,
        mutableListOf(),
      )
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to 3)))
    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) }
    val drawingModel =
      ColumnCartesianLayerDrawingModel(
        listOf(
          mapOf(0.0 to ColumnCartesianLayerDrawingModel.Entry(0.1f)),
          mapOf(0.0 to ColumnCartesianLayerDrawingModel.Entry(0.2f)),
        ),
        model.seriesKeys,
      )
    val extras = MutableExtraStore().also { it[layer.exposedDrawingModelKey] = drawingModel }

    layer.draw(createContext(layer, model, ranges = ranges, extraStore = extras), model)

    val label = layer.labels.single()
    assertClose(30f, label.topHeight, "animated height")
    assertEquals(5.0, label.topY)
  }

  // Marker targets.

  @Test
  fun `marker targets follow stack order for sparse interleaved stacks`() {
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        stackKeyProvider = sparseStackKeyProvider,
      )
    val model = sparseModel(withUnstackedSeries = true)

    layer.draw(createContext(layer, model), model)

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
    val layer = CustomMarkerLayer { context, model ->
      mark(context, model.series[0][0], stackIndex = 0, canvasX = 53f)
      mark(context, model.series[0][0], stackIndex = 0, canvasX = 60f)
      mark(context, model.series[1][0], stackIndex = 1, canvasX = 70f)
    }
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)))

    layer.draw(createContext(layer, model), model)

    val targets = layer.columnTargets(0.0)
    assertEquals(listOf(53f, 70f), targets.map { it.canvasX })
    assertEquals(listOf(2, 1), targets.map { it.columns.size })
  }

  @Test
  fun `an override that doesn't call the marker hook suppresses targets`() {
    val layer =
      TrackingLayer(
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp), RecordingColumn(4.dp))
      )
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to 3)))

    layer.draw(createContext(layer, model), model)

    assertEquals(2, layer.markerTargetUpdates)
    assertTrue(layer.markerTargets.isEmpty())
  }

  @Test
  fun `marker targets keep the horizontal cutoff and vertical clamping`() {
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(
          RecordingColumn(4.dp),
          RecordingColumn(8.dp),
          RecordingColumn(6.dp),
        ),
        stackKeyProvider = { seriesKey, _, _ -> if (seriesKey == "c") "B" else "A" },
        stackSpacing = 10.dp,
      )
    val model =
      ColumnCartesianLayerModel(
        listOf(entries(0 to 5), entries(0 to 2), entries(0 to 1)),
        listOf("a", "b", "c"),
      )
    val ranges =
      MutableCartesianChartRanges().apply {
        tryUpdate(minX = 0.0, maxX = 0.0, minY = 0.0, maxY = 1.0, axisPosition = null)
      }

    layer.draw(
      createContext(layer, model, ranges = ranges, layerBounds = Rect(0f, 0f, 20f, 100f)),
      model,
    )

    val target = layer.columnTargets(0.0).single()
    assertEquals(listOf("a", "b"), target.seriesKeys())
    assertEquals(0f, target.columns.last().canvasY)
  }

  @Test
  fun `marker targets are rebuilt on every frame`() {
    val stackedKey = ExtraStore.Key<Boolean>()
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        stackKeyProvider = { seriesKey, seriesIndex, extraStore ->
          if (extraStore.getOrNull(stackedKey) == true) Unit
          else sparseStackKeyProvider.getStackKey(seriesKey, seriesIndex, extraStore)
        },
      )
    val grouped = sparseModel()
    val stacked =
      grouped.copy(MutableExtraStore().also { it[stackedKey] = true }) as ColumnCartesianLayerModel

    layer.draw(createContext(layer, grouped), grouped)
    layer.draw(createContext(layer, grouped), grouped)
    val repeated = layer.columnTargets(0.0).map { it.seriesKeys() }
    layer.draw(createContext(layer, stacked), stacked)
    val asStacked = layer.columnTargets(0.0).map { it.seriesKeys() }
    layer.draw(createContext(layer, grouped), grouped)

    assertEquals(listOf(listOf("A1", "A2"), listOf("B1")), repeated)
    assertEquals(listOf(listOf("A1", "B1", "A2")), asStacked)
    assertEquals(repeated, layer.columnTargets(0.0).map { it.seriesKeys() })
  }

  // Fixtures.

  private val sparseStackKeyProvider = StackKeyProvider { seriesKey, _, _ ->
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
        ColumnCartesianLayer(
            ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
            stackKeyProvider = stackKeyProvider,
            rangeProvider = CartesianLayerRangeProvider.Intrinsic,
          )
          .updateChartRanges(it, model)
      }
      .getYRange(null)

  @Suppress("DEPRECATION")
  private fun intrinsicYRange(
    mergeMode: ColumnCartesianLayer.MergeMode,
    model: ColumnCartesianLayerModel,
  ) =
    MutableCartesianChartRanges()
      .also {
        ColumnCartesianLayer(
            ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
            mergeMode = { mergeMode },
            rangeProvider = CartesianLayerRangeProvider.Intrinsic,
          )
          .updateChartRanges(it, model)
      }
      .getYRange(null)

  private fun renderCenters(
    widths: List<Dp>,
    model: ColumnCartesianLayerModel,
    direction: LayoutDirection,
    createLayer: (ColumnCartesianLayer.ColumnProvider) -> ColumnCartesianLayer,
  ): List<List<Float>> {
    val columns = widths.map { RecordingColumn(it) }
    val layer = createLayer(ColumnCartesianLayer.ColumnProvider.series(columns))
    layer.draw(createContext(layer, model, layoutDirection = direction), model)
    return columns.map { it.centers }
  }

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

  private class CountingProvider(val columns: List<RecordingColumn>) :
    ColumnCartesianLayer.ColumnProvider {
    var widestCalls = 0

    override fun getColumn(entry: ColumnCartesianLayerModel.Entry, extraStore: ExtraStore) =
      columns[entry.seriesIndex % columns.size]

    override fun getWidestSeriesColumn(
      seriesKey: Any,
      seriesIndex: Int,
      extraStore: ExtraStore,
    ): LineComponent {
      widestCalls++
      return columns[seriesIndex % columns.size]
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
    columnProvider: ColumnProvider,
    stackKeyProvider: StackKeyProvider,
    private val events: MutableList<String>,
  ) : ColumnCartesianLayer(columnProvider, stackKeyProvider = stackKeyProvider) {
    val labels = mutableListOf<LabelCall>()
    val exposedDrawingModelKey: ExtraStore.Key<ColumnCartesianLayerDrawingModel>
      get() = drawingModelKey

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
    columnProvider: ColumnProvider,
    stackKeyProvider: StackKeyProvider,
  ) : ColumnCartesianLayer(columnProvider, stackKeyProvider = stackKeyProvider) {
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
    private val action:
      CustomMarkerLayer.(CartesianDrawingContext, ColumnCartesianLayerModel) -> Unit
  ) : ColumnCartesianLayer(ColumnProvider.series(LineComponent(Fill.Black, 4.dp))) {
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
      model: ColumnCartesianLayerModel,
      ranges: CartesianChartRanges,
      drawingModel: ColumnCartesianLayerDrawingModel?,
    ) {
      action(this, model)
    }
  }

  private class TrackingLayer(columnProvider: ColumnProvider) :
    ColumnCartesianLayer(columnProvider) {
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

  private fun createContext(
    layer: ColumnCartesianLayer,
    model: ColumnCartesianLayerModel,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    zoom: Float = 1f,
    density: Float = 1f,
    layerBounds: Rect = Rect(0f, 0f, 200f, 100f),
    dimensions: MutableCartesianLayerDimensions =
      MutableCartesianLayerDimensions(xSpacing = 100f, scalableStartPadding = 20f),
    extraStore: ExtraStore = ExtraStore.Empty,
    ranges: CartesianChartRanges =
      MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) },
  ): CartesianDrawingContext {
    val scaledDimensions = dimensions.copyScaled(zoom)
    val baseContext = mockk<CartesianDrawingContext>(relaxed = true)
    every { baseContext.model } returns CartesianChartModel(model)
    every { baseContext.ranges } returns ranges
    every { baseContext.layerBounds } returns layerBounds
    every { baseContext.layerDimensions } returns scaledDimensions
    every { baseContext.scroll } returns 0f
    every { baseContext.zoom } returns zoom
    every { baseContext.layerPadding } returns CartesianLayerPadding()
    val ltr = layoutDirection == LayoutDirection.Ltr
    val contextExtraStore = extraStore
    val contextDensity = Density(density)
    val contextLayoutDirection = layoutDirection
    return object : CartesianDrawingContext by baseContext {
      override val extraStore = contextExtraStore
      override val density = contextDensity
      override val Dp.pixels: Float
        get() = value * contextDensity.density

      override val layoutDirection = contextLayoutDirection
      override val isLtr = ltr
      override val layoutDirectionMultiplier = if (ltr) 1 else -1
    }
  }
}
