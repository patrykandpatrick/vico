package pl.patrykgoworowski.liftchart_core.data_set

import pl.patrykgoworowski.liftchart_core.data_set.segment.SegmentSpec
import pl.patrykgoworowski.liftchart_core.entry.DataEntry

typealias AnyEntry = DataEntry<*, *>

abstract class BaseDataSet<T: AnyEntry>(
    entryManager: EntryManager<T>
) : EntryManager<T> by entryManager, DataSet {
    abstract var color: Int
    abstract var segmentSpec: SegmentSpec
}