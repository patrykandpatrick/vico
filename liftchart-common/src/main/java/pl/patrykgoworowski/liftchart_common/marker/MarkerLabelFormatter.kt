package pl.patrykgoworowski.liftchart_common.marker

import pl.patrykgoworowski.liftchart_common.entry.DataEntry

fun interface MarkerLabelFormatter {

    fun getLabel(
        markedEntries: List<Marker.EntryModel>,
        allEntries: List<DataEntry>,
    ): CharSequence

}