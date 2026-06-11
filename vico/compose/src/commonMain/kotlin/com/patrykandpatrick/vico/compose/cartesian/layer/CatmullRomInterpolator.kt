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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext

internal data class CatmullRomInterpolator(private val alpha: Float) :
  LineCartesianLayer.Interpolator {

  override val visiblePadding: Int = 1

  init {
    require(alpha in 0f..<1f) { "`alpha` must be in [0, 1)." }
  }

  private val scale = (1f - alpha) / 6f

  override fun interpolate(
    context: CartesianDrawingContext,
    path: Path,
    points: List<Offset>,
    visibleIndexRange: IntRange,
  ) {
    if (visibleIndexRange.isEmpty()) return
    path.moveTo(points[visibleIndexRange.first].x, points[visibleIndexRange.first].y)
    for (index in visibleIndexRange.first + 1..visibleIndexRange.last) {
      val p0 = points[(index - 2).coerceAtLeast(0)]
      val p1 = points[index - 1]
      val p2 = points[index]
      val p3 = points[(index + 1).coerceAtMost(points.lastIndex)]
      addCatmullRomSegment(path, p0.y, p1.y, p2.y, p3.y, p0.x, p1.x, p2.x, p3.x)
    }
  }

  override fun getYRange(y: List<Double>): ClosedRange<Double> = getCatmullRomYRange(y, alpha)

  private fun addCatmullRomSegment(
    path: Path,
    y0: Float,
    y1: Float,
    y2: Float,
    y3: Float,
    x0: Float,
    x1: Float,
    x2: Float,
    x3: Float,
  ) {
    // The control points’ _x_-coordinates are clamped to [[x1, x2]]. With uneven point spacing,
    // the raw values can fall outside this range, making the spline non-monotonic in _x_ (it
    // overshoots horizontally and doubles back). That produces a self-intersecting area-fill
    // polygon, which can invert the fill. See https://github.com/patrykandpatrick/vico/issues/1517.
    val cp1x = (x1 + scale * (x2 - x0)).coerceIn(x1, x2)
    val cp1y = y1 + scale * (y2 - y0)
    val cp2x = (x2 - scale * (x3 - x1)).coerceIn(x1, x2)
    val cp2y = y2 - scale * (y3 - y1)
    path.cubicTo(cp1x, cp1y, cp2x, cp2y, x2, y2)
  }
}
