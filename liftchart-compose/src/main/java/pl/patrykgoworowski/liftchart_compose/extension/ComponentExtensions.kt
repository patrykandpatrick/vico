@file:Suppress("ComposableNaming")

package pl.patrykgoworowski.liftchart_compose.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.DEF_SHADOW_COLOR
import pl.patrykgoworowski.liftchart_common.component.shape.ShapeComponent
import pl.patrykgoworowski.liftchart_compose.component.ChartShape

@Composable
fun <T : ChartShape> ShapeComponent<T>.setShadow(
    radius: Dp,
    dx: Dp = 0.dp,
    dy: Dp = 0.dp,
    color: Color = Color(DEF_SHADOW_COLOR),
) = setShadow(
    radius = radius.pixels,
    dx = dx.pixels,
    dy = dy.pixels,
    color = color.toArgb(),
)