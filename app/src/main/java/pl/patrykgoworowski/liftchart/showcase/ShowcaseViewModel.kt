package pl.patrykgoworowski.liftchart.showcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import pl.patrykgoworowski.liftchart.data.RandomEntriesGenerator
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryList
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryList
import pl.patrykgoworowski.liftchart_common.entry.entriesOf

class ShowcaseViewModel : ViewModel() {

    private val generator = RandomEntriesGenerator()
    private val multiGenerator = RandomEntriesGenerator(0..5)

    //val entries1 = SingleEntryList<AnyEntry>(generator.generateRandomEntries())

    val entries = SingleEntryList<AnyEntry>(entriesOf(0 to 1, 1 to 2, 1.2 to 40))

    val multiEntries = MultiEntryList<AnyEntry>(
        multiGenerator.generateRandomEntries(),
        multiGenerator.generateRandomEntries(),
        multiGenerator.generateRandomEntries()
    )


    init {
        flow {
            while (currentCoroutineContext().isActive) {
                delay(2_000)
                emit(generator.generateRandomEntries())
            }
        }.onEach { entries ->
            this.entries.setEntries(entries)
        }.launchIn(viewModelScope)

        flow {
            while (currentCoroutineContext().isActive) {
                delay(2_000)
                emit(listOf(
                    multiGenerator.generateRandomEntries(),
                    multiGenerator.generateRandomEntries(),
                    multiGenerator.generateRandomEntries()
                ))
            }
        }.onEach { entries ->
            multiEntries.setEntryCollection(entries)
        }.launchIn(viewModelScope)
    }

}