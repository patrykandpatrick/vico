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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal data class CatmullRomInterpolator(private val alpha: Float) :
  LineCartesianLayer.Interpolator {

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

  override fun getYRange(y: List<Double>): ClosedRange<Double> {
    if (y.size < 2) return y.min()..y.max()
    val scale = (1.0 - alpha) / 6.0
    var minY = y.min()
    var maxY = y.max()
    for (index in 1..y.lastIndex) {
      val y0 = y[(index - 2).coerceAtLeast(0)]
      val y1 = y[index - 1]
      val y2 = y[index]
      val y3 = y[(index + 1).coerceAtMost(y.lastIndex)]
      val cp1 = y1 + scale * (y2 - y0)
      val cp2 = y2 - scale * (y3 - y1)
      for (y in cubicBezierExtrema(y1, cp1, cp2, y2)) {
        minY = min(minY, y)
        maxY = max(maxY, y)
      }
    }
    return minY..maxY
  }

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
    val cp1x = x1 + scale * (x2 - x0)
    val cp1y = y1 + scale * (y2 - y0)
    val cp2x = x2 - scale * (x3 - x1)
    val cp2y = y2 - scale * (y3 - y1)
    path.cubicTo(cp1x, cp1y, cp2x, cp2y, x2, y2)
  }

  private companion object {
    /**
     * Returns the _y_ values at the extrema of the cubic Bézier curve defined by [a], [b], [c], and
     * [d], excluding the endpoints.
     */
    fun cubicBezierExtrema(a: Double, b: Double, c: Double, d: Double): List<Double> {
      // dy/dt = 3[(d - 3c + 3b - a)t² + (2a - 4b + 2c)t + (b - a)]
      val qa = d - 3 * c + 3 * b - a
      val qb = 2 * a - 4 * b + 2 * c
      val qc = b - a
      if (qa == 0.0) {
        // Linear: qb * t + qc = 0
        if (qb == 0.0) return emptyList()
        val t = -qc / qb
        return if (t in 0.0..1.0) listOf(evalCubicBezier(a, b, c, d, t)) else emptyList()
      }
      val discriminant = qb * qb - 4 * qa * qc
      if (discriminant < 0) return emptyList()
      val sqrtD = sqrt(discriminant)
      val results = mutableListOf<Double>()
      for (t in listOf((-qb + sqrtD) / (2 * qa), (-qb - sqrtD) / (2 * qa))) {
        if (t > 0.0 && t < 1.0) results += evalCubicBezier(a, b, c, d, t)
      }
      return results
    }

    fun evalCubicBezier(a: Double, b: Double, c: Double, d: Double, t: Double): Double {
      val u = 1 - t
      return u * u * u * a + 3 * u * u * t * b + 3 * u * t * t * c + t * t * t * d
    }
  }
}
