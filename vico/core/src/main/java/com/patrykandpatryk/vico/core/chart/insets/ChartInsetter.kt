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

package com.patrykandpatryk.vico.core.chart.insets

import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.context.MeasureContext

/**
 * An interface used by various components (e.g., Axis, Marker) to inset [com.patrykandpatryk.vico.core.chart.Chart].
 * It prevents the chart from taking all available space inside a view, so various components don’t overlap it
 * when they shouldn’t.
 */
public interface ChartInsetter {

    /**
     * Called during the measurement phase, before [getHorizontalInsets].
     * Subclasses can specify both vertical and horizontal insets for the chart.
     * The [com.patrykandpatryk.vico.core.layout.VirtualLayout] will use the highest inset values returned by any
     * [ChartInsetter] for the resulting insets.
     *
     * @param context the [ChartDrawContext] that holds the data used for component drawing.
     * @param outInsets the mutable class used to store the [ChartInsetter] subclass’s desired insets.
     */
    public fun getInsets(
        context: ChartDrawContext,
        outInsets: Insets,
    ): Unit = Unit

    /**
     * Called during the measurement phase, after [getInsets].
     * Subclasses can specify the chart’s horizontal insets only.
     * Unless the component needs to know the [availableHeight] that it may use,
     * it can specify all insets in [getInsets].
     * The [com.patrykandpatryk.vico.core.layout.VirtualLayout] will use the highest horizontal inset values returned by
     * any [ChartInsetter] for the resulting insets.
     *
     * @param context The measuring context holding data used for component measurements.
     * @param availableHeight The height that may be used by the [ChartInsetter] subclass.
     * @param outInsets The mutable class used to store the [ChartInsetter] subclass’s desired insets.
     */
    public fun getHorizontalInsets(
        context: MeasureContext,
        availableHeight: Float,
        outInsets: HorizontalInsets,
    ): Unit = Unit
}
