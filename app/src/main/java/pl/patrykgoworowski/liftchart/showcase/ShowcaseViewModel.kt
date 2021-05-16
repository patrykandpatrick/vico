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
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryList
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryList
import pl.patrykgoworowski.liftchart_common.entry.entriesOf

class ShowcaseViewModel : ViewModel() {

    private val generator = RandomEntriesGenerator()
    private val multiGenerator = RandomEntriesGenerator(0..2)

    val entries = SingleEntryList(entriesOf(0 to 4, 1 to 3, 1.5 to 3, 2 to 2, 6 to 1))
//    val entries = SingleEntryList<AnyEntry>(entriesOf(0 to 4, 4.5 to 1))

    val multiEntries = MultiEntryList(
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