/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart.pie

import com.patrykandpatrick.vico.core.chart.pie.slice.Slice
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.half

/**
 * Defines the size of a component.
 */
public interface Size {

    /**
     * Returns the radius of a [Slice] based on the available width and height.
     */
    public fun getRadius(
        context: MeasureContext,
        availableWidth: Float,
        availableHeight: Float,
    ): Float

    /**
     * Defines the size of the outer part of a [Slice].
     */
    public interface OuterSize : Size {

        public companion object {

            /**
             * Fills the available space.
             */
            public fun fill(): OuterSize = Fill

            /**
             * Creates a fixed [OuterSize], with the size specified by the [maxDiameterDp].
             * If the available space is smaller than [maxDiameterDp], all available space is used.
             */
            public fun fixed(maxDiameterDp: Float): OuterSize = Fixed(maxDiameterDp)
        }
    }

    /**
     * Defines the size of the inner (donut) part of a [Slice].
     */
    public interface InnerSize : Size {

        public companion object {

            /**
             * Creates a fixed size with the value of 0.
             */
            public fun zero(): InnerSize = fixed(maxDiameterDp = 0f)

            /**
             * Creates a fixed [InnerSize], with the size specified by the [maxDiameterDp].
             * If the available space is smaller than [maxDiameterDp], all available space is used.
             */
            public fun fixed(maxDiameterDp: Float): InnerSize = Fixed(maxDiameterDp)
        }
    }

    private object Fill : OuterSize {

        override fun getRadius(
            context: MeasureContext,
            availableWidth: Float,
            availableHeight: Float,
        ): Float = minOf(availableWidth, availableHeight).half
    }

    private class Fixed(public val maxDiameterDp: Float) : OuterSize, InnerSize {

        init {
            require(maxDiameterDp >= 0f) { "The max diameter cannot be negative." }
        }

        override fun getRadius(
            context: MeasureContext,
            availableWidth: Float,
            availableHeight: Float,
        ): Float = with(context) {
            minOf(availableWidth, availableHeight, maxDiameterDp.pixels).half
        }
    }
}
