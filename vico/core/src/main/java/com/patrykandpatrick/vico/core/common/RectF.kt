/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.common

import android.graphics.RectF
import androidx.annotation.RestrictTo
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun RectF.set(left: Number, top: Number, right: Number, bottom: Number) {
  set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}

internal fun RectF.clear() {
  set(0, 0, 0, 0)
}

internal fun RectF.copy(): RectF = RectF(this)

internal fun RectF.rotate(degrees: Float): RectF {
  when {
    degrees % PI_RAD == 0f -> Unit
    degrees % 0.5f.piRad == 0f -> {
      if (width() != height()) {
        set(
          left = centerX() - height().half,
          top = centerY() - width().half,
          right = centerX() + height().half,
          bottom = centerY() + width().half,
        )
      }
    }
    else -> {
      val alpha = Math.toRadians(degrees.toDouble())
      val sinAlpha = sin(alpha)
      val cosAlpha = cos(alpha)

      val newWidth = abs(width() * cosAlpha) + abs(height() * sinAlpha)
      val newHeight = abs(width() * sinAlpha) + abs(height() * cosAlpha)

      set(
        left = centerX() - newWidth.half,
        top = centerY() - newHeight.half,
        right = centerX() + newWidth.half,
        bottom = centerY() + newHeight.half,
      )
    }
  }

  return this
}

internal fun RectF.translate(x: Float, y: Float): RectF = apply {
  left += x
  top += y
  right += x
  bottom += y
}

internal fun RectF.getStart(isLtr: Boolean): Float = if (isLtr) left else right

internal fun RectF.getEnd(isLtr: Boolean): Float = if (isLtr) right else left
