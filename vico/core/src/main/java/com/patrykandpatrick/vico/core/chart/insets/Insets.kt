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

package com.patrykandpatrick.vico.core.chart.insets

import com.patrykandpatrick.vico.core.extension.half
import kotlin.math.max

/**
 * Used to store the insets requested by [ChartInsetter]s.
 *
 * @param start the start inset.
 * @param top the top inset.
 * @param end the end inset.
 * @param bottom the bottom inset.
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
     * The sum of the sizes of the start inset and the end inset.
     */
    public val horizontal: Float
        get() = start + end

    /**
     * The sum of the sizes of the top inset and the bottom inset.
     */
    public val vertical: Float
        get() = top + bottom

    /**
     * Updates the size of each of the four insets to match the size of its corresponding inset from the provided
     * [Insets] instance.
     */
    public fun set(other: Insets): Insets = set(other.start, other.top, other.end, other.bottom)

    /**
     * Sets a common size for all four insets.
     */
    public fun set(all: Float): Insets = set(all, all, all, all)

    /**
     * Updates the size of each of the four insets individually.
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
     * Updates the sizes of the start inset and the end inset.
     */
    override fun set(start: Float, end: Float) {
        this.start = start
        this.end = end
    }

    /**
     * Returns the size of the left inset, taking into account the layout direction.
     *
     * @param isLtr whether the layout is left-to-right.
     */
    public fun getLeft(isLtr: Boolean): Float = if (isLtr) start else end

    /**
     * Returns the size of the right inset, taking into account the layout direction.
     *
     * @param isLtr whether the layout is left-to-right.
     */
    public fun getRight(isLtr: Boolean): Float = if (isLtr) end else start

    /**
     * Sets the sizes of the start inset and the end inset. [value] represents the sum of the two insets’ sizes, meaning
     * the size of either inset will be half of [value].
     */
    public fun setHorizontal(value: Float): Insets = apply {
        start = value.half
        end = value.half
    }

    /**
     * Sets the sizes of the top inset and the bottom inset. [value] represents the sum of the two insets’ sizes,
     * meaning the size of either inset will be half of [value].
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
     * For each of the four insets, updates the size of the inset to the size of the corresponding inset from the
     * provided [Insets] instance if the size of the corresponding inset from the provided [Insets] instance is greater.
     */
    public fun setValuesIfGreater(other: Insets) {
        start = max(start, other.start)
        top = max(top, other.top)
        end = max(end, other.end)
        bottom = max(bottom, other.bottom)
    }

    /**
     * For each of the four insets, updates the size of the inset to the corresponding provided value if the
     * corresponding provided value is greater.
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
     * Sets the size of each of the four insets to zero.
     */
    public fun clear() {
        set(0f)
    }
}
