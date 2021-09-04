package pl.patrykgoworowski.liftchart.showcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.patrykgoworowski.liftchart.data.RandomEntriesGenerator
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryList
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.composed.plus

class ShowcaseViewModel : ViewModel() {

    private val generator = RandomEntriesGenerator(0..96)
    private val multiGenerator = RandomEntriesGenerator(0..32)

    val entries = EntryList()
    val multiEntries = EntryList()

    val composedEntries = multiEntries + entries

    init {
        viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                entries.setEntries(generator.generateRandomEntries())
                multiEntries.setEntries(
                    listOf(
                        multiGenerator.generateRandomEntries(),
                        multiGenerator.generateRandomEntries(),
                        multiGenerator.generateRandomEntries(),
                    )
                )
                delay(2_000)
            }
        }
    }
}
