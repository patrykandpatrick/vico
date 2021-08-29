package pl.patrykgoworowski.liftchart_common.data_set.entry.collection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import pl.patrykgoworowski.liftchart_common.entry.entryOf

@ExperimentalCoroutinesApi
val <Model : EntryModel> EntryCollection<Model>.collectAsFlow: Flow<Model>
    get() = callbackFlow {

        val listener: (Model) -> Unit = { entriesModel ->
            trySendBlocking(entriesModel)
        }

        addOnEntriesChangedListener(listener)
        awaitClose {
            removeOnEntriesChangedListener(listener)
        }
    }.flowOn(Dispatchers.IO)

fun entryModelOf(vararg entries: Pair<Number, Number>): EntryModel =
    entries
        .map { (x, y) -> entryOf(x.toFloat(), y.toFloat()) }
        .let { entryList -> EntryList(listOf(entryList), false) }
        .model

fun entryModelOf(vararg values: Number): EntryModel =
    values
        .mapIndexed { index, value -> entryOf(index.toFloat(), value.toFloat()) }
        .let { entryList -> EntryList(listOf(entryList), false) }
        .model