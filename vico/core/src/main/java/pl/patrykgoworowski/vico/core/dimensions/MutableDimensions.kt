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

public class MutableDimensions(
    override var startDp: Float,
    override var topDp: Float,
    override var endDp: Float,
    override var bottomDp: Float,
) : Dimensions {

    /**
     * The sum of [startDp] and [endDp].
     */
    public val horizontalDp: Float
        get() = startDp + endDp

    /**
     * The sum of [topDp] and [bottomDp].
     */
    public val verticalDp: Float
        get() = topDp + bottomDp

    public fun set(other: Dimensions): MutableDimensions =
        set(other.startDp, other.topDp, other.endDp, other.bottomDp)

    public fun set(all: Float): MutableDimensions =
        set(all, all, all, all)

    public fun set(
        startDp: Float = 0f,
        topDp: Float = 0f,
        endDp: Float = 0f,
        bottomDp: Float = 0f,
    ): MutableDimensions = apply {
        this.startDp = startDp
        this.topDp = topDp
        this.endDp = endDp
        this.bottomDp = bottomDp
    }

    public fun setLeft(isLtr: Boolean, valueDp: Float): MutableDimensions = apply {
        if (isLtr) startDp = valueDp
        else endDp = valueDp
    }

    public fun setRight(isLtr: Boolean, valueDp: Float): MutableDimensions = apply {
        if (isLtr) endDp = valueDp
        else startDp = valueDp
    }

    /**
     * Evenly distributes the provided measurement between [startDp] and [endDp].
     */
    public fun setHorizontal(valueDp: Float): MutableDimensions = apply {
        startDp = if (valueDp == 0f) valueDp else valueDp / 2
        endDp = if (valueDp == 0f) valueDp else valueDp / 2
    }

    /**
     * Evenly distributes the provided measurement between [topDp] and [bottomDp].
     */
    public fun setVertical(valueDp: Float): MutableDimensions = apply {
        topDp = if (valueDp == 0f) valueDp else valueDp / 2
        bottomDp = if (valueDp == 0f) valueDp else valueDp / 2
    }

    /**
     * Sets all coordinates to 0.
     */
    public fun clear() {
        set(0f)
    }
}

public fun emptyDimensions(): MutableDimensions = MutableDimensions(0f, 0f, 0f, 0f)
