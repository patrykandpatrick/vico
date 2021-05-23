package pl.patrykgoworowski.liftchart_common.dimensions

interface Dimensions<T: Number> {

    val start: T
    val top: T
    val end: T
    val bottom: T

    fun getLeft(isLTR: Boolean) = if (isLTR) start else end

    fun getRight(isLTR: Boolean) = if (isLTR) end else start

}