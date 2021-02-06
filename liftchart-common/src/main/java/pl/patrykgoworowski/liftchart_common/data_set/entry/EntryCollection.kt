package pl.patrykgoworowski.liftchart_common.data_set.entry

interface EntryCollection {
    val minX: Float
    val maxX: Float

    val minY: Float
    val maxY: Float

    val step: Float
}