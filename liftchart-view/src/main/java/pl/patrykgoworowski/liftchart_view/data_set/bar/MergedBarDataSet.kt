package pl.patrykgoworowski.liftchart_view.data_set.bar

import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection
import pl.patrykgoworowski.liftchart_core.data_set.bar.BarPathCreator
import pl.patrykgoworowski.liftchart_core.data_set.bar.CoreMergedBarDataSet
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_view.extension.dp

class MergedBarDataSet<T: AnyEntry>(
    entryCollections: List<EntryCollection<T>>,
    barWidth: Float = DEF_BAR_WIDTH.dp,
    barSpacing: Float = DEF_BAR_SPACING.dp,
    barInnerSpacing: Float = DEF_BAR_INNER_SPACING.dp,
    groupMode: GroupMode = GroupMode.Stack,
    colors: List<Int> = emptyList(),
    barPathCreators: List<BarPathCreator> = emptyList(),

    ) : CoreMergedBarDataSet<T>(entryCollections) {

    init {
        setColors(colors)
        this.barPathCreators.addAll(barPathCreators)
        this.groupMode = groupMode
        this.barWidth = barWidth
        this.barSpacing = barSpacing
        this.barInnerSpacing = barInnerSpacing
    }

}