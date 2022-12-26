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

package com.patrykandpatrick.vico.core.chart.segment

import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.Chart

/**
 * [SegmentProperties] holds information about the width of each segment of a [Chart].
 */
public interface SegmentProperties {

    /**
     * The width of each cell (e.g., a column in a column chart or a point in a line chart).
     */
    public val cellWidth: Float

    /**
     * The width of the margin around each cell.
     */
    public val marginWidth: Float

    /**
     * The sum of the cell width and margin width.
     */
    public val segmentWidth: Float
        get() = cellWidth + marginWidth

    /**
     * Defines the position of labels.
     */
    public val labelPosition: HorizontalAxis.LabelPosition?

    /**
     * Defines the position of labels.
     * Returns [labelPosition] if it’s not null, and [HorizontalAxis.LabelPosition.Center] otherwise.
     */
    public val labelPositionOrDefault: HorizontalAxis.LabelPosition
        get() = labelPosition ?: HorizontalAxis.LabelPosition.Center

    /**
     * @see cellWidth
     */
    public operator fun component1(): Float = cellWidth

    /**
     * @see marginWidth
     */
    public operator fun component2(): Float = marginWidth

    /**
     * Creates a new [SegmentProperties] implementation by multiplying the cell width
     * and margin width from these [SegmentProperties] by the provided factor.
     */
    public fun scaled(scale: Float): SegmentProperties =
        SegmentProperties(
            cellWidth = cellWidth * scale,
            marginWidth = marginWidth * scale,
            labelPosition = labelPosition,
        )
}

/**
 * A convenience function that creates an anonymous [SegmentProperties] implementation.
 *
 * @param cellWidth the width of each individual cell (e.g., a column in a column chart or a point in a line chart).
 * @param marginWidth the sum of the cell width and margin width.
 */
public fun SegmentProperties(
    cellWidth: Float,
    marginWidth: Float,
    labelPosition: HorizontalAxis.LabelPosition?,
): SegmentProperties = object : SegmentProperties {

    override val cellWidth: Float = cellWidth

    override val marginWidth: Float = marginWidth

    override val labelPosition: HorizontalAxis.LabelPosition? = labelPosition
}
