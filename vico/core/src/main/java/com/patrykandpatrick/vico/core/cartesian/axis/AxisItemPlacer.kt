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

package com.patrykandpatrick.vico.core.cartesian.axis

import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.draw.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.values.ChartValues
import com.patrykandpatrick.vico.core.common.ExtraStore

/**
 * Determines for what values a [HorizontalAxis] or a [VerticalAxis] is to display labels, ticks, and guidelines.
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
        public fun getShiftExtremeTicks(context: CartesianDrawContext): Boolean = true

        /**
         * If the [HorizontalAxis] is to reserve room for the first label, returns the first label’s _x_ value.
         * Otherwise, returns `null`.
         */
        public fun getFirstLabelValue(
            context: CartesianMeasureContext,
            maxLabelWidth: Float,
        ): Float? = null

        /**
         * If the [HorizontalAxis] is to reserve room for the last label, returns the last label’s _x_ value. Otherwise,
         * returns `null`.
         */
        public fun getLastLabelValue(
            context: CartesianMeasureContext,
            maxLabelWidth: Float,
        ): Float? = null

        /**
         * Returns, as a list, the _x_ values for which labels are to be displayed, restricted to [visibleXRange] and
         * with two extra values on either side (if applicable).
         */
        public fun getLabelValues(
            context: CartesianDrawContext,
            visibleXRange: ClosedFloatingPointRange<Float>,
            fullXRange: ClosedFloatingPointRange<Float>,
            maxLabelWidth: Float,
        ): List<Float>

        /**
         * Returns, as a list, the _x_ values for which the [HorizontalAxis] is to create labels and measure their
         * widths during the measuring phase. The width of the widest label is passed to other functions.
         */
        public fun getWidthMeasurementLabelValues(
            context: CartesianMeasureContext,
            horizontalDimensions: HorizontalDimensions,
            fullXRange: ClosedFloatingPointRange<Float>,
        ): List<Float>

        /**
         * Returns, as a list, the _x_ values for which the [HorizontalAxis] is to create labels and measure their
         * heights during the measuring phase. This affects how much vertical space the [HorizontalAxis] requests.
         */
        public fun getHeightMeasurementLabelValues(
            context: CartesianMeasureContext,
            horizontalDimensions: HorizontalDimensions,
            fullXRange: ClosedFloatingPointRange<Float>,
            maxLabelWidth: Float,
        ): List<Float>

        /**
         * Returns, as a list, the _x_ values for which ticks and guidelines are to be displayed, restricted to
         * [visibleXRange] and with an extra value on either side (if applicable). If `null` is returned, the values
         * returned by [getLabelValues] are used.
         */
        public fun getLineValues(
            context: CartesianDrawContext,
            visibleXRange: ClosedFloatingPointRange<Float>,
            fullXRange: ClosedFloatingPointRange<Float>,
            maxLabelWidth: Float,
        ): List<Float>? = null

        /**
         * Returns the start inset required by the [HorizontalAxis].
         */
        public fun getStartHorizontalAxisInset(
            context: CartesianMeasureContext,
            horizontalDimensions: HorizontalDimensions,
            tickThickness: Float,
            maxLabelWidth: Float,
        ): Float

        /**
         * Returns the end inset required by the [HorizontalAxis].
         */
        public fun getEndHorizontalAxisInset(
            context: CartesianMeasureContext,
            horizontalDimensions: HorizontalDimensions,
            tickThickness: Float,
            maxLabelWidth: Float,
        ): Float

        /** Houses an [AxisItemPlacer.Horizontal] factory function. */
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
         * Returns a boolean indicating whether to shift the lines whose _y_ values are equal to
         * [ChartValues.YRange.maxY], if such lines are present, such that they’re immediately above the
         * [CartesianChart]’s bounds. If the chart has a top axis, the shifted tick will then be aligned with this axis,
         * and the shifted guideline will be hidden.
         */
        public fun getShiftTopLines(context: CartesianDrawContext): Boolean = true

        /**
         * Returns, as a list, the _y_ values for which labels are to be displayed.
         */
        public fun getLabelValues(
            context: CartesianDrawContext,
            axisHeight: Float,
            maxLabelHeight: Float,
            position: AxisPosition.Vertical,
        ): List<Float>

        /**
         * Returns, as a list, the _y_ values for which the [VerticalAxis] is to create labels and measure their widths
         * during the measuring phase. This affects how much horizontal space the [VerticalAxis] requests.
         */
        public fun getWidthMeasurementLabelValues(
            context: CartesianMeasureContext,
            axisHeight: Float,
            maxLabelHeight: Float,
            position: AxisPosition.Vertical,
        ): List<Float>

        /**
         * Returns, as a list, the _y_ values for which the [VerticalAxis] is to create labels and measure their heights
         * during the measuring phase. The height of the tallest label is passed to other functions.
         */
        public fun getHeightMeasurementLabelValues(
            context: CartesianMeasureContext,
            position: AxisPosition.Vertical,
        ): List<Float>

        /**
         * Returns, as a list, the _y_ values for which ticks and guidelines are to be displayed.
         */
        public fun getLineValues(
            context: CartesianDrawContext,
            axisHeight: Float,
            maxLabelHeight: Float,
            position: AxisPosition.Vertical,
        ): List<Float>? = null

        /**
         * Returns the top inset required by the [VerticalAxis].
         */
        public fun getTopVerticalAxisInset(
            context: CartesianMeasureContext,
            verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
            maxLabelHeight: Float,
            maxLineThickness: Float,
        ): Float

        /**
         * Returns the bottom inset required by the [VerticalAxis].
         */
        public fun getBottomVerticalAxisInset(
            context: CartesianMeasureContext,
            verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
            maxLabelHeight: Float,
            maxLineThickness: Float,
        ): Float

        /** Houses [AxisItemPlacer.Vertical] factory functions. */
        public companion object {
            /**
             * Creates a step-based [AxisItemPlacer.Vertical] implementation. [step] returns the difference between the
             * _y_ values of neighboring labels (and their corresponding line pairs). A multiple of this may be used for
             * overlap prevention. If `null` is returned, the step will be determined automatically. [shiftTopLines]
             * defines whether to shift the lines whose _y_ values are equal to [ChartValues.YRange.maxY], if such lines
             * are present, such that they’re immediately above the [CartesianChart]’s bounds. If the chart has a top
             * axis, the shifted tick will then be aligned with this axis, and the shifted guideline will be hidden.
             */
            public fun step(
                step: (ExtraStore) -> Float? = { null },
                shiftTopLines: Boolean = true,
            ): Vertical = DefaultVerticalAxisItemPlacer(DefaultVerticalAxisItemPlacer.Mode.Step(step), shiftTopLines)

            /**
             * Creates a count-based [AxisItemPlacer.Vertical] implementation. [count] returns the number of labels (and
             * their corresponding line pairs) to be displayed. This may be reduced for overlap prevention. If `null` is
             * returned, the [VerticalAxis] will display as many items as possible. [shiftTopLines] defines whether to
             * shift the lines whose _y_ values are equal to [ChartValues.YRange.maxY], if such lines are present, such
             * that they’re immediately above the [CartesianChart]’s bounds. If the chart has a top axis, the shifted
             * tick will then be aligned with this axis, and the shifted guideline will be hidden.
             */
            public fun count(
                count: (ExtraStore) -> Int? = { null },
                shiftTopLines: Boolean = true,
            ): Vertical = DefaultVerticalAxisItemPlacer(DefaultVerticalAxisItemPlacer.Mode.Count(count), shiftTopLines)
        }
    }
}
