package pl.patrykgoworowski.liftchart_common.data_set.entry

import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryCollection
import kotlin.math.abs
import kotlin.math.min

open class SingleEntryModelCalculator {

    var minX: Float = Float.MAX_VALUE
    var maxX: Float = Float.MIN_VALUE
    var minY: Float = Float.MAX_VALUE
    var maxY: Float = Float.MIN_VALUE
    var step: Float = Float.MAX_VALUE

    open fun resetValues() {
        minX = Float.MAX_VALUE
        maxX = Float.MIN_VALUE
        minY = Float.MAX_VALUE
        maxY = Float.MIN_VALUE
        step = Float.MAX_VALUE
    }

    fun calculateData(entries: Collection<AnyEntry>) {
        resetValues()
        calculateMinMax(entries)
        calculateStep(entries)
    }

    open fun calculateMinMax(entries: Collection<AnyEntry>) {
        entries.forEach { entry ->
            minX = minX.coerceAtMost(entry.x)
            maxX = maxX.coerceAtLeast(entry.x)
            minY = minY.coerceAtMost(entry.y)
            maxY = maxY.coerceAtLeast(entry.y)
        }
    }

    fun calculateStep(entries: Collection<AnyEntry>) {
        val iterator = entries.iterator()
        var currentEntry: AnyEntry
        var previousEntry: AnyEntry? = null
        while (iterator.hasNext()) {
            currentEntry = iterator.next()
            previousEntry?.let { prevEntry ->
                val difference = abs(currentEntry.x - prevEntry.x)
                step = if (step == SingleEntryCollection.NO_VALUE) difference else min(step, difference)
            }

            previousEntry = currentEntry
        }
        if (step == SingleEntryCollection.NO_VALUE) step = 1f
    }

}