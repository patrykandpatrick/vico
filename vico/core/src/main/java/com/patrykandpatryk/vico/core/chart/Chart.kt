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

package com.patrykandpatryk.vico.core.chart

import com.patrykandpatryk.vico.core.axis.model.MutableChartModel
import com.patrykandpatryk.vico.core.chart.decoration.Decoration
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.chart.segment.SegmentProperties
import com.patrykandpatryk.vico.core.dimensions.BoundsAware
import com.patrykandpatryk.vico.core.context.MeasureContext
import com.patrykandpatryk.vico.core.marker.Marker

/**
 * The interface defining minimal set of properties and functions required by other parts of the library to draw
 * a chart.
 */
public interface Chart<in Model> : BoundsAware {

    /**
     * A [Map] holding x-axis values as keys to their corresponding [Marker.EntryModel] holding data necessary
     * to draw a [Marker].
     */
    public val entryLocationMap: Map<Float, MutableList<Marker.EntryModel>>

    /**
     * The minimum value shown on the y-axis.
     * In case [Model] is a subclass of [com.patrykandpatryk.vico.core.entry.ChartEntryModel], and the [minY]
     * is not null, it overrides the minimal y-axis value defined in
     * [com.patrykandpatryk.vico.core.entry.ChartEntryModel.minY].
     *
     * @see com.patrykandpatryk.vico.core.entry.ChartEntryModel.minY
     */
    public var minY: Float?

    /**
     * The maximum value shown on the y-axis.
     * In case [Model] is a subclass of [com.patrykandpatryk.vico.core.entry.ChartEntryModel], and the [maxY]
     * is not null, it overrides the maximal y-axis value defined in
     * [com.patrykandpatryk.vico.core.entry.ChartEntryModel.maxY].
     *
     * @see com.patrykandpatryk.vico.core.entry.ChartEntryModel.maxY
     */
    public var maxY: Float?

    /**
     * The minimum value shown on the x-axis.
     * In case [Model] is a subclass of [com.patrykandpatryk.vico.core.entry.ChartEntryModel], and the [minX]
     * is not null, it overrides the minimal x-axis value defined in
     * [com.patrykandpatryk.vico.core.entry.ChartEntryModel.minX].
     *
     * @see com.patrykandpatryk.vico.core.entry.ChartEntryModel.minX
     */
    public var minX: Float?

    /**
     * The maximum value shown on the x-axis.
     * In case [Model] is a subclass of [com.patrykandpatryk.vico.core.entry.ChartEntryModel], and the [maxX]
     * is not null, it overrides the maximal x-axis value defined in
     * [com.patrykandpatryk.vico.core.entry.ChartEntryModel.maxX].
     *
     * @see com.patrykandpatryk.vico.core.entry.ChartEntryModel.maxX
     */
    public var maxX: Float?

    /**
     * The function responsible for drawing the chart itself.
     * @param context The drawing context holding data about environment, as well as Canvas to draw on.
     * @param model The model holding data about entries that are meant to be drawn.
     *
     * @see ChartDrawContext
     */
    public fun draw(
        context: ChartDrawContext,
        model: Model,
    )

    /**
     * Adds a [Decoration] to this [Chart].
     *
     * @return true if decoration was added successfully.
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
     * @return true if decoration was removed successfully.
     *
     * @see Decoration
     */
    public fun removeDecoration(decoration: Decoration): Boolean

    /**
     * Removes each [Decoration] from [decorations] from this [Chart].
     *
     * @return true if all decorations were removed successfully.
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
     * @param context the measuring context that holds the data used for component measurements.
     * @param model the model used to represent the data rendered by this chart.
     */
    public fun getSegmentProperties(context: MeasureContext, model: Model): SegmentProperties

    /**
     * Called to update the values stored in a [MutableChartModel] to the values managed by this chart.
     *
     * @param chartModel the [MutableChartModel] whose properties will be updated.
     * @param model the model used to represent the data rendered by this chart.
     */
    public fun setToChartModel(chartModel: MutableChartModel, model: Model)
}

/**
 * Adds each [Decoration] from [decorations] to this [Chart].
 *
 * @return true if all decorations were added successfully.
 *
 * @see addDecoration
 * @see Decoration
 */
public fun <Model> Chart<Model>.addDecorations(decorations: List<Decoration>): Boolean =
    decorations.all(::addDecoration)
