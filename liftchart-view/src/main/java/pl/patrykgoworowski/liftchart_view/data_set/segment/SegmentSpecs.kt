package pl.patrykgoworowski.liftchart_view.data_set.segment

import pl.patrykgoworowski.liftchart_core.data_set.segment.BarSegmentSpec
import pl.patrykgoworowski.liftchart_core.data_set.segment.SegmentSpec
import pl.patrykgoworowski.liftchart_core.extension.dp
import pl.patrykgoworowski.liftchart_view.Dp

fun SegmentSpec(
    @Dp minWidth: Float,
    @Dp preferredWidth: Float = minWidth
) = object : SegmentSpec {
    override val minWidth: Float = minWidth.dp
    override val preferredWidth: Float = preferredWidth.dp
}

fun BarSegmentSpec(
    @Dp minWidth: Float,
    @Dp preferredWidth: Float = minWidth,
    @Dp spacing: Float = 0f
) = object : BarSegmentSpec {
    override val minWidth: Float = minWidth.dp
    override val preferredWidth: Float = preferredWidth.dp
    override val spacing: Float = spacing.dp
}