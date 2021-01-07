package pl.patrykgoworowski.liftchart_view.data_set.bar

import android.graphics.Color.MAGENTA
import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection
import pl.patrykgoworowski.liftchart_core.data_set.bar.BarPathCreator
import pl.patrykgoworowski.liftchart_core.data_set.bar.CoreBarDataSet
import pl.patrykgoworowski.liftchart_core.data_set.bar.DefaultBarPath
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_view.extension.dp

public open class BarDataSet<T: AnyEntry>(
    entryCollection: EntryCollection<T>,
    color: Int = MAGENTA,
    barWidth: Float = DEF_BAR_WIDTH.dp,
    barSpacing: Float = DEF_BAR_SPACING.dp,
    barPathCreator: BarPathCreator = DefaultBarPath()
) : CoreBarDataSet<T>(entryCollection) {

    init {
        this.color = color
        this.barWidth = barWidth
        this.barSpacing = barSpacing
        this.barPathCreator = barPathCreator
    }

}