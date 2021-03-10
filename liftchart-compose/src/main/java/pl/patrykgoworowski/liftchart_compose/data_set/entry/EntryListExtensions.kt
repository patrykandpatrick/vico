package pl.patrykgoworowski.liftchart_compose.data_set.entry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.collectAsStateFlow
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryCollection

val <T : AnyEntry> SingleEntryCollection<T>.collectAsState: State<SingleEntriesModel<T>>
    @Composable
    get() = collectAsStateFlow.collectAsState()


val <T : AnyEntry> MultiEntryCollection<T>.collectAsState: State<MultiEntriesModel<T>>
    @Composable
    get() = collectAsStateFlow.collectAsState()
