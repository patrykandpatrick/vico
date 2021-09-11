package pl.patrykgoworowski.liftchart_common.dimensions

interface Dimensions {

    val start: Float
    val top: Float
    val end: Float
    val bottom: Float

    fun getLeft(isLTR: Boolean) = if (isLTR) start else end

    fun getRight(isLTR: Boolean) = if (isLTR) end else start
}
