package pl.patrykgoworowski.liftchart_common.data_set.entry.collection

import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class EntryModelCalculator {

    val stackedMap: HashMap<Float, Float> = HashMap()

    var minX: Float = Float.MAX_VALUE
    var maxX: Float = Float.MIN_VALUE
    var minY: Float = Float.MAX_VALUE
    var maxY: Float = Float.MIN_VALUE
    var step: Float = Float.MAX_VALUE
    var stackedMinY: Float = Float.MAX_VALUE
    var stackedMaxY: Float = Float.MIN_VALUE

    fun resetValues() {
        minX = Float.MAX_VALUE
        maxX = Float.MIN_VALUE
        minY = Float.MAX_VALUE
        maxY = Float.MIN_VALUE
        step = Float.MAX_VALUE
        stackedMinY = Float.MAX_VALUE
        stackedMaxY = Float.MIN_VALUE
        stackedMap.clear()
    }

    fun calculateData(data: List<List<DataEntry>>) {
        resetValues()
        calculateMinMax(data)
    }

    protected open fun calculateMinMax(data: List<List<DataEntry>>) {
        data.forEach { entryCollection ->
            entryCollection.forEach { entry ->
                minX = minX.coerceAtMost(entry.x)
                maxX = maxX.coerceAtLeast(entry.x)
                minY = minY.coerceAtMost(entry.y)
                maxY = maxY.coerceAtLeast(entry.y)
                stackedMap[entry.x] = stackedMap.getOrElse(entry.x) { 0f } + entry.y
            }
            calculateStep(entryCollection)
        }
        stackedMap.values.forEach { y ->
            stackedMinY = min(stackedMinY, y)
            stackedMaxY = max(stackedMaxY, y)
        }
    }

    private fun calculateStep(entries: Collection<DataEntry>) {
        val iterator = entries.iterator()
        var currentEntry: DataEntry
        var previousEntry: DataEntry? = null
        while (iterator.hasNext()) {
            currentEntry = iterator.next()
            previousEntry?.let { prevEntry ->
                val difference = abs(currentEntry.x - prevEntry.x)
                step = if (step == NO_VALUE) difference else min(
                    step,
                    difference
                )
            }

            previousEntry = currentEntry
        }
        if (step == NO_VALUE) step = 1f
    }

    companion object {
        const val NO_VALUE = -1f
    }

}