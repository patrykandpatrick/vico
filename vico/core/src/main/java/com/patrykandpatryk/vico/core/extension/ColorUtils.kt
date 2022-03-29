/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.core.extension

import android.graphics.Color

private const val BYTE_MAX_VALUE: Int = 0xFF

@Suppress("MagicNumber")
internal val Long.alpha: Float
    get() = if (this and 0x3fL == 0L) (this shr 56 and 0xff) / 255.0f
    else (this shr 6 and 0x3ff) / 1023.0f

internal fun Int.overlayColor(overlayingColor: Int): Int {
    val bgAlpha = Color.alpha(this)
    val fgAlpha = Color.alpha(overlayingColor)
    val alpha = compositeAlpha(fgAlpha, bgAlpha)
    val red = compositeComponent(
        Color.red(overlayingColor), fgAlpha,
        Color.red(this), bgAlpha, alpha
    )
    val green = compositeComponent(
        Color.green(overlayingColor), fgAlpha,
        Color.green(this), bgAlpha, alpha
    )
    val blue = compositeComponent(
        Color.blue(overlayingColor), fgAlpha,
        Color.blue(this), bgAlpha, alpha
    )
    return Color.argb(alpha, red, green, blue)
}

private fun compositeAlpha(foregroundAlpha: Int, backgroundAlpha: Int): Int =
    BYTE_MAX_VALUE - (BYTE_MAX_VALUE - backgroundAlpha) * (BYTE_MAX_VALUE - foregroundAlpha) / BYTE_MAX_VALUE

private fun compositeComponent(fgC: Int, fgA: Int, bgC: Int, bgA: Int, a: Int): Int =
    if (a == 0) 0 else (BYTE_MAX_VALUE * fgC * fgA + bgC * bgA * (BYTE_MAX_VALUE - fgA)) / (a * BYTE_MAX_VALUE)
