package pl.patrykgoworowski.liftchart_compose.component.dimension

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions
import pl.patrykgoworowski.liftchart_compose.extension.pixels

@Composable
fun dimensionsOf(all: Dp) = dimensionsOf(all, all, all, all)

@Composable
fun dimensionsOf(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
) = MutableDimensions(start.pixels, top.pixels, end.pixels, bottom.pixels)