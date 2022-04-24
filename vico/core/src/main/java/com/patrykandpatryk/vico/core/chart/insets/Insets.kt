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

package com.patrykandpatryk.vico.core.chart.insets

import com.patrykandpatryk.vico.core.extension.half
import kotlin.math.max

/**
 * The class used to store insets specified by [ChartInsetter].
 *
 * @param start defines an inset at the start of the given rectangle.
 * @param top defines an inset at the top of the given rectangle.
 * @param end defines an inset at the end of the given rectangle.
 * @param bottom defines an inset at the bottom of the given rectangle.
 *
 * @see ChartInsetter
 */
public class Insets(
    public var start: Float = 0f,
    public var top: Float = 0f,
    public var end: Float = 0f,
    public var bottom: Float = 0f,
) : HorizontalInsets {

    /**
     * Returns the total value of the horizontal insets.
     */
    public val horizontal: Float
        get() = start + end

    /**
     * Returns the total value of the vertical insets.
     */
    public val vertical: Float
        get() = top + bottom

    /**
     * Sets the inset values specified by another [Insets] instance.
     */
    public fun set(other: Insets): Insets = set(other.start, other.top, other.end, other.bottom)

    /**
     * Sets all inset values to [all].
     */
    public fun set(all: Float): Insets = set(all, all, all, all)

    /**
     * Sets the insets for each side.
     */
    public fun set(
        start: Float = 0f,
        top: Float = 0f,
        end: Float = 0f,
        bottom: Float = 0f,
    ): Insets = apply {
        this.start = start
        this.top = top
        this.end = end
        this.bottom = bottom
    }

    /**
     * Sets the [start] and [end] insets.
     */
    override fun set(start: Float, end: Float) {
        this.start = start
        this.end = end
    }

    /**
     * Returns the left inset, depending on the layout direction.
     * @param isLtr true if layout is Left-to-Right, false otherwise.
     */
    public fun getLeft(isLtr: Boolean): Float = if (isLtr) start else end

    /**
     * Returns the right inset, depending the on layout direction.
     * @param isLtr true if layout is Left-to-Right, false otherwise.
     */
    public fun getRight(isLtr: Boolean): Float = if (isLtr) end else start

    /**
     * Sets the [start] and [end] insets.
     * The [value] defines a total width. Thus, each horizontal inset gets half of the [value].
     */
    public fun setHorizontal(value: Float): Insets = apply {
        start = value.half
        end = value.half
    }

    /**
     * Sets the [top] and [bottom] insets.
     * The [value] defines a total height. Thus, each vertical inset gets half of the [value].
     */
    public fun setVertical(value: Float): Insets = apply {
        top = value.half
        bottom = value.half
    }

    override fun setValuesIfGreater(start: Float, end: Float) {
        this.start = max(start, this.start)
        this.end = max(end, this.end)
    }

    /**
     * For each of these [Insets]ʼ four values, updates the value to the corresponding value
     * from [other] if the value from [other] is greater than the current value.
     */
    public fun setValuesIfGreater(other: Insets) {
        start = max(start, other.start)
        top = max(top, other.top)
        end = max(end, other.end)
        bottom = max(bottom, other.bottom)
    }

    /**
     * For each of these [Insets]ʼ four values, updates the value to the corresponding provided
     * value if the provided value is greater than the current value.
     */
    public fun setAllIfGreater(
        start: Float = this.start,
        top: Float = this.top,
        end: Float = this.end,
        bottom: Float = this.bottom,
    ) {
        this.start = max(start, this.start)
        this.top = max(top, this.top)
        this.end = max(end, this.end)
        this.bottom = max(bottom, this.bottom)
    }

    /**
     * Sets all inset values to 0.
     */
    public fun clear() {
        set(0f)
    }
}
