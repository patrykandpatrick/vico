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

package com.patrykandpatrick.vico.core.common

import android.graphics.Canvas
import kotlin.math.roundToInt

internal inline fun Canvas.inClip(
  left: Float,
  top: Float,
  right: Float,
  bottom: Float,
  block: () -> Unit,
) {
  val clipRestoreCount = save()
  clipRect(left, top, right, bottom)
  block()
  restoreToCount(clipRestoreCount)
}

internal fun Canvas.saveLayer(): Int = saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

internal fun Canvas.saveLayer(opacity: Float): Int =
  saveLayerAlpha(0f, 0f, width.toFloat(), height.toFloat(), (opacity * MAX_HEX_VALUE).roundToInt())
