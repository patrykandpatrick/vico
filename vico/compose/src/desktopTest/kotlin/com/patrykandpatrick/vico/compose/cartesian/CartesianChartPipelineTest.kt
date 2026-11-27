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
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerDrawingModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerDrawingModel
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerDrawingModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

class CartesianChartPipelineTest {
  private val columnProvider =
    ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill(Color.Red)))
  private val otherColumnProvider =
    ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill(Color.Blue)))
  private val lineProvider =
    LineCartesianLayer.LineProvider.series(
      LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color.Green))),
    )

  private fun columns(
    vararg y: Number,
    key: Any = "columns",
    provider: ColumnCartesianLayer.ColumnProvider = columnProvider,
  ) = ColumnCartesianLayer(ColumnCartesianLayerModel.build { series(*y) }, provider, key = key)

  private fun line(vararg y: Number, key: Any = "line") =
    LineCartesianLayer(LineCartesianLayerModel.build { series(*y) }, lineProvider, key = key)

  private fun chart(vararg layers: CartesianLayer<*>, xStep: Double? = null) =
    testCartesianChart(*layers, xStep = xStep)

  private class Harness(val pipeline: CartesianChartPipeline, val clock: BroadcastFrameClock) {
    var time = 0L

    fun submit(chart: CartesianChart) {
      pipeline.apply(pipeline.prepare(chart))
    }

    suspend fun settle() {
      repeat(20) { yield() }
    }

    /** Advances the frame clock by [millis] and lets the pipeline react. */
    suspend fun frame(millis: Long = 16) {
      settle()
      time += millis * 1_000_000
      clock.sendFrame(time)
      settle()
    }

    val display: CartesianChartDisplay
      get() = assertNotNull(pipeline.display)
  }

  /** Runs tasks immediately while open, and queues them until [release] while closed. */
  private class GateDispatcher : CoroutineDispatcher() {
    var isOpen = true
    private val queue = ArrayDeque<Runnable>()

    override fun isDispatchNeeded(context: CoroutineContext) = !isOpen

    override fun dispatch(context: CoroutineContext, block: Runnable) {
      queue += block
    }

    fun release() {
      isOpen = true
      while (queue.isNotEmpty()) queue.removeFirst().run()
    }
  }

  private val gate = GateDispatcher()

  private fun test(
    animate: Boolean = true,
    initialAnimation: Boolean = true,
    skipInitialAnimation: Boolean = false,
    block: suspend Harness.() -> Unit,
  ) = runBlocking {
    val clock = BroadcastFrameClock()
    val scope = CoroutineScope(coroutineContext + clock + Job())
    val pipeline = CartesianChartPipeline(scope, backgroundContext = gate)
    // The specs differ, so that tests catch the wrong one being used.
    pipeline.animationSpec =
      if (animate) tween(durationMillis = 100, easing = LinearEasing) else null
    pipeline.initialAnimationSpec =
      if (animate && initialAnimation) tween(durationMillis = 60, easing = LinearEasing) else null
    pipeline.skipInitialAnimation = skipInitialAnimation
    try {
      Harness(pipeline, clock).block()
    } finally {
      scope.cancel()
    }
  }

  @Test
  fun `The first chart is committed synchronously`() = test {
    val chart = chart(columns(1, 2, 3))
    val commit = pipeline.prepare(chart)
    assertNotNull(commit)
    assertNull(pipeline.display)
    pipeline.apply(commit)
    assertSame(chart, display.frame.chart)
    assertEquals(2.0, display.frame.ranges.maxX)
    // The initial animation starts from the baseline.
    val drawingModel = display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel
    assertEquals(0f, drawingModel.single().getValue(2.0).height)
  }

  @Test
  fun `In previews, data updates are committed synchronously without animation`() = test {
    pipeline.isInPreview = true
    submit(chart(columns(1, 2)))
    val updated = chart(columns(1, 2, 4))
    submit(updated)
    assertSame(updated, display.frame.chart)
    val drawingModel = display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel
    assertEquals(1f, drawingModel.single().getValue(2.0).height)
  }

  @Test
  fun `A data update keeps the previous chart on screen until it’s committed as a whole`() = test {
    val initial = chart(columns(1, 2))
    submit(initial)
    val updated = chart(columns(1, 2, 3, 4))
    val commit = pipeline.prepare(updated)
    assertTrue(assertNotNull(commit).isAsync)
    pipeline.apply(commit)
    // Nothing of the new chart is visible before the derivation finishes.
    assertSame(initial, display.frame.chart)
    assertEquals(1.0, display.frame.ranges.maxX)
    settle()
    assertSame(updated, display.frame.chart)
    assertEquals(3.0, display.frame.ranges.maxX)
    assertEquals(setOf<Any>("columns"), display.drawingModels.keys)
  }

  @Test
  fun `Pending charts are conflated, and the latest one wins`() = test {
    submit(chart(columns(1)))
    submit(chart(columns(1, 2)))
    submit(chart(columns(1, 2, 3)))
    val latest = chart(columns(1, 2, 3, 4))
    submit(latest)
    settle()
    assertSame(latest, display.frame.chart)
  }

  @Test
  fun `Rapid updates don’t starve the pipeline`() = test {
    submit(chart(columns(1)))
    repeat(60) { index ->
      val chart = chart(columns(*Array(index + 2) { it }))
      submit(chart)
      frame()
      assertSame(chart, display.frame.chart)
    }
  }

  @Test
  fun `A settings-only update is committed synchronously without restarting the animation`() =
    test {
      val first = columns(1, 2)
      submit(chart(first))
      frame()
      frame(50)
      val midAnimation = display.drawingModels.getValue("columns")
      val restyled = chart(ColumnCartesianLayer(first.model, otherColumnProvider, key = "columns"))
      val commit = assertNotNull(pipeline.prepare(restyled))
      assertFalse(commit.isAsync)
      pipeline.apply(commit)
      assertSame(restyled, display.frame.chart)
      assertSame(midAnimation, display.drawingModels.getValue("columns"))
      frame(100)
      val drawingModel =
        display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel
      assertEquals(1f, drawingModel.single().getValue(1.0).height)
    }

  @Test
  fun `A settings change that alters the drawing models animates from the current state`() = test {
    val model = ColumnCartesianLayerModel.build { series(2, 4) }
    submit(chart(ColumnCartesianLayer(model, columnProvider, key = "columns")))
    frame()
    frame(200)
    val before = display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel
    val rescaled =
      chart(
        ColumnCartesianLayer(
          model,
          columnProvider,
          rangeProvider = CartesianLayerRangeProvider.fixed(maxY = 8.0),
          key = "columns",
        ),
      )
    val commit = assertNotNull(pipeline.prepare(rescaled))
    assertFalse(commit.isAsync)
    pipeline.apply(commit)
    // The new chart and ranges apply at once, and the columns start from where they were.
    assertSame(rescaled, display.frame.chart)
    assertEquals(8.0, display.frame.ranges.getYRange(null).maxY)
    assertEquals(before, display.drawingModels.getValue("columns"))
    frame()
    frame(50)
    val midAnimation = display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel
    assertEquals(0.75f, midAnimation.single().getValue(1.0).height, absoluteTolerance = 1e-3f)
    frame(100)
    val after = display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel
    assertEquals(0.5f, after.single().getValue(1.0).height, absoluteTolerance = 1e-3f)
  }

  @Test
  fun `A pure style change applies at once without starting an animation`() = test {
    val model = ColumnCartesianLayerModel.build { series(2, 4) }
    submit(chart(ColumnCartesianLayer(model, columnProvider, key = "columns")))
    frame()
    frame(200)
    val settled = display.drawingModels.getValue("columns")
    val restyled = chart(ColumnCartesianLayer(model, otherColumnProvider, key = "columns"))
    submit(restyled)
    assertSame(restyled, display.frame.chart)
    assertSame(settled, display.drawingModels.getValue("columns"))
    val displayAfterCommit = display
    frame()
    frame(50)
    // No animation runs, so no new frames are published.
    assertSame(displayAfterCommit, display)
  }

  @Test
  fun `A settings change without animations jumps to the new drawing models`() =
    test(animate = false) {
      val model = ColumnCartesianLayerModel.build { series(2, 4) }
      submit(chart(ColumnCartesianLayer(model, columnProvider, key = "columns")))
      submit(
        chart(
          ColumnCartesianLayer(
            model,
            columnProvider,
            rangeProvider = CartesianLayerRangeProvider.fixed(maxY = 8.0),
            key = "columns",
          ),
        ),
      )
      val drawingModel =
        display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel
      assertEquals(0.5f, drawingModel.single().getValue(1.0).height, absoluteTolerance = 1e-3f)
    }

  @Test
  fun `A settings-only update recomputes the ranges`() = test {
    val layer = columns(1, 2)
    submit(chart(layer))
    val withXStep = chart(layer, xStep = 0.5)
    submit(withXStep)
    assertSame(withXStep, display.frame.chart)
    assertEquals(0.5, display.frame.ranges.xStep)
  }

  @Test
  fun `An inserted layer is paired structurally and animates in`() =
    test(animate = true) {
      submit(chart(columns(2, 4), line(1, 2, key = "trend")))
      frame()
      frame(200)
      val columnsBefore = display.drawingModels.getValue("columns")
      val trendBefore = display.drawingModels.getValue("trend")
      submit(chart(columns(2, 4), line(3, 3, key = "target"), line(1, 2, key = "trend")))
      settle()
      assertEquals(listOf("columns", "target", "trend"), display.drawList.map { it.layerKey() })
      // The existing layers start from where they were…
      assertEquals(columnsBefore, display.drawingModels.getValue("columns"))
      assertEquals(trendBefore, display.drawingModels.getValue("trend"))
      // …and the new one animates in from the baseline.
      val target = display.drawingModels.getValue("target") as LineCartesianLayerDrawingModel
      assertEquals(0f, target.opacity)
    }

  @Test
  fun `A removed layer exits in place and is then dropped`() = test {
    submit(chart(line(1, 2, key = "a"), columns(5, 5, key = "b"), line(2, 1, key = "c")))
    frame()
    frame(200)
    val previousRanges = display.frame.ranges
    submit(chart(line(1, 2, key = "a"), line(2, 1, key = "c")))
    settle()
    assertEquals(listOf("a", "b", "c"), display.drawList.map { it.layerKey() })
    val exiting = display.drawList[1]
    assertSame(previousRanges, exiting.exitRanges)
    // The exiting layer contributes nothing to the new chart.
    assertEquals(2, display.frame.chart.layers.size)
    assertEquals(2.0, display.frame.ranges.getYRange(null).maxY)
    frame()
    frame(50)
    val midExit = display.drawingModels.getValue("b") as ColumnCartesianLayerDrawingModel
    // The exit mirrors the initial animation: the columns shrink and fade.
    assertEquals(0.5f, midExit.single().getValue(0.0).height, absoluteTolerance = 1e-3f)
    assertEquals(0.5f, midExit.opacity, absoluteTolerance = 1e-3f)
    frame(100)
    assertEquals(listOf("a", "c"), display.drawList.map { it.layerKey() })
    assertFalse("b" in display.drawingModels)
  }

  @Test
  fun `An update mid-exit doesn’t restart the exit or leak the exiting layer`() = test {
    submit(chart(line(1, 2, key = "a"), columns(5, 5, key = "b")))
    frame()
    frame(200)
    submit(chart(line(1, 2, key = "a")))
    settle()
    frame()
    frame(50)
    submit(chart(line(2, 3, key = "a")))
    settle()
    assertEquals(listOf("a", "b"), display.drawList.map { it.layerKey() })
    frame(60)
    assertEquals(listOf("a"), display.drawList.map { it.layerKey() })
    assertEquals(setOf<Any>("a"), display.drawingModels.keys)
  }

  @Test
  fun `A layer re-added mid-exit continues from its current state`() = test {
    submit(chart(line(1, 2, key = "a"), columns(5, 5, key = "b")))
    frame()
    frame(200)
    submit(chart(line(1, 2, key = "a")))
    settle()
    frame()
    frame(50)
    val midExit = display.drawingModels.getValue("b")
    submit(chart(line(1, 2, key = "a"), columns(5, 5, key = "b")))
    settle()
    assertEquals(listOf("a", "b"), display.drawList.map { it.layerKey() })
    assertNull(display.drawList[1].exitRanges)
    assertEquals(midExit, display.drawingModels.getValue("b"))
    frame()
    frame(200)
    val drawingModel = display.drawingModels.getValue("b") as ColumnCartesianLayerDrawingModel
    assertEquals(1f, drawingModel.single().getValue(0.0).height)
  }

  @Test
  fun `Removing the last layer animates it out and then leaves the chart empty`() = test {
    submit(chart(columns(2, 4)))
    frame()
    frame(200)
    val previousRanges = display.frame.ranges
    val empty = chart()
    submit(empty)
    settle()
    assertSame(empty, display.frame.chart)
    // The empty chart keeps the previous ranges, so the exiting layer can be drawn.
    assertSame(previousRanges, display.frame.ranges)
    assertEquals(listOf<Any>("columns"), display.drawList.map { it.layerKey() })
    assertSame(previousRanges, display.drawList.single().exitRanges)
    frame()
    frame(50)
    val midExit = display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel
    assertEquals(0.5f, midExit.opacity, absoluteTolerance = 1e-3f)
    frame(100)
    assertTrue(display.drawList.isEmpty())
    assertTrue(display.drawingModels.isEmpty())
    assertTrue(assertNotNull(pipeline.layout).drawList.isEmpty())
  }

  @Test
  fun `A chart with no layers clears the display immediately when nothing is animated`() =
    test(animate = false) {
      submit(chart(columns(1, 2)))
      val empty = chart()
      submit(empty)
      settle()
      assertSame(empty, display.frame.chart)
      assertTrue(display.drawList.isEmpty())
    }

  @Test
  fun `Re-adding data while the last layer exits continues from the current state`() = test {
    submit(chart(columns(2, 4)))
    frame()
    frame(200)
    submit(chart())
    settle()
    frame()
    frame(50)
    val midExit = display.drawingModels.getValue("columns")
    val refilled = chart(columns(2, 4))
    // This isn’t a first commit, as the exiting layer is still on screen.
    assertTrue(assertNotNull(pipeline.prepare(refilled)).isAsync)
    submit(refilled)
    settle()
    assertSame(refilled, display.frame.chart)
    assertNull(display.drawList.single().exitRanges)
    assertEquals(midExit, display.drawingModels.getValue("columns"))
  }

  @Test
  fun `The layout state changes on commits and when animations end, not with every frame`() = test {
    submit(chart(columns(1, 2)))
    val committedLayout = pipeline.layout
    frame()
    frame(50)
    assertSame(committedLayout, pipeline.layout)
    frame(100)
    val finalLayout = assertNotNull(pipeline.layout)
    assertTrue(finalLayout !== committedLayout)
    assertSame(display.drawingModels.getValue("columns"), finalLayout.drawingModels["columns"])
  }

  @Test
  fun `Exiting series keep their keys in the layout state until their exit ends`() = test {
    val twoSeries =
      ColumnCartesianLayer(
        ColumnCartesianLayerModel.build {
          series(1, 2, key = "a")
          series(3, 4, key = "b")
        },
        columnProvider,
        key = "columns",
      )
    submit(chart(twoSeries))
    frame()
    frame(200)
    val oneSeries =
      ColumnCartesianLayer(
        ColumnCartesianLayerModel.build { series(1, 2, key = "a") },
        columnProvider,
        key = "columns",
      )
    submit(chart(oneSeries))
    settle()
    fun layoutKeys() =
      (assertNotNull(pipeline.layout).drawingModels.getValue("columns")
          as ColumnCartesianLayerDrawingModel)
        .seriesKeys
    fun displayKeys() =
      (display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel).seriesKeys
    assertEquals(listOf<Any>("a", "b"), layoutKeys())
    frame()
    frame(50)
    assertEquals(layoutKeys(), displayKeys())
    frame(100)
    assertEquals(listOf<Any>("a"), layoutKeys())
    assertEquals(layoutKeys(), displayKeys())
  }

  @Test
  fun `Exit timelines are paused while frames are held for a derivation`() = test {
    submit(chart(line(1, 2, key = "a"), columns(5, 5, key = "b")))
    frame()
    frame(200)
    submit(chart(line(1, 2, key = "a")))
    settle()
    frame()
    frame(30)
    gate.isOpen = false
    submit(chart(line(2, 3, key = "a")))
    settle()
    // These frames are held, as the derivation hasn’t finished.
    frame(40)
    frame(40)
    gate.release()
    settle()
    frame(20)
    // 30 ms + 20 ms of the 100 ms exit have elapsed; the 80 ms of held frames don’t count.
    val drawingModel = display.drawingModels.getValue("b") as ColumnCartesianLayerDrawingModel
    assertEquals(0.5f, drawingModel.opacity, absoluteTolerance = 1e-3f)
  }

  @Test
  fun `Data going from non-empty to empty animates the series out, and back in`() = test {
    submit(chart(columns(2, 4)))
    frame()
    frame(200)
    val emptied =
      chart(
        ColumnCartesianLayer(ColumnCartesianLayerModel.build {}, columnProvider, key = "columns"),
      )
    assertTrue(assertNotNull(pipeline.prepare(emptied)).isAsync)
    submit(emptied)
    settle()
    assertSame(emptied, display.frame.chart)
    fun drawingModel() =
      display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel
    // The series is exiting: it’s still in the drawing model, at full height.
    assertEquals(listOf<Any>(0), drawingModel().seriesKeys)
    assertEquals(1f, drawingModel().single().getValue(1.0).height)
    frame()
    frame(50)
    assertEquals(0.5f, drawingModel().single().getValue(1.0).height, absoluteTolerance = 1e-3f)
    frame(100)
    assertTrue(drawingModel().isEmpty())
    assertEquals(1.0, display.frame.ranges.getYRange(null).maxY)
    // Back to data: this isn’t a first commit; the series enters from the baseline.
    val refilled = chart(columns(2, 4))
    assertTrue(assertNotNull(pipeline.prepare(refilled)).isAsync)
    submit(refilled)
    settle()
    assertEquals(0f, drawingModel().single().getValue(1.0).height)
    frame()
    frame(200)
    assertEquals(1f, drawingModel().single().getValue(1.0).height)
  }

  private fun Harness.columnHeight(): Float =
    (display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel)
      .single()
      .getValue(1.0)
      .height

  @Test
  fun `A restored pipeline skips only its first initial animation`() =
    test(skipInitialAnimation = true) {
      submit(chart(columns(2, 4)))
      assertEquals(1f, columnHeight())
      submit(chart())
      settle()
      frame()
      frame(200)
      assertTrue(display.drawList.isEmpty())
      // Revealing a layer again isn’t a restoration, so it animates in with the initial spec.
      submit(chart(columns(2, 4)))
      assertEquals(0f, columnHeight())
      frame()
      frame(30)
      assertEquals(0.5f, columnHeight(), absoluteTolerance = 1e-3f)
    }

  @Test
  fun `The animation spec is chosen together with the transitions`() =
    test(initialAnimation = false) {
      submit(chart(columns(2, 4)))
      submit(chart())
      settle()
      frame()
      frame(90)
      // The final exit frame is computed while the next chart is derived.
      gate.isOpen = false
      frame(20)
      val refilled = chart(columns(2, 4))
      submit(refilled)
      settle()
      gate.release()
      settle()
      assertSame(refilled, display.frame.chart)
      // A reveal without an initial animation shows the final state, not the first frame of a
      // transition that will never run.
      assertEquals(1f, columnHeight())
      frame(16)
      assertEquals(1f, columnHeight())
    }

  private fun Harness.assertConsistent() {
    display.drawList.forEach { item ->
      assertTrue(
        item.layerKey() in display.drawingModels,
        "No drawing model for ${item.layerKey()}.",
      )
    }
  }

  @Test
  fun `An unanimated update mid-exit drops the exiting layer`() = test {
    submit(chart(line(1, 2, key = "a"), columns(5, 5, key = "b")))
    frame()
    frame(200)
    submit(chart(line(1, 2, key = "a")))
    settle()
    frame()
    frame(50)
    assertEquals(listOf("a", "b"), display.drawList.map { it.layerKey() })
    pipeline.animationSpec = null
    submit(chart(line(2, 3, key = "a")))
    settle()
    assertEquals(listOf("a"), display.drawList.map { it.layerKey() })
    assertConsistent()
    frame(20)
    assertEquals(listOf("a"), display.drawList.map { it.layerKey() })
  }

  @Test
  fun `An unanimated settings change mid-exit drops the exiting layer`() = test {
    val a = line(1, 2, key = "a")
    submit(chart(a, columns(5, 5, key = "b")))
    frame()
    frame(200)
    submit(chart(a))
    settle()
    frame()
    frame(50)
    pipeline.animationSpec = null
    submit(chart(a, xStep = 0.5))
    assertEquals(listOf("a"), display.drawList.map { it.layerKey() })
    assertConsistent()
  }

  @Test
  fun `An animated settings change mid-exit keeps the exit running`() = test {
    val a = line(1, 2, key = "a")
    submit(chart(a, columns(5, 5, key = "b")))
    frame()
    frame(200)
    submit(chart(a))
    settle()
    frame()
    frame(50)
    submit(chart(a, xStep = 0.5))
    assertEquals(listOf("a", "b"), display.drawList.map { it.layerKey() })
    assertConsistent()
    frame(60)
    assertEquals(listOf("a"), display.drawList.map { it.layerKey() })
  }

  @Test
  fun `A commit prepared from a display that has since changed is prepared again`() = test {
    val a = columns(2, 4)
    submit(chart(a))
    frame()
    frame(200)
    // Prepared while `a` is on screen: a settings-only update relative to it.
    val restyled = chart(ColumnCartesianLayer(a.model, otherColumnProvider, key = "columns"))
    val staleCommit = assertNotNull(pipeline.prepare(restyled))
    // Meanwhile, a data update lands asynchronously.
    submit(chart(columns(4, 2)))
    settle()
    frame()
    frame(50)
    pipeline.apply(staleCommit)
    settle()
    frame()
    frame(200)
    // The latest chart wins with its own data, rather than the stale derivation leaving the
    // animation heading for the intermediate chart’s data.
    assertSame(restyled, display.frame.chart)
    val drawingModel = display.drawingModels.getValue("columns") as ColumnCartesianLayerDrawingModel
    assertEquals(0.5f, drawingModel.single().getValue(0.0).height, absoluteTolerance = 1e-3f)
    assertEquals(1f, drawingModel.single().getValue(1.0).height, absoluteTolerance = 1e-3f)
  }

  @Test
  fun `Preparing doesn’t change the settings, but applying does`() = test {
    val settings = CartesianChartPipeline.Settings(null, null, isInPreview = true)
    val commit = pipeline.prepare(chart(columns(1, 2)), settings)
    assertTrue(pipeline.animationSpec != null)
    assertFalse(pipeline.isInPreview)
    pipeline.apply(commit, settings)
    assertNull(pipeline.animationSpec)
    assertTrue(pipeline.isInPreview)
  }

  @Test
  fun `A pure restyle of a large chart creates no drawing models and reuses the ranges`() = test {
    val model = ColumnCartesianLayerModel.build { repeat(5) { series(List(2_000) { it % 37 }) } }
    submit(chart(ColumnCartesianLayer(model, columnProvider, key = "columns")))
    frame()
    frame(200)
    val ranges = display.frame.ranges
    val drawingModel = display.frame.drawingModels.getValue("columns")
    submit(chart(ColumnCartesianLayer(model, otherColumnProvider, key = "columns")))
    assertSame(ranges, display.frame.ranges)
    assertSame(drawingModel, display.frame.drawingModels.getValue("columns"))
  }

  @Test
  fun `A restyle that changes a derivation input still re-derives`() = test {
    val model = ColumnCartesianLayerModel.build { series(2, 4) }
    submit(chart(ColumnCartesianLayer(model, columnProvider, key = "columns")))
    val drawingModel = display.frame.drawingModels.getValue("columns")
    submit(
      chart(
        ColumnCartesianLayer(
          model,
          otherColumnProvider,
          stackKeyProvider = ColumnCartesianLayer.StackKeyProvider.Stacked,
          key = "columns",
        ),
      ),
    )
    assertTrue(drawingModel !== display.frame.drawingModels.getValue("columns"))
  }

  @Test
  fun `Frames from a custom interpolator get the series sources attached`() {
    val oldLayer = line(1, 2, key = "line")
    val newLayer =
      LineCartesianLayer(
        LineCartesianLayerModel.build { series(1, 2, key = 1) },
        lineProvider,
        key = "line",
      )
    val ranges = CartesianChartPipeline.getRanges(testCartesianChart(oldLayer))
    val old = oldLayer.createDrawingModel(ranges)
    val new = newLayer.createDrawingModel(ranges)
    // A public-API frame with the new series and the removed one (key 0).
    val frame = LineCartesianLayerDrawingModel(new.toList() + old.toList(), listOf<Any>(1, 0))
    val transition =
      with(CartesianChartPipeline) {
        CartesianLayerDrawingModelInterpolator.Transition<Any> { frame }.withSeriesSources(old, new)
      }
    val attached = transition.interpolate(0.5f) as LineCartesianLayerDrawingModel
    assertSame(newLayer, attached.getSeriesSource(0))
    assertSame(oldLayer, attached.getSeriesSource(1))
    assertEquals(frame, attached)
  }

  @Test
  fun `Frames that already carry series sources are passed through`() {
    val layer = line(1, 2, key = "line")
    val ranges = CartesianChartPipeline.getRanges(testCartesianChart(layer))
    val drawingModel = layer.createDrawingModel(ranges)
    val transition =
      with(CartesianChartPipeline) {
        CartesianLayerDrawingModelInterpolator.Transition<Any> { drawingModel }
          .withSeriesSources(drawingModel, drawingModel)
      }
    assertSame(drawingModel, transition.interpolate(0.5f))
    // Drawing models of other types are passed through, too.
    val custom = listOf(0.5f)
    val customTransition =
      with(CartesianChartPipeline) {
        CartesianLayerDrawingModelInterpolator.Transition<Any> { custom }
          .withSeriesSources(null, custom)
      }
    assertSame(custom, customTransition.interpolate(0.5f))
    // So are user-defined subclasses of `CartesianLayerDrawingModel`.
    class CustomEntry : CartesianLayerDrawingModel.Entry {
      override fun equals(other: Any?) = this === other

      override fun hashCode() = 0
    }

    class CustomDrawingModel(entries: List<Map<Double, CustomEntry>>, keys: List<Any>) :
      CartesianLayerDrawingModel<CustomEntry>(entries, keys) {
      override fun equals(other: Any?) = this === other

      override fun hashCode() = 0
    }
    val subclassed = CustomDrawingModel(listOf(emptyMap()), listOf(0))
    val subclassTransition =
      with(CartesianChartPipeline) {
        CartesianLayerDrawingModelInterpolator.Transition<Any> { subclassed }
          .withSeriesSources(subclassed, subclassed)
      }
    assertSame(subclassed, subclassTransition.interpolate(0.5f))
  }

  private fun CartesianLayerDrawItem.layerKey(): Any =
    when (val layer = layer) {
      is ColumnCartesianLayer -> layer.key
      is LineCartesianLayer -> layer.key
      else -> error("Unexpected layer.")
    }
}
