package pl.patrykgoworowski.liftchart_common.marker

import android.text.Spannable
import android.text.style.ForegroundColorSpan
import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import pl.patrykgoworowski.liftchart_common.extension.appendCompat
import pl.patrykgoworowski.liftchart_common.extension.transformToSpannable

object DefaultMarkerLabelFormatter : MarkerLabelFormatter {

    override fun getLabel(
        markedEntries: List<Marker.EntryModel>,
        allEntries: List<DataEntry>
    ): CharSequence = markedEntries.transformToSpannable { model ->
        appendCompat(
            "%.02f".format(model.entry.y),
            ForegroundColorSpan(model.color),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
    }

}