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

import androidx.compose.animation.core.tween
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlin.coroutines.CoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.Timeout

@Timeout(1)
class VicoZoomStateTest {
  @MockK private lateinit var context: CartesianMeasuringContext
  @MockK private lateinit var ranges: CartesianChartRanges

  // As in CartesianChartHostImpl, one MutableCartesianLayerDimensions instance is shared: the zoom
  // state scales it, and the scroll state then derives its maximum from it. The content is much
  // wider than the bounds so that the scroll compensation isn’t clamped.
  private val layerDimensions = MutableCartesianLayerDimensions(xSpacing = 100f)
  private var bounds = Rect(0f, 0f, 100f, 100f)
  private var layoutDirection = LayoutDirection.Ltr

  @BeforeTest
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    bounds = Rect(0f, 0f, 100f, 100f)
    layoutDirection = LayoutDirection.Ltr
    every { context.layoutDirection } answers { layoutDirection }
    every { context.isLtr } answers { layoutDirection == LayoutDirection.Ltr }
    every { context.layoutDirectionMultiplier } answers
      {
        if (layoutDirection == LayoutDirection.Ltr) 1 else -1
      }
    every { context.ranges } returns ranges
    every { ranges.xLength } returns 10.0
    every { ranges.xStep } returns 1.0
  }

  @Test
  fun `When maxZoom produces smaller factor than minZoom, then IllegalArgumentException is thrown`() {
    val minZoom = Zoom.fixed(2f)
    val maxZoom = Zoom.fixed(1f)
    val sut =
      VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(1f),
        minZoom = minZoom,
        maxZoom = maxZoom,
      )

    val exception =
      assertFailsWith<IllegalArgumentException> {
        sut.update(context, MutableCartesianLayerDimensions(), Rect(0f, 0f, 10f, 10f))
      }

    assertTrue(exception.message!!.contains("maxZoom") && exception.message!!.contains("minZoom"))
  }

  @Test
  fun `When maxZoom produces equal factor as minZoom, then no exception is thrown`() {
    val minZoom = Zoom.fixed(1f)
    val maxZoom = Zoom.fixed(1f)
    val sut =
      VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(1f),
        minZoom = minZoom,
        maxZoom = maxZoom,
      )

    sut.update(context, MutableCartesianLayerDimensions(), Rect(0f, 0f, 10f, 10f))

    assertEquals(1f..1f, sut.valueRange)
  }

  @Test
  fun `When maxZoom produces greater factor than minZoom, then no exception is thrown`() {
    val minZoom = Zoom.fixed(1f)
    val maxZoom = Zoom.fixed(2f)
    val sut =
      VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(1f),
        minZoom = minZoom,
        maxZoom = maxZoom,
      )

    sut.update(context, MutableCartesianLayerDimensions(), Rect(0f, 0f, 10f, 10f))

    assertEquals(1f..2f, sut.valueRange)
  }

  @Test
  fun `When animateZoom is called during an ongoing animated zoom, then the ongoing zoom is canceled`() =
    runBlocking(SuspendingFrameClock()) {
      val frameClock = coroutineContext[MonotonicFrameClock] as SuspendingFrameClock
      val sut = createZoomState()

      val ongoingJob =
        launch(start = CoroutineStart.UNDISPATCHED) {
          sut.animateZoom(Zoom.fixed(4f), tween(durationMillis = 1))
        }

      assertEquals(1f, sut.value)
      assertFalse(ongoingJob.isCompleted)
      assertEquals(1, frameClock.frameRequests)

      sut.animateZoom(Zoom.fixed(2f), tween(durationMillis = 0))
      ongoingJob.join()

      assertTrue(ongoingJob.isCancelled)
      assertEquals(2f, sut.value)
    }

  @Test
  fun `When a zoom gesture occurs, then the zoom factor and the scroll value are updated together`() =
    runBlocking {
      val scrollState = createScrollState()
      val sut = createZoomState(scrollState)
      scrollState.scroll(Scroll.Absolute.pixels(50f))
      val scrollBefore = scrollState.value

      // The host applies gesture zooms undispatched, so this mirrors what a pointer event does.
      launch(start = CoroutineStart.UNDISPATCHED) { sut.zoom(factor = 2f, centroidX = 50f) }

      // Both updates have already been applied, with no suspension in between for a frame to be
      // drawn with the new zoom factor and the old scroll value.
      assertEquals(2f, sut.value)
      assertNotEquals(scrollBefore, scrollState.value)
      assertEquals(150f, scrollState.value)
    }

  @Test
  fun `When zoom gestures occur in rapid succession, then every scroll compensation is applied`() =
    runBlocking {
      val scrollState = createScrollState()
      val sut = createZoomState(scrollState)
      scrollState.scroll(Scroll.Absolute.pixels(50f))

      // Two events arriving back to back, as during a fast pinch. Neither may lose its
      // compensation: each one’s is an absolute scroll value derived from the current one, so a
      // dropped step permanently mis-anchors the content.
      repeat(2) {
        launch(start = CoroutineStart.UNDISPATCHED) { sut.zoom(factor = 1.5f, centroidX = 50f) }
      }

      assertEquals(2.25f, sut.value)
      assertEquals(175f, scrollState.value)
    }

  @Test
  fun `When zoom gestures occur in rapid succession, then the maximum scroll value tracks the total zoom`() =
    runBlocking {
      val scrollState = createScrollState()
      val sut = createZoomState(scrollState)
      // Near the end of the content, where an understated maximum clamps the compensation.
      scrollState.scroll(Scroll.Absolute.pixels(800f))

      // Two steps between draws, so `layerDimensions` stays scaled to the zoom factor of the last
      // one. The second step's maximum must describe the total zoom (2.25x), not the step ratio
      // (1.5x) applied to the last-drawn dimensions.
      repeat(2) {
        launch(start = CoroutineStart.UNDISPATCHED) { sut.zoom(factor = 1.5f, centroidX = 50f) }
      }

      assertEquals(2.25f, sut.value)
      // Content is 10 * 100 * 2.25 = 2250 wide against 100 of viewport.
      assertEquals(2150f, scrollState.maxValue)
      // Unclamped, so the anchor is preserved. A maximum computed from the step ratio would be
      // 1400, clamping this to 1400 and mis-anchoring the content by 462.5px.
      assertEquals(1862.5f, scrollState.value)
    }

  @Test
  fun `When a scroll is in progress, then a zoom gesture's scroll compensation is still applied`() =
    runBlocking {
      val scrollState = createScrollState()
      val sut = createZoomState(scrollState)
      scrollState.scroll(Scroll.Absolute.pixels(50f))

      // A fling left over from a previous pan. During a pinch the zoom detector consumes the
      // pointer events, so no drag starts to stop it; the compensation must not be gated on it.
      val scrollJob =
        launch(start = CoroutineStart.UNDISPATCHED) {
          scrollState.scrollableState.scroll { suspendCancellableCoroutine<Unit> {} }
        }
      assertTrue(scrollState.scrollableState.isScrollInProgress)

      sut.zoom(factor = 2f, centroidX = 50f)

      assertEquals(2f, sut.value)
      assertEquals(150f, scrollState.value)

      scrollJob.cancelAndJoin()
    }

  @Test
  fun `When zooming in RTL, then the content under the anchor stays in place`() = runBlocking {
    layoutDirection = LayoutDirection.Rtl
    val scrollState = createScrollState()
    val sut = createZoomState(scrollState)
    // In RTL the scroll value is negative, running from 0 at the content's start edge.
    assertEquals(-900f, scrollState.maxValue)

    // The anchor is the bounds' left edge, which in RTL is 100px into the content from the start
    // edge. Doubling the zoom moves that content point to 200px, so the viewport's start edge has
    // to move to 100px into the content, i.e. a scroll value of -100.
    sut.zoom(factor = 2f, centroidX = bounds.left)

    assertEquals(2f, sut.value)
    assertEquals(-100f, scrollState.value)
  }

  @Test
  fun `When zooming in RTL at the start edge, then the scroll value is unchanged`() = runBlocking {
    layoutDirection = LayoutDirection.Rtl
    val scrollState = createScrollState()
    val sut = createZoomState(scrollState)

    // The bounds' right edge is the start edge in RTL, so the anchor sits on the content's start
    // edge and there is nothing to compensate—mirroring a zoom at `bounds.left` in LTR.
    sut.zoom(factor = 2f, centroidX = bounds.right)

    assertEquals(2f, sut.value)
    assertEquals(0f, scrollState.value)
  }

  @Test
  fun `When zooming in RTL mid-content, then the anchor is preserved`() = runBlocking {
    layoutDirection = LayoutDirection.Rtl
    val scrollState = createScrollState()
    val sut = createZoomState(scrollState)
    scrollState.scroll(Scroll.Absolute.pixels(-300f))

    // Start edge 300px into the content, anchor a further 50px in (bounds are 100 wide), so the
    // anchored content point is at 350px and moves to 700px. Keeping it 50px from the start edge
    // puts that edge at 650px.
    sut.zoom(factor = 2f, centroidX = 50f)

    assertEquals(2f, sut.value)
    assertEquals(-650f, scrollState.value)
  }

  @Test
  fun `When a zoom changes nothing, then initialZoom keeps being applied`() = runBlocking {
    var initialZoomValue = 1f
    val scrollState = createScrollState()
    val sut =
      VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom { _, _, _ -> initialZoomValue },
        minZoom = Zoom.fixed(1f),
        maxZoom = Zoom.fixed(4f),
      )
    sut.setScrollState(scrollState)
    sut.update(context, MutableCartesianLayerDimensions(xSpacing = 100f), bounds)
    assertEquals(1f, sut.value)

    // Already at minZoom, so this is clamped back and nothing moves on screen. It must therefore
    // not mark the state overridden, which would permanently suppress initialZoom.
    sut.zoom(factor = 0.5f, centroidX = 50f)
    assertEquals(1f, sut.value)

    initialZoomValue = 2f
    sut.update(context, MutableCartesianLayerDimensions(xSpacing = 100f), bounds)

    assertEquals(2f, sut.value)
  }

  @Test
  fun `When a zoom does change the factor, then initialZoom stops being applied`() = runBlocking {
    var initialZoomValue = 1f
    val scrollState = createScrollState()
    val sut =
      VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom { _, _, _ -> initialZoomValue },
        minZoom = Zoom.fixed(1f),
        maxZoom = Zoom.fixed(4f),
      )
    sut.setScrollState(scrollState)
    sut.update(context, MutableCartesianLayerDimensions(xSpacing = 100f), bounds)

    sut.zoom(factor = 2f, centroidX = 50f)
    assertEquals(2f, sut.value)

    initialZoomValue = 3f
    sut.update(context, MutableCartesianLayerDimensions(xSpacing = 100f), bounds)

    assertEquals(2f, sut.value)
  }

  @Test
  fun `When the layer bounds are inset, then zoom and animateZoom anchor identically`() =
    runBlocking {
      // A 400px canvas with a 60px start axis, so the layer bounds' center (230) differs from the
      // canvas' (200).
      bounds = Rect(60f, 0f, 400f, 100f)

      val zoomScrollState = createScrollState()
      val zoomed = createZoomState(zoomScrollState)
      zoomed.zoom(Zoom.fixed(2f))

      val animatedScrollState = createScrollState()
      val animated = createZoomState(animatedScrollState)
      animated.animateZoom(Zoom.fixed(2f), tween(durationMillis = 0))

      assertEquals(2f, zoomed.value)
      assertEquals(2f, animated.value)
      // Anchored on the layer bounds' center: 230 - 60 = 170px into the content, doubling to 340,
      // so the start edge moves to 170. Anchoring on the canvas' center would give 140.
      assertEquals(170f, zoomScrollState.value)
      assertEquals(animatedScrollState.value, zoomScrollState.value)
    }

  @Test
  fun `When the scroll state is detached, then a zoom does not write to it`() = runBlocking {
    val scrollState = createScrollState()
    val sut = createZoomState(scrollState)
    scrollState.scroll(Scroll.Absolute.pixels(50f))
    val scrollBefore = scrollState.value
    val maxValueBefore = scrollState.maxValue

    // As the host does when it leaves composition.
    scrollState.clearUpdated()
    sut.zoom(factor = 2f, centroidX = 50f)

    assertEquals(2f, sut.value)
    assertEquals(scrollBefore, scrollState.value)
    assertEquals(maxValueBefore, scrollState.maxValue)
  }

  @Test
  fun `When the scroll state is replaced, then a zoom compensates the new one`() = runBlocking {
    val replaced = createScrollState()
    val sut = createZoomState(replaced)
    replaced.scroll(Scroll.Absolute.pixels(50f))
    val current = createScrollState()
    current.scroll(Scroll.Absolute.pixels(50f))

    sut.setScrollState(current)
    sut.zoom(factor = 2f, centroidX = 50f)

    assertEquals(150f, current.value)
    assertEquals(50f, replaced.value)
  }

  private fun createZoomState(scrollState: VicoScrollState = createScrollState()): VicoZoomState =
    VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(1f),
        minZoom = Zoom.fixed(1f),
        maxZoom = Zoom.fixed(4f),
      )
      .also {
        it.setScrollState(scrollState)
        it.update(context = context, layerDimensions = layerDimensions, bounds = bounds)
      }

  private fun createScrollState(): VicoScrollState =
    VicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.Start,
        autoScroll = Scroll.Absolute.Start,
        autoScrollCondition = AutoScrollCondition.Never,
        autoScrollAnimationSpec = tween(),
        xSnapStep = null,
        snapAnimationSpec = tween(),
      )
      .also {
        it.update(context = context, bounds = bounds, layerDimensions = layerDimensions)
      }

  private class SuspendingFrameClock : MonotonicFrameClock {
    var frameRequests: Int = 0

    override val key: CoroutineContext.Key<*>
      get() = MonotonicFrameClock.Key

    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
      frameRequests++
      return suspendCancellableCoroutine {}
    }
  }
}
