package pl.patrykgoworowski.liftchart.showcase

import androidx.lifecycle.ViewModel
import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryList

class ShowcaseViewModel : ViewModel() {

    val entries1 = EntryList<AnyEntry>(0 to 1, 1 to 2, 2 to 5)
    val entries2 = EntryList<AnyEntry>(0f to 2, 1 to 1, 2 to 5)
    val entries3 = EntryList<AnyEntry>(0 to 2, 1 to 1, 2 to 4)

    val allEntries = listOf(entries1, entries2, entries3)



}