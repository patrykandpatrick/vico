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

package pl.patrykgoworowski.vico.core.component.shape.cornered

import pl.patrykgoworowski.vico.core.throwable.IllegalPercentageException

/**
 * The class used to specify a size and look of given corner of a shape.
 *
 * @param cornerTreatment affects the final appearance of the corner.
 */
public sealed class Corner(
    public val cornerTreatment: CornerTreatment,
) {

    /**
     * Calculates a size of the corner.
     *
     * @param availableCornerSize the available space that this corner can take.
     * @param density the density of the screen used in pixel size calculation.
     *
     * @return pixel size of the corner.
     */
    public abstract fun getCornerSize(availableCornerSize: Float, density: Float): Float

    /**
     * Defines an absolute size of the corner in the dp unit.
     *
     * @param sizeDp the size of the corner in the dp unit.
     */
    public class Absolute(
        public val sizeDp: Float,
        cornerTreatment: CornerTreatment,
    ) : Corner(cornerTreatment) {

        override fun getCornerSize(availableCornerSize: Float, density: Float): Float =
            sizeDp * density
    }

    /**
     * Defines a relative size of the corner expressed in percent value.
     *
     * @param percentage the percentage of available space for corner that will be used as its size.
     */
    public class Relative(
        public val percentage: Int,
        cornerTreatment: CornerTreatment,
    ) : Corner(cornerTreatment) {

        init {
            if (percentage !in 0..MAX_PERCENTAGE) throw IllegalPercentageException(percentage)
        }

        override fun getCornerSize(availableCornerSize: Float, density: Float): Float =
            availableCornerSize / MAX_PERCENTAGE * percentage
    }

    public companion object {
        private const val MAX_PERCENTAGE = 100

        /**
         * [Corner] which is completely rounded.
         */
        public val FullyRounded: Corner = Relative(MAX_PERCENTAGE, RoundedCornerTreatment)

        /**
         * [Corner] which has sharp corners.
         */
        public val Sharp: Corner = Relative(0, SharpCornerTreatment)
    }
}
