package pl.patrykgoworowski.liftchart_compose.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Dp.pixels: Float
    @Composable
    get() = value * LocalDensity.current.density

val density: Float
    @Composable
    get() = LocalDensity.current.density

val Int.pxToDp: Dp
    @Composable
    get() = (this / density).dp
