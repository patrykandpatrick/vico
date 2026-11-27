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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Checks that [VicoScrollState.scroll], [VicoScrollState.animateScroll], [VicoZoomState.zoom], and
 * [VicoZoomState.animateZoom] wait for a pending chart update, through a real composition.
 */
class PendingUpdateCommandTest {
  private val spec = tween<Float>(durationMillis = 100, easing = LinearEasing)
  private val tag = "chart"

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

  private class Host(model: ColumnCartesianLayerModel) {
    val gate = GateDispatcher()
    var model by mutableStateOf(model)
    var showHost by mutableStateOf(true)
    lateinit var scope: CoroutineScope
    lateinit var scrollState: VicoScrollState
    lateinit var zoomState: VicoZoomState
  }

  private fun columns(count: Int, xStep: Int = 1) = ColumnCartesianLayerModel.build {
    series(x = List(count) { it * xStep }, y = List(count) { it % 5 + 1 })
  }

  private fun ComposeUiTest.setHost(host: Host) {
    mainClock.autoAdvance = false
    setContent {
      host.scope = rememberCoroutineScope()
      host.scrollState = rememberVicoScrollState()
      host.zoomState = rememberVicoZoomState(initialZoom = Zoom.fixed())
      CompositionLocalProvider(LocalCartesianChartBackgroundContext provides host.gate) {
        if (host.showHost) {
          CartesianChartHost(
            rememberCartesianChart(rememberColumnCartesianLayer(host.model)),
            Modifier.size(300.dp, 200.dp).testTag(tag),
            scrollState = host.scrollState,
            zoomState = host.zoomState,
            animationSpec = spec,
            initialAnimationSpec = null,
          )
        }
      }
    }
    mainClock.advanceTimeBy(500)
  }

  /** Submits [model] while the derivation is held, so that the update is pending. */
  private fun ComposeUiTest.submitPending(host: Host, model: ColumnCartesianLayerModel) {
    host.gate.isOpen = false
    host.model = model
    mainClock.advanceTimeByFrame()
  }

  private fun ComposeUiTest.launch(host: Host, command: suspend () -> Unit): () -> Boolean {
    var isDone = false
    runOnUiThread {
      host.scope.launch {
        command()
        isDone = true
      }
    }
    return { isDone }
  }

  private fun ComposeUiTest.releaseAndSettle(host: Host) {
    host.gate.release()
    mainClock.advanceTimeBy(500)
  }

  private fun scrollsToNewEnd(command: suspend VicoScrollState.() -> Unit) = runComposeUiTest {
    val host = Host(columns(30))
    setHost(host)
    val oldMaxValue = runOnUiThread { host.scrollState.maxValue }
    assertTrue(oldMaxValue > 0f)
    submitPending(host, columns(60))
    val isDone = launch(host) { host.scrollState.command() }
    mainClock.advanceTimeByFrame()
    releaseAndSettle(host)
    assertTrue(isDone())
    runOnUiThread {
      assertTrue(host.scrollState.maxValue > oldMaxValue)
      assertEquals(host.scrollState.maxValue, host.scrollState.value)
    }
  }

  @Test
  fun `scroll after an asynchronous data update uses the new ranges`() = scrollsToNewEnd {
    scroll(Scroll.Absolute.End)
  }

  @Test
  fun `animateScroll after an asynchronous data update uses the new ranges`() = scrollsToNewEnd {
    animateScroll(Scroll.Absolute.End, spec)
  }

  private fun zoomsWithNewXStep(command: suspend VicoZoomState.() -> Unit) = runComposeUiTest {
    val host = Host(columns(30))
    setHost(host)
    submitPending(host, columns(30, xStep = 2))
    val isDone = launch(host) { host.zoomState.command() }
    mainClock.advanceTimeByFrame()
    releaseAndSettle(host)
    assertTrue(isDone())
    val zoom = runOnUiThread { host.zoomState.value }
    // Repeating the command against the settled chart must change nothing.
    val isRepeatDone = launch(host) { host.zoomState.command() }
    mainClock.advanceTimeBy(500)
    assertTrue(isRepeatDone())
    val settledZoom = runOnUiThread { host.zoomState.value }
    assertTrue(abs(zoom - settledZoom) < 1e-3f, "$zoom ≠ $settledZoom")
  }

  @Test
  fun `zoom after an asynchronous data update uses the new ranges`() = zoomsWithNewXStep {
    zoom(Zoom.x(10.0))
  }

  @Test
  fun `animateZoom after an asynchronous data update uses the new ranges`() = zoomsWithNewXStep {
    animateZoom(Zoom.x(10.0), spec)
  }

  @Test
  fun `Commands run immediately when no update is pending`() = runComposeUiTest {
    val host = Host(columns(30))
    setHost(host)
    val isScrollDone = launch(host) { host.scrollState.scroll(Scroll.Absolute.End) }
    val isZoomDone = launch(host) { host.zoomState.zoom(Zoom.x(10.0)) }
    waitForIdle()
    assertTrue(isScrollDone())
    assertTrue(isZoomDone())
    // A state that isn’t attached to a host doesn’t wait either.
    val detached = VicoScrollState()
    val isDetachedDone = launch(host) { detached.scroll(Scroll.Absolute.End) }
    waitForIdle()
    assertTrue(isDetachedDone())
  }

  @Test
  fun `A command waits while the update is pending`() = runComposeUiTest {
    val host = Host(columns(30))
    setHost(host)
    submitPending(host, columns(60))
    val isDone = launch(host) { host.scrollState.scroll(Scroll.Absolute.End) }
    mainClock.advanceTimeBy(200)
    assertFalse(isDone())
    releaseAndSettle(host)
    assertTrue(isDone())
  }

  @Test
  fun `Continuous updates don’t starve a command`() = runComposeUiTest {
    val host = Host(columns(30))
    setHost(host)
    submitPending(host, columns(40))
    val isDone = launch(host) { host.scrollState.scroll(Scroll.Absolute.End) }
    repeat(5) { index ->
      // Each round lands the pending derivation and submits a new update before the landed one is
      // drawn, so an update is pending whenever the host draws.
      host.gate.release()
      host.gate.isOpen = false
      host.model = columns(50 + index * 10)
      mainClock.advanceTimeByFrame()
      mainClock.advanceTimeByFrame()
    }
    assertTrue(isDone())
    releaseAndSettle(host)
  }

  @Test
  fun `A command doesn’t hang when the host is disposed mid-wait`() = runComposeUiTest {
    val host = Host(columns(30))
    setHost(host)
    submitPending(host, columns(60))
    val isScrollDone = launch(host) { host.scrollState.scroll(Scroll.Absolute.End) }
    val isZoomDone = launch(host) { host.zoomState.animateZoom(Zoom.x(10.0), spec) }
    mainClock.advanceTimeByFrame()
    host.showHost = false
    mainClock.advanceTimeBy(200)
    assertTrue(isScrollDone())
    assertTrue(isZoomDone())
    releaseAndSettle(host)
  }

  @Test
  fun `Gesture scrolling doesn’t wait for a pending update`() = runComposeUiTest {
    val host = Host(columns(60))
    setHost(host)
    submitPending(host, columns(70))
    val before = runOnUiThread { host.scrollState.value }
    onNodeWithTag(tag).performTouchInput { swipeLeft() }
    mainClock.advanceTimeBy(100)
    val after = runOnUiThread { host.scrollState.value }
    assertTrue(after > before, "$after ≤ $before")
    releaseAndSettle(host)
  }
}
