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

package com.patrykandpatrick.vico.core.chart.layer

import com.patrykandpatrick.vico.core.chart.CartesianChart
import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.CartesianChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.ChartInsetter
import com.patrykandpatrick.vico.core.chart.values.AxisValueOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.MutableChartValues
import com.patrykandpatrick.vico.core.context.CartesianMeasureContext
import com.patrykandpatrick.vico.core.dimensions.BoundsAware
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.model.CartesianLayerModel
import com.patrykandpatrick.vico.core.model.MutableExtraStore

/**
 * Visualizes data on a Cartesian plane. [CartesianLayer]s are combined and drawn by [CartesianChart]s.
 */
public interface CartesianLayer<T : CartesianLayerModel> : BoundsAware, ChartInsetter {
    /**
     * Links _x_ values to [Marker.EntryModel]s.
     */
    public val entryLocationMap: Map<Float, MutableList<Marker.EntryModel>>

    /**
     * Overrides the _x_ and _y_ ranges.
     */
    public var axisValueOverrider: AxisValueOverrider<T>?

    /**
     * Draws the [CartesianLayer].
     */
    public fun draw(
        context: CartesianChartDrawContext,
        model: T,
    )

    /**
     * Updates [horizontalDimensions] to match this [CartesianLayer]’s dimensions.
     */
    public fun updateHorizontalDimensions(
        context: CartesianMeasureContext,
        horizontalDimensions: MutableHorizontalDimensions,
        model: T,
    )

    /**
     * Updates [chartValues] in accordance with [model].
     */
    public fun updateChartValues(
        chartValues: MutableChartValues,
        model: T,
    )

    /**
     * Prepares the [CartesianLayer] for a difference animation.
     */
    public fun prepareForTransformation(
        model: T?,
        extraStore: MutableExtraStore,
        chartValues: ChartValues,
    )

    /**
     * Carries out the pending difference animation.
     */
    public suspend fun transform(
        extraStore: MutableExtraStore,
        fraction: Float,
    )
}
