package pl.patrykgoworowski.liftchart_common.entry

data class FloatEntry(
    override val x: Float,
    override val y: Float,
) : DataEntry

fun entryOf(x: Float, y: Float) = FloatEntry(x, y)

fun entriesOf(vararg pairs: Pair<Number, Number>): List<FloatEntry> =
    pairs.map { (x, y) ->
        FloatEntry(x.toFloat(), y.toFloat())
    }