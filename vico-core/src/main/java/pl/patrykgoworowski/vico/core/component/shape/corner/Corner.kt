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

package pl.patrykgoworowski.vico.core.component.shape.corner

import pl.patrykgoworowski.vico.core.throwable.IllegalPercentageException

sealed class Corner(
    public val absoluteSize: Float,
    public val cornerTreatment: CornerTreatment,
) {

    public abstract fun getCornerSize(availableCornerSize: Float): Float

    public class Absolute(
        size: Float,
        cornerTreatment: CornerTreatment,
    ) : Corner(size, cornerTreatment) {

        override fun getCornerSize(availableCornerSize: Float): Float = absoluteSize
    }

    public class Relative(
        public val percentage: Int,
        cornerTreatment: CornerTreatment,
    ) : Corner(0f, cornerTreatment) {

        init {
            if (percentage !in 0..MAX_PERCENTAGE) throw IllegalPercentageException(percentage)
        }

        override fun getCornerSize(availableCornerSize: Float): Float =
            availableCornerSize / MAX_PERCENTAGE * percentage
    }

    companion object {
        private const val MAX_PERCENTAGE = 100
    }
}
