package pl.patrykgoworowski.liftchart_core.data_set.mergeable

import pl.patrykgoworowski.liftchart_core.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_core.data_set.DataSet
import pl.patrykgoworowski.liftchart_core.data_set.bar.BarDataSet
import pl.patrykgoworowski.liftchart_core.data_set.bar.MergedBarDataSet
import pl.patrykgoworowski.liftchart_core.data_set.default.DefaultMergedDataSet

public operator fun DataSet.plus(other: DataSet): DefaultMergedDataSet {
    return DefaultMergedDataSet(this, other)
}

public operator fun BarDataSet<AnyEntry>.plus(other: BarDataSet<AnyEntry>): MergedBarDataSet {
    return MergedBarDataSet(this, other)
}

public operator fun MergedBarDataSet.plus(other: BarDataSet<AnyEntry>): MergedBarDataSet {
    return this.add(other)
}