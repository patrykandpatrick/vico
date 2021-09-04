package pl.patrykgoworowski.liftchart_common.marker

public fun interface MarkerLabelFormatter {

    public fun getLabel(
        markedEntries: List<Marker.EntryModel>,
    ): CharSequence
}
