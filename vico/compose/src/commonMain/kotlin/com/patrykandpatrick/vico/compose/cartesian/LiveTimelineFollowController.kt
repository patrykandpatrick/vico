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

/** Whether the chart is automatically following “now” or the user is browsing history. */
public enum class LiveTimelineFollowMode {
  Following,
  Browsing,
}

/**
 * Configuration for [LiveTimelineFollowController]: re-follow thresholds, hysteresis, and scroll
 * deadband.
 *
 * Re-entry into [LiveTimelineFollowMode.Following] while browsing uses a Schmitt-style band:
 * [nearNowInnerSpanFraction] defines how close the viewport center must be to `now` to count as
 * “near,” and [nearNowOuterSpanFraction] (must be ≥ the inner fraction) widens the band so small
 * oscillations do not flicker eligibility. Both are scaled by the visible time span, then clamped
 * to [[thresholdMinMs], [thresholdMaxMs]].
 *
 * @param nearNowInnerSpanFraction fraction of visible span for the inner (stricter) threshold.
 * @param nearNowOuterSpanFraction fraction of visible span for the outer (looser) threshold.
 * @param thresholdMinMs minimum threshold width after scaling, in milliseconds.
 * @param thresholdMaxMs maximum threshold width after scaling, in milliseconds.
 * @param scrollDeadbandPixels minimum absolute scroll delta before a follow update is applied
 *   (~½ px recommended).
 */
public data class LiveTimelineFollowConfig(
  public val nearNowInnerSpanFraction: Double = 0.06,
  public val nearNowOuterSpanFraction: Double = 0.10,
  public val thresholdMinMs: Long = 200L,
  public val thresholdMaxMs: Long = 10_000L,
  public val scrollDeadbandPixels: Float = 0.5f,
) {
  init {
    require(nearNowInnerSpanFraction > 0) { "`nearNowInnerSpanFraction` must be positive." }
    require(nearNowOuterSpanFraction >= nearNowInnerSpanFraction) {
      "`nearNowOuterSpanFraction` must be ≥ `nearNowInnerSpanFraction`."
    }
  }

  public companion object {
    /** Default thresholds and deadband suitable for typical live timelines. */
    public val Default: LiveTimelineFollowConfig = LiveTimelineFollowConfig()
  }
}

/**
 * Result of [LiveTimelineFollowController.onFrame]: either leave scroll unchanged or jump to an
 * absolute pixel scroll.
 */
public sealed class LiveTimelineFollowFrameResult {
  /** Do not adjust scroll for this frame. */
  public data object NoOp : LiveTimelineFollowFrameResult()

  /** Scroll to the given absolute pixel offset (already clamped to the chart’s scroll range). */
  public data class ScrollToAbsolutePixels(public val pixels: Float) : LiveTimelineFollowFrameResult()
}

/**
 * Maps scroll position to a live timeline centered on [nowProvider], with follow vs browse modes,
 * optional pause, re-follow thresholds, and sub-pixel deadband.
 *
 * **Data contract:** Callers should only pass sample _x_ values ≤ `now` (epoch or logical clock). If
 * points lie in the future relative to `now`, the chart may still draw them; this controller does
 * not filter series data.
 *
 * @param nowProvider supplies the current instant in the same unit as chart _x_ (typically epoch
 *   milliseconds).
 * @param config threshold and deadband parameters.
 */
public class LiveTimelineFollowController(
  public var nowProvider: () -> Long,
  public var config: LiveTimelineFollowConfig = LiveTimelineFollowConfig.Default,
) {
  /** Current interaction mode. */
  public var mode: LiveTimelineFollowMode = LiveTimelineFollowMode.Following
    private set

  /**
   * When `true`, programmatic follow scrolling is suppressed; [nowProvider] and model updates are
   * unaffected.
   */
  public var pauseFollow: Boolean = false

  private var nearNowLatched: Boolean = false

  /**
   * After a user pan, re-follow is disabled until the viewport center moves at least as far from
   * `now` as the outer threshold (prevents an immediate snap back on release while still “near”).
   */
  private var reFollowArmed: Boolean = true

  /** Call when the user starts a horizontal pan/drag so follow stops immediately. */
  public fun notifyUserPanStarted() {
    mode = LiveTimelineFollowMode.Browsing
    nearNowLatched = false
    reFollowArmed = false
  }

  /** Resume follow, clear pause, and center `now` on the next applicable frame. */
  public fun goLive() {
    pauseFollow = false
    mode = LiveTimelineFollowMode.Following
    nearNowLatched = true
    reFollowArmed = true
  }

  /**
   * Computes the scroll action for one frame.
   *
   * @param viewport geometry snapshot, or `null` if layout is not ready.
   * @param currentScroll current scroll in pixels.
   */
  public fun onFrame(
    viewport: CartesianViewportSnapshot?,
    currentScroll: Float,
  ): LiveTimelineFollowFrameResult {
    if (viewport == null || viewport.xSpacing <= 0f) return LiveTimelineFollowFrameResult.NoOp

    val now = nowProvider().toDouble()
    val center =
      LiveTimelineFollowMath.centerX(
        scroll = currentScroll,
        fullXRangeStart = viewport.fullXRangeStart,
        layoutDirectionMultiplier = viewport.layoutDirectionMultiplier,
        xSpacing = viewport.xSpacing,
        xStep = viewport.xStep,
        layerBoundsWidth = viewport.layerBoundsWidth,
      )
    val spanMs =
      LiveTimelineFollowMath.visibleSpanX(
        layerBoundsWidth = viewport.layerBoundsWidth,
        xSpacing = viewport.xSpacing,
        xStep = viewport.xStep,
      )
    val dist = abs(center - now)

    if (mode == LiveTimelineFollowMode.Browsing) {
      val innerMs =
        LiveTimelineFollowMath.thresholdMs(
          spanMs,
          config.nearNowInnerSpanFraction,
          config.thresholdMinMs,
          config.thresholdMaxMs,
        )
      val outerMs =
        LiveTimelineFollowMath.thresholdMs(
          spanMs,
          config.nearNowOuterSpanFraction,
          config.thresholdMinMs,
          config.thresholdMaxMs,
        )
      if (!reFollowArmed) {
        if (dist >= outerMs.toDouble()) reFollowArmed = true
      } else {
        updateNearNowSchmitt(dist <= innerMs.toDouble(), dist >= outerMs.toDouble())
        if (nearNowLatched) {
          mode = LiveTimelineFollowMode.Following
        }
      }
    }

    if (mode != LiveTimelineFollowMode.Following || pauseFollow) {
      return LiveTimelineFollowFrameResult.NoOp
    }

    val target =
      LiveTimelineFollowMath.scrollForCenterX(
        centerX = now,
        fullXRangeStart = viewport.fullXRangeStart,
        layoutDirectionMultiplier = viewport.layoutDirectionMultiplier,
        xSpacing = viewport.xSpacing,
        xStep = viewport.xStep,
        layerBoundsWidth = viewport.layerBoundsWidth,
      )
    val clamped =
      LiveTimelineFollowMath.clampScroll(target, viewport.maxScrollValue)
    return if (abs(clamped - currentScroll) < config.scrollDeadbandPixels) {
      LiveTimelineFollowFrameResult.NoOp
    } else {
      LiveTimelineFollowFrameResult.ScrollToAbsolutePixels(clamped)
    }
  }

  private fun updateNearNowSchmitt(withinInner: Boolean, beyondOuter: Boolean) {
    when {
      withinInner -> nearNowLatched = true
      beyondOuter -> nearNowLatched = false
    }
  }
}
