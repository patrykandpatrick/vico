package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff

import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import pl.patrykgoworowski.liftchart_common.entry.entryOf
import pl.patrykgoworowski.liftchart_common.extension.setAll
import java.util.*

public class DefaultDiffProcessor : DiffProcessor<DataEntry> {

    private val progressMaps = ArrayList<TreeMap<Float, ProgressModel>>()

    private val oldEntries = ArrayList<List<DataEntry>>()
    private val newEntries = ArrayList<List<DataEntry>>()

    override fun setEntries(old: List<List<DataEntry>>, new: List<List<DataEntry>>) {
        oldEntries.setAll(old)
        newEntries.setAll(new)
        updateProgressMap()
    }

    override fun setEntries(new: List<List<DataEntry>>) {
        oldEntries.setAll(newEntries)
        newEntries.setAll(new)
        updateProgressMap()
    }

    override fun progressDiff(progress: Float): List<List<DataEntry>> =
        progressMaps
            .map { map ->
                map.map { (x, model) -> entryOf(x, model.progressDiff(progress)) }
            }

    private fun updateProgressMap() {
        progressMaps.clear()
        val maxListSize = maxOf(oldEntries.size, newEntries.size)
        for (i in 0 until maxListSize) {
            val map = TreeMap<Float, ProgressModel>()
            oldEntries
                .getOrNull(i)
                ?.forEach { (x, y) ->
                    map[x] = ProgressModel(oldY = y)
                }
            newEntries
                .getOrNull(i)
                ?.forEach { (x, y) ->
                    map[x] = ProgressModel(
                        oldY = map[x]?.oldY,
                        newY = y,
                    )
                }
            progressMaps.add(map)
        }
    }

    private operator fun DataEntry.component1(): Float = x
    private operator fun DataEntry.component2(): Float = y

    private data class ProgressModel(
        val oldY: Float? = null,
        val newY: Float? = null,
    ) {

        fun progressDiff(progress: Float): Float {
            val oldY = oldY ?: 0f
            val newY = newY ?: 0f
            return oldY + ((newY - oldY) * progress)
        }

    }

}