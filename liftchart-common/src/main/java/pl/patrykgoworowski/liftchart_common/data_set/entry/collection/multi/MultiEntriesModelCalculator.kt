package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi

import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.SingleEntryModelCalculator
import kotlin.math.max
import kotlin.math.min

class MultiEntriesModelCalculator: SingleEntryModelCalculator() {

    private val stackedMap: HashMap<Float, Float> = HashMap()

    var stackedMinY: Float = Float.MAX_VALUE
    var stackedMaxY: Float = Float.MIN_VALUE

    override fun resetValues() {
        super.resetValues()
        stackedMinY = Float.MAX_VALUE
        stackedMaxY = Float.MIN_VALUE
        stackedMap.clear()
    }

    fun calculateData(data: List<List<AnyEntry>>) {
        resetValues()
        calculateMinMax(data)
    }

    private fun calculateMinMax(data: List<List<AnyEntry>>) {
        data.forEach { entryCollection ->
            calculateMinMax(entryCollection)
            calculateStep(entryCollection)
        }
        stackedMap.values.forEach { y ->
            stackedMinY = min(stackedMinY, y)
            stackedMaxY = max(stackedMaxY, y)
        }
    }

    override fun calculateMinMax(entries: Collection<AnyEntry>) {
        entries.forEach { entry ->
            minX = minX.coerceAtMost(entry.x)
            maxX = maxX.coerceAtLeast(entry.x)
            minY = minY.coerceAtMost(entry.y)
            maxY = maxY.coerceAtLeast(entry.y)
            stackedMap[entry.x] = stackedMap.getOrElse(entry.x) { 0f } + entry.y
        }
    }

}