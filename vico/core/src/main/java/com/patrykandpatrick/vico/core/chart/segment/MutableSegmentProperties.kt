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

/**
 * An implementation of [SegmentProperties] whose every property is mutable.
 */
public data class MutableSegmentProperties(
    override var cellWidth: Float = 0f,
    override var marginWidth: Float = 0f,
    override var labelPosition: HorizontalAxis.LabelPosition? = null,
) : SegmentProperties {

    /**
     * Sets the cell width and margin width.
     */
    public fun set(
        cellWidth: Float,
        marginWidth: Float,
        labelPosition: HorizontalAxis.LabelPosition? = this.labelPosition,
    ): MutableSegmentProperties = apply {
        this.cellWidth = cellWidth
        this.marginWidth = marginWidth
        this.labelPosition = labelPosition
    }

    /**
     * Sets the cell width and margin width to 0.
     */
    public fun clear() {
        cellWidth = 0f
        marginWidth = 0f
    }

    override fun toString(): String =
        "MutableSegmentProperties(segmentWidth=$segmentWidth, contentWidth=$cellWidth, marginWidth=$marginWidth)"
}
