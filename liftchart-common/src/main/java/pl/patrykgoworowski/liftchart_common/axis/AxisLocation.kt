package pl.patrykgoworowski.liftchart_common.axis

sealed class AxisPosition(
    val position: Position
)

sealed class HorizontalAxisPosition(
    position: Position
) : AxisPosition(position)

sealed class VerticalAxisPosition(
    position: Position
) : AxisPosition(position)

object TopAxis : HorizontalAxisPosition(Position.TOP)
object BottomAxis : HorizontalAxisPosition(Position.BOTTOM)
object StartAxis : VerticalAxisPosition(Position.START)
object EndAxis : VerticalAxisPosition(Position.END)

enum class Position {
    START,
    TOP,
    END,
    BOTTOM
}