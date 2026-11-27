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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Exercises [CartesianChartHost] through a real composition, layout, and draw. */
class CartesianChartHostTest {
  private val blue = Color(0xFF0000FF)
  private val magenta = Color(0xFFFF00FF)
  private val linearSpec: AnimationSpec<Float> = tween(durationMillis = 400, easing = LinearEasing)
  private val tag = "chart"

  private fun ComposeUiTest.setChart(
    animationSpec: AnimationSpec<Float>? = linearSpec,
    initialAnimationSpec: AnimationSpec<Float>? = null,
    chart: @Composable () -> CartesianChart,
  ) {
    setContent {
      CompositionLocalProvider(
        LocalCartesianChartBackgroundContext provides EmptyCoroutineContext
      ) {
        CartesianChartHost(
          chart(),
          Modifier.size(300.dp, 200.dp).background(Color.White).testTag(tag),
          animationSpec = animationSpec,
          initialAnimationSpec = initialAnimationSpec,
        )
      }
    }
  }

  /** Counts the pixels that are mostly [color] (possibly faded over white). */
  private fun ComposeUiTest.countPixels(color: Color): Int {
    val pixels = onNodeWithTag(tag).captureToImage().toPixelMap()
    var count = 0
    for (x in 0..<pixels.width) {
      for (y in 0..<pixels.height) {
        val pixel = pixels[x, y]
        val matches =
          if (color == blue) {
            pixel.blue > 0.8f && pixel.red < 0.6f && pixel.green < 0.6f
          } else {
            pixel.red > 0.8f && pixel.blue > 0.8f && pixel.green < 0.6f
          }
        if (matches) count++
      }
    }
    return count
  }

  @Composable
  private fun columns(model: ColumnCartesianLayerModel) =
    rememberColumnCartesianLayer(
      model,
      columnProvider = ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill(blue), 16.dp)),
    )

  @Composable
  private fun line(
    model: LineCartesianLayerModel,
    verticalAxisPosition: Axis.Position.Vertical? = null,
    colors: List<Color> = listOf(magenta),
  ) =
    rememberLineCartesianLayer(
      model,
      lineProvider =
        LineCartesianLayer.LineProvider.series(
          colors.map { color ->
            LineCartesianLayer.rememberLine(
              LineCartesianLayer.LineFill.single(Fill(color)),
              LineCartesianLayer.LineStroke.Continuous(8.dp),
            )
          }
        ),
      verticalAxisPosition = verticalAxisPosition,
    )

  @Test
  fun `The first frame draws a non-empty chart`() = runComposeUiTest {
    mainClock.autoAdvance = false
    setChart {
      rememberCartesianChart(columns(ColumnCartesianLayerModel.build { series(2, 4, 3) }))
    }
    assertTrue(countPixels(blue) > 0)
  }

  @Test
  fun `An exiting line series is drawn during the transition`() = runComposeUiTest {
    mainClock.autoAdvance = false
    var showSecond by mutableStateOf(true)
    val withSecond = LineCartesianLayerModel.build {
      series(1, 1, 1, key = "first")
      series(4, 3, 4, key = "second")
    }
    val withoutSecond = LineCartesianLayerModel.build { series(1, 1, 1, key = "first") }
    setChart {
      rememberCartesianChart(
        line(if (showSecond) withSecond else withoutSecond, colors = listOf(blue, magenta))
      )
    }
    assertTrue(countPixels(magenta) > 0)
    showSecond = false
    mainClock.advanceTimeBy(100)
    assertTrue(countPixels(magenta) > 0, "The exiting series should still be drawn.")
    mainClock.advanceTimeBy(1_000)
    assertEquals(0, countPixels(magenta))
    assertTrue(countPixels(blue) > 0)
  }

  @Test
  fun `A layer that was the only user of its vertical axis position exits`() = runComposeUiTest {
    mainClock.autoAdvance = false
    var showLine by mutableStateOf(true)
    val columnModel = ColumnCartesianLayerModel.build { series(1, 2, 1) }
    val lineModel = LineCartesianLayerModel.build { series(500, 800, 600) }
    setChart {
      rememberCartesianChart(
        columns(columnModel),
        if (showLine) line(lineModel, Axis.Position.Vertical.End) else null,
      )
    }
    val before = countPixels(magenta)
    assertTrue(before > 0)
    showLine = false
    mainClock.advanceTimeBy(100)
    assertTrue(countPixels(magenta) > 0, "The exiting layer should still be drawn.")
    mainClock.advanceTimeBy(1_000)
    assertEquals(0, countPixels(magenta))
    assertTrue(countPixels(blue) > 0)
  }

  @Test
  fun `Removing the last layer animates it out`() = runComposeUiTest {
    mainClock.autoAdvance = false
    var showColumns by mutableStateOf(true)
    val model = ColumnCartesianLayerModel.build { series(2, 4, 3) }
    setChart { rememberCartesianChart(if (showColumns) columns(model) else null) }
    val before = countPixels(blue)
    assertTrue(before > 0)
    showColumns = false
    mainClock.advanceTimeBy(150)
    val during = countPixels(blue)
    assertTrue(during in 1..<before, "Expected a partial exit, got $during of $before.")
    mainClock.advanceTimeBy(1_000)
    assertEquals(0, countPixels(blue))
  }

  /** Pinches out on a chart with a marker. If [update] is `true`, the data changes mid-gesture. */
  private fun pinch(update: Boolean): Float {
    var zoom = 0f
    runComposeUiTest {
      var data by
        mutableStateOf(ColumnCartesianLayerModel.build { series(List(40) { it % 5 + 1 }) })
      lateinit var zoomState: VicoZoomState
      val marker = object : CartesianMarker {}
      setContent {
        CompositionLocalProvider(
          LocalCartesianChartBackgroundContext provides EmptyCoroutineContext
        ) {
          val scrollState = rememberVicoScrollState()
          zoomState = rememberVicoZoomState()
          CartesianChartHost(
            rememberCartesianChart(columns(data), marker = marker),
            Modifier.size(300.dp, 200.dp).testTag(tag),
            scrollState = scrollState,
            zoomState = zoomState,
            animationSpec = null,
            initialAnimationSpec = null,
          )
        }
      }
      waitForIdle()
      fun spread(distance: Float) {
        onNodeWithTag(tag).performTouchInput {
          updatePointerTo(0, Offset(150f, 100f - distance))
          updatePointerTo(1, Offset(150f, 100f + distance))
          move()
        }
      }
      onNodeWithTag(tag).performTouchInput {
        down(0, Offset(150f, 90f))
        down(1, Offset(150f, 110f))
      }
      spread(20f)
      spread(30f)
      if (update) {
        data = ColumnCartesianLayerModel.build { series(List(40) { it % 4 + 2 }) }
        waitForIdle()
      }
      spread(45f)
      spread(60f)
      onNodeWithTag(tag).performTouchInput {
        up(0)
        up(1)
      }
      waitForIdle()
      zoom = zoomState.value
    }
    return zoom
  }

  @Test
  fun `A zoom gesture survives a data update mid-gesture`() {
    val control = pinch(update = false)
    val updated = pinch(update = true)
    assertTrue(control > 1.5f, "The pinch should zoom in (got $control).")
    assertEquals(control, updated, absoluteTolerance = 1e-3f)
  }

  @Composable
  private fun axesChart(
    vararg layers: com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer<*>?
  ) =
    rememberCartesianChart(
      *layers,
      startAxis = VerticalAxis.rememberStart(line = rememberAxisLineComponent(Fill(magenta), 4.dp)),
      bottomAxis =
        HorizontalAxis.rememberBottom(line = rememberAxisLineComponent(Fill(magenta), 4.dp)),
    )

  @Test
  fun `A chart whose layers are all empty draws its axes`() = runComposeUiTest {
    mainClock.autoAdvance = false
    setChart {
      axesChart(
        columns(ColumnCartesianLayerModel.build {}),
        line(LineCartesianLayerModel.build {}, colors = listOf(blue)),
      )
    }
    assertTrue(countPixels(magenta) > 0)
    assertEquals(0, countPixels(blue))
  }

  @Test
  fun `Empty and non-empty layers can be mixed`() = runComposeUiTest {
    mainClock.autoAdvance = false
    setChart {
      rememberCartesianChart(
        columns(ColumnCartesianLayerModel.build { series(2, 4, 3) }),
        line(LineCartesianLayerModel.build {}),
      )
    }
    assertTrue(countPixels(blue) > 0)
    assertEquals(0, countPixels(magenta))
  }

  @Test
  fun `Data can go from non-empty to empty and back`() = runComposeUiTest {
    mainClock.autoAdvance = false
    var model by mutableStateOf(ColumnCartesianLayerModel.build { series(2, 4, 3) })
    setChart { axesChart(columns(model)) }
    val before = countPixels(blue)
    assertTrue(before > 0)
    model = ColumnCartesianLayerModel.build {}
    mainClock.advanceTimeBy(150)
    assertTrue(countPixels(blue) in 1..<before, "The series should be exiting.")
    mainClock.advanceTimeBy(1_000)
    assertEquals(0, countPixels(blue))
    assertTrue(countPixels(magenta) > 0, "The axes should remain.")
    model = ColumnCartesianLayerModel.build { series(2, 4, 3) }
    mainClock.advanceTimeBy(150)
    assertTrue(countPixels(blue) in 1..<before, "The series should be entering.")
    mainClock.advanceTimeBy(1_000)
    assertTrue(countPixels(blue) > 0)
  }

  @Test
  fun `Scrolling and zooming an empty chart doesn’t throw`() = runComposeUiTest {
    lateinit var scrollState: VicoScrollState
    lateinit var zoomState: VicoZoomState
    setContent {
      CompositionLocalProvider(
        LocalCartesianChartBackgroundContext provides EmptyCoroutineContext
      ) {
        scrollState = rememberVicoScrollState()
        zoomState = rememberVicoZoomState()
        CartesianChartHost(
          axesChart(columns(ColumnCartesianLayerModel.build {})),
          Modifier.size(300.dp, 200.dp).testTag(tag),
          scrollState = scrollState,
          zoomState = zoomState,
        )
      }
    }
    waitForIdle()
    onNodeWithTag(tag).performTouchInput {
      down(0, Offset(150f, 90f))
      down(1, Offset(150f, 110f))
      updatePointerTo(0, Offset(150f, 50f))
      updatePointerTo(1, Offset(150f, 150f))
      move()
      up(0)
      up(1)
    }
    onNodeWithTag(tag).performTouchInput { swipeLeft() }
    waitForIdle()
    assertTrue(scrollState.value.isFinite() && zoomState.value.isFinite())
    assertEquals(0f, scrollState.value)
  }

  private class RecordingMarker : CartesianMarker {
    var drawnTargets: List<CartesianMarker.Target>? = null

    override fun drawOverLayers(
      context: CartesianDrawingContext,
      targets: List<CartesianMarker.Target>,
    ) {
      drawnTargets = targets
    }
  }

  @Test
  fun `A held marker survives data updates`() = runComposeUiTest {
    mainClock.autoAdvance = false
    val marker = RecordingMarker()
    var model by mutableStateOf(ColumnCartesianLayerModel.build { series(2, 4, 3) })
    setChart { rememberCartesianChart(columns(model), marker = marker) }
    mainClock.advanceTimeByFrame()
    onNodeWithTag(tag).performTouchInput { down(Offset(150f, 100f)) }
    mainClock.advanceTimeByFrame()
    assertTrue(marker.drawnTargets?.isNotEmpty() == true, "The marker should be shown.")
    repeat(3) { index ->
      model = ColumnCartesianLayerModel.build { series(3 + index, 4, 3) }
      marker.drawnTargets = null
      mainClock.advanceTimeBy(100)
      assertTrue(marker.drawnTargets?.isNotEmpty() == true, "The marker should still be shown.")
    }
    onNodeWithTag(tag).performTouchInput { up() }
    marker.drawnTargets = null
    mainClock.advanceTimeByFrame()
    assertEquals(null, marker.drawnTargets)
  }

  @Test
  fun `A selected column stays selected when a series is inserted before it`() = runComposeUiTest {
    mainClock.autoAdvance = false
    val marker = RecordingMarker()
    fun model(vararg keys: String) = ColumnCartesianLayerModel.build {
      keys.forEach { key -> series(2, 4, 3, key = key) }
    }
    var columnModel by mutableStateOf(model("a", "b"))
    setChart(animationSpec = null) {
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnModel,
          columnProvider =
            ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill(blue), 16.dp)),
        ),
        marker = marker,
        // An x-locked controller, so the selection isn’t re-resolved from the pointer position.
        markerController =
          com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
            .rememberToggleOnTap(),
      )
    }
    mainClock.advanceTimeByFrame()
    onNodeWithTag(tag).performTouchInput { click(Offset(160f, 150f)) }
    mainClock.advanceTimeByFrame()
    fun markedSeriesKeys() =
      marker.drawnTargets?.map { target ->
        (target
            as com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget)
          .columns
          .single()
          .entry
          .seriesKey
      }
    val selected = markedSeriesKeys()?.singleOrNull()
    assertTrue(selected != null, "A single column should be marked; got ${markedSeriesKeys()}.")
    columnModel = model("new", "a", "b")
    mainClock.advanceTimeBy(100)
    assertEquals(listOf<Any>(selected), markedSeriesKeys())
    columnModel = model(*listOf("new", "a", "b").filter { it != selected }.toTypedArray())
    marker.drawnTargets = null
    mainClock.advanceTimeBy(100)
    // The selected series is gone, so the marker hides rather than jumping to another series.
    assertEquals(null, marker.drawnTargets)
  }

  /** Runs tasks immediately while open, and queues them until [release] while closed. */
  private class GateDispatcher : kotlinx.coroutines.CoroutineDispatcher() {
    var isOpen = true
    private val queue = ArrayDeque<kotlinx.coroutines.Runnable>()

    override fun isDispatchNeeded(context: kotlin.coroutines.CoroutineContext) = !isOpen

    override fun dispatch(
      context: kotlin.coroutines.CoroutineContext,
      block: kotlinx.coroutines.Runnable,
    ) {
      queue += block
    }

    fun release() {
      isOpen = true
      while (queue.isNotEmpty()) queue.removeFirst().run()
    }
  }

  @Test
  fun `Disposing the host mid-derivation and mid-animation is safe`() = runComposeUiTest {
    mainClock.autoAdvance = false
    val gate = GateDispatcher()
    var showHost by mutableStateOf(true)
    var model by mutableStateOf(ColumnCartesianLayerModel.build { series(2, 4, 3) })
    setContent {
      CompositionLocalProvider(LocalCartesianChartBackgroundContext provides gate) {
        if (showHost) {
          CartesianChartHost(
            rememberCartesianChart(columns(model)),
            Modifier.size(300.dp, 200.dp).testTag(tag),
            animationSpec = linearSpec,
            initialAnimationSpec = linearSpec,
          )
        }
      }
    }
    // Mid-animation: the initial animation is running.
    mainClock.advanceTimeBy(100)
    // Mid-derivation: a data update waits on the closed gate.
    gate.isOpen = false
    model = ColumnCartesianLayerModel.build { series(5, 1, 3) }
    mainClock.advanceTimeBy(50)
    showHost = false
    mainClock.advanceTimeBy(50)
    gate.release()
    mainClock.advanceTimeBy(1_000)
    waitForIdle()
  }
}
