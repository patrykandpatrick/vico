/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.multiplatform.common

import androidx.compose.ui.geometry.Rect
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

internal fun Rect.rotate(degrees: Float): Rect =
  when {
    degrees % PI_RAD == 0f -> this
    degrees % 0.5f.piRad == 0f -> {
      if (width != height) {
        Rect(
          left = center.x - height.half,
          top = center.y - width.half,
          right = center.x + height.half,
          bottom = center.y + width.half,
        )
      } else {
        this
      }
    }

    else -> {
      val alpha = degrees.toRadians()
      val sinAlpha = sin(alpha)
      val cosAlpha = cos(alpha)

      val newWidth = (abs(width * cosAlpha) + abs(height * sinAlpha)).toFloat()
      val newHeight = (abs(width * sinAlpha) + abs(height * cosAlpha)).toFloat()

      Rect(
        left = center.x - newWidth.half,
        top = center.y - newHeight.half,
        right = center.x + newWidth.half,
        bottom = center.y + newHeight.half,
      )
    }
  }

internal fun Rect.getStart(isLtr: Boolean): Float = if (isLtr) left else right

internal fun Rect.getEnd(isLtr: Boolean): Float = if (isLtr) right else left

internal fun Rect.extendBy(
  left: Float = 0f,
  top: Float = 0f,
  right: Float = 0f,
  bottom: Float = 0f,
): Rect =
  Rect(
    left = this.left - left,
    top = this.top - top,
    right = this.right + right,
    bottom = this.bottom + bottom,
  )
