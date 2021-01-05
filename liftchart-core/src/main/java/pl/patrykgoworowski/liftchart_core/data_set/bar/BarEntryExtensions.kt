package pl.patrykgoworowski.liftchart_core.data_set.bar

import pl.patrykgoworowski.liftchart_core.data_set.AnyEntry

public val Collection<BarDataSet<AnyEntry>>.minX : Float get() = minOfOrNull { it.minX } ?: 0f
public val Collection<BarDataSet<AnyEntry>>.maxX : Float get() = maxOfOrNull { it.maxX } ?: 0f
public val Collection<BarDataSet<AnyEntry>>.minY : Float get() = minOfOrNull { it.minY } ?: 0f
public val Collection<BarDataSet<AnyEntry>>.maxY : Float get() = maxOfOrNull { it.maxY } ?: 0f
public val Collection<BarDataSet<AnyEntry>>.step : Float get() = minOfOrNull { it.step } ?: 0f