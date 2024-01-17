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

package com.patrykandpatrick.vico.core.marker

import android.graphics.RectF
import com.patrykandpatrick.vico.core.chart.CartesianChart
import com.patrykandpatrick.vico.core.chart.insets.ChartInsetter
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.extension.updateList
import com.patrykandpatrick.vico.core.model.CartesianLayerModel
import com.patrykandpatrick.vico.core.util.Point

/**
 * Highlights points on a chart and displays their corresponding values in a bubble.
 */
public interface Marker : ChartInsetter {
    /**
     * Draws the marker.
     * @param context the [DrawContext] used to draw the marker.
     * @param bounds the bounds in which the marker is drawn.
     * @param markedEntries a list of [EntryModel]s representing the entries to which the marker refers.
     * @param chartValues the [CartesianChart]’s [ChartValues].
     */
    public fun draw(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<EntryModel>,
        chartValues: ChartValues,
    )

    /**
     * Contains information on a single chart entry to which a chart marker refers.
     * @param location the coordinates of the indicator.
     * @param entry the [CartesianLayerModel.Entry].
     * @param color the color associated with the [CartesianLayerModel.Entry].
     * @param index the index of the [CartesianLayerModel.Entry] in its series.
     */
    public data class EntryModel(
        public val location: Point,
        public val entry: CartesianLayerModel.Entry,
        public val color: Int,
        public val index: Int,
    )

    /**
     * This sealed class represents the position where the label should be rendered
     */
    public sealed interface LabelPosition {
        /**
         * This is the default position.
         *
         * The label will be rendered on the top of the chart
         */
        public data object Top : LabelPosition

        /**
         * The label will be rendered on the top of the indicator.
         *
         * For the case of the chart holds dynamic values, the label will update its position  one the indicator updates too.
         *
         * @param spacingDp it's an additional space between the indicator and the label. That makes the appearance
         * a bit more customizable for the case of custom indicators or custom label layouts.
         */
        public data class AboveIndicator(val spacingDp: Float = 2f) : LabelPosition
    }
}

internal fun HashMap<Float, MutableList<Marker.EntryModel>>.put(
    x: Float,
    y: Float,
    entry: CartesianLayerModel.Entry,
    color: Int,
    index: Int,
) {
    updateList(x) { add(Marker.EntryModel(Point(x, y), entry, color, index)) }
}
