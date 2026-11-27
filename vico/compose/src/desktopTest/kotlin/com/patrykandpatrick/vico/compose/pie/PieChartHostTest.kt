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

package com.patrykandpatrick.vico.compose.pie

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PieChartHostTest {
  private val tag = "pie"
  private val spec = tween<Float>(durationMillis = 400, easing = LinearEasing)

  private fun ComposeUiTest.coloredPixels(): Int {
    val pixels = onNodeWithTag(tag).captureToImage().toPixelMap()
    var count = 0
    for (x in 0..<pixels.width) {
      for (y in 0..<pixels.height) {
        val pixel = pixels[x, y]
        if (pixel.green < 0.5f && (pixel.red > 0.8f || pixel.blue > 0.8f)) count++
      }
    }
    return count
  }

  /**
   * Shows [first], switches to [second] after [switchAfterMillis], and returns the final pixels.
   */
  private fun finalPixels(
    first: PieChartModel,
    second: PieChartModel,
    switchAfterMillis: Long,
  ): Int {
    var result = 0
    runComposeUiTest {
      mainClock.autoAdvance = false
      var model by mutableStateOf(first)
      setContent {
        PieChartHost(
          rememberPieChart(
            model,
            PieChart.SliceProvider.series(
              PieChart.Slice(Fill(Color.Red)),
              PieChart.Slice(Fill(Color.Blue)),
            ),
          ),
          Modifier.size(200.dp).background(Color.White).testTag(tag),
          animationSpec = spec,
          initialAnimationSpec = spec,
        )
      }
      mainClock.advanceTimeBy(switchAfterMillis)
      model = second
      mainClock.advanceTimeBy(2_000)
      result = coloredPixels()
    }
    return result
  }

  @Test
  fun `A data update with a structurally equal target mid-animation completes the animation`() {
    val control = finalPixels(PieChartModel.build(1, 2), PieChartModel.build(1, 2), 2_000)
    // [2, 4] has the same proportions as [1, 2], so its drawing model is equal to the current
    // target.
    val updated = finalPixels(PieChartModel.build(1, 2), PieChartModel.build(2, 4), 100)
    assertTrue(control > 0)
    assertEquals(control, updated)
  }

  @Test
  fun `A data update mid-animation completes the animation`() {
    val control = finalPixels(PieChartModel.build(1, 3), PieChartModel.build(1, 3), 2_000)
    val updated = finalPixels(PieChartModel.build(1, 2), PieChartModel.build(1, 3), 100)
    assertEquals(control, updated)
  }

  private fun ComposeUiTest.countPixels(predicate: (Color) -> Boolean): Int {
    val pixels = onNodeWithTag(tag).captureToImage().toPixelMap()
    var count = 0
    for (x in 0..<pixels.width) for (y in 0..<pixels.height) if (predicate(pixels[x, y])) count++
    return count
  }

  private val isGreen = { pixel: Color ->
    pixel.green > pixel.red + 0.15f && pixel.green > pixel.blue + 0.15f
  }
  private val isColored = { pixel: Color ->
    pixel.red < 0.9f || pixel.green < 0.9f || pixel.blue < 0.9f
  }

  private fun ComposeUiTest.setKeyedPie(model: () -> PieChartModel) {
    mainClock.autoAdvance = false
    setContent {
      val currentModel = model()
      // This provider knows only the current keys, so an exiting slice must not be styled via it.
      val sliceProvider =
        androidx.compose.runtime.remember(currentModel) {
          val styles =
            mapOf<Any, PieChart.Slice>(
              "a" to PieChart.Slice(Fill(Color.Red)),
              "b" to PieChart.Slice(Fill(Color.Green)),
              "c" to PieChart.Slice(Fill(Color.Blue)),
            )
          val keys = currentModel.entries.map { it.key }.toSet()
          PieChart.SliceProvider { entry, _ ->
            check(entry.key in keys) { "Asked about `${entry.key}`, which isn’t in the model." }
            styles.getValue(entry.key)
          }
        }
      PieChartHost(
        rememberPieChart(currentModel, sliceProvider),
        Modifier.size(200.dp).background(Color.White).testTag(tag),
        animationSpec = spec,
        initialAnimationSpec = null,
      )
    }
  }

  private fun keyed(vararg keys: String) = PieChartModel.build(keys.associateWith { 1 })

  @Test
  fun `A removed middle slice is drawn while it exits and is gone afterward`() = runComposeUiTest {
    var model by mutableStateOf(keyed("a", "b", "c"))
    setKeyedPie { model }
    val before = countPixels(isGreen)
    assertTrue(before > 0)
    model = keyed("a", "c")
    mainClock.advanceTimeBy(200)
    val during = countPixels(isGreen)
    assertTrue(during in 1..<before, "Expected a partial exit; got $during of $before.")
    mainClock.advanceTimeBy(1_000)
    assertEquals(0, countPixels(isGreen))
  }

  @Test
  fun `An update mid-exit doesn’t leak the exiting slice, and re-adding it doesn’t flicker`() =
    runComposeUiTest {
      var model by mutableStateOf(keyed("a", "b", "c"))
      setKeyedPie { model }
      model = keyed("a", "c")
      mainClock.advanceTimeBy(150)
      model = PieChartModel.build(mapOf("a" to 2, "c" to 1))
      mainClock.advanceTimeBy(100)
      assertTrue(countPixels(isGreen) > 0, "The slice should still be exiting.")
      mainClock.advanceTimeBy(1_000)
      assertEquals(0, countPixels(isGreen))
      model = keyed("a", "b", "c")
      mainClock.advanceTimeBy(150)
      val entering = countPixels(isGreen)
      model = keyed("a", "c")
      mainClock.advanceTimeByFrame()
      // Removing the slice again mid-entry continues from where it is.
      val afterRemoval = countPixels(isGreen)
      assertTrue(
        entering > 0 && afterRemoval > 0 && afterRemoval <= entering + entering / 5,
        "$entering, $afterRemoval",
      )
    }

  @Test
  fun `Going to an empty model animates every slice out`() = runComposeUiTest {
    var model by mutableStateOf(keyed("a", "b", "c"))
    setKeyedPie { model }
    val before = countPixels(isColored)
    model = PieChartModel.build(emptyMap<Any, Number>())
    mainClock.advanceTimeBy(200)
    val during = countPixels(isColored)
    assertTrue(during in 1..<before, "Expected a partial exit; got $during of $before.")
    mainClock.advanceTimeBy(1_000)
    assertEquals(0, countPixels(isColored))
  }

  private class LabelledPie {
    var chart: PieChart? = null
    var layoutFormatCalls = 0
  }

  /**
   * Shows a pie whose slices have outside labels, with an unconstrained height. A value of 9 gets a
   * three-line label, so its slice needs the most room.
   */
  private fun ComposeUiTest.setLabelledPie(
    state: LabelledPie,
    animationSpec: androidx.compose.animation.core.AnimationSpec<Float> = spec,
    model: () -> PieChartModel,
  ) {
    mainClock.autoAdvance = false
    val formatter =
      com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter { context, value, _ ->
        if (context !is PieChartDrawingContext) state.layoutFormatCalls++
        if (value == 9f) "nine\nnine\nnine" else value.toString()
      }
    val label =
      PieChart.SliceLabel.Outside(
        com.patrykandpatrick.vico.compose.common.component.TextComponent(lineCount = 3)
      )
    val sliceProvider =
      PieChart.SliceProvider.series(
        PieChart.Slice(Fill(Color.Red), label = label),
        PieChart.Slice(Fill(Color.Blue), label = label),
      )
    setContent {
      val chart = rememberPieChart(model(), sliceProvider, valueFormatter = formatter)
      state.chart = chart
      PieChartHost(
        chart,
        Modifier.width(200.dp).background(Color.White).testTag(tag),
        animationSpec = animationSpec,
        initialAnimationSpec = null,
      )
    }
  }

  private fun ComposeUiTest.hostHeight() = onNodeWithTag(tag).fetchSemanticsNode().size.height

  private fun LabelledPie.radius() = checkNotNull(chart).gestureGeometry.outerRadius

  @Test
  fun `Animations cause no layout passes, as the layout changes only on commit and animation end`() =
    runComposeUiTest {
      val state = LabelledPie()
      var model by mutableStateOf(PieChartModel.build(mapOf("a" to 1)))
      setLabelledPie(state) { model }
      mainClock.advanceTimeByFrame()
      model = PieChartModel.build(mapOf("a" to 1, "b" to 2))
      mainClock.advanceTimeByFrame()
      val afterCommit = state.layoutFormatCalls
      repeat(10) { mainClock.advanceTimeBy(20) }
      assertEquals(afterCommit, state.layoutFormatCalls, "The layout ran during the animation.")
    }

  @Test
  fun `An entering labelled slice’s room is reserved from its first frame`() = runComposeUiTest {
    val state = LabelledPie()
    var model by mutableStateOf(PieChartModel.build(mapOf("a" to 1)))
    setLabelledPie(state) { model }
    mainClock.advanceTimeByFrame()
    model = PieChartModel.build(mapOf("a" to 1, "b" to 9))
    mainClock.advanceTimeByFrame()
    val firstRadius = state.radius()
    val firstHeight = hostHeight()
    mainClock.advanceTimeBy(1_000)
    assertEquals(firstRadius, state.radius())
    assertEquals(firstHeight, hostHeight())
  }

  @Test
  fun `An exiting labelled slice holds its room until it’s dropped`() = runComposeUiTest {
    val state = LabelledPie()
    var model by mutableStateOf(PieChartModel.build(mapOf("a" to 1, "b" to 9)))
    setLabelledPie(state) { model }
    mainClock.advanceTimeByFrame()
    val radius = state.radius()
    val height = hostHeight()
    model = PieChartModel.build(mapOf("a" to 1))
    mainClock.advanceTimeByFrame()
    assertEquals(height, hostHeight())
    mainClock.advanceTimeBy(200)
    assertEquals(height, hostHeight(), "The room should be held mid-exit.")
    mainClock.advanceTimeBy(1_000)
    // The three-line label is gone, so the room is released in one step.
    assertTrue(hostHeight() < height, "The room should be released after the exit.")
    assertTrue(radius > 0f)
  }

  @Test
  fun `An animation is finalized even if its last frame isn’t exactly 1`() = runComposeUiTest {
    val state = LabelledPie()
    // An easing that never quite reaches 1.
    val shortSpec = tween<Float>(durationMillis = 400, easing = { if (it >= 1f) 0.999f else it })
    var model by mutableStateOf(PieChartModel.build(mapOf("a" to 1, "b" to 9)))
    setLabelledPie(state, shortSpec) { model }
    mainClock.advanceTimeByFrame()
    val height = hostHeight()
    model = PieChartModel.build(mapOf("a" to 1))
    mainClock.advanceTimeBy(2_000)
    // The exited slice is dropped, so its room is released.
    assertTrue(hostHeight() < height, "The exited slice is still in the layout.")
  }

  @Test
  fun `A pie that starts empty reveals its first data with the initial spec`() = runComposeUiTest {
    mainClock.autoAdvance = false
    var model by mutableStateOf(PieChartModel.build(emptyList<Number>()))
    setContent {
      PieChartHost(
        rememberPieChart(
          model,
          PieChart.SliceProvider.series(
            PieChart.Slice(Fill(Color.Red)),
            PieChart.Slice(Fill(Color.Blue)),
          ),
        ),
        Modifier.size(200.dp).background(Color.White).testTag(tag),
        animationSpec = null,
        initialAnimationSpec = spec,
      )
    }
    mainClock.advanceTimeByFrame()
    model = PieChartModel.build(1, 2)
    mainClock.advanceTimeBy(200)
    val during = countPixels(isColored)
    mainClock.advanceTimeBy(1_000)
    val after = countPixels(isColored)
    assertTrue(during in 1..<after, "Expected a reveal animation; got $during of $after.")
  }
}
