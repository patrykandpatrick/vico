package pl.patrykgoworowski.liftchart_compose.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.unit.Dp

@Composable
val Dp.pixels: Float
    get() = value * AmbientDensity.current.density