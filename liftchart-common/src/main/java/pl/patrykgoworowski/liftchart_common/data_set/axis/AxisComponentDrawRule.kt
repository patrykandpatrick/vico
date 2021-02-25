package pl.patrykgoworowski.liftchart_common.data_set.axis

fun interface AxisComponentDrawRule {
    fun getShouldDraw(index: Int, count: Int): Boolean
}

object AlwaysDrawRule : AxisComponentDrawRule {

    override fun getShouldDraw(index: Int, count: Int): Boolean = true

}

object MiddleDrawRule : AxisComponentDrawRule {

    override fun getShouldDraw(index: Int, count: Int): Boolean = when (index) {
        count - 1,
        0 -> false
        else -> true
    }
}