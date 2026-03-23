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

/** Pure math for mapping between scroll position and viewport center time on a Cartesian chart. */
internal object LiveTimelineFollowMath {

  /** Visible span in _x_ units (e.g. milliseconds if _x_ is epoch time). */
  fun visibleSpanX(layerBoundsWidth: Float, xSpacing: Float, xStep: Double): Double {
    if (xSpacing == 0f) return 0.0
    return layerBoundsWidth / xSpacing * xStep
  }

  /** _x_ value at the horizontal center of the viewport for the given scroll offset. */
  fun centerX(
    scroll: Float,
    fullXRangeStart: Double,
    layoutDirectionMultiplier: Int,
    xSpacing: Float,
    xStep: Double,
    layerBoundsWidth: Float,
  ): Double {
    if (xSpacing == 0f) return fullXRangeStart
    val start =
      fullXRangeStart + layoutDirectionMultiplier * scroll / xSpacing * xStep
    val end = start + layerBoundsWidth / xSpacing * xStep
    return (start + end) / 2.0
  }

  /**
   * Scroll value that places [centerX] at the horizontal center of the viewport (before clamping to
   * `[0, maxScroll]`).
   */
  fun scrollForCenterX(
    centerX: Double,
    fullXRangeStart: Double,
    layoutDirectionMultiplier: Int,
    xSpacing: Float,
    xStep: Double,
    layerBoundsWidth: Float,
  ): Float {
    if (xSpacing == 0f) return 0f
    val halfSpanX = layerBoundsWidth / xSpacing * xStep / 2.0
    val deltaFromStart = centerX - fullXRangeStart - halfSpanX
    return (layoutDirectionMultiplier * deltaFromStart * xSpacing / xStep).toFloat()
  }

  fun clampScroll(scroll: Float, maxScroll: Float): Float = scroll.coerceIn(0f, maxScroll)

  fun thresholdMs(
    spanMs: Double,
    fraction: Double,
    minMs: Long,
    maxMs: Long,
  ): Long {
    val raw = (abs(spanMs) * fraction).toLong()
    return raw.coerceIn(minMs, maxMs)
  }
}
