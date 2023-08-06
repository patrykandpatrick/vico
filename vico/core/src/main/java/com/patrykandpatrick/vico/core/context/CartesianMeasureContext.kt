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

package com.patrykandpatrick.vico.core.context

import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager

/**
 * [CartesianMeasureContext] holds data used by various cartesian chart components during the measuring and drawing
 * phases.
 */
public interface CartesianMeasureContext : MeasureContext {

    /**
     * Manages the associated [Chart]’s [ChartValues].
     *
     * @see [ChartValuesManager]
     */
    public val chartValuesManager: ChartValuesManager

    /**
     * Whether horizontal scrolling is enabled.
     */
    public val isHorizontalScrollEnabled: Boolean

    /**
     * The scale of the chart. Used to handle zooming in and out.
     */
    public val chartScale: Float

    /**
     * Defines how the chart’s content is positioned horizontally.
     */
    public val horizontalLayout: HorizontalLayout
}
