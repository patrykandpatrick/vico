package pl.patrykgoworowski.liftchart_compose.data_set.segment

import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.liftchart_core.data_set.segment.SegmentSpec


//@Composable
fun SegmentSpec(
    preferredWidth: Dp,
    spacing: Dp
) = object : SegmentSpec {
    val density = 3f //AmbientDensity.current.density
    override val width: Float = preferredWidth.value * density
    override val spacing: Float = spacing.value * density
}

