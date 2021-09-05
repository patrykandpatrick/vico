package pl.patrykgoworowski.liftchart_common.data_set.segment

interface SegmentProperties {
    val contentWidth: Float
    val marginWidth: Float
    val segmentWidth: Float

    operator fun component1(): Float = contentWidth
    operator fun component2(): Float = marginWidth
    operator fun component3(): Float = segmentWidth
}
