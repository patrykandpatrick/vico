package pl.patrykgoworowski.liftchart_common.data_set.entry

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModelListener
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModelListener
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryCollection

val <T: AnyEntry> SingleEntryCollection<T>.collectAsFlow: Flow<SingleEntriesModel<T>>
        get() = callbackFlow {

            val listener: SingleEntriesModelListener<T> = { entriesModel ->
                sendBlocking((entriesModel))
            }

            addOnEntriesChangedListener(listener)
            awaitClose {
                removeOnEntriesChangedListener(listener)
            }
        }.flowOn(Dispatchers.IO)

val <T: AnyEntry> SingleEntryCollection<T>.collectAsStateFlow: StateFlow<SingleEntriesModel<T>>
    get() = collectAsFlow
        .stateIn(GlobalScope, SharingStarted.Lazily, model)

val <T: AnyEntry> MultiEntryCollection<T>.collectAsFlow: Flow<MultiEntriesModel<T>>
    get() = callbackFlow {

        val listener: MultiEntriesModelListener<T> = { entriesModel ->
            sendBlocking((entriesModel))
        }

        addOnEntriesChangedListener(listener)
        awaitClose {
            removeOnEntriesChangedListener(listener)
        }
    }.flowOn(Dispatchers.IO)

val <T: AnyEntry> MultiEntryCollection<T>.collectAsStateFlow: StateFlow<MultiEntriesModel<T>>
    get() = collectAsFlow
        .stateIn(GlobalScope, SharingStarted.Lazily, model)