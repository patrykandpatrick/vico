package pl.patrykgoworowski.liftchart_compose.data_set.entry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModelListener
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModelListener
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryCollection

@Composable
fun SingleEntryCollection.collectAsState(): State<SingleEntriesModel> =
    produceState(initialValue = model) {
        val listener: SingleEntriesModelListener = { entriesModel ->
            value = entriesModel
        }

        addOnEntriesChangedListener(listener)

        awaitDispose {
            removeOnEntriesChangedListener(listener)
        }
    }

@Composable
fun MultiEntryCollection.collectAsState(): State<MultiEntriesModel> =
    produceState(initialValue = model) {
        val listener: MultiEntriesModelListener = { entriesModel ->
            value = entriesModel
        }

        addOnEntriesChangedListener(listener)

        awaitDispose {
            removeOnEntriesChangedListener(listener)
        }
    }
