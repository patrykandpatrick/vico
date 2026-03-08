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
import kotlin.math.abs

internal data class CubicInterpolator(private val curvature: Float) :
  LineCartesianLayer.Interpolator {
  init {
    require(curvature > 0 && curvature <= 1) { "`curvature` must be in (0, 1]." }
  }

  override fun interpolate(
    context: CartesianDrawingContext,
    path: Path,
    points: List<Offset>,
    visibleIndexRange: IntRange,
  ) {
    if (visibleIndexRange.isEmpty()) return
    path.moveTo(points[visibleIndexRange.first].x, points[visibleIndexRange.first].y)
    for (index in visibleIndexRange.first + 1..visibleIndexRange.last) {
      val prev = points[index - 1]
      val current = points[index]
      val xDelta =
        (Y_MULTIPLIER * abs(current.y - prev.y) / context.layerBounds.height).coerceAtMost(1f) *
          curvature *
          (current.x - prev.x)
      path.cubicTo(prev.x + xDelta, prev.y, current.x - xDelta, current.y, current.x, current.y)
    }
  }

  private companion object {
    const val Y_MULTIPLIER = 4
  }
}
