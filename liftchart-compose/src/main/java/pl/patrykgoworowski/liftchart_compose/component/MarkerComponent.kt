package pl.patrykgoworowski.liftchart_compose.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import pl.patrykgoworowski.liftchart_common.component.Component
import pl.patrykgoworowski.liftchart_common.component.MarkerComponent
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.path.corner.MarkerCorneredShape

@Composable
fun markerComponent(
    label: TextComponent,
    indicator: Component,
    guideline: LineComponent,
    shape: MarkerCorneredShape,
    markerBackgroundColor: Color,
): MarkerComponent = MarkerComponent(
    label = label,
    indicator = indicator,
    guideline = guideline,
    shape = shape,
    markerBackgroundColor = markerBackgroundColor.toArgb()
)