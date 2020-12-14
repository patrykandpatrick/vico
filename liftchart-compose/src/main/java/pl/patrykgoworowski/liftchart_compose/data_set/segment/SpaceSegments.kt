package pl.patrykgoworowski.liftchart_compose.data_set.segment

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_core.data_set.segment.BarSegmentSpec
import pl.patrykgoworowski.liftchart_core.data_set.segment.SegmentSpec


//@Composable
fun SegmentSpec(
    minWidth: Dp,
    preferredWidth: Dp = minWidth
) = object : SegmentSpec {
    val density = 3f //AmbientDensity.current.density
    override val minWidth: Float = minWidth.value * density
    override val preferredWidth: Float = preferredWidth.value * density
}

//@Composable
fun BarSegmentSpec(
    minWidth: Dp,
    preferredWidth: Dp = minWidth,
    spacing: Dp = 0.dp
) = object : BarSegmentSpec {
    val density = 3f //AmbientDensity.current.density
    override val minWidth: Float = minWidth.value * density
    override val preferredWidth: Float = preferredWidth.value * density
    override val spacing: Float = spacing.value * density
}