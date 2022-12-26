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

package com.patrykandpatrick.vico.core.chart.insets

import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.marker.Marker

/**
 * Enables a component to add insets to [Chart]s to make room for itself. This is used by [Axis], [Marker], and the
 * like.
 */
public interface ChartInsetter {

    /**
     * Called during the measurement phase, before [getHorizontalInsets]. Both horizontal and vertical insets can be
     * requested from this function. The final inset for a given edge of the associated [Chart] is the largest of the
     * insets requested for the edge.
     *
     * @param context holds data used for the measuring of components.
     * @param outInsets used to store the requested insets.
     * @param segmentProperties the associated [Chart]’s [SegmentProperties].
     */
    public fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        segmentProperties: SegmentProperties,
    ): Unit = Unit

    /**
     * Called during the measurement phase, after [getInsets]. Only horizontal insets can be requested from this
     * function. Unless the available height is of interest, [getInsets] can be used to set all insets. The final inset
     * for a given edge of the associated [Chart] is the largest of the insets requested for the edge.
     *
     * @param context holds data used for the measuring of components.
     * @param availableHeight the available height. The vertical insets are considered here.
     * @param outInsets used to store the requested insets.
     */
    public fun getHorizontalInsets(
        context: MeasureContext,
        availableHeight: Float,
        outInsets: HorizontalInsets,
    ): Unit = Unit
}
