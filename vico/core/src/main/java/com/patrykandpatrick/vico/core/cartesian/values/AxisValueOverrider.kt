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

package com.patrykandpatrick.vico.core.cartesian.values

import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.cartesian.model.CartesianLayerModel
import com.patrykandpatrick.vico.core.common.extension.round
import kotlin.math.abs

/**
 * Overrides a [CartesianLayer]’s _x_ and _y_ ranges.
 */
public interface AxisValueOverrider<T> {
    /**
     * The minimum value shown on the x-axis. If `null` is returned, the chart will fall back to the default value.
     *
     * @param model holds data about the chart’s entries, which can be used to calculate the new minimum x-axis value.
     */
    public fun getMinX(model: T): Float? = null

    /**
     * The maximum value shown on the x-axis. If `null` is returned, the chart will fall back to the default value.
     *
     * @param model holds data about the chart’s entries, which can be used to calculate the new maximum x-axis value.
     */
    public fun getMaxX(model: T): Float? = null

    /**
     * The minimum value shown on the y-axis. If `null` is returned, the chart will fall back to the default value.
     *
     * @param model holds data about the chart’s entries, which can be used to calculate the new minimum y-axis value.
     */
    public fun getMinY(model: T): Float? = null

    /**
     * The maximum value shown on the y-axis. If `null` is returned, the chart will fall back to the default value.
     *
     * @param model holds data about the chart’s entries, which can be used to calculate the new maximum y-axis value.
     */
    public fun getMaxY(model: T): Float? = null

    public companion object {
        /**
         * Creates an [AxisValueOverrider] with fixed values for [minX], [maxX], [minY], and [maxY]. If one of the
         * values is `null`, the chart will fall back to the default value.
         */
        public fun <T : CartesianLayerModel> fixed(
            minX: Float? = null,
            maxX: Float? = null,
            minY: Float? = null,
            maxY: Float? = null,
        ): AxisValueOverrider<T> =
            object : AxisValueOverrider<T> {
                override fun getMinX(model: T): Float? = minX

                override fun getMaxX(model: T): Float? = maxX

                override fun getMinY(model: T): Float? = minY

                override fun getMaxY(model: T): Float? = maxY
            }

        /**
         * Creates an [AxisValueOverrider] with adaptive minimum and maximum y-axis values. The overridden maximum
         * y-axis value is equal to [CartesianLayerModel.maxY] × [yFraction]. The overridden minimum y-axis value is
         * smaller than [CartesianLayerModel.minY] by [CartesianLayerModel.maxY] × [yFraction] −
         * [CartesianLayerModel.maxY].
         */
        public fun <T : CartesianLayerModel> adaptiveYValues(
            yFraction: Float,
            round: Boolean = false,
        ): AxisValueOverrider<T> =
            object : AxisValueOverrider<T> {
                init {
                    require(yFraction > 0f)
                }

                override fun getMinY(model: T): Float {
                    val difference = abs(getMaxY(model) - model.maxY)
                    return (model.minY - difference).maybeRound().coerceAtLeast(0f)
                }

                override fun getMaxY(model: T): Float = (model.maxY * yFraction).maybeRound()

                private fun Float.maybeRound() = if (round) this.round else this
            }
    }
}
