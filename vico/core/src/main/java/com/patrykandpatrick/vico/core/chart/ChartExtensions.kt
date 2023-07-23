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

package com.patrykandpatrick.vico.core.chart

import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.extension.updateList
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.model.Point

internal fun HashMap<Float, MutableList<Marker.EntryModel>>.put(
    x: Float,
    y: Float,
    entry: ChartEntry,
    color: Int,
    index: Int,
) {
    updateList(x) {
        add(
            Marker.EntryModel(
                location = Point(x = x, y = y),
                entry = entry,
                color = color,
                index = index,
            ),
        )
    }
}
