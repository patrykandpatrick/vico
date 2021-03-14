package pl.patrykgoworowski.liftchart_common.axis

sealed class AxisPosition {
    val isTop: Boolean
        get() = this is TopAxis

    val isBottom: Boolean
        get() = this is BottomAxis

    fun isLeft(isLtr: Boolean): Boolean = this is StartAxis && isLtr

    fun isRight(isLtr: Boolean): Boolean = this is EndAxis && isLtr
}

sealed class HorizontalAxisPosition : AxisPosition()

sealed class VerticalAxisPosition : AxisPosition()

object TopAxis : HorizontalAxisPosition()
object BottomAxis : HorizontalAxisPosition()
object StartAxis : VerticalAxisPosition()
object EndAxis : VerticalAxisPosition()