package pl.patrykgoworowski.liftchart_common.data_set.bar

import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection

enum class GroupMode {
    Stack, Grouped;

    fun <T: AnyEntry> calculateMaxY(entryCollections: Collection<EntryCollection<T>>): Float = when (this) {
        Grouped -> entryCollections.maxY
        Stack -> {
            entryCollections.fold(HashMap<Float, Float>()) { map, dataSet ->
                dataSet.entries.forEach { entry ->
                    map[entry.x] = map.getOrElse(entry.x) { 0f } + entry.y
                }
                map
            }.values.maxOrNull() ?: 0f
        }
    }

}