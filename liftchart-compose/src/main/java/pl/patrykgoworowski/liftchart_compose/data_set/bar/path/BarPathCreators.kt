package pl.patrykgoworowski.liftchart_compose.data_set.bar.path

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.BarPathCreator
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.CutCornerBarPath
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.RoundedCornerBarPath
import pl.patrykgoworowski.liftchart_compose.extension.density

@Composable
fun RoundedCornerBarPath(
    all: Dp = Dp(0f),
): BarPathCreator = RoundedCornerBarPath(all.value * density)

@Composable
fun RoundedCornerBarPath(
    topLeft: Dp = Dp(0f),
    topRight: Dp = Dp(0f),
    bottomRight: Dp = Dp(0f),
    bottomLeft: Dp = Dp(0f),
): BarPathCreator = RoundedCornerBarPath(
    topLeft.value * density,
    topRight.value * density,
bottomRight.value * density,
bottomLeft.value * density
)

@Composable
fun CutCornerBarPath(
    all: Dp = Dp(0f),
): BarPathCreator = CutCornerBarPath(all.value * density)

@Composable
fun CutCornerBarPath(
    topLeft: Dp = Dp(0f),
    topRight: Dp = Dp(0f),
    bottomRight: Dp = Dp(0f),
    bottomLeft: Dp = Dp(0f),
): BarPathCreator = CutCornerBarPath(
    topLeft.value * density,
    topRight.value * density,
    bottomRight.value * density,
    bottomLeft.value * density
)