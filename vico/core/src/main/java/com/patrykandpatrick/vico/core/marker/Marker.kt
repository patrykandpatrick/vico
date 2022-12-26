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

package com.patrykandpatrick.vico.core.marker

import android.graphics.RectF
import com.patrykandpatrick.vico.core.chart.insets.ChartInsetter
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.model.Point

/**
 * Highlights points on a chart and displays their corresponding values in a bubble.
 */
public interface Marker : ChartInsetter {

    /**
     * Draws the marker.
     * @param context the [DrawContext] used to draw the marker.
     * @param bounds the bounds in which the marker is drawn.
     * @param markedEntries a list of [EntryModel]s representing the entries to which the marker refers.
     */
    public fun draw(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<EntryModel>,
    )

    /**
     * Contains information on a single chart entry to which a chart marker refers.
     * @param location the coordinates of the indicator.
     * @param entry the [ChartEntry].
     * @param color the color associated with the [ChartEntry].
     */
    public class EntryModel(
        public val location: Point,
        public val entry: ChartEntry,
        public val color: Int,
    )
}
