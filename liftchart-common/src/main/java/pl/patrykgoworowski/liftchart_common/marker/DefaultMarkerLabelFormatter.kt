package pl.patrykgoworowski.liftchart_common.marker

import android.text.Spannable
import android.text.style.ForegroundColorSpan
import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import pl.patrykgoworowski.liftchart_common.extension.appendCompat
import pl.patrykgoworowski.liftchart_common.extension.sumOf
import pl.patrykgoworowski.liftchart_common.extension.transformToSpannable

object DefaultMarkerLabelFormatter : MarkerLabelFormatter {

    private const val PATTERN = "%.02f"

    override fun getLabel(
            markedEntries: List<Marker.EntryModel>,
            allEntries: List<DataEntry>
    ): CharSequence = markedEntries.transformToSpannable(
            prefix = if (markedEntries.size > 1)
                PATTERN.format(markedEntries.sumOf { it.entry.y }) + " ("
            else "",
            postfix = if (markedEntries.size > 1) ")" else ""
    ) { model ->
        appendCompat(
                PATTERN.format(model.entry.y),
                ForegroundColorSpan(model.color),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
    }

}