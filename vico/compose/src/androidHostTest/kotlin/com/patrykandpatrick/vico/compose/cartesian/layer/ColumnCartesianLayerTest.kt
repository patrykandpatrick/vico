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
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerDrawingModel
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer.MergeMode
import com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.narrowMarkerTargets
import com.patrykandpatrick.vico.compose.common.Defaults
import com.patrykandpatrick.vico.compose.common.DrawingContext
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
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

  // Existing behavior, migrated to `Grouped(stackKey = ...)`.

  @Test
  fun `stacks share centers, stack members, and group marker targets`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val layer =
      ColumnCartesianLayer(
        columnProvider = ColumnCartesianLayer.ColumnProvider.series(columns),
        mergeMode = {
          MergeMode.Grouped(columnSpacing = 10.dp) { seriesKey ->
            if (seriesKey == "a" || seriesKey == "b") "income" else "expenses"
          }
        },
      )
    val model =
      ColumnCartesianLayerModel(
        series = listOf(entries(0 to 2), entries(0 to 3), entries(0 to -4)),
        seriesKeys = listOf("a", "b", "c"),
      )

    layer.draw(createContext(layer, model), model)

    assertEquals(columns[0].centers.single(), columns[1].centers.single())
    assertEquals(17f, columns[2].centers.single() - columns[0].centers.single())
    assertEquals(columns[0].tops.single(), columns[1].bottoms.single())
    val targets = layer.columnTargets(0.0)
    assertEquals(listOf(listOf("a", "b"), listOf("c")), targets.map { it.seriesKeys() })
  }

  @Test
  fun `signed stack members are laid out independently around zero`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(4.dp))
    val layer =
      ColumnCartesianLayer(
        columnProvider = ColumnCartesianLayer.ColumnProvider.series(columns),
        mergeMode = { MergeMode.Grouped(stackKey = { "group" }) },
      )
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to -3)))

    layer.draw(createContext(layer, model), model)

    assertEquals(columns[0].centers.single(), columns[1].centers.single())
    assertEquals(columns[0].bottoms.single(), columns[1].tops.single())
    assertTrue(columns[0].tops.single() < columns[0].bottoms.single())
    assertTrue(columns[1].tops.single() < columns[1].bottoms.single())
    assertEquals(listOf(2.0, -3.0), layer.columnTargets(0.0).single().columns.map { it.entry.y })
  }

  @Test
  fun `an override that does not call the marker hook suppresses targets`() {
    val layer =
      TrackingLayer(
        columnProvider =
          ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp), RecordingColumn(4.dp)),
        mergeMode = { MergeMode.Grouped(stackKey = { "group" }) },
      )
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to 3)))

    layer.draw(createContext(layer, model), model)

    assertEquals(2, layer.markerTargetUpdates)
    assertTrue(layer.markerTargets.isEmpty())
  }

  // Task 3: API routing and selector cost.

  @Test
  fun `range updates call the selector once per series and drawing reuses the mapping`() {
    var calls = 0
    val selector: (Any) -> Any? = { key ->
      calls++
      if (key == "c") null else "ab"
    }
    val layer = layer(mergeMode = { MergeMode.Grouped(stackKey = selector) })
    val model = model(listOf("a", "b", "c"), 3, 3, 3)

    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) }
    assertEquals(3, calls)
    layer.updateChartRanges(MutableCartesianChartRanges(), model)
    layer.draw(createContext(layer, model, ranges = ranges), model)
    layer.draw(createContext(layer, model, ranges = ranges), model)

    assertEquals(3, calls)
  }

  @Test
  fun `the selector receives series indices when no keys are given`() {
    val received = mutableListOf<Any>()
    val selector: (Any) -> Any? = { key ->
      received += key
      key
    }
    val layer = layer(mergeMode = { MergeMode.Grouped(stackKey = selector) })
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2), entries(0 to 3)))

    layer.updateChartRanges(MutableCartesianChartRanges(), model)

    assertEquals(listOf<Any>(0, 1, 2), received)
  }

  @Test
  fun `the selector receives explicit series keys`() {
    val received = mutableListOf<Any>()
    val selector: (Any) -> Any? = { key ->
      received += key
      key
    }
    val layer = layer(mergeMode = { MergeMode.Grouped(stackKey = selector) })

    layer.updateChartRanges(MutableCartesianChartRanges(), model(listOf("x", "y"), 1, 1))

    assertEquals(listOf<Any>("x", "y"), received)
  }

  @Test
  fun `null selector results keep series in separate columns`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(4.dp))
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        mergeMode = { MergeMode.Grouped(stackKey = { null }) },
      )
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to 3)))

    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) }
    layer.draw(createContext(layer, model, ranges = ranges), model)

    assertEquals(3.0, ranges.getYRange(null).maxY)
    assertTrue(columns[0].centers.single() != columns[1].centers.single())
    assertEquals(columns[0].bottoms.single(), columns[1].bottoms.single())
    assertEquals(2, layer.columnTargets(0.0).size)
  }

  @Test
  fun `data-only changes reuse the mapping but recompute totals`() {
    var calls = 0
    val selector: (Any) -> Any? = {
      calls++
      "stack"
    }
    val layer = layer(mergeMode = { MergeMode.Grouped(stackKey = selector) })
    val first =
      ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)), listOf("a", "b"))
    val second =
      ColumnCartesianLayerModel(listOf(entries(0 to 4), entries(0 to 5)), listOf("a", "b"))

    val firstRanges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, first) }
    val secondRanges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, second) }

    assertEquals(3.0, firstRanges.getYRange(null).maxY)
    assertEquals(9.0, secondRanges.getYRange(null).maxY)
    assertEquals(2, calls)
  }

  @Test
  fun `an unstable selector costs one call per series per update or draw, never per entry`() {
    var calls = 0
    val layer =
      layer(
        mergeMode = {
          MergeMode.Grouped(
            stackKey = { key ->
              calls++
              if (key == "c") null else "ab"
            }
          )
        }
      )
    val model = model(listOf("a", "b", "c"), 50, 50, 50)

    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) }
    assertEquals(3, calls)
    layer.draw(createContext(layer, model, ranges = ranges), model)
    assertEquals(6, calls)
    layer.draw(createContext(layer, model, ranges = ranges), model)
    assertEquals(9, calls)
  }

  @Test
  fun `the layer range matches the public getMinY and getMaxY`() {
    val selector: (Any) -> Any? = { key -> if (key == "a" || key == "b") "ab" else "c" }
    val mergeMode = MergeMode.Grouped(stackKey = selector)
    val layer = layer(mergeMode = { mergeMode })
    val model =
      ColumnCartesianLayerModel(
        listOf(entries(0 to 7, 1 to -2), entries(0 to 3, 1 to -1), entries(0 to -5, 1 to 2)),
        listOf("a", "b", "c"),
      )

    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) }

    assertEquals(-5.0, mergeMode.getMinY(model))
    assertEquals(10.0, mergeMode.getMaxY(model))
    assertEquals(-5.0, ranges.getYRange(null).minY)
    assertEquals(10.0, ranges.getYRange(null).maxY)
  }

  @Test
  fun `range updates make no column provider calls`() {
    val provider = CountingProvider(listOf(RecordingColumn(4.dp)))
    val layer =
      ColumnCartesianLayer(provider, mergeMode = { MergeMode.Grouped(stackKey = { "stack" }) })

    layer.updateChartRanges(MutableCartesianChartRanges(), model(listOf("a", "b"), 2, 2))

    assertEquals(0, provider.widestCalls)
    assertEquals(0, provider.columnCalls)
  }

  @Test
  fun `Grouped without a selector keeps its range, labels, and marker targets`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(6.dp))
    val events = mutableListOf<String>()
    val layer =
      RecordingLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        mergeMode = { MergeMode.Grouped() },
        events = events,
      )
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2, 1 to 5), entries(0 to 3, 1 to 1)))

    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) }
    layer.draw(createContext(layer, model, ranges = ranges), model)

    assertEquals(model.minY, MergeMode.Grouped().getMinY(model))
    assertEquals(model.maxY, MergeMode.Grouped().getMaxY(model))
    assertEquals(4, events.count { it == "dataLabel" })
    assertEquals(0, events.count { it == "stackedLabel" })
    assertEquals(2, layer.columnTargets(0.0).size)
  }

  // Task 4: geometry, caching, and hooks.

  @Test
  fun `one stack per series matches Grouped geometry`() {
    val widths = listOf(4.dp, 8.dp, 6.dp)
    val model =
      ColumnCartesianLayerModel(
        listOf(entries(0 to 2, 1 to -1), entries(0 to 3, 1 to 4), entries(0 to -2, 1 to 1)),
        listOf("a", "b", "c"),
      )
    val ranges = rangesFor(MergeMode.Grouped(), model)
    for (direction in LayoutDirection.entries) {
      for (zoom in listOf(1f, 2f)) {
        val grouped = renderGeometry(MergeMode.Grouped(), widths, model, ranges, direction, zoom)
        val separate =
          renderGeometry(
            MergeMode.Grouped(stackKey = { it }),
            widths,
            model,
            ranges,
            direction,
            zoom,
          )
        assertGeometryEquals(grouped, separate, "$direction zoom=$zoom")
      }
    }
  }

  @Test
  fun `one shared stack with equal widths matches Stacked geometry`() {
    val widths = listOf(6.dp, 6.dp, 6.dp)
    val model =
      ColumnCartesianLayerModel(
        listOf(entries(0 to 2, 1 to -1), entries(0 to 3, 1 to 4), entries(0 to -2, 1 to 1))
      )
    val ranges = rangesFor(MergeMode.Stacked, model)
    for (direction in LayoutDirection.entries) {
      for (zoom in listOf(1f, 2f)) {
        val stacked = renderGeometry(MergeMode.Stacked, widths, model, ranges, direction, zoom)
        val shared =
          renderGeometry(
            MergeMode.Grouped(stackKey = { "all" }),
            widths,
            model,
            ranges,
            direction,
            zoom,
          )
        assertGeometryEquals(stacked, shared, "$direction zoom=$zoom")
      }
    }
  }

  @Test
  fun `mixed widths share the widest center and scale with density, zoom, and direction`() {
    for (direction in LayoutDirection.entries) {
      for ((density, zoom) in listOf(1f to 1f, 2f to 1f, 1f to 2f, 2f to 2f)) {
        val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
        val layer =
          ColumnCartesianLayer(
            ColumnCartesianLayer.ColumnProvider.series(columns),
            mergeMode = {
              MergeMode.Grouped(columnSpacing = 10.dp) { key -> if (key == "c") "B" else "A" }
            },
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
        assertEquals(columns[0].centers.single(), columns[1].centers.single())
        assertClose(
          sign * 17f * density * zoom,
          columns[2].centers.single() - columns[0].centers.single(),
          "$direction density=$density zoom=$zoom",
        )
        val dimensions = MutableCartesianLayerDimensions()
        layer.updateDimensions(context, dimensions, model)
        assertClose(
          24f * density,
          dimensions.xSpacing - Defaults.COLUMN_COLLECTION_SPACING * density,
          "collection width, density=$density",
        )
      }
    }
  }

  @Test
  fun `a getDrawingStart override moves the drawn columns`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val layer =
      object :
        ColumnCartesianLayer(
          ColumnProvider.series(columns),
          mergeMode = { MergeMode.Grouped(10.dp) { key -> if (key == 2) "B" else "A" } },
        ) {
        override fun CartesianDrawingContext.getDrawingStart(
          entryCollectionIndex: Int,
          entryCollectionCount: Int,
          seriesKeys: List<Any>,
          mergeMode: MergeMode,
        ): Float = 100f + 30f * entryCollectionIndex
      }
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2), entries(0 to 3)))

    layer.draw(createContext(layer, model), model)

    assertEquals(listOf(104f, 134f, 163f), columns.map { it.centers.single() })
  }

  @Test
  fun `a getColumnCollectionWidth override affects measurement and drawing`() {
    val selector: (Any) -> Any? = { key -> if (key == 2) "B" else "A" }
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2), entries(0 to 3)))
    val plainColumns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val plain =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(plainColumns),
        mergeMode = { MergeMode.Grouped(10.dp, selector) },
      )
    val overriddenColumns =
      listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val overridden =
      object :
        ColumnCartesianLayer(
          ColumnProvider.series(overriddenColumns),
          mergeMode = { MergeMode.Grouped(10.dp, selector) },
        ) {
        override fun CartesianMeasuringContext.getColumnCollectionWidth(
          entryCollectionSize: Int,
          seriesKeys: List<Any>,
          mergeMode: MergeMode,
        ): Float = 100f
      }
    val dimensions = MutableCartesianLayerDimensions()

    overridden.updateDimensions(createContext(overridden, model), dimensions, model)
    plain.draw(createContext(plain, model), model)
    overridden.draw(createContext(overridden, model), model)

    assertEquals(100f + Defaults.COLUMN_COLLECTION_SPACING, dimensions.xSpacing)
    assertEquals(
      plainColumns.map { it.centers.single() - 38f },
      overriddenColumns.map { it.centers.single() },
    )
  }

  @Test
  fun `geometry is cached across draws and rebuilt when its inputs change`() {
    val widthKey = ExtraStore.Key<Dp>()
    val extras = MutableExtraStore()
    var selectorCalls = 0
    val selector: (Any) -> Any? = {
      selectorCalls++
      "stack"
    }
    var spacing = 10.dp
    val provider =
      CountingProvider(listOf(RecordingColumn(4.dp), RecordingColumn(8.dp)), widthKey = widthKey)
    val layer = ColumnCartesianLayer(provider, mergeMode = { MergeMode.Grouped(spacing, selector) })
    val model =
      ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)), listOf("a", "b"))
        .copy(extras) as ColumnCartesianLayerModel
    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) }
    fun draw(density: Float = 1f) =
      layer.draw(createContext(layer, model, density = density, ranges = ranges), model)

    draw()
    assertEquals(2, provider.widestCalls)
    draw()
    draw()
    assertEquals(2, provider.widestCalls, "repeated draws must reuse the geometry")
    draw(density = 2f)
    assertEquals(4, provider.widestCalls, "a density change rebuilds the geometry")
    spacing = 12.dp
    draw(density = 2f)
    assertEquals(6, provider.widestCalls, "a spacing change rebuilds the geometry")
    extras[widthKey] = 20.dp
    draw(density = 2f)
    assertEquals(8, provider.widestCalls, "an extras change rebuilds the geometry")
    assertEquals(2, selectorCalls, "geometry changes must not call the selector again")
  }

  @Test
  fun `geometry is rebuilt when the series order or the selector changes`() {
    val provider = CountingProvider(listOf(RecordingColumn(4.dp), RecordingColumn(8.dp)))
    var selector: (Any) -> Any? = { "stack" }
    val layer =
      ColumnCartesianLayer(provider, mergeMode = { MergeMode.Grouped(stackKey = selector) })
    val forward =
      ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)), listOf("a", "b"))
    val reversed =
      ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)), listOf("b", "a"))

    layer.draw(createContext(layer, forward), forward)
    val afterFirst = provider.widestCalls
    layer.draw(createContext(layer, reversed), reversed)
    val afterReorder = provider.widestCalls
    selector = { it }
    layer.draw(createContext(layer, reversed), reversed)

    assertEquals(afterFirst + 2, afterReorder)
    assertEquals(afterReorder + 2, provider.widestCalls)
  }

  @Test
  fun `viewport changes move the columns without rebuilding the geometry`() {
    val provider = CountingProvider(listOf(RecordingColumn(4.dp), RecordingColumn(8.dp)))
    val layer =
      ColumnCartesianLayer(provider, mergeMode = { MergeMode.Grouped(stackKey = { "s" }) })
    val model = model(listOf("a", "b"), 6, 6)
    val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) }

    layer.draw(createContext(layer, model, ranges = ranges), model)
    val calls = provider.widestCalls
    val firstCenter = provider.columns[0].centers.first()
    provider.columns.forEach { it.clear() }
    layer.draw(createContext(layer, model, ranges = ranges, scroll = 50f), model)
    val scrolledCenter = provider.columns[0].centers.first()
    provider.columns.forEach { it.clear() }
    layer.draw(
      createContext(
        layer,
        model,
        ranges = ranges,
        layerBounds = Rect(30f, 0f, 230f, 100f),
        dimensions = MutableCartesianLayerDimensions(xSpacing = 100f, scalableStartPadding = 40f),
      ),
      model,
    )
    val movedCenter = provider.columns[0].centers.first()

    assertEquals(firstCenter - 50f, scrolledCenter)
    assertEquals(firstCenter + 50f, movedCenter)
    assertEquals(calls, provider.widestCalls)
  }

  @Test
  fun `per-frame work grows linearly with the series count`() {
    fun measure(seriesCount: Int): Pair<Int, Int> {
      val keys = List(seriesCount) { "s$it" }
      val provider = CountingProvider(listOf(RecordingColumn(4.dp)))
      val layer =
        ColumnCartesianLayer(
          provider,
          mergeMode = { MergeMode.Grouped(stackKey = { key -> (key as String).length }) },
        )
      val real = model(keys, *IntArray(seriesCount) { 3 })
      val keyReads = IntArray(1)
      val countingKeys = CountingList(real.seriesKeys, keyReads)
      val spy = spyk(real)
      every { spy.seriesKeys } returns countingKeys
      val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, real) }
      layer.draw(createContext(layer, real, ranges = ranges), spy)
      val coldWidestCalls = provider.widestCalls
      keyReads[0] = 0
      layer.draw(createContext(layer, real, ranges = ranges), spy)
      assertEquals(coldWidestCalls, provider.widestCalls, "warm frames make no width calls")
      return coldWidestCalls to keyReads[0]
    }

    val (coldSmall, readsSmall) = measure(32)
    val (coldLarge, readsLarge) = measure(64)

    assertEquals(32, coldSmall)
    assertEquals(64, coldLarge)
    assertTrue(readsLarge <= 2 * readsSmall + 8, "key reads: $readsSmall -> $readsLarge")
  }

  // Task 5: labels.

  @Test
  fun `stack totals are drawn after all columns, including for sparse stacks`() {
    val events = mutableListOf<String>()
    val columns = List(3) { RecordingColumn(4.dp, events) }
    val layer =
      RecordingLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        mergeMode = { MergeMode.Grouped(stackKey = sparseSelector) },
        events = events,
      )
    val model = sparseModel()

    layer.draw(createContext(layer, model), model)

    val firstLabel = events.indexOf("stackedLabel")
    assertTrue(firstLabel > events.lastIndexOf("column"), "events: $events")
    val totals = layer.labels.map { it.stack to it.topY }
    assertEquals(
      listOf("A" to 5.0, "A" to 11.0, "B" to 5.0, "B" to 7.0),
      totals.sortedWith(compareBy({ it.first }, { it.second })),
    )
    assertTrue(layer.labels.all { it.modelEntriesSize == 2 })
  }

  @Test
  fun `the stacked label hook runs without a data label component`() {
    val events = mutableListOf<String>()
    val layer =
      RecordingLayer(
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        mergeMode = { MergeMode.Grouped(stackKey = sparseSelector) },
        events = events,
      )
    val model = sparseModel()

    layer.draw(createContext(layer, model), model)

    assertEquals(4, events.count { it == "stackedLabel" })
  }

  @Test
  fun `stack labels keep positive and negative totals separate`() {
    val layer =
      RecordingLayer(
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        mergeMode = { MergeMode.Grouped(stackKey = { "s" }) },
        events = mutableListOf(),
      )
    val model =
      ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to -3), entries(0 to 4)))

    layer.draw(createContext(layer, model), model)

    val label = layer.labels.single()
    assertEquals(6.0, label.topY)
    assertEquals(-3.0, label.bottomY)
  }

  @Test
  fun `stack labels report stack width, center, and range edges`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(8.dp), RecordingColumn(6.dp))
    val layer =
      RecordingLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        mergeMode = { MergeMode.Grouped(stackKey = sparseSelector) },
        events = mutableListOf(),
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
  fun `stack labels follow animated column heights`() {
    val columns = listOf(RecordingColumn(4.dp), RecordingColumn(4.dp))
    val layer =
      RecordingLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        mergeMode = { MergeMode.Grouped(stackKey = { "s" }) },
        events = mutableListOf(),
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

    val drawnHeight = columns.sumOf { (it.bottoms.single() - it.tops.single()).toDouble() }
    val label = layer.labels.single()
    assertClose(drawnHeight.toFloat(), label.topHeight, "label height")
    assertClose(30f, label.topHeight, "animated height")
    assertEquals(5.0, label.topY)
  }

  @Test
  fun `Stacked labels keep the current column thickness`() {
    val wide = RecordingColumn(10.dp)
    val narrow = RecordingColumn(4.dp)
    val provider =
      object : ColumnCartesianLayer.ColumnProvider {
        override fun getColumn(entry: ColumnCartesianLayerModel.Entry, extraStore: ExtraStore) =
          narrow

        override fun getWidestSeriesColumn(
          seriesKey: Any,
          seriesIndex: Int,
          extraStore: ExtraStore,
        ) = wide
      }
    val layer =
      RecordingLayer(provider, mergeMode = { MergeMode.Stacked }, events = mutableListOf())
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 2), entries(0 to 3)))

    layer.draw(createContext(layer, model), model)

    assertEquals(listOf(4.dp), layer.labels.map { it.columnThickness })
  }

  @Test
  fun `data labels add no full-series pass`() {
    fun reads(entryCount: Int, labeled: Boolean, middle: Boolean): Int {
      val layer =
        ColumnCartesianLayer(
          ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
          mergeMode = { MergeMode.Grouped(stackKey = { "s" }) },
          dataLabel = if (labeled) mockk<TextComponent>(relaxed = true) else null,
        )
      val real = model(listOf("a", "b", "c", "d"), entryCount, entryCount, entryCount, entryCount)
      val ranges = MutableCartesianChartRanges().also { layer.updateChartRanges(it, real) }
      val counter = IntArray(1)
      val spy = spyk(real)
      every { spy.series } returns real.series.map { CountingList(it, counter) }
      val scroll = if (middle) 100f * entryCount / 2 else 0f
      layer.draw(createContext(layer, real, ranges = ranges, scroll = scroll), spy)
      counter[0] = 0
      layer.draw(createContext(layer, real, ranges = ranges, scroll = scroll), spy)
      return counter[0]
    }

    for (middle in listOf(false, true)) {
      val smallDifference =
        reads(2_000, labeled = true, middle) - reads(2_000, labeled = false, middle)
      val largeDifference =
        reads(20_000, labeled = true, middle) - reads(20_000, labeled = false, middle)
      assertEquals(smallDifference, largeDifference, "middle=$middle")
      assertTrue(smallDifference <= 0, "labels must not read entries: $smallDifference")
    }
    assertEquals(reads(2_000, labeled = true, false), reads(20_000, labeled = true, false))
  }

  // Task 6: marker targets.

  @Test
  fun `marker targets follow stack order for sparse interleaved stacks`() {
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        mergeMode = { MergeMode.Grouped(stackKey = sparseSelector) },
      )
    val model = sparseModel(withNullSeries = true)

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
  fun `a tapped stack stays selected after a redraw`() {
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        mergeMode = { MergeMode.Grouped(stackKey = sparseSelector) },
      )
    val model = sparseModel()
    layer.draw(createContext(layer, model), model)
    val targets = layer.columnTargets(1.0)
    val stackB = targets.first { it.seriesKeys() == listOf("B1") }

    val (narrowed, index) = narrowMarkerTargets(targets, stackB.canvasX)
    layer.draw(createContext(layer, model), model)

    assertEquals(listOf("B1"), (narrowed.single() as ColumnCartesianLayerMarkerTarget).seriesKeys())
    assertEquals(listOf("B1"), layer.columnTargets(1.0)[index!!].seriesKeys())
  }

  @Test
  fun `custom drawing can call the marker hook directly`() {
    val layer =
      CustomMarkerLayer(mergeMode = { MergeMode.Grouped(stackKey = sparseSelector) }) {
        context,
        model ->
        model.series.forEachIndexed { index, series ->
          series.forEach { entry -> mark(context, entry, model.seriesKeys[index], 40f + index) }
        }
      }
    val model = sparseModel()

    layer.draw(createContext(layer, model), model)

    assertEquals(
      listOf(listOf("A1", "A2"), listOf("B1")),
      layer.columnTargets(0.0).map { it.seriesKeys() },
    )
  }

  @Test
  fun `custom drawInternal implementations can call the marker hook directly`() {
    val layer =
      CustomMarkerLayer(
        mergeMode = { MergeMode.Grouped(stackKey = sparseSelector) },
        viaDrawInternal = true,
      ) { context, model ->
        model.series.forEachIndexed { index, series ->
          series.forEach { entry -> mark(context, entry, model.seriesKeys[index], 40f) }
        }
      }
    val model = sparseModel()

    layer.draw(createContext(layer, model), model)

    assertEquals(
      listOf(listOf("A2"), listOf("B1")),
      layer.columnTargets(1.0).map { it.seriesKeys() },
    )
  }

  @Test
  fun `the marker hook uses the canvasX it receives`() {
    val layer =
      CustomMarkerLayer(mergeMode = { MergeMode.Grouped(stackKey = { "s" }) }) { context, model ->
        mark(context, model.series[0][0], model.seriesKeys[0], 50f + 3f)
      }
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 1)))

    layer.draw(createContext(layer, model), model)

    assertEquals(53f, layer.columnTargets(0.0).single().canvasX)
  }

  @Test
  fun `calling the marker hook twice keeps both columns`() {
    val layer =
      CustomMarkerLayer(mergeMode = { MergeMode.Grouped(stackKey = { "s" }) }) { context, model ->
        mark(context, model.series[0][0], model.seriesKeys[0], 50f)
        mark(context, model.series[0][0], model.seriesKeys[0], 50f)
      }
    val model = ColumnCartesianLayerModel(listOf(entries(0 to 1)))

    layer.draw(createContext(layer, model), model)

    assertEquals(2, layer.columnTargets(0.0).single().columns.size)
  }

  @Test
  fun `the drawn model's own series order is used`() {
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        mergeMode = { MergeMode.Grouped(stackKey = { it }) },
      )
    val first =
      ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)), listOf("x", "y"))
    val second =
      ColumnCartesianLayerModel(listOf(entries(0 to 1), entries(0 to 2)), listOf("y", "x"))
    val chart = CartesianChartModel(first, second)

    layer.draw(createContext(layer, second, chartModel = chart), second)
    val secondOrder = layer.columnTargets(0.0).map { it.seriesKeys() }
    layer.draw(createContext(layer, first, chartModel = chart), first)
    val firstOrder = layer.columnTargets(0.0).map { it.seriesKeys() }

    assertEquals(listOf(listOf("y"), listOf("x")), secondOrder)
    assertEquals(listOf(listOf("x"), listOf("y")), firstOrder)
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
        mergeMode = { MergeMode.Grouped(10.dp) { key -> if (key == "c") "B" else "A" } },
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
  fun `targets from earlier frames and modes are cleared`() {
    val stackedKey = ExtraStore.Key<Boolean>()
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
        mergeMode = { extras ->
          if (extras.getOrNull(stackedKey) == true) MergeMode.Stacked
          else MergeMode.Grouped(stackKey = sparseSelector)
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

  private val sparseSelector: (Any) -> Any? = { key ->
    when (key) {
      "A1",
      "A2" -> "A"
      "B1" -> "B"
      else -> null
    }
  }

  private fun sparseModel(withNullSeries: Boolean = false): ColumnCartesianLayerModel {
    val series = mutableListOf(entries(0 to 2), entries(0 to 5, 1 to 7), entries(0 to 3, 1 to 11))
    val keys = mutableListOf<Any>("A1", "B1", "A2")
    if (withNullSeries) {
      series += entries(0 to 1, 1 to 1)
      keys += "N"
    }
    return ColumnCartesianLayerModel(series, keys)
  }

  private fun entries(vararg points: Pair<Number, Number>) = points.map { (x, y) ->
    ColumnCartesianLayerModel.Entry(x, y)
  }

  private fun model(keys: List<Any>, vararg entryCounts: Int): ColumnCartesianLayerModel =
    ColumnCartesianLayerModel(
      entryCounts.mapIndexed { seriesIndex, count ->
        List(count) { x -> ColumnCartesianLayerModel.Entry(x, seriesIndex + 1) }
      },
      keys,
    )

  private fun layer(mergeMode: (ExtraStore) -> MergeMode) =
    ColumnCartesianLayer(
      ColumnCartesianLayer.ColumnProvider.series(RecordingColumn(4.dp)),
      mergeMode = mergeMode,
    )

  private fun rangesFor(mergeMode: MergeMode, model: ColumnCartesianLayerModel) =
    MutableCartesianChartRanges().also {
      layer(mergeMode = { mergeMode }).updateChartRanges(it, model)
    }

  private data class Geometry(
    val centers: List<List<Float>>,
    val tops: List<List<Float>>,
    val bottoms: List<List<Float>>,
  )

  private fun renderGeometry(
    mergeMode: MergeMode,
    widths: List<Dp>,
    model: ColumnCartesianLayerModel,
    ranges: CartesianChartRanges,
    direction: LayoutDirection,
    zoom: Float,
  ): Geometry {
    val columns = widths.map { RecordingColumn(it) }
    val layer =
      ColumnCartesianLayer(
        ColumnCartesianLayer.ColumnProvider.series(columns),
        mergeMode = { mergeMode },
      )
    layer.draw(
      createContext(layer, model, layoutDirection = direction, zoom = zoom, ranges = ranges),
      model,
    )
    return Geometry(columns.map { it.centers }, columns.map { it.tops }, columns.map { it.bottoms })
  }

  private fun assertGeometryEquals(expected: Geometry, actual: Geometry, message: String) {
    for ((name, e, a) in
      listOf(
        Triple("centers", expected.centers, actual.centers),
        Triple("tops", expected.tops, actual.tops),
        Triple("bottoms", expected.bottoms, actual.bottoms),
      )) {
      assertEquals(e.map { it.size }, a.map { it.size }, "$message $name")
      e.flatten().zip(a.flatten()).forEach { (x, y) -> assertClose(x, y, "$message $name") }
    }
  }

  private fun assertClose(expected: Float, actual: Float, message: String) {
    assertTrue(abs(expected - actual) < 0.001f, "$message: expected $expected, was $actual")
  }

  private fun ColumnCartesianLayer.columnTargets(x: Double) =
    markerTargets.getValue(x).map { it as ColumnCartesianLayerMarkerTarget }

  private fun ColumnCartesianLayerMarkerTarget.seriesKeys() = columns.map { it.entry.seriesKey }

  private class CountingList<T>(private val delegate: List<T>, private val counter: IntArray) :
    AbstractList<T>() {
    override val size: Int
      get() = delegate.size

    override fun get(index: Int): T {
      counter[0]++
      return delegate[index]
    }
  }

  private class RecordingColumn(thickness: Dp, private val events: MutableList<String>? = null) :
    LineComponent(Fill.Black, thickness) {
    val centers = mutableListOf<Float>()
    val tops = mutableListOf<Float>()
    val bottoms = mutableListOf<Float>()

    fun clear() {
      centers.clear()
      tops.clear()
      bottoms.clear()
    }

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

  private class CountingProvider(
    val columns: List<RecordingColumn>,
    private val widthKey: ExtraStore.Key<Dp>? = null,
  ) : ColumnCartesianLayer.ColumnProvider {
    var widestCalls = 0
    var columnCalls = 0

    override fun getColumn(entry: ColumnCartesianLayerModel.Entry, extraStore: ExtraStore) =
      columns[entry.seriesIndex % columns.size].also { columnCalls++ }

    override fun getWidestSeriesColumn(
      seriesKey: Any,
      seriesIndex: Int,
      extraStore: ExtraStore,
    ): LineComponent {
      widestCalls++
      val width = widthKey?.let { extraStore.getOrNull(it) }
      return if (width != null) LineComponent(Fill.Black, width)
      else columns[seriesIndex % columns.size]
    }
  }

  private data class LabelCall(
    val stack: String?,
    val x: Double,
    val canvasX: Float,
    val modelEntriesSize: Int,
    val columnThickness: Dp,
    val topY: Double,
    val bottomY: Double,
    val topHeight: Float,
    val isFirst: Boolean,
    val isLast: Boolean,
  )

  private class RecordingLayer(
    columnProvider: ColumnProvider,
    mergeMode: (ExtraStore) -> MergeMode,
    private val events: MutableList<String>,
  ) : ColumnCartesianLayer(columnProvider, mergeMode = mergeMode) {
    val labels = mutableListOf<LabelCall>()
    val exposedDrawingModelKey: ExtraStore.Key<ColumnCartesianLayerDrawingModel>
      get() = drawingModelKey

    override fun CartesianDrawingContext.drawStackedDataLabel(
      modelEntriesSize: Int,
      columnThickness: Dp,
      stackInfo: StackInfo,
      x: Float,
      zeroLinePosition: Float,
      heightMultiplier: Float,
      isFirst: Boolean,
      isLast: Boolean,
      mergeMode: MergeMode,
    ) {
      events += "stackedLabel"
      val target =
        markerTargets.entries
          .flatMap { (targetX, targets) -> targets.map { targetX to it } }
          .firstOrNull { (_, target) -> abs(target.canvasX - x) < 0.01f }
      val stack =
        (target?.second as? ColumnCartesianLayerMarkerTarget)
          ?.columns
          ?.firstOrNull()
          ?.entry
          ?.seriesKey
          ?.let { it.toString().take(1) }
      labels +=
        LabelCall(
          stack = stack,
          x = target?.first ?: Double.NaN,
          canvasX = x,
          modelEntriesSize = modelEntriesSize,
          columnThickness = columnThickness,
          topY = stackInfo.topY,
          bottomY = stackInfo.bottomY,
          topHeight = stackInfo.topHeight,
          isFirst = isFirst,
          isLast = isLast,
        )
    }

    override fun CartesianDrawingContext.drawDataLabel(
      modelEntriesSize: Int,
      columnThickness: Dp,
      dataLabelValue: Double,
      x: Float,
      y: Float,
      isFirst: Boolean,
      isLast: Boolean,
      mergeMode: MergeMode,
    ) {
      events += "dataLabel"
    }
  }

  private class CustomMarkerLayer(
    mergeMode: (ExtraStore) -> MergeMode,
    private val viaDrawInternal: Boolean = false,
    private val action:
      CustomMarkerLayer.(CartesianDrawingContext, ColumnCartesianLayerModel) -> Unit,
  ) :
    ColumnCartesianLayer(
      ColumnProvider.series(LineComponent(Fill.Black, 4.dp)),
      mergeMode = mergeMode,
    ) {
    private val resolveMergeMode = mergeMode

    fun mark(
      context: CartesianDrawingContext,
      entry: ColumnCartesianLayerModel.Entry,
      seriesKey: Any,
      canvasX: Float,
    ) {
      with(context) {
        updateMarkerTargets(
          entry,
          seriesKey,
          canvasX,
          50f,
          10f,
          LineComponent(Fill.Black, 4.dp),
          resolveMergeMode(ExtraStore.Empty),
        )
      }
    }

    override fun CartesianDrawingContext.drawChartInternal(
      model: ColumnCartesianLayerModel,
      ranges: CartesianChartRanges,
      drawingModel: ColumnCartesianLayerDrawingModel?,
    ) {
      action(this, model)
    }

    override fun drawInternal(context: CartesianDrawingContext, model: ColumnCartesianLayerModel) {
      if (viaDrawInternal) action(context, model) else super.drawInternal(context, model)
    }
  }

  private class TrackingLayer(
    columnProvider: ColumnProvider,
    mergeMode: (ExtraStore) -> MergeMode,
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
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    zoom: Float = 1f,
    scroll: Float = 0f,
    density: Float = 1f,
    layerBounds: Rect = Rect(0f, 0f, 200f, 100f),
    dimensions: MutableCartesianLayerDimensions =
      MutableCartesianLayerDimensions(xSpacing = 100f, scalableStartPadding = 20f),
    extraStore: ExtraStore = ExtraStore.Empty,
    chartModel: CartesianChartModel = CartesianChartModel(model),
    ranges: CartesianChartRanges =
      MutableCartesianChartRanges().also { layer.updateChartRanges(it, model) },
  ): CartesianDrawingContext {
    val scaledDimensions = dimensions.copyScaled(zoom)
    val baseContext = mockk<CartesianDrawingContext>(relaxed = true)
    every { baseContext.model } returns chartModel
    every { baseContext.ranges } returns ranges
    every { baseContext.layerBounds } returns layerBounds
    every { baseContext.layerDimensions } returns scaledDimensions
    every { baseContext.scroll } returns scroll
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
