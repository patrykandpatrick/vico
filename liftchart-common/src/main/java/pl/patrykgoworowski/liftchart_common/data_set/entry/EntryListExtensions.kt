package pl.patrykgoworowski.liftchart_common.data_set.entry

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModelListener
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModelListener
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryCollection

val SingleEntryCollection.collectAsFlow: Flow<SingleEntriesModel>
        get() = callbackFlow {

            val listener: SingleEntriesModelListener = { entriesModel ->
                sendBlocking((entriesModel))
            }

            addOnEntriesChangedListener(listener)
            awaitClose {
                removeOnEntriesChangedListener(listener)
            }
        }.flowOn(Dispatchers.IO)

val SingleEntryCollection.collectAsStateFlow: StateFlow<SingleEntriesModel>
    get() = collectAsFlow
        .stateIn(GlobalScope, SharingStarted.Lazily, model)

val MultiEntryCollection.collectAsFlow: Flow<MultiEntriesModel>
    get() = callbackFlow {

        val listener: MultiEntriesModelListener = { entriesModel ->
            sendBlocking((entriesModel))
        }

        addOnEntriesChangedListener(listener)
        awaitClose {
            removeOnEntriesChangedListener(listener)
        }
    }.flowOn(Dispatchers.IO)

val MultiEntryCollection.collectAsStateFlow: StateFlow<MultiEntriesModel>
    get() = collectAsFlow
        .stateIn(GlobalScope, SharingStarted.Lazily, model)