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

package pl.patrykgoworowski.vico.core.axis

/**
 * Defines a position of axis relative to the [pl.patrykgoworowski.vico.core.chart.Chart].
 */
public sealed class AxisPosition {

    /**
     * Returns true if position points to top of the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public val isTop: Boolean
        get() = this is Horizontal.Top

    /**
     * Returns true if position points to bottom of the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public val isBottom: Boolean
        get() = this is Horizontal.Bottom

    /**
     * Returns true if position points to start of the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public val isStart: Boolean
        get() = this is Vertical.Start

    /**
     * Returns true if position points to end of the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public val isEnd: Boolean
        get() = this is Vertical.End

    /**
     * Returns true if position points to left of the [pl.patrykgoworowski.vico.core.chart.Chart]
     * depending on layout direction.
     */
    public fun isLeft(isLtr: Boolean): Boolean =
        this is Vertical.Start && isLtr || this is Vertical.End && isLtr.not()

    /**
     * Returns true if position points to right of the [pl.patrykgoworowski.vico.core.chart.Chart]
     * depending on layout direction.
     */
    public fun isRight(isLtr: Boolean): Boolean =
        this is Vertical.End && isLtr || this is Vertical.Start && isLtr.not()

    /**
     * Defines possible positions of horizontal axis relative to the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public sealed class Horizontal : AxisPosition() {

        /**
         * Horizontal axis will be placed to the top of the [pl.patrykgoworowski.vico.core.chart.Chart].
         */
        public object Top : Horizontal()

        /**
         * Horizontal axis will be placed to the bottom of the [pl.patrykgoworowski.vico.core.chart.Chart].
         */
        public object Bottom : Horizontal()
    }

    /**
     * Defines possible positions of vertical axis relative to the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public sealed class Vertical : AxisPosition() {

        /**
         * Vertical axis will be placed to the start of the [pl.patrykgoworowski.vico.core.chart.Chart].
         */
        public object Start : Vertical()

        /**
         * Vertical axis will be placed to the end of the [pl.patrykgoworowski.vico.core.chart.Chart].
         */
        public object End : Vertical()
    }
}
