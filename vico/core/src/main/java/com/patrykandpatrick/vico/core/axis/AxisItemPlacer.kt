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

package com.patrykandpatrick.vico.core.axis

import com.patrykandpatrick.vico.core.axis.horizontal.DefaultHorizontalAxisItemPlacer
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.context.MeasureContext

/**
 * Determines for what values an [Axis] is to display labels, ticks, and guidelines.
 */
public interface AxisItemPlacer {
    /**
     * An [AxisItemPlacer] subinterface for [HorizontalAxis] instances.
     */
    public interface Horizontal : AxisItemPlacer {
        /**
         * Returns, as a list, the _x_ values for which labels are to be displayed, restricted to [visibleXRange] and
         * with two extra values on either side (if applicable).
         */
        public fun getLabelValues(
            context: ChartDrawContext,
            visibleXRange: ClosedFloatingPointRange<Float>,
            fullXRange: ClosedFloatingPointRange<Float>,
        ): List<Float>

        /**
         * Returns, as a list, the _x_ values for which labels are to be created and measured during the measuring
         * phase. This affects how much vertical space the [HorizontalAxis] requests.
         */
        public fun getMeasuredLabelValues(
            context: MeasureContext,
            horizontalDimensions: HorizontalDimensions,
            fullXRange: ClosedFloatingPointRange<Float>,
        ): List<Float>

        /**
         * Returns the smallest expected distance between a label measured during the measuring phase and the next
         * label or the previous label, whichever is closer to the measured label. This distance is expressed as the
         * difference between the two labels’ _x_ values divided by [ChartValues.xStep].
         */
        public fun getMeasuredLabelClearance(
            context: MeasureContext,
            horizontalDimensions: HorizontalDimensions,
            fullXRange: ClosedFloatingPointRange<Float>,
        ): Float

        /**
         * Returns, as a list, the _x_ values for which ticks and guidelines are to be displayed, restricted to
         * [visibleXRange] and with an extra value on either side (if applicable). If `null` is returned, the values
         * returned by [getLabelValues] are used.
         */
        public fun getLineValues(
            context: ChartDrawContext,
            visibleXRange: ClosedFloatingPointRange<Float>,
            fullXRange: ClosedFloatingPointRange<Float>,
        ): List<Float>?

        /**
         * Returns the start inset required by the [HorizontalAxis].
         */
        public fun getStartHorizontalAxisInset(
            context: MeasureContext,
            horizontalDimensions: HorizontalDimensions,
            tickThickness: Float,
        ): Float

        /**
         * Returns the end inset required by the [HorizontalAxis].
         */
        public fun getEndHorizontalAxisInset(
            context: MeasureContext,
            horizontalDimensions: HorizontalDimensions,
            tickThickness: Float,
        ): Float

        public companion object {
            /**
             * Creates a base [AxisItemPlacer.Horizontal] implementation. [spacing] defines how often items should be
             * drawn (relative to [ChartValues.xStep]). [offset] is the number of labels (and, for
             * [HorizontalLayout.FullWidth], their corresponding ticks and guidelines) to skip from the start.
             */
            public fun default(spacing: Int = 1, offset: Int = 0): Horizontal =
                DefaultHorizontalAxisItemPlacer(spacing, offset)
        }
    }
}
