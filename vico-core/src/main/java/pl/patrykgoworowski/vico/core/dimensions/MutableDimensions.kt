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

package pl.patrykgoworowski.vico.core.dimensions

class MutableDimensions(
    override var start: Float,
    override var top: Float,
    override var end: Float,
    override var bottom: Float,
) : Dimensions {

    val horizontal: Float
        get() = start + end

    val vertical: Float
        get() = top + bottom

    fun set(other: Dimensions) = set(other.start, other.top, other.end, other.bottom)

    fun set(all: Float) = set(all, all, all, all)

    fun set(
        start: Float = 0f,
        top: Float = 0f,
        end: Float = 0f,
        bottom: Float = 0f,
    ): Dimensions = apply {
        this.start = start
        this.top = top
        this.end = end
        this.bottom = bottom
    }

    fun setLeft(isLTR: Boolean, value: Float) = apply {
        if (isLTR) start = value
        else end = value
    }

    fun setRight(isLTR: Boolean, value: Float) = apply {
        if (isLTR) end = value
        else start = value
    }

    fun setHorizontal(value: Float) = apply {
        start = if (value == 0f) value else value / 2
        end = if (value == 0f) value else value / 2
    }

    fun setVertical(value: Float) = apply {
        top = if (value == 0f) value else value / 2
        bottom = if (value == 0f) value else value / 2
    }

    public fun clear() {
        set(0f)
    }
}

fun dimensionsOf(all: Float) = dimensionsOf(all, all, all, all)

fun dimensionsOf(
    start: Float = 0f,
    top: Float = 0f,
    end: Float = 0f,
    bottom: Float = 0f,
) = MutableDimensions(start, top, end, bottom)

fun emptyDimensions() = dimensionsOf()
