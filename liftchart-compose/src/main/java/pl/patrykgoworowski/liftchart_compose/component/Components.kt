package pl.patrykgoworowski.liftchart_compose.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.component.Component
import pl.patrykgoworowski.liftchart_common.component.OverlayingComponent
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.shape.ShapeComponent
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader
import pl.patrykgoworowski.liftchart_common.constants.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.path.DashedShape
import pl.patrykgoworowski.liftchart_compose.extension.pixels
import pl.patrykgoworowski.liftchart_compose.path.chartShape

typealias ChartShape = pl.patrykgoworowski.liftchart_common.path.Shape

@Composable
public fun rectComponent(
    color: Color,
    thickness: Dp = DEF_BAR_WIDTH.dp,
    shape: Shape = RectangleShape,
    dynamicShader: DynamicShader? = null,
): LineComponent = LineComponent(
    color = color.toArgb(),
    thickness = thickness.pixels,
    shape = shape.chartShape(),
    dynamicShader = dynamicShader,
)

@Composable
public fun rectComponent(
    color: Color,
    thickness: Dp,
    shape: ChartShape,
    dynamicShader: DynamicShader? = null,
): LineComponent = LineComponent(
    color = color.toArgb(),
    thickness = thickness.pixels,
    shape = shape,
    dynamicShader = dynamicShader,
)

@Composable
fun shapeComponent(
    shape: Shape,
    color: Color,
    shader: DynamicShader? = null,
): ShapeComponent<ChartShape> = ShapeComponent(
    shape = shape.chartShape(),
    color = color.toArgb(),
    dynamicShader = shader,
)

@Composable
fun shapeComponent(
    shape: ChartShape,
    color: Color,
    shader: DynamicShader? = null,
): ShapeComponent<ChartShape> = ShapeComponent(
    shape = shape,
    color = color.toArgb(),
    dynamicShader = shader,
)

@Composable
fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingAll: Dp,
) = OverlayingComponent(
    outer = outer,
    inner = inner,
    innerPaddingAll = innerPaddingAll.pixels,
)

@Composable
fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingStart: Dp,
    innerPaddingTop: Dp,
    innerPaddingBottom: Dp,
    innerPaddingEnd: Dp,
) = OverlayingComponent(
    outer = outer,
    inner = inner,
    innerPaddingStart = innerPaddingStart.pixels,
    innerPaddingTop = innerPaddingTop.pixels,
    innerPaddingBottom = innerPaddingBottom.pixels,
    innerPaddingEnd = innerPaddingEnd.pixels,
)

@Composable
fun dashedShape(
    shape: Shape,
    dashLength: Dp,
    gapLength: Dp,
    fitStrategy: DashedShape.FitStrategy = DashedShape.FitStrategy.Resize
) = DashedShape(
    shape = shape.chartShape(),
    dashLength = dashLength.pixels,
    gapLength = gapLength.pixels,
    fitStrategy = fitStrategy,
)

@Composable
fun dashedShape(
    shape: ChartShape,
    dashLength: Dp,
    gapLength: Dp,
    fitStrategy: DashedShape.FitStrategy = DashedShape.FitStrategy.Resize
) = DashedShape(
    shape = shape,
    dashLength = dashLength.pixels,
    gapLength = gapLength.pixels,
    fitStrategy = fitStrategy,
)