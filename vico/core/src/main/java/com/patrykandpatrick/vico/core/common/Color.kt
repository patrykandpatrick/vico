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

import androidx.annotation.RestrictTo

private const val ALPHA_BIT_SHIFT = 24
private const val RED_BIT_SHIFT = 16
private const val GREEN_BIT_SHIFT = 8
private const val BLUE_BIT_SHIFT = 0
private const val COLOR_MASK = 0xff
internal const val MAX_HEX_VALUE = 255f

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun Int.copyColor(
  alpha: Int = this.extractColorChannel(ALPHA_BIT_SHIFT),
  red: Int = this.extractColorChannel(RED_BIT_SHIFT),
  green: Int = this.extractColorChannel(GREEN_BIT_SHIFT),
  blue: Int = this.extractColorChannel(BLUE_BIT_SHIFT),
): Int =
  alpha shl
    ALPHA_BIT_SHIFT or
    (red shl RED_BIT_SHIFT) or
    (green shl GREEN_BIT_SHIFT) or
    (blue shl BLUE_BIT_SHIFT)

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun Int.copyColor(
  alpha: Float = this.extractColorChannel(ALPHA_BIT_SHIFT) / MAX_HEX_VALUE,
  red: Float = this.extractColorChannel(RED_BIT_SHIFT) / MAX_HEX_VALUE,
  green: Float = this.extractColorChannel(GREEN_BIT_SHIFT) / MAX_HEX_VALUE,
  blue: Float = this.extractColorChannel(BLUE_BIT_SHIFT) / MAX_HEX_VALUE,
): Int =
  copyColor(
    alpha = (alpha * MAX_HEX_VALUE).toInt(),
    red = (red * MAX_HEX_VALUE).toInt(),
    green = (green * MAX_HEX_VALUE).toInt(),
    blue = (blue * MAX_HEX_VALUE).toInt(),
  )

internal val Int.alpha: Int
  get() = extractColorChannel(ALPHA_BIT_SHIFT)

internal val Int.alphaFloat: Float
  get() = alpha / MAX_HEX_VALUE

private fun Int.extractColorChannel(bitShift: Int): Int = this shr bitShift and COLOR_MASK

internal val Long.alpha: Float
  get() =
    if (this and 0x3fL == 0L) (this shr 56 and 0xff) / 255.0f else (this shr 6 and 0x3ff) / 1023.0f
