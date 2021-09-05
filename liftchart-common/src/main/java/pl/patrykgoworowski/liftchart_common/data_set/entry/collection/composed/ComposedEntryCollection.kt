package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.composed

import pl.patrykgoworowski.liftchart_common.data_set.composed.ComposedEntryModel
import pl.patrykgoworowski.liftchart_common.data_set.emptyComposedEntryModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.extension.runEach

class ComposedEntryCollection<Model : EntryModel>(
    public val entryCollections: List<EntryCollection<Model>>
) : EntryCollection<ComposedEntryModel<Model>> {

    override var model: ComposedEntryModel<Model> = emptyComposedEntryModel()
        private set

    private val listeners = ArrayList<(ComposedEntryModel<Model>) -> Unit>()

    private val internalListener = { _: Model ->
        recalculateModel()
        listeners.runEach(model)
    }

    init {
        entryCollections.forEach { entryCollection ->
            entryCollection.addOnEntriesChangedListener(internalListener)
        }
    }

    private fun recalculateModel() {
        val models = entryCollections.map { it.model }

        model = ComposedEntryModel(
            composedEntryCollections = models,
            entryCollections = models.map { it.entryCollections }.flatten(),
            minX = models.minOf { it.minX },
            maxX = models.maxOf { it.maxX },
            minY = models.minOf { it.minY },
            maxY = models.maxOf { it.maxY },
            composedMaxY = models.maxOf { it.composedMaxY },
            step = models.minOf { it.step }
        )
    }

    override fun addOnEntriesChangedListener(listener: (ComposedEntryModel<Model>) -> Unit) {
        listeners += listener
        listener(model)
    }

    override fun removeOnEntriesChangedListener(listener: (ComposedEntryModel<Model>) -> Unit) {
        listeners -= listener
    }
}
