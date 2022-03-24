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

package pl.patrykgoworowski.vico.core.axis

/**
 * Defines the position of an axis relative to the [pl.patrykgoworowski.vico.core.chart.Chart].
 */
public sealed class AxisPosition {

    /**
     * Returns true if the position points to the top of the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public val isTop: Boolean
        get() = this is Horizontal.Top

    /**
     * Returns true if the position points to the bottom of the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public val isBottom: Boolean
        get() = this is Horizontal.Bottom

    /**
     * Returns true if the position points to the start of the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public val isStart: Boolean
        get() = this is Vertical.Start

    /**
     * Returns true if the position points to the end of the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public val isEnd: Boolean
        get() = this is Vertical.End

    /**
     * Returns true if the position points to the left of the [pl.patrykgoworowski.vico.core.chart.Chart],
     * depending on the layout direction.
     */
    public fun isLeft(isLtr: Boolean): Boolean =
        this is Vertical.Start && isLtr || this is Vertical.End && isLtr.not()

    /**
     * Returns true if the position points to the right of the [pl.patrykgoworowski.vico.core.chart.Chart],
     * depending on the layout direction.
     */
    public fun isRight(isLtr: Boolean): Boolean =
        this is Vertical.End && isLtr || this is Vertical.Start && isLtr.not()

    /**
     * Defines the possible positions of a horizontal axis relative to the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public sealed class Horizontal : AxisPosition() {

        /**
         * The horizontal axis will be placed at the top of the [pl.patrykgoworowski.vico.core.chart.Chart].
         */
        public object Top : Horizontal()

        /**
         * The horizontal axis will be placed at the bottom of the [pl.patrykgoworowski.vico.core.chart.Chart].
         */
        public object Bottom : Horizontal()
    }

    /**
     * Defines the possible positions of a vertical axis relative to the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public sealed class Vertical : AxisPosition() {

        /**
         * The vertical axis will be placed at the start of the [pl.patrykgoworowski.vico.core.chart.Chart].
         */
        public object Start : Vertical()

        /**
         * The vertical axis will be placed at the end of the [pl.patrykgoworowski.vico.core.chart.Chart].
         */
        public object End : Vertical()
    }
}
