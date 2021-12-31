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

private const val MAX_DEGREES = 360

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
    if (degrees % MAX_DEGREES == 0f) return this
    val radians = Math.toRadians(degrees.toDouble())
    // Top-left
    val x1 = rotatePointX(left, top, centerX(), centerY(), radians)
    val y1 = rotatePointY(left, top, centerX(), centerY(), radians)
    // Top-right
    val x2 = rotatePointX(right, top, centerX(), centerY(), radians)
    val y2 = rotatePointY(right, top, centerX(), centerY(), radians)
    // Bottom-right
    val x3 = rotatePointX(right, bottom, centerX(), centerY(), radians)
    val y3 = rotatePointY(right, bottom, centerX(), centerY(), radians)
    // Bottom-left
    val x4 = rotatePointX(left, bottom, centerX(), centerY(), radians)
    val y4 = rotatePointY(left, bottom, centerX(), centerY(), radians)

    set(
        left = minOf(x1, x2, x3, x4),
        top = minOf(y1, y2, y3, y4),
        right = maxOf(x1, x2, x3, x4),
        bottom = maxOf(y1, y2, y3, y4),
    )
    return this
}
