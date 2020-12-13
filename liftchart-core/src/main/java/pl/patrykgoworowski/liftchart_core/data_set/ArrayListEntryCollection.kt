package pl.patrykgoworowski.liftchart_core.data_set

import pl.patrykgoworowski.liftchart_core.data_set.EntryCollection.Companion.NO_VALUE


class ArrayListEntryCollection : EntryCollection<AnyEntry, ArrayList<AnyEntry>> {

    override val entries: ArrayList<AnyEntry> = ArrayList()

    private var _minX: Float = NO_VALUE
    private var _maxX: Float = NO_VALUE
    private var _minY: Float = NO_VALUE
    private var _maxY: Float = NO_VALUE

    override val minX: Float by this::_minX
    override val maxX: Float by this::_maxX
    override val minY: Float by this::_minY
    override val maxY: Float by this::_maxY

    override fun setEntries(entries: ArrayList<AnyEntry>) {
        entries.clear()
        entries.addAll(entries)
        recalculateMinMax()
    }

    override fun plusAssign(entry: AnyEntry) {
        entries += entry
        recalculateMinMax()
    }

    override fun plusAssign(entries: Collection<AnyEntry>) {
        this.entries.addAll(entries)
        recalculateMinMax()
    }

    override fun minusAssign(entry: AnyEntry) {
        entries -= entry
        recalculateMinMax()
    }

    override fun minusAssign(entries: Collection<AnyEntry>) {
        this.entries.removeAll(entries)
        recalculateMinMax()
    }

    private fun recalculateMinMax() {
        entries.forEach { entry ->
            _minX = if (_minX == NO_VALUE) entry.x else _minX.coerceAtMost(entry.x)
            _maxX = _maxX.coerceAtLeast(entry.x)
            _minY = if (_minY == NO_VALUE) entry.y else _minY.coerceAtMost(entry.y)
            _maxY = _maxY.coerceAtLeast(entry.y)
        }
    }

}
