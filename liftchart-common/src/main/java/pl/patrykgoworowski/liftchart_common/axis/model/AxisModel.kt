package pl.patrykgoworowski.liftchart_common.axis.model

import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

interface AxisModel : EntriesModel {
    val xSegmentWidth: Float
    val xSegmentSpacing: Float
    val entries: List<AnyEntry>
}