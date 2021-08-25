package pl.patrykgoworowski.liftchart_common.scroll

class ScrollHandler(
    private val setScrollAmount: (Float) -> Unit,
    var maxScrollDistance: Float = 0f,
) {

    var currentScroll: Float = 0f
        set(value) {
            field = getClampedScroll(value)
            setScrollAmount(value)
        }

    fun getClampedScroll(scroll: Float): Float =
        maxOf(0f, minOf(scroll, maxScrollDistance))

    fun handleScrollDelta(delta: Float): Float {
        currentScroll = getClampedScroll(currentScroll - delta)
        return minOf(maxOf(maxScrollDistance - currentScroll, 0f), delta)
    }

    fun handleScroll(targetScroll: Float): Float =
        handleScrollDelta((currentScroll - targetScroll))

}