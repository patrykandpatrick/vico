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

package pl.patrykgoworowski.vico.core.dataset

import pl.patrykgoworowski.vico.core.dataset.composed.ComposedEntryModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.entry.DataEntry

fun emptyEntryModel(): EntryModel =
    EntryModel(
        emptyList(),
        1f,
        1f,
        1f,
        1f,
        1f,
        1f
    )

fun <Model : EntryModel> emptyComposedEntryModel(): ComposedEntryModel<Model> =
    ComposedEntryModel(
        emptyList(),
        emptyList(),
        1f,
        1f,
        1f,
        1f,
        1f,
        1f,
    )

inline fun List<DataEntry>.forEachIn(
    range: ClosedFloatingPointRange<Float>,
    action: (DataEntry) -> Unit,
) {
    for (entry in this) {
        if (entry.x in range) action(entry)
    }
}
