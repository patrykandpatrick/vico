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

package pl.patrykgoworowski.vico.core.dataset.entry.collection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import pl.patrykgoworowski.vico.core.entry.entryOf

@ExperimentalCoroutinesApi
public val <Model : EntryModel> EntryCollection<Model>.collectAsFlow: Flow<Model>
    get() = callbackFlow {

        val listener: (Model) -> Unit = { entriesModel ->
            trySendBlocking(entriesModel)
        }

        addOnEntriesChangedListener(listener)
        awaitClose {
            removeOnEntriesChangedListener(listener)
        }
    }.flowOn(Dispatchers.IO)

public fun entryModelOf(vararg entries: Pair<Number, Number>): EntryModel =
    entries
        .map { (x, y) -> entryOf(x.toFloat(), y.toFloat()) }
        .let { entryList -> EntryList(listOf(entryList), false) }
        .model

public fun entryModelOf(vararg values: Number): EntryModel =
    values
        .mapIndexed { index, value -> entryOf(index.toFloat(), value.toFloat()) }
        .let { entryList -> EntryList(listOf(entryList), false) }
        .model
