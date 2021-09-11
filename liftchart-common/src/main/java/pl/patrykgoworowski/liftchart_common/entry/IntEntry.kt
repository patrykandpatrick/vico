package pl.patrykgoworowski.liftchart_common.entry

data class IntEntry(val positionX: Int, val positionY: Int) : DataEntry {
    override val x: Float = positionX.toFloat()
    override val y: Float = positionY.toFloat()
}

fun entryOf(x: Int, y: Int) = IntEntry(x, y)
