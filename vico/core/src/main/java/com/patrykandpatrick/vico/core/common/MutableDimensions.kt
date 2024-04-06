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

/**
 * An implementation of [Dimensions] whose every property is mutable.
 */
public data class MutableDimensions(
    override var startDp: Float,
    override var topDp: Float,
    override var endDp: Float,
    override var bottomDp: Float,
) : Dimensions {
    public constructor(
        horizontalDp: Float,
        verticalDp: Float,
    ) : this(
        startDp = horizontalDp,
        topDp = verticalDp,
        endDp = horizontalDp,
        bottomDp = verticalDp,
    )

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

    /**
     * Updates these [MutableDimensions] to match the provided [Dimensions].
     */
    public fun set(other: Dimensions): MutableDimensions = set(other.startDp, other.topDp, other.endDp, other.bottomDp)

    /**
     * Sets a common value for each coordinate.
     */
    public fun set(allDp: Float): MutableDimensions = set(allDp, allDp, allDp, allDp)

    /**
     * Updates the coordinates to the provided values.
     */
    public fun set(
        startDp: Float = 0f,
        topDp: Float = 0f,
        endDp: Float = 0f,
        bottomDp: Float = 0f,
    ): MutableDimensions =
        apply {
            this.startDp = startDp
            this.topDp = topDp
            this.endDp = endDp
            this.bottomDp = bottomDp
        }

    /**
     * Evenly distributes the provided measurement between [startDp] and [endDp].
     */
    public fun setHorizontal(valueDp: Float): MutableDimensions =
        apply {
            startDp = if (valueDp == 0f) valueDp else valueDp / 2
            endDp = if (valueDp == 0f) valueDp else valueDp / 2
        }

    /**
     * Evenly distributes the provided measurement between [topDp] and [bottomDp].
     */
    public fun setVertical(valueDp: Float): MutableDimensions =
        apply {
            topDp = if (valueDp == 0f) valueDp else valueDp / 2
            bottomDp = if (valueDp == 0f) valueDp else valueDp / 2
        }

    /**
     * Sets all coordinates to 0.
     */
    public fun clear() {
        set(0f)
    }

    public companion object {
        /**
         * Creates a [Dimensions] instance with all coordinates set to 0.
         */
        public fun empty(): MutableDimensions = MutableDimensions(0f, 0f)

        /**
         * Creates a [MutableDimensions] instance with a common value for each coordinate.
         */
        public fun of(allDp: Float): MutableDimensions =
            of(
                startDp = allDp,
                topDp = allDp,
                endDp = allDp,
                bottomDp = allDp,
            )

        /**
         * Creates a [MutableDimensions] instance using the provided measurements.
         */
        public fun of(
            startDp: Float = 0f,
            topDp: Float = 0f,
            endDp: Float = 0f,
            bottomDp: Float = 0f,
        ): MutableDimensions =
            MutableDimensions(
                startDp = startDp,
                topDp = topDp,
                endDp = endDp,
                bottomDp = bottomDp,
            )

        /**
         * Creates a [MutableDimensions] instance using the provided measurements.
         */
        public fun of(
            horizontalDp: Float = 0f,
            verticalDp: Float = 0f,
        ): MutableDimensions =
            MutableDimensions(
                startDp = horizontalDp,
                topDp = verticalDp,
                endDp = horizontalDp,
                bottomDp = verticalDp,
            )
    }
}
