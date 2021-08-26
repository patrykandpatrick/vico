package pl.patrykgoworowski.liftchart_common.data_set.entry

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModelListener
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModelListener
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryCollection

@ExperimentalCoroutinesApi
val SingleEntryCollection.collectAsFlow: Flow<SingleEntriesModel>
    get() = callbackFlow {

        val listener: SingleEntriesModelListener = { entriesModel ->
            trySendBlocking(entriesModel)
        }

        addOnEntriesChangedListener(listener)
        awaitClose {
            removeOnEntriesChangedListener(listener)
        }
    }.flowOn(Dispatchers.IO)

@ExperimentalCoroutinesApi
val MultiEntryCollection.collectAsFlow: Flow<MultiEntriesModel>
    get() = callbackFlow {

        val listener: MultiEntriesModelListener = { entriesModel ->
            trySendBlocking(entriesModel)
        }

        addOnEntriesChangedListener(listener)
        awaitClose {
            removeOnEntriesChangedListener(listener)
        }
    }.flowOn(Dispatchers.IO)