package pl.patrykgoworowski.liftchart_common.marker

import pl.patrykgoworowski.liftchart_common.entry.DataEntry

public fun interface MarkerLabelFormatter {

    public fun getLabel(
        markedEntries: List<Marker.EntryModel>,
        allEntries: List<DataEntry>,
    ): CharSequence

}