package pl.patrykgoworowski.liftchart_compose.data_set.entry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel

@Composable
fun <Model : EntryModel> EntryCollection<Model>.collectAsState(): State<Model> =
    produceState(initialValue = model) {
        val listener: (Model) -> Unit = { entriesModel ->
            value = entriesModel
        }

        addOnEntriesChangedListener(listener)

        awaitDispose {
            removeOnEntriesChangedListener(listener)
        }
    }