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
import kotlin.test.assertFalse
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.Timeout

@Timeout(1)
class VicoScrollStateTest {
  @MockK private lateinit var context: CartesianMeasuringContext
  @MockK private lateinit var ranges: CartesianChartRanges
  private var minX = 0.0
  private var maxX = 10.0
  private var xLength = 10.0
  private var xStep = 1.0
  private var layoutDirection = LayoutDirection.Ltr
  private val layoutDirectionMultiplier: Int
    get() = if (layoutDirection == LayoutDirection.Ltr) 1 else -1

  @BeforeTest
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    minX = 0.0
    maxX = 10.0
    xLength = 10.0
    xStep = 1.0
    layoutDirection = LayoutDirection.Ltr
    every { context.layoutDirection } answers { layoutDirection }
    every { context.isLtr } answers { layoutDirection == LayoutDirection.Ltr }
    every { context.layoutDirectionMultiplier } answers { layoutDirectionMultiplier }
    every { context.ranges } returns ranges
    every { ranges.minX } answers { minX }
    every { ranges.maxX } answers { maxX }
    every { ranges.xLength } answers { xLength }
    every { ranges.xStep } answers { xStep }
  }

  @Test
  fun `When animateScroll uses zero-duration animation, then it updates the value immediately`() =
    runBlocking(SuspendingFrameClock()) {
      val frameClock = coroutineContext[MonotonicFrameClock] as SuspendingFrameClock
      val sut = createScrollState()
      val targetValue = 30f

      sut.animateScroll(Scroll.Absolute.pixels(targetValue), tween(durationMillis = 0))

      assertEquals(targetValue, sut.value)
      assertEquals(0, frameClock.frameRequests)
    }

  @Test
  fun `When animateScroll uses non-zero-duration animation, then it does not update the value immediately`() =
    runBlocking(SuspendingFrameClock()) {
      val frameClock = coroutineContext[MonotonicFrameClock] as SuspendingFrameClock
      val sut = createScrollState()
      val targetValue = 30f

      val job =
        launch(start = CoroutineStart.UNDISPATCHED) {
          sut.animateScroll(Scroll.Absolute.pixels(targetValue), tween(durationMillis = 1))
        }

      assertEquals(0f, sut.value)
      assertFalse(job.isCompleted)
      assertEquals(1, frameClock.frameRequests)

      job.cancelAndJoin()
    }

  @Test
  fun `When xSnapStep is set, then snap delta accounts for layer start padding`() = runBlocking {
    val sut =
      createScrollState(
        xSnapStep = 2.0,
        layerDimensions =
          MutableCartesianLayerDimensions(
            xSpacing = 10f,
            scalableStartPadding = 7f,
            unscalableStartPadding = 3f,
          ),
      )

    sut.scroll(Scroll.Absolute.pixels(27f))

    assertEquals(3f, sut.getSnapDelta())
  }

  @Test
  fun `When xSnapStep is set and scroll is at start, then snap delta is null`() = runBlocking {
    val sut =
      createScrollState(
        xSnapStep = 2.0,
        layerDimensions =
          MutableCartesianLayerDimensions(
            xSpacing = 10f,
            scalableStartPadding = 7f,
            unscalableStartPadding = 3f,
          ),
      )

    assertEquals(0f, sut.value)
    assertEquals(null, sut.getSnapDelta())
  }

  @Test
  fun `When xSnapStep is set and target is projected, then snap delta uses projected target`() =
    runBlocking {
      val sut =
        createScrollState(
          xSnapStep = 2.0,
          layerDimensions =
            MutableCartesianLayerDimensions(
              xSpacing = 10f,
              scalableStartPadding = 7f,
              unscalableStartPadding = 3f,
            ),
        )

      sut.scroll(Scroll.Absolute.pixels(12f))

      assertEquals(38f, sut.getSnapDelta(targetValue = 49f))
    }

  @Test
  fun `When x range shifts, then update preserves the visible x range`() = runBlocking {
    maxX = 20.0
    xLength = 20.0
    val bounds = Rect(0f, 0f, 100f, 100f)
    val layerDimensions =
      MutableCartesianLayerDimensions(
        xSpacing = 10f,
        scalableStartPadding = 6f,
        unscalableStartPadding = 4f,
      )
    val sut =
      createScrollState(
        initialScroll = Scroll.Absolute.pixels(40f),
        layerDimensions = layerDimensions,
      )

    val visibleStart = context.getVisibleXRange(layerDimensions, bounds, sut.value).start

    minX = -2.0
    xLength = 22.0
    sut.update(context = context, bounds = bounds, layerDimensions = layerDimensions)

    assertEquals(visibleStart, context.getVisibleXRange(layerDimensions, bounds, sut.value).start)
    assertEquals(60f, sut.value)
  }

  @Test
  fun `When x range shifts in RTL, then update preserves the visible x range`() = runBlocking {
    layoutDirection = LayoutDirection.Rtl
    maxX = 20.0
    xLength = 20.0
    val bounds = Rect(0f, 0f, 100f, 100f)
    val layerDimensions =
      MutableCartesianLayerDimensions(
        xSpacing = 10f,
        scalableStartPadding = 6f,
        unscalableStartPadding = 4f,
      )
    val sut =
      createScrollState(
        initialScroll = Scroll.Absolute.pixels(-40f),
        layerDimensions = layerDimensions,
      )

    val visibleStart = context.getVisibleXRange(layerDimensions, bounds, sut.value).start

    minX = -2.0
    xLength = 22.0
    sut.update(context = context, bounds = bounds, layerDimensions = layerDimensions)

    assertEquals(visibleStart, context.getVisibleXRange(layerDimensions, bounds, sut.value).start)
    assertEquals(-20f, sut.value)
  }

  @Test
  fun `When data is trimmed on the start side while pinned to the end, then update keeps the scroll pinned to the end`() =
    runBlocking {
      maxX = 19.0
      xLength = 19.0
      val bounds = Rect(0f, 0f, 100f, 100f)
      val layerDimensions =
        MutableCartesianLayerDimensions(
          xSpacing = 10f,
          scalableStartPadding = 6f,
          unscalableStartPadding = 4f,
        )
      // The initial content is 100px wider than the chart, so the end-pinned scroll value is 100,
      // matching the max scroll distance. The scroll is at the end.
      val sut = createScrollState(initialScroll = Scroll.Absolute.End, layerDimensions = layerDimensions)
      assertEquals(sut.maxValue, sut.value)

      val visibleStart = context.getVisibleXRange(layerDimensions, bounds, sut.value).start

      // Trim the five oldest points. The remaining content still overflows the chart, so the new
      // max scroll distance is smaller than the old scroll value.
      minX = 5.0
      xLength = 14.0
      sut.update(context = context, bounds = bounds, layerDimensions = layerDimensions)

      // The visible x range is unchanged (the trimmed points were off-screen), and the scroll stays
      // pinned to the end rather than rolling toward the start by the size of the trim.
      assertEquals(visibleStart, context.getVisibleXRange(layerDimensions, bounds, sut.value).start)
      assertEquals(sut.maxValue, sut.value)
    }

  private fun createScrollState(
    xSnapStep: Double? = null,
    initialScroll: Scroll.Absolute = Scroll.Absolute.Start,
    layerDimensions: MutableCartesianLayerDimensions =
      MutableCartesianLayerDimensions(xSpacing = 20f),
  ): VicoScrollState =
    VicoScrollState(
        scrollEnabled = true,
        initialScroll = initialScroll,
        autoScroll = Scroll.Absolute.Start,
        autoScrollCondition = AutoScrollCondition.Never,
        autoScrollAnimationSpec = tween(),
        xSnapStep = xSnapStep,
        snapAnimationSpec = tween(),
      )
      .also {
        it.update(
          context = context,
          bounds = Rect(0f, 0f, 100f, 100f),
          layerDimensions = layerDimensions,
        )
        it.maxValue = 100f
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
