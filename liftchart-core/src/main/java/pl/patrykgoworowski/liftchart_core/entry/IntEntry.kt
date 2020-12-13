package pl.patrykgoworowski.liftchart_core.entry

data class IntEntry(val positionX: Int, val positionY: Int) : DataEntry<Int, Int> {
    override val x: Float = positionX.toFloat()
    override val y: Float = positionY.toFloat()
}

fun entryOf(x: Int, y: Int) = IntEntry(x, y)

fun entriesOf(vararg pairs: Pair<Int, Int>): List<IntEntry> =
    pairs.map { pair ->
        IntEntry(pair.first, pair.second)
    }