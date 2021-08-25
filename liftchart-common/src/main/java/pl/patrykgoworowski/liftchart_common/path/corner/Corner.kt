package pl.patrykgoworowski.liftchart_common.path.corner

import pl.patrykgoworowski.liftchart_common.IllegalPercentageException

sealed class Corner(
    val absoluteSize: Float,
    val cornerTreatment: CornerTreatment,
) {

    abstract fun getCornerSize(availableCornerSize: Float): Float

    class Absolute(
        size: Float,
        cornerTreatment: CornerTreatment,
    ) : Corner(size, cornerTreatment) {

        override fun getCornerSize(availableCornerSize: Float): Float = absoluteSize

    }

    class Relative(
        val percentage: Int,
        cornerTreatment: CornerTreatment,
    ) : Corner(0f, cornerTreatment) {

        init {
            if (percentage !in 0..100) throw IllegalPercentageException(percentage)
        }

        override fun getCornerSize(availableCornerSize: Float): Float =
            availableCornerSize / 100 * percentage

    }

}