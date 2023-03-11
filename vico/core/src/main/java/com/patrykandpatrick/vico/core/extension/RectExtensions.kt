/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.extension

import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Updates the coordinates of this [RectF].
 */
public fun RectF.updateBounds(
    left: Float = this.left,
    top: Float = this.top,
    right: Float = this.right,
    bottom: Float = this.bottom,
) {
    set(left, top, right, bottom)
}

/**
 * Increments the coordinates of this [RectF] by the provided values.
 */
public fun RectF.updateBy(
    left: Float = 0f,
    top: Float = 0f,
    right: Float = 0f,
    bottom: Float = 0f,
) {
    set(
        left = this.left + left,
        top = this.top + top,
        right = this.right + right,
        bottom = this.bottom + bottom,
    )
}

/**
 * Sets the coordinates of this [RectF] to the provided values converted to [Float]s.
 */
public fun RectF.set(
    left: Number,
    top: Number,
    right: Number,
    bottom: Number,
) {
    set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}

/**
 * Sets all coordinates of this [RectF] to 0.
 */
public fun RectF.clear() {
    set(0, 0, 0, 0)
}

/**
 * Applies the provided coordinates to this [RectF] and rotates it by the given number of degrees.
 */
public fun RectF.setAndRotate(
    left: Number,
    top: Number,
    right: Number,
    bottom: Number,
    rotationDegrees: Float,
): RectF {
    set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    return rotate(rotationDegrees)
}

/**
 * Creates a new [RectF] with the same coordinates as this [RectF] without modifying this [RectF].
 */
public fun RectF.copy(): RectF = RectF(this)

/**
 * Creates a [RectF] representing the bounding box of this [RectF] rotated by the provided number of degrees.
 */
public fun RectF.rotate(degrees: Float): RectF {

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

/**
 * Moves this [RectF] horizontally and vertically by the specified distances.
 */
public fun RectF.translate(x: Float, y: Float): RectF = apply {
    left += x
    top += y
    right += x
    bottom += y
}

/**
 * Returns [RectF.left] if [isLtr] is true, and [RectF.right] otherwise.
 */
public fun RectF.getStart(isLtr: Boolean): Float = if (isLtr) left else right

/**
 * Returns [RectF.right] if [isLtr] is true, and [RectF.left] otherwise.
 */
public fun RectF.getEnd(isLtr: Boolean): Float = if (isLtr) right else left
