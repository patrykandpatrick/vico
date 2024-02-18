/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.cartesian.marker

import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.insets.ChartInsetter
import com.patrykandpatrick.vico.core.cartesian.model.CartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.values.ChartValues
import com.patrykandpatrick.vico.core.common.Point
import com.patrykandpatrick.vico.core.common.extension.updateList

/**
 * Highlights points on a chart and displays their corresponding values in a bubble.
 */
public interface CartesianMarker : ChartInsetter {
    /**
     * Draws the marker.
     * @param context the [CartesianDrawContext] used to draw the marker.
     * @param bounds the bounds in which the marker is drawn.
     * @param markedEntries a list of [EntryModel]s representing the entries to which the marker refers.
     * @param chartValues the [CartesianChart]’s [ChartValues].
     */
    public fun draw(
        context: CartesianDrawContext,
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
}

internal fun HashMap<Float, MutableList<CartesianMarker.EntryModel>>.put(
    x: Float,
    y: Float,
    entry: CartesianLayerModel.Entry,
    color: Int,
    index: Int,
) {
    updateList(x) { add(CartesianMarker.EntryModel(Point(x, y), entry, color, index)) }
}
