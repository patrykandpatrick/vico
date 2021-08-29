package pl.patrykgoworowski.liftchart_common.data_set.entry.collection

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff.DefaultDiffAnimator
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff.DefaultDiffProcessor
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff.DiffAnimator
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff.DiffProcessor
import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import pl.patrykgoworowski.liftchart_common.entry.entryOf
import pl.patrykgoworowski.liftchart_common.extension.setAll

typealias EntryListModelListener = (EntryModel) -> Unit

class EntryList(
    public var diffAnimator: DiffAnimator = DefaultDiffAnimator(),
    public var animateChanges: Boolean = true
) : EntryCollection<EntryModel> {

    private val calculator = EntryModelCalculator()
    private val diffProcessor: DiffProcessor<DataEntry> = DefaultDiffProcessor()
    private val listeners: ArrayList<EntryListModelListener> = ArrayList()

    public val data: ArrayList<List<DataEntry>> = ArrayList()

    override var model: EntryModel = emptyEntryModel()

    override val minX: Float by calculator::minX
    override val maxX: Float by calculator::maxX
    override val minY: Float by calculator::minY
    override val maxY: Float by calculator::maxY
    override val step: Float by calculator::step
    override val stackedMaxY: Float by calculator::stackedMaxY
    override val stackedMinY: Float by calculator::stackedMinY

    constructor(
        entryCollections: List<List<DataEntry>>,
        animateChanges: Boolean = true,
    ) : this(animateChanges = animateChanges) {
        setEntries(entryCollections)
    }

    constructor(
        vararg entryCollections: List<DataEntry>,
        animateChanges: Boolean = true,
    ) : this(animateChanges = animateChanges) {
        setEntries(entryCollections.toList())
    }

    override fun setEntries(entries: List<List<DataEntry>>) {
        if (animateChanges) {
            diffProcessor.setEntries(
                old = diffProcessor.progressDiff(diffAnimator.currentProgress),
                new = entries,
            )
            diffAnimator.start { progress ->
                refreshModel(diffProcessor.progressDiff(progress))
            }
        } else {
            refreshModel(entries)
        }
    }

    override fun setEntries(vararg entries: List<DataEntry>) {
        setEntries(entries.toList())
    }

    private fun refreshModel(entries: List<List<DataEntry>>) {
        data.setAll(entries)
        calculator.resetValues()
        calculator.calculateData(data)
        notifyChange()
    }

    override fun addOnEntriesChangedListener(listener: EntryListModelListener) {
        listeners += listener
        listener(model)
    }

    override fun removeOnEntriesChangedListener(listener: EntryListModelListener) {
        listeners -= listener
    }

    private fun notifyChange() {
        val mergedEntries = calculator.stackedMap.map { (x, y) ->
            entryOf(x, y)
        }

        model = EntryModel(
            data,
            mergedEntries,
            minX,
            maxX,
            minY,
            maxY,
            stackedMinY,
            stackedMaxY,
            step
        )
        listeners.forEach { it(model) }
    }
}