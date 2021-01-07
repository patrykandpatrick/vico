package pl.patrykgoworowski.liftchart_core.data_set.bar

import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection

public val <T: AnyEntry> Collection<EntryCollection<T>>.minX : Float get() = minOfOrNull { it.minX } ?: 0f
public val <T: AnyEntry> Collection<EntryCollection<T>>.maxX : Float get() = maxOfOrNull { it.maxX } ?: 0f
public val <T: AnyEntry> Collection<EntryCollection<T>>.minY : Float get() = minOfOrNull { it.minY } ?: 0f
public val <T: AnyEntry> Collection<EntryCollection<T>>.maxY : Float get() = maxOfOrNull { it.maxY } ?: 0f
public val <T: AnyEntry> Collection<EntryCollection<T>>.step : Float get() = minOfOrNull { it.step } ?: 0f