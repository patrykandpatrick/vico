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

import android.graphics.Color
import androidx.annotation.RestrictTo

private const val ALPHA_BIT_SHIFT = 24
private const val RED_BIT_SHIFT = 16
private const val GREEN_BIT_SHIFT = 8
private const val BLUE_BIT_SHIFT = 0
private const val COLOR_MASK = 0xff
private const val BYTE_MAX_VALUE: Int = 0xFF
internal const val MAX_HEX_VALUE = 255f

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun Int.copyColor(
    alpha: Int = this.extractColorChannel(ALPHA_BIT_SHIFT),
    red: Int = this.extractColorChannel(RED_BIT_SHIFT),
    green: Int = this.extractColorChannel(GREEN_BIT_SHIFT),
    blue: Int = this.extractColorChannel(BLUE_BIT_SHIFT),
): Int =
    alpha shl ALPHA_BIT_SHIFT or
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

internal val Int.isNotTransparent: Boolean
    get() = this != Color.TRANSPARENT

internal val Int.isTransparent: Boolean
    get() = this == Color.TRANSPARENT

private fun Int.extractColorChannel(bitShift: Int): Int = this shr bitShift and COLOR_MASK

private fun getElevationOverlayColorWithCorrectAlpha(
    elevationOverlayColor: Long,
    elevationDp: Float,
): Int {
    val overlayPercentage =
        when {
            elevationDp < 1f -> 0.00f
            elevationDp < 2f -> 0.05f
            elevationDp < 3f -> 0.07f
            elevationDp < 4f -> 0.08f
            elevationDp < 6f -> 0.09f
            elevationDp < 8f -> 0.11f
            elevationDp < 12f -> 0.12f
            elevationDp < 16f -> 0.14f
            elevationDp < 24f -> 0.15f
            else -> 0.16f
        }
    return if (elevationOverlayColor.alpha == 0f) {
        Color.TRANSPARENT
    } else {
        elevationOverlayColor.toInt().copyColor(alpha = overlayPercentage)
    }
}

internal fun DrawContext.applyElevationOverlayToColor(
    color: Int,
    elevationDp: Float,
): Int =
    color.overlayColor(
        getElevationOverlayColorWithCorrectAlpha(
            elevationOverlayColor = elevationOverlayColor,
            elevationDp = elevationDp,
        ),
    )

internal val Long.alpha: Float
    get() = if (this and 0x3fL == 0L) (this shr 56 and 0xff) / 255.0f else (this shr 6 and 0x3ff) / 1023.0f

internal fun Int.overlayColor(overlayingColor: Int): Int {
    val bgAlpha = Color.alpha(this)
    val fgAlpha = Color.alpha(overlayingColor)
    val alpha = compositeAlpha(fgAlpha, bgAlpha)
    val red = compositeComponent(Color.red(overlayingColor), fgAlpha, Color.red(this), bgAlpha, alpha)
    val green = compositeComponent(Color.green(overlayingColor), fgAlpha, Color.green(this), bgAlpha, alpha)
    val blue = compositeComponent(Color.blue(overlayingColor), fgAlpha, Color.blue(this), bgAlpha, alpha)
    return Color.argb(alpha, red, green, blue)
}

private fun compositeAlpha(
    foregroundAlpha: Int,
    backgroundAlpha: Int,
): Int = BYTE_MAX_VALUE - (BYTE_MAX_VALUE - backgroundAlpha) * (BYTE_MAX_VALUE - foregroundAlpha) / BYTE_MAX_VALUE

private fun compositeComponent(
    fgC: Int,
    fgA: Int,
    bgC: Int,
    bgA: Int,
    a: Int,
): Int = if (a == 0) 0 else (BYTE_MAX_VALUE * fgC * fgA + bgC * bgA * (BYTE_MAX_VALUE - fgA)) / (a * BYTE_MAX_VALUE)
