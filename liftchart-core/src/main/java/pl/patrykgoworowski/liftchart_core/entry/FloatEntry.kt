package pl.patrykgoworowski.liftchart_core.entry

data class FloatEntry(override val x: Float, override val y: Float) : DataEntry<Float, Float>

fun entryOf(x: Float, y: Float) = FloatEntry(x, y)

fun entriesOf(vararg pairs: Pair<Float, Float>): List<FloatEntry> =
    pairs.map { pair ->
        FloatEntry(pair.first, pair.second)
    }