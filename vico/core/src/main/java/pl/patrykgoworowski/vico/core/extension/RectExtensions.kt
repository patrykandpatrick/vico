/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.extension

import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

public fun RectF.updateBounds(
    left: Float = this.left,
    top: Float = this.top,
    right: Float = this.right,
    bottom: Float = this.bottom
) {
    set(left, top, right, bottom)
}

public fun RectF.updateBy(
    left: Float = 0f,
    top: Float = 0f,
    right: Float = 0f,
    bottom: Float = 0f
) {
    set(
        left = this.left + left,
        top = this.top + top,
        right = this.right + right,
        bottom = this.bottom + bottom
    )
}

public fun RectF.set(
    left: Number,
    top: Number,
    right: Number,
    bottom: Number
) {
    set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}

public val RectF.isNotEmpty: Boolean
    get() = left != 0f && top != 0f && right != 0f && bottom != 0f

public fun RectF.clear() {
    set(0, 0, 0, 0)
}

public fun RectF.set(
    isLtr: Boolean,
    left: Number = this.left,
    top: Number = this.top,
    right: Number = this.right,
    bottom: Number = this.bottom,
) {
    set(
        if (isLtr) left.toFloat() else right.toFloat(),
        top.toFloat(),
        if (isLtr) right.toFloat() else left.toFloat(),
        bottom.toFloat()
    )
}

public fun RectF.start(isLtr: Boolean): Float = if (isLtr) left else right

public fun RectF.end(isLtr: Boolean): Float = if (isLtr) right else left

public fun RectF.rotate(degrees: Float): RectF {

    when {
        degrees % PI_RAD == 0f -> Unit
        degrees % 0.5f.piRad == 0f -> {
            if (width() != height()) {
                set(
                    left = centerX() - height().half,
                    top = centerY() + width().half,
                    right = centerX() + height().half,
                    bottom = centerY() - width().half,
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
                top = centerY() + newHeight.half,
                right = centerX() + newWidth.half,
                bottom = centerY() - newHeight.half
            )
        }
    }

    return this
}

public fun RectF.translate(x: Float, y: Float): RectF = apply {
    left += x
    top += y
    right += x
    bottom += y
}
