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

public class Insets(
    public var start: Float = 0f,
    public var top: Float = 0f,
    public var end: Float = 0f,
    public var bottom: Float = 0f,
) {

    public val horizontal: Float
        get() = start + end

    public val vertical: Float
        get() = top + bottom

    public fun set(other: Insets): Insets = set(other.start, other.top, other.end, other.bottom)

    public fun set(all: Float): Insets = set(all, all, all, all)

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

    public fun getLeft(isLtr: Boolean): Float = if (isLtr) start else end

    public fun getRight(isLtr: Boolean): Float = if (isLtr) end else start

    public fun setHorizontal(value: Float): Insets = apply {
        start = if (value == 0f) value else value / 2
        end = if (value == 0f) value else value / 2
    }

    public fun setVertical(value: Float): Insets = apply {
        top = if (value == 0f) value else value / 2
        bottom = if (value == 0f) value else value / 2
    }

    public fun clear() {
        set(0f)
    }
}
