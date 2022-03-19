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

package pl.patrykgoworowski.vico.core.chart.segment

/**
 * [SegmentProperties] holds information about the width of each individual segment on the x-axis.
 */
public interface SegmentProperties {

    /**
     * The width of individual cell, e.g. a column in the column chart, or a point in the line chart.
     */
    public val cellWidth: Float

    /**
     * The width of margin around given cell.
     */
    public val marginWidth: Float

    /**
     * The sum of the cell width and margin width.
     */
    public val segmentWidth: Float
        get() = cellWidth + marginWidth

    /**
     * @see cellWidth
     */
    public operator fun component1(): Float = cellWidth

    /**
     * @see marginWidth
     */
    public operator fun component2(): Float = marginWidth

    /**
     * @see segmentWidth
     */
    public operator fun component3(): Float = segmentWidth

    /**
     * Creates a new `SegmentProperties` implementation by multiplying the cell width
     * and margin width from these `SegmentProperties` by the provided factor.
     */
    public fun scaled(scale: Float): SegmentProperties =
        SegmentProperties(cellWidth * scale, marginWidth * scale)
}

/**
 * A convenience function creating an anonymous implementation of the [SegmentProperties].
 *
 * @param cellWidth the width of individual cell, e.g. a column in the column chart, or a point in the line chart.
 * @param marginWidth the sum of the cell width and margin width.
 */
public fun SegmentProperties(cellWidth: Float, marginWidth: Float): SegmentProperties = object : SegmentProperties {
    override val cellWidth: Float = cellWidth
    override val marginWidth: Float = marginWidth
}
