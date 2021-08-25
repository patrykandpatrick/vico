@file:Suppress("LocalVariableName", "ComposableNaming")

package pl.patrykgoworowski.liftchart_compose.component.dimension

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.component.dimension.Padding
import pl.patrykgoworowski.liftchart_compose.extension.pixels

@Composable
fun Padding.setPadding(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
) {
    padding.set(start.pixels, top.pixels, end.pixels, bottom.pixels)
}

@Composable
fun Padding.setPadding(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
) {
    val _horizontal = horizontal.pixels
    val _vertical = vertical.pixels
    padding.set(_horizontal, _vertical, _horizontal, _vertical)
}

@Composable
fun Padding.setPadding(
    all: Dp = 0.dp,
) {
    val _all = all.pixels
    padding.set(_all, _all, _all, _all)
}