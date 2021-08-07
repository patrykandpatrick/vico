package pl.patrykgoworowski.liftchart_common.scroll


class ScrollHandler(
    private val setScrollAmount: (Float) -> Unit,
    public var maxScrollDistance: Float = 0f,
) {

    var currentScroll: Float = 0f
        set(value) {
            field = value
            setScrollAmount(value)
        }

    public fun getClampedScroll(scroll: Float): Float =
        maxOf(0f, minOf(scroll, maxScrollDistance))

    public fun handleScrollDelta(delta: Float): Float {
        currentScroll = getClampedScroll(currentScroll - delta)
        return minOf(maxOf(maxScrollDistance - currentScroll, 0f), delta)
    }

    public fun handleScroll(targetScroll: Float): Float =
        handleScrollDelta((currentScroll - targetScroll))

}