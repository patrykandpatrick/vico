package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff

import pl.patrykgoworowski.liftchart_common.entry.DataEntry

public interface DiffProcessor<Entry : DataEntry> {

    public fun setEntries(old: List<List<Entry>>, new: List<List<Entry>>)

    public fun setEntries(new: List<List<Entry>>)

    public fun progressDiff(progress: Float): List<List<Entry>>

}