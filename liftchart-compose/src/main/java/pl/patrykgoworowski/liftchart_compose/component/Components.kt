package pl.patrykgoworowski.liftchart_compose.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.ShapeComponent
import pl.patrykgoworowski.liftchart_common.constants.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_compose.extension.colorInt
import pl.patrykgoworowski.liftchart_compose.extension.pixels
import pl.patrykgoworowski.liftchart_compose.path.chartShape

@Composable
public fun rectComponent(
    color: Color,
    thickness: Dp = DEF_BAR_WIDTH.dp,
    shape: Shape = RectangleShape
): RectComponent = RectComponent(
    color = color.colorInt,
    thickness = thickness.pixels,
    shape = shape.chartShape(),
)

@Composable
fun shapeComponent(
    shape: Shape,
    color: Color
): ShapeComponent = ShapeComponent(
    shape = shape.chartShape(),
    color = color.toArgb(),
)