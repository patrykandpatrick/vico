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

public sealed class Corner(
    public val cornerTreatment: CornerTreatment,
) {

    public abstract fun getCornerSize(availableCornerSize: Float, density: Float): Float

    public class Absolute(
        public val sizeDp: Float,
        cornerTreatment: CornerTreatment,
    ) : Corner(cornerTreatment) {

        override fun getCornerSize(availableCornerSize: Float, density: Float): Float =
            sizeDp * density
    }

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

        public val FullyRounded: Corner = Relative(MAX_PERCENTAGE, RoundedCornerTreatment)
        public val Sharp: Corner = Relative(0, SharpCornerTreatment)
    }
}
