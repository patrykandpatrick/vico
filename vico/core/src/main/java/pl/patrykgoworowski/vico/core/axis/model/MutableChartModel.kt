/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.axis.model

import pl.patrykgoworowski.vico.core.chart.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.chart.entry.collection.entryModel

public class MutableChartModel(
    override var minX: Float = 0f,
    override var maxX: Float = 0f,
    override var minY: Float = 0f,
    override var maxY: Float = 0f,
    override var entryModel: EntryModel = entryModel(),
) : ChartModel {

    private val emptyModel = entryModel()

    public fun clear() {
        minX = 0f
        maxX = 0f
        minY = 0f
        maxY = 0f
        entryModel = emptyModel
    }
}
