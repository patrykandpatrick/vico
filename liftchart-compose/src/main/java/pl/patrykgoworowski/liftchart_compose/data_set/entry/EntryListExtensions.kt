package pl.patrykgoworowski.liftchart_compose.data_set.entry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryList
import pl.patrykgoworowski.liftchart_common.entry.entryOf

@Composable
fun <Model : EntriesModel> EntryCollection<Model>.collectAsState(): State<Model> =
    produceState(initialValue = model) {
        val listener: (Model) -> Unit = { entriesModel ->
            value = entriesModel
        }

        addOnEntriesChangedListener(listener)

        awaitDispose {
            removeOnEntriesChangedListener(listener)
        }
    }

fun multiEntryModelOf(vararg entries: Pair<Number, Number>): MultiEntriesModel =
    entries
        .map { (x, y) -> entryOf(x.toFloat(), y.toFloat()) }
        .let { entryList -> MultiEntryList(listOf(entryList), false) }
        .model

fun multiEntryModelOf(vararg values: Number): MultiEntriesModel =
    values
        .mapIndexed { index, value -> entryOf(index.toFloat(), value.toFloat()) }
        .let { entryList -> MultiEntryList(listOf(entryList), false) }
        .model