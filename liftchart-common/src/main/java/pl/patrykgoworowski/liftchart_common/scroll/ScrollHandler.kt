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

    public fun handleScrollDelta(delta: Float): Float {
        currentScroll = maxOf(0f, minOf(currentScroll - delta, maxScrollDistance))
        return minOf(maxOf(maxScrollDistance - currentScroll, 0f), delta)
    }

}