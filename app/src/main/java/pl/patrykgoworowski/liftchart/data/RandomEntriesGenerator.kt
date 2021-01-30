package pl.patrykgoworowski.liftchart.data

import pl.patrykgoworowski.liftchart_common.entry.FloatEntry
import pl.patrykgoworowski.liftchart_common.entry.entryOf

class RandomEntriesGenerator(
    val xRange: IntRange = 0..10,
    val yRange: IntRange = 0..20
) {

    fun generateRandomEntries(): List<FloatEntry> {
        val result = ArrayList<FloatEntry>()
        val yLength = yRange.last - yRange.first
        for (x in xRange) {
            result += entryOf(x.toFloat(), (Math.random() * yLength).toFloat())
        }
        return result
    }

}