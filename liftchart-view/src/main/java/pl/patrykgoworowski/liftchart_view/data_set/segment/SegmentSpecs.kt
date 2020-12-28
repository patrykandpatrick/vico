package pl.patrykgoworowski.liftchart_view.data_set.segment

import pl.patrykgoworowski.liftchart_core.data_set.segment.SegmentSpec
import pl.patrykgoworowski.liftchart_core.extension.dp
import pl.patrykgoworowski.liftchart_view.Dp

fun SegmentSpec(
    @Dp preferredWidth: Float,
    @Dp spacing: Float
) = object : SegmentSpec {
    override val width: Float = preferredWidth.dp
    override val spacing: Float = spacing.dp
}

