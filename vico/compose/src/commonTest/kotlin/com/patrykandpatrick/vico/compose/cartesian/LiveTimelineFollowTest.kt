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

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LiveTimelineFollowTest {

  private val ltrViewport =
    CartesianViewportSnapshot(
      scrollValue = 0f,
      maxScrollValue = 500f,
      fullXRangeStart = 0.0,
      layoutDirectionMultiplier = 1,
      xSpacing = 10f,
      xStep = 1.0,
      layerBoundsWidth = 100f,
    )

  @Test
  fun centerX_and_scrollForCenterX_areInverse_ltr() {
    val scroll = 37f
    val center =
      LiveTimelineFollowMath.centerX(
        scroll = scroll,
        fullXRangeStart = ltrViewport.fullXRangeStart,
        layoutDirectionMultiplier = ltrViewport.layoutDirectionMultiplier,
        xSpacing = ltrViewport.xSpacing,
        xStep = ltrViewport.xStep,
        layerBoundsWidth = ltrViewport.layerBoundsWidth,
      )
    val back =
      LiveTimelineFollowMath.scrollForCenterX(
        centerX = center,
        fullXRangeStart = ltrViewport.fullXRangeStart,
        layoutDirectionMultiplier = ltrViewport.layoutDirectionMultiplier,
        xSpacing = ltrViewport.xSpacing,
        xStep = ltrViewport.xStep,
        layerBoundsWidth = ltrViewport.layerBoundsWidth,
      )
    assertTrue(abs(back - scroll) < 1e-3f)
  }

  @Test
  fun goLive_targetsScrollCenteringNow() {
    val now = 5.0
    val controller =
      LiveTimelineFollowController(
        nowProvider = { now.toLong() },
        config = LiveTimelineFollowConfig(scrollDeadbandPixels = 0.01f),
      )
    // At scroll 200, viewport center is not at `now`; follow should correct toward scroll 0.
    val viewport = ltrViewport.copy(scrollValue = 200f, maxScrollValue = 500f)
    val result = controller.onFrame(viewport, viewport.scrollValue)
    val scrollTo = assertIs<LiveTimelineFollowFrameResult.ScrollToAbsolutePixels>(result)
    val centered =
      LiveTimelineFollowMath.centerX(
        scroll = scrollTo.pixels,
        fullXRangeStart = viewport.fullXRangeStart,
        layoutDirectionMultiplier = viewport.layoutDirectionMultiplier,
        xSpacing = viewport.xSpacing,
        xStep = viewport.xStep,
        layerBoundsWidth = viewport.layerBoundsWidth,
      )
    assertTrue(abs(centered - now) < 1e-6)
  }

  @Test
  fun deadband_suppressesTinyDeltas() {
    val controller =
      LiveTimelineFollowController(
        nowProvider = { 50L },
        config = LiveTimelineFollowConfig(scrollDeadbandPixels = 10f),
      )
    val targetScroll =
      LiveTimelineFollowMath.scrollForCenterX(
        centerX = 50.0,
        fullXRangeStart = ltrViewport.fullXRangeStart,
        layoutDirectionMultiplier = ltrViewport.layoutDirectionMultiplier,
        xSpacing = ltrViewport.xSpacing,
        xStep = ltrViewport.xStep,
        layerBoundsWidth = ltrViewport.layerBoundsWidth,
      )
    val nearScroll = targetScroll + 5f
    val result = controller.onFrame(ltrViewport.copy(scrollValue = nearScroll), nearScroll)
    assertIs<LiveTimelineFollowFrameResult.NoOp>(result)
  }

  @Test
  fun pauseFollow_blocksProgrammaticScroll() {
    val controller =
      LiveTimelineFollowController(
        nowProvider = { 10_000L },
        config = LiveTimelineFollowConfig(scrollDeadbandPixels = 0.01f),
      )
    controller.pauseFollow = true
    val result =
      controller.onFrame(ltrViewport.copy(scrollValue = 0f, maxScrollValue = 10_000f), 0f)
    assertIs<LiveTimelineFollowFrameResult.NoOp>(result)
  }

  @Test
  fun userPan_switchesToBrowsing() {
    val controller = LiveTimelineFollowController(nowProvider = { 0L })
    assertEquals(LiveTimelineFollowMode.Following, controller.mode)
    controller.notifyUserPanStarted()
    assertEquals(LiveTimelineFollowMode.Browsing, controller.mode)
  }

  @Test
  fun hysteresis_reentry_requires_outerClearance_then_innerProximity() {
    val now = 1_000.0
    val controller =
      LiveTimelineFollowController(
        nowProvider = { now.toLong() },
        config =
          LiveTimelineFollowConfig(
            nearNowInnerSpanFraction = 0.05,
            nearNowOuterSpanFraction = 0.15,
            thresholdMinMs = 100L,
            thresholdMaxMs = 500_000L,
            scrollDeadbandPixels = 0.01f,
          ),
      )
    val base =
      ltrViewport.copy(
        maxScrollValue = 120_000f,
        layerBoundsWidth = 100f,
      )
    val alignedScroll =
      LiveTimelineFollowMath.scrollForCenterX(
        centerX = now,
        fullXRangeStart = base.fullXRangeStart,
        layoutDirectionMultiplier = base.layoutDirectionMultiplier,
        xSpacing = base.xSpacing,
        xStep = base.xStep,
        layerBoundsWidth = base.layerBoundsWidth,
      )
    controller.notifyUserPanStarted()
    // Still aligned with `now`; distance is below the outer threshold, so re-follow stays disarmed.
    controller.onFrame(base.copy(scrollValue = alignedScroll), alignedScroll)
    assertEquals(LiveTimelineFollowMode.Browsing, controller.mode)

    val farScroll =
      LiveTimelineFollowMath.scrollForCenterX(
        centerX = now + 5_000,
        fullXRangeStart = base.fullXRangeStart,
        layoutDirectionMultiplier = base.layoutDirectionMultiplier,
        xSpacing = base.xSpacing,
        xStep = base.xStep,
        layerBoundsWidth = base.layerBoundsWidth,
      )
    val farClamped = LiveTimelineFollowMath.clampScroll(farScroll, base.maxScrollValue)
    controller.onFrame(base.copy(scrollValue = farClamped), farClamped)
    assertEquals(LiveTimelineFollowMode.Browsing, controller.mode)

    val innerMs =
      LiveTimelineFollowMath.thresholdMs(
        LiveTimelineFollowMath.visibleSpanX(
          base.layerBoundsWidth,
          base.xSpacing,
          base.xStep,
        ),
        controller.config.nearNowInnerSpanFraction,
        controller.config.thresholdMinMs,
        controller.config.thresholdMaxMs,
      )
    val closeScroll =
      LiveTimelineFollowMath.scrollForCenterX(
        centerX = now + innerMs.toDouble() / 2,
        fullXRangeStart = base.fullXRangeStart,
        layoutDirectionMultiplier = base.layoutDirectionMultiplier,
        xSpacing = base.xSpacing,
        xStep = base.xStep,
        layerBoundsWidth = base.layerBoundsWidth,
      )
    val closeClamped = LiveTimelineFollowMath.clampScroll(closeScroll, base.maxScrollValue)
    controller.onFrame(base.copy(scrollValue = closeClamped), closeClamped)
    assertEquals(LiveTimelineFollowMode.Following, controller.mode)
  }
}
