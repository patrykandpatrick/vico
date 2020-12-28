package pl.patrykgoworowski.liftchart_core.data_set.segment

data class DrawSegmentSpec(
    var startMargin: Float = 0f,
    var endMargin: Float = 0f,
    override var width: Float = 0f,
    override var spacing: Float = 0f
): SegmentSpec