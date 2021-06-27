package pl.patrykgoworowski.liftchart_common.axis

sealed class AxisPosition {
    val isTop: Boolean
        get() = this is Horizontal.Top

    val isBottom: Boolean
        get() = this is Horizontal.Bottom

    val isStart: Boolean
        get() = this is Vertical.Start

    val isEnd: Boolean
        get() = this is Vertical.End

    fun isLeft(isLtr: Boolean): Boolean = this is Vertical.Start && isLtr

    fun isRight(isLtr: Boolean): Boolean = this is Vertical.End && isLtr

    sealed class Horizontal : AxisPosition() {
        object Top : Horizontal()
        object Bottom : Horizontal()
    }

    sealed class Vertical : AxisPosition() {
        object Start : Vertical()
        object End : Vertical()
    }
}