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

package com.patrykandpatryk.vico.core.chart.values

import com.patrykandpatryk.vico.core.axis.Axis
import com.patrykandpatryk.vico.core.chart.Chart
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.extension.round
import kotlin.math.abs

/**
 * Overrides minimum and maximum values on x-axis and y-axis displayed in [Chart] and [Axis].
 * It can be set in [com.patrykandpatryk.vico.view.chart.BaseChartView] and
 * [com.patrykandpatryk.vico.compose.chart.Chart] `@Composable` function.
 */
public interface AxisValuesOverrider<Model> {

    /**
     * The minimum value shown on the x-axis.
     *
     * @param model holds entries data which can be used to calculate the new min x-axis value.
     */
    public fun getMinX(model: Model): Float

    /**
     * The maximum value shown on the x-axis.
     *
     * @param model holds entries data which can be used to calculate the new max x-axis value.
     */
    public fun getMaxX(model: Model): Float

    /**
     * The minimum value shown on the y-axis.
     *
     * @param model holds entries data which can be used to calculate the new min y-axis value.
     */
    public fun getMinY(model: Model): Float

    /**
     * The maximum value shown on the y-axis.
     *
     * @param model holds entries data which can be used to calculate the new max y-axis value.
     */
    public fun getMaxY(model: Model): Float

    public companion object {

        /**
         * Creates an [AxisValuesOverrider] with fixed values for [minX], [maxX], [minY] and [maxY].
         */
        public fun fixed(
            minX: Float? = null,
            maxX: Float? = null,
            minY: Float? = null,
            maxY: Float? = null,
        ): AxisValuesOverrider<ChartEntryModel> = object : AxisValuesOverrider<ChartEntryModel> {

            override fun getMinX(model: ChartEntryModel): Float = minX ?: model.minX

            override fun getMaxX(model: ChartEntryModel): Float = maxX ?: model.maxX

            override fun getMinY(model: ChartEntryModel): Float = minY ?: minOf(model.minY, 0f)

            override fun getMaxY(model: ChartEntryModel): Float = maxY ?: model.maxY
        }

        /**
         * Creates an [AxisValuesOverrider] with adaptive min and max y-axis values.
         * The overridden max y-axis value is equal to `[ChartEntryModel.maxY] × [yFraction]`.
         * The overridden min y-axis value is smaller than [ChartEntryModel.minY] by
         * `[ChartEntryModel.maxY] × [yFraction] - [ChartEntryModel.maxY]`.
         */
        public fun adaptiveYValues(
            yFraction: Float,
            round: Boolean = false,
        ): AxisValuesOverrider<ChartEntryModel> = object : AxisValuesOverrider<ChartEntryModel> {

            init {
                require(yFraction > 0f)
            }

            override fun getMinX(model: ChartEntryModel): Float = model.minX

            override fun getMaxX(model: ChartEntryModel): Float = model.maxX

            override fun getMinY(model: ChartEntryModel): Float {
                val difference = abs(getMaxY(model) - model.maxY)
                return (model.minY - difference).maybeRound().coerceAtLeast(0f)
            }

            override fun getMaxY(model: ChartEntryModel): Float = (model.maxY * yFraction).maybeRound()

            private fun Float.maybeRound() = if (round) this.round else this
        }
    }
}
