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

import com.patrykandpatrick.vico.core.DEF_LABEL_COUNT
import com.patrykandpatrick.vico.core.axis.horizontal.DefaultHorizontalAxisItemPlacer
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.axis.vertical.DefaultVerticalAxisItemPlacer
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.Chart
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
         * Whether ticks whose _x_ values are bounds of the _x_-axis value range should be shifted to the edges of the
         * axis bounds, to be aligned with the vertical axes.
         */
        public fun getShiftExtremeTicks(context: ChartDrawContext): Boolean = true

        /**
         * Returns a boolean indicating whether the [HorizontalAxis] should reserve room for a label for
         * [ChartValues.minX]. If `true` is returned, indicating that this behavior is desired, then [getLabelValues]
         * should request a label for [ChartValues.minX].
         */
        public fun getAddFirstLabelPadding(context: MeasureContext): Boolean

        /**
         * Returns a boolean indicating whether the [HorizontalAxis] should reserve room for a label for
         * [ChartValues.maxX]. If `true` is returned, indicating that this behavior is desired, then [getLabelValues]
         * should request a label for [ChartValues.maxX].
         */
        public fun getAddLastLabelPadding(context: MeasureContext): Boolean

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
         * Returns, as a list, the _x_ values for which ticks and guidelines are to be displayed, restricted to
         * [visibleXRange] and with an extra value on either side (if applicable). If `null` is returned, the values
         * returned by [getLabelValues] are used.
         */
        public fun getLineValues(
            context: ChartDrawContext,
            visibleXRange: ClosedFloatingPointRange<Float>,
            fullXRange: ClosedFloatingPointRange<Float>,
        ): List<Float>? = null

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
             * [shiftExtremeTicks] defines whether ticks whose _x_ values are bounds of the _x_-axis value range should
             * be shifted to the edges of the axis bounds, to be aligned with the vertical axes.
             * [addExtremeLabelPadding] specifies whether, for [HorizontalLayout.FullWidth], padding should be added for
             * the first and last labels, ensuring their visibility.
             */
            public fun default(
                spacing: Int = 1,
                offset: Int = 0,
                shiftExtremeTicks: Boolean = true,
                addExtremeLabelPadding: Boolean = false,
            ): Horizontal = DefaultHorizontalAxisItemPlacer(spacing, offset, shiftExtremeTicks, addExtremeLabelPadding)
        }
    }

    /**
     * An [AxisItemPlacer] subinterface for [VerticalAxis] instances.
     */
    public interface Vertical {
        /**
         * Returns a boolean indicating whether to shift the lines whose _y_ values are equal to [ChartValues.maxY], if
         * such lines are present, such that they’re immediately above the [Chart]’s bounds. If the chart has a top
         * axis, the shifted tick will then be aligned with this axis, and the shifted guideline will be hidden.
         */
        public fun getShiftTopLines(chartDrawContext: ChartDrawContext): Boolean = true

        /**
         * Returns, as a list, the _y_ values for which labels are to be displayed.
         */
        public fun getLabelValues(
            context: ChartDrawContext,
            axisHeight: Float,
            maxLabelHeight: Float,
            position: AxisPosition.Vertical,
        ): List<Float>

        /**
         * Returns, as a list, the _y_ values for which the [VerticalAxis] is to create labels and measure their heights
         * during the measuring phase. The height of the tallest label is passed to [getWidthMeasurementLabelValues] and
         * [getLabelValues].
         */
        public fun getHeightMeasurementLabelValues(
            context: MeasureContext,
            position: AxisPosition.Vertical,
        ): List<Float>

        /**
         * Returns, as a list, the _y_ values for which the [VerticalAxis] is to create labels and measure their widths
         * during the measuring phase. This affects how much horizontal space the [VerticalAxis] requests.
         */
        public fun getWidthMeasurementLabelValues(
            context: MeasureContext,
            axisHeight: Float,
            maxLabelHeight: Float,
            position: AxisPosition.Vertical,
        ): List<Float>

        /**
         * Returns, as a list, the _y_ values for which ticks and guidelines are to be displayed.
         */
        public fun getLineValues(
            context: ChartDrawContext,
            axisHeight: Float,
            maxLabelHeight: Float,
            position: AxisPosition.Vertical,
        ): List<Float>? = null

        /**
         * Returns the top inset required by the [VerticalAxis].
         */
        public fun getTopVerticalAxisInset(
            verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
            maxLabelHeight: Float,
            maxLineThickness: Float,
        ): Float

        /**
         * Returns the bottom inset required by the [VerticalAxis].
         */
        public fun getBottomVerticalAxisInset(
            verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
            maxLabelHeight: Float,
            maxLineThickness: Float,
        ): Float

        public companion object {
            /**
             * Creates a base [AxisItemPlacer.Vertical] implementation. [maxItemCount] is the maximum number of labels
             * (and their corresponding line pairs) to be displayed. The actual item count is the greatest number
             * smaller than or equal to [maxItemCount] for which no overlaps occur. [shiftTopLines] defines whether
             * to shift the lines whose _y_ values are equal to [ChartValues.maxY], if such lines are present, such that
             * they’re immediately above the [Chart]’s bounds. If the chart has a top axis, the shifted tick will then
             * be aligned with this axis, and the shifted guideline will be hidden.
             */
            public fun default(
                maxItemCount: Int = DEF_LABEL_COUNT,
                shiftTopLines: Boolean = true,
            ): Vertical = DefaultVerticalAxisItemPlacer(maxItemCount, shiftTopLines)
        }
    }
}
