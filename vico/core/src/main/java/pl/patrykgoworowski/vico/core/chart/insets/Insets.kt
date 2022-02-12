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

package pl.patrykgoworowski.vico.core.chart.insets

import pl.patrykgoworowski.vico.core.extension.half

/**
 * The class used to store insets specified by [ChartInsetter].
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
     * Returns a total value of horizontal insets.
     */
    public val horizontal: Float
        get() = start + end

    /**
     * Returns a total value of vertical insets.
     */
    public val vertical: Float
        get() = top + bottom

    /**
     * Sets inset values specified by other [Insets] instance.
     */
    public fun set(other: Insets): Insets = set(other.start, other.top, other.end, other.bottom)

    /**
     * Sets all inset values equal to [all].
     */
    public fun set(all: Float): Insets = set(all, all, all, all)

    /**
     * Sets insets for each side.
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
     * Sets [start] and [end] insets.
     */
    override fun set(start: Float, end: Float) {
        this.start = start
        this.end = end
    }

    /**
     * Returns left inset depending on layout direction.
     * @param isLtr true if layout is Left-to-Right, false otherwise.
     */
    public fun getLeft(isLtr: Boolean): Float = if (isLtr) start else end

    /**
     * Returns right inset depending on layout direction.
     * @param isLtr true if layout is Left-to-Right, false otherwise.
     */
    public fun getRight(isLtr: Boolean): Float = if (isLtr) end else start

    /**
     * Sets [start] and [end] insets.
     * The [value] defines a total width, thus each horizontal inset gets half of the [value].
     */
    public fun setHorizontal(value: Float): Insets = apply {
        start = value.half
        end = value.half
    }

    /**
     * Sets [top] and [bottom] insets.
     * The [value] defines a total height, thus each vertical inset gets half of the [value].
     */
    public fun setVertical(value: Float): Insets = apply {
        top = value.half
        bottom = value.half
    }

    /**
     * Sets all inset values to 0.
     */
    public fun clear() {
        set(0f)
    }
}
