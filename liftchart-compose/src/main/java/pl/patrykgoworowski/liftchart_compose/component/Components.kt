package pl.patrykgoworowski.liftchart_compose.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.constants.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.path.Shape
import pl.patrykgoworowski.liftchart_common.path.rectShape
import pl.patrykgoworowski.liftchart_compose.extension.colorInt
import pl.patrykgoworowski.liftchart_compose.extension.pixels
import pl.patrykgoworowski.liftchart_common.component.RectComponent as LibRectComponent

@Composable
public fun rectComponent(
    color: Color,
    thickness: Dp = DEF_BAR_WIDTH.dp,
    shape: Shape = rectShape()
): LibRectComponent = LibRectComponent(
    color = color.colorInt,
    thickness = thickness.pixels,
    shape = shape
)