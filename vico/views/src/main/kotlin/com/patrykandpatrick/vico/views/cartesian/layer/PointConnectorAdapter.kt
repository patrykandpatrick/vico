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

package com.patrykandpatrick.vico.views.cartesian.layer

import android.graphics.Path
import android.graphics.PointF
import com.patrykandpatrick.vico.views.cartesian.CartesianDrawingContext

@Suppress("DEPRECATION")
internal class PointConnectorAdapter(val pointConnector: LineCartesianLayer.PointConnector) :
  LineCartesianLayer.Interpolator {
  override fun interpolate(
    context: CartesianDrawingContext,
    path: Path,
    points: List<PointF>,
    visibleIndexRange: IntRange,
  ) {
    if (visibleIndexRange.isEmpty()) return
    val first = points[visibleIndexRange.first]
    path.moveTo(first.x, first.y)
    for (index in visibleIndexRange.first + 1..visibleIndexRange.last) {
      val prev = points[index - 1]
      val current = points[index]
      pointConnector.connect(context, path, prev.x, prev.y, current.x, current.y)
    }
  }
}
