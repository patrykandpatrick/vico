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

package com.patrykandpatrick.vico.views.pie

import android.graphics.RectF
import com.patrykandpatrick.vico.views.common.Point
import com.patrykandpatrick.vico.views.common.half
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

internal const val FULL_DEGREES: Float = 360f

internal val Number.radians: Float
  get() = radiansDouble.toFloat()

internal val Number.radiansDouble: Double
  get() = Math.toRadians(toDouble())

internal val RectF.centerPoint: Point
  get() = Point(centerX(), centerY())

internal val RectF.radius: Float
  get() = width().half

internal val Int.isTransparent: Boolean
  get() = this ushr 24 == 0

internal val Int.isNotTransparent: Boolean
  get() = !isTransparent

internal fun translatePointByAngle(center: Point, point: Point, angle: Double): Point =
  Point(
    ((point.x - center.x) * cos(angle) - (point.y - center.y) * sin(angle) + center.x).toFloat(),
    ((point.y - center.y) * cos(angle) + (point.x - center.x) * sin(angle) + center.y).toFloat(),
  )

internal fun interpolateOpacity(fraction: Float): Float {
  require(fraction in 0f..1f) { "`fraction` must be in the range [0, 1]." }
  return abs(-2 * fraction + 1)
}

internal fun RectF.updateBounds(
  left: Float = this.left,
  top: Float = this.top,
  right: Float = this.right,
  bottom: Float = this.bottom,
) {
  set(left, top, right, bottom)
}

internal class PieInsets {
  public var start: Float = 0f
  public var top: Float = 0f
  public var end: Float = 0f
  public var bottom: Float = 0f

  public val largestEdge: Float
    get() = max(max(start, top), max(end, bottom))

  public fun clear() {
    start = 0f
    top = 0f
    end = 0f
    bottom = 0f
  }

  public fun setAllIfGreater(start: Float, top: Float, end: Float, bottom: Float) {
    this.start = max(this.start, start)
    this.top = max(this.top, top)
    this.end = max(this.end, end)
    this.bottom = max(this.bottom, bottom)
  }
}
