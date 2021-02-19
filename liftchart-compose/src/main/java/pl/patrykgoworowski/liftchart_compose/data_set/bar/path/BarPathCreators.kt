package pl.patrykgoworowski.liftchart_compose.data_set.bar.path

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.CutCornerBarPath
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.RoundedCornersShape
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.Shape
import pl.patrykgoworowski.liftchart_compose.extension.density

@Composable
fun RoundedCornerBarPath(
    all: Dp = Dp(0f),
): Shape = RoundedCornersShape(all.value * density)

@Composable
fun RoundedCornerBarPath(
    topLeft: Dp = Dp(0f),
    topRight: Dp = Dp(0f),
    bottomRight: Dp = Dp(0f),
    bottomLeft: Dp = Dp(0f),
): Shape = RoundedCornersShape(
    topLeft.value * density,
    topRight.value * density,
bottomRight.value * density,
bottomLeft.value * density
)

@Composable
fun CutCornerBarPath(
    all: Dp = Dp(0f),
): Shape = CutCornerBarPath(all.value * density)

@Composable
fun CutCornerBarPath(
    topLeft: Dp = Dp(0f),
    topRight: Dp = Dp(0f),
    bottomRight: Dp = Dp(0f),
    bottomLeft: Dp = Dp(0f),
): Shape = CutCornerBarPath(
    topLeft.value * density,
    topRight.value * density,
    bottomRight.value * density,
    bottomLeft.value * density
)