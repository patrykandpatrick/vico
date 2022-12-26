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

package com.patrykandpatrick.vico.core.chart

import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.decoration.Decoration
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.ChartInsetter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.dimensions.BoundsAware
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.marker.Marker

internal const val AXIS_VALUES_DEPRECATION_MESSAGE: String = "Axis values should be overridden via " +
    "`Chart#axisValuesOverrider`."

/**
 * Defines the minimal set of properties and functions required by other parts of the library to draw a chart.
 */
public interface Chart<in Model> : BoundsAware, ChartInsetter {

    /**
     * Links x-axis values to [Marker.EntryModel]s. A [Marker.EntryModel] holds the data needed to draw a [Marker].
     */
    public val entryLocationMap: Map<Float, MutableList<Marker.EntryModel>>

    /**
     * A [Collection] of the [ChartInsetter]s that are part of this [Chart]. Each [ChartInsetter] can influence the
     * final layout of the chart and its components.
     *
     * @see ChartInsetter
     */
    public val chartInsetters: Collection<ChartInsetter>

    /**
     * Overrides the minimum and maximum x-axis and y-axis values. In the case of [ColumnChart]s and [LineChart]s
     * contained in [ComposedChart]s, these overrides can be applied to one vertical axis instead of both. Use
     * [ColumnChart.targetVerticalAxisPosition] and [LineChart.targetVerticalAxisPosition] for this purpose.
     */
    public var axisValuesOverrider: AxisValuesOverrider<@UnsafeVariance Model>?

    /**
     * The minimum value shown on the y-axis. If [Model] implements [ChartEntryModel], and [minY] is not null, this
     * overrides [ChartEntryModel.minY].
     *
     * @see ChartEntryModel.minY
     */
    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE)
    public var minY: Float?

    /**
     * The maximum value shown on the y-axis. If [Model] implements [ChartEntryModel], and [maxY] is not null, this
     * overrides [ChartEntryModel.maxY].
     *
     * @see ChartEntryModel.maxY
     */
    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE)
    public var maxY: Float?

    /**
     * The minimum value shown on the x-axis. If [Model] implements [ChartEntryModel], and [minX] is not null, this
     * overrides [ChartEntryModel.minX].
     *
     * @see ChartEntryModel.minX
     */
    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE)
    public var minX: Float?

    /**
     * The maximum value shown on the x-axis. If [Model] implements [ChartEntryModel], and [maxX] is not null, this
     * overrides [ChartEntryModel.maxX].
     *
     * @see ChartEntryModel.maxX
     */
    @Deprecated(message = AXIS_VALUES_DEPRECATION_MESSAGE)
    public var maxX: Float?

    /**
     * Responsible for drawing the chart itself and any decorations behind it.
     *
     * @param context holds the data needed to draw the [Chart].
     * @param model holds data about the [Chart]’s entries.
     *
     * @see ChartDrawContext
     */
    public fun drawScrollableContent(
        context: ChartDrawContext,
        model: Model,
    )

    /**
     * Responsible for drawing any decorations placed above the chart, as well as persistent markers.
     *
     * @param context holds the data needed to draw the [Chart].
     * @param model holds data about the [Chart]’s entries.
     *
     * @see ChartDrawContext
     */
    public fun drawNonScrollableContent(
        context: ChartDrawContext,
        model: Model,
    )

    /**
     * Adds a [Decoration] to this [Chart].
     *
     * @return `true` if the decoration was added successfully.
     *
     * @see Decoration
     */
    public fun addDecoration(decoration: Decoration): Boolean

    /**
     * Replaces the current list of decorations with the provided [decorations].
     */
    public fun setDecorations(decorations: List<Decoration>)

    /**
     * Removes a [Decoration] from this [Chart].
     *
     * @return `true` if the decoration was removed successfully.
     *
     * @see Decoration
     */
    public fun removeDecoration(decoration: Decoration): Boolean

    /**
     * Removes each [Decoration] from [decorations] from this [Chart].
     *
     * @return `true` if all decorations were removed successfully.
     *
     * @see removeDecoration
     * @see Decoration
     */
    public fun removeDecorations(decorations: List<Decoration>): Boolean =
        decorations.all(::removeDecoration)

    /**
     * Adds a persistent [Marker] to this [Chart]. The [Marker] will be anchored to the given [x] value on the x-axis.
     *
     * @see Marker
     */
    public fun addPersistentMarker(x: Float, marker: Marker)

    /**
     * Replaces the current map of markers with the provided [markers].
     *
     * @see addPersistentMarker
     * @see Marker
     */
    public fun setPersistentMarkers(markers: Map<Float, Marker>)

    /**
     * Removes a persistent [Marker] from this [Chart].
     *
     * @see Marker
     */
    public fun removePersistentMarker(x: Float)

    /**
     * Called to get the [SegmentProperties] of this chart. The [SegmentProperties] influence the look of various
     * parts of the chart.
     *
     * @param context holds data used for component measurements.
     * @param model holds data about the [Chart]’s entries.
     */
    public fun getSegmentProperties(context: MeasureContext, model: Model): SegmentProperties

    /**
     * Updates the [ChartValues] stored in the provided [ChartValuesManager] instance to this [Chart]’s [ChartValues].
     *
     * @param chartValuesManager the [ChartValuesManager] whose properties will be updated.
     * @param model holds data about the [Chart]’s entries.
     */
    public fun updateChartValues(chartValuesManager: ChartValuesManager, model: Model)
}

/**
 * Adds each [Decoration] from [decorations] to this [Chart].
 *
 * @return true if all decorations were added successfully.
 *
 * @see Chart.addDecoration
 * @see Decoration
 */
public fun <Model> Chart<Model>.addDecorations(decorations: List<Decoration>): Boolean =
    decorations.all(::addDecoration)
