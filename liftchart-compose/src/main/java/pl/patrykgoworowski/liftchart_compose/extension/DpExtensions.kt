package pl.patrykgoworowski.liftchart_compose.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
val Dp.pixels: Float
    get() = value * AmbientDensity.current.density

@Composable
val density: Float
    get() = AmbientDensity.current.density

@Composable
val Int.pxToDp: Dp
    get() = (this / density).dp