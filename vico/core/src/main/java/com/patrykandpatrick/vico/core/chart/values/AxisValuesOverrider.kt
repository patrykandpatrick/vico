/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart.values

import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.round
import kotlin.math.abs

/**
 * Overrides a chart’s minimum and maximum x-axis and y-axis values.
 * This can be used with [com.patrykandpatrick.vico.views.chart.BaseChartView] and the
 * [com.patrykandpatrick.vico.compose.chart.Chart] composable.
 */
public interface AxisValuesOverrider<Model> {

    /**
     * The minimum value shown on the x-axis. If `null` is returned, the chart will fall back to the default value.
     *
     * @param model holds data about the chart’s entries, which can be used to calculate the new minimum x-axis value.
     */
    public fun getMinX(model: Model): Float? = null

    /**
     * The maximum value shown on the x-axis. If `null` is returned, the chart will fall back to the default value.
     *
     * @param model holds data about the chart’s entries, which can be used to calculate the new maximum x-axis value.
     */
    public fun getMaxX(model: Model): Float? = null

    /**
     * The minimum value shown on the y-axis. If `null` is returned, the chart will fall back to the default value.
     *
     * @param model holds data about the chart’s entries, which can be used to calculate the new minimum y-axis value.
     */
    public fun getMinY(model: Model): Float? = null

    /**
     * The maximum value shown on the y-axis. If `null` is returned, the chart will fall back to the default value.
     *
     * @param model holds data about the chart’s entries, which can be used to calculate the new maximum y-axis value.
     */
    public fun getMaxY(model: Model): Float? = null

    public companion object {

        /**
         * Creates an [AxisValuesOverrider] with fixed values for [minX], [maxX], [minY], and [maxY]. If one of the
         * values is `null`, the chart will fall back to the default value.
         */
        public fun fixed(
            minX: Float? = null,
            maxX: Float? = null,
            minY: Float? = null,
            maxY: Float? = null,
        ): AxisValuesOverrider<ChartEntryModel> = object : AxisValuesOverrider<ChartEntryModel> {

            override fun getMinX(model: ChartEntryModel): Float? = minX

            override fun getMaxX(model: ChartEntryModel): Float? = maxX

            override fun getMinY(model: ChartEntryModel): Float? = minY

            override fun getMaxY(model: ChartEntryModel): Float? = maxY
        }

        /**
         * Creates an [AxisValuesOverrider] with adaptive minimum and maximum y-axis values. The overridden maximum
         * y-axis value is equal to [ChartEntryModel.maxY] × [yFraction]. The overridden minimum y-axis value is smaller
         * than [ChartEntryModel.minY] by [ChartEntryModel.maxY] × [yFraction] − [ChartEntryModel.maxY].
         */
        public fun adaptiveYValues(
            yFraction: Float,
            round: Boolean = false,
        ): AxisValuesOverrider<ChartEntryModel> = object : AxisValuesOverrider<ChartEntryModel> {

            init {
                require(yFraction > 0f)
            }

            override fun getMinY(model: ChartEntryModel): Float {
                val difference = abs(getMaxY(model) - model.maxY)
                return (model.minY - difference).maybeRound().coerceAtLeast(0f)
            }

            override fun getMaxY(model: ChartEntryModel): Float = (model.maxY * yFraction).maybeRound()

            private fun Float.maybeRound() = if (round) this.round else this
        }
    }
}
