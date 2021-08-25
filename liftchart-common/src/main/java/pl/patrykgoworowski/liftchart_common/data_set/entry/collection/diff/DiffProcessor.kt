package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff

import pl.patrykgoworowski.liftchart_common.entry.DataEntry

interface DiffProcessor<Entry : DataEntry> {

    fun setEntries(old: List<List<Entry>>, new: List<List<Entry>>)

    fun setEntries(new: List<List<Entry>>)

    fun progressDiff(progress: Float): List<List<Entry>>

}