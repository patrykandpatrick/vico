package pl.patrykgoworowski.liftchart_common.data_set.segment

data class MutableSegmentProperties(
    override var contentWidth: Float = 0f,
    override var marginWidth: Float = 0f,
) : SegmentProperties {

    override val segmentWidth: Float
        get() = contentWidth + marginWidth

    public fun clear() {
        contentWidth = 0f
        marginWidth = 0f
    }

    override fun toString(): String =
        "MutableSegmentProperties(segmentWidth=$segmentWidth, contentWidth=$contentWidth," +
                "marginWidth=$marginWidth)"
}
