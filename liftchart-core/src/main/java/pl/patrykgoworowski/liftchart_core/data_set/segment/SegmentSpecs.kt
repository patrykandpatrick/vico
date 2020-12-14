package pl.patrykgoworowski.liftchart_core.data_set.segment

import pl.patrykgoworowski.liftchart_core.extension.dp

interface SegmentSpec {
    val minWidth: Float
    val preferredWidth: Float
}

interface BarSegmentSpec : SegmentSpec {
    val spacing: Float
}

fun DefaultSegmentSpec() = object : SegmentSpec {
    override val minWidth: Float = 4f.dp
    override val preferredWidth: Float = 8f.dp
}

fun DefaultBarSegmentSpec() = object : BarSegmentSpec {
    override val minWidth: Float = 4f.dp
    override val preferredWidth: Float = 8f.dp
    override val spacing: Float = 4f.dp
}