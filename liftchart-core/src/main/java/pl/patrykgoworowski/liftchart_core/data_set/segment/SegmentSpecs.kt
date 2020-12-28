package pl.patrykgoworowski.liftchart_core.data_set.segment

import pl.patrykgoworowski.liftchart_core.extension.dp

interface SegmentSpec {
    val width: Float
    val spacing: Float
}

fun SegmentSpec(
    preferredWidth: Float = 8f.dp,
    spacing: Float = 4f.dp
) = object : SegmentSpec {
    override val width: Float = preferredWidth
    override val spacing: Float = spacing
}