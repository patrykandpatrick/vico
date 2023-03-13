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

package com.patrykandpatrick.vico.core.entry

public open class PieEntryModelProducer(
    entries: List<PieEntry> = emptyList(),
) {

    private val entries: ArrayList<PieEntry> = ArrayList(entries)

    public fun getModel(): Model = Model(
        entries = entries,
        maxValue = entries.fold(0f) { sum, entry ->
            sum + entry.value
        },
    )

    public data class Model(
        override val entries: List<PieEntry>,
        override val maxValue: Float,
    ) : PieEntryModel
}
