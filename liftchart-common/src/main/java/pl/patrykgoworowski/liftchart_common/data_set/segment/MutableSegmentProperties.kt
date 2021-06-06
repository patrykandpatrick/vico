package pl.patrykgoworowski.liftchart_common.data_set.segment

data class MutableSegmentProperties(
    override var contentWidth: Float = 0f,
    override var marginWidth: Float = 0f,
) : SegmentProperties {

    override val segmentWidth: Float
        get() = contentWidth + marginWidth

    override fun toString(): String =
        "MutableSegmentProperties(segmentWidth=$segmentWidth, contentWidth=$contentWidth," +
                "marginWidth=$marginWidth)"

}