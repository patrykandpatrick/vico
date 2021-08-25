package pl.patrykgoworowski.liftchart_common.path.corner

import pl.patrykgoworowski.liftchart_common.IllegalPercentageException

sealed class Corner(
    public val absoluteSize: Float,
    public val cornerTreatment: CornerTreatment,
) {

    public abstract fun getCornerSize(availableCornerSize: Float): Float

    public class Absolute(
        size: Float,
        cornerTreatment: CornerTreatment,
    ) : Corner(size, cornerTreatment) {

        override fun getCornerSize(availableCornerSize: Float): Float = absoluteSize

    }

    public class Relative(
        public val percentage: Int,
        cornerTreatment: CornerTreatment,
    ) : Corner(0f, cornerTreatment) {

        init {
            if (percentage !in 0..100) throw IllegalPercentageException(percentage)
        }

        override fun getCornerSize(availableCornerSize: Float): Float =
            availableCornerSize / 100 * percentage

    }

}