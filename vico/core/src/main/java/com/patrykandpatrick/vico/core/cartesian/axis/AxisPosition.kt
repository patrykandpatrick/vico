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

package com.patrykandpatrick.vico.core.cartesian.axis

import com.patrykandpatrick.vico.core.cartesian.CartesianChart

/**
 * Defines the position of an axis relative to its [CartesianChart].
 */
public sealed class AxisPosition {
    /**
     * Whether the axis is at the top of its [CartesianChart].
     */
    public val isTop: Boolean
        get() = this is Horizontal.Top

    /**
     * Whether the axis is at the bottom of its [CartesianChart].
     */
    public val isBottom: Boolean
        get() = this is Horizontal.Bottom

    /**
     * Whether the axis is at the start of its [CartesianChart].
     */
    public val isStart: Boolean
        get() = this is Vertical.Start

    /**
     * Whether the axis is at the end of its [CartesianChart].
     */
    public val isEnd: Boolean
        get() = this is Vertical.End

    /**
     * Whether the axis is on the left of its [CartesianChart]. The layout direction is considered here.
     */
    public fun isLeft(isLtr: Boolean): Boolean = this is Vertical.Start && isLtr || this is Vertical.End && isLtr.not()

    /**
     * Whether the axis is on the right of its [CartesianChart]. The layout direction is considered here.
     */
    public fun isRight(isLtr: Boolean): Boolean = this is Vertical.End && isLtr || this is Vertical.Start && isLtr.not()

    /**
     * Defines the position of a horizontal axis relative to its [CartesianChart].
     */
    public sealed class Horizontal : AxisPosition() {
        /**
         * The horizontal axis will be placed at the top of its [CartesianChart].
         */
        public object Top : Horizontal()

        /**
         * The horizontal axis will be placed at the bottom of its [CartesianChart].
         */
        public object Bottom : Horizontal()
    }

    /**
     * Defines the position of a vertical axis relative to its [CartesianChart].
     */
    public sealed class Vertical : AxisPosition() {
        /**
         * The vertical axis will be placed at the start of its [CartesianChart].
         */
        public object Start : Vertical()

        /**
         * The vertical axis will be placed at the end of its [CartesianChart].
         */
        public object End : Vertical()
    }
}
