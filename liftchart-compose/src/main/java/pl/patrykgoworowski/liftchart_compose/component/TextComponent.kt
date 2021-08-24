package pl.patrykgoworowski.liftchart_compose.component

import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import pl.patrykgoworowski.liftchart_common.DEF_LABEL_LINE_COUNT
import pl.patrykgoworowski.liftchart_common.component.shape.ShapeComponent
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.path.Shape
import pl.patrykgoworowski.liftchart_compose.extension.pixelSize

@Composable
fun textComponent(
    color: Color,
    textSize: TextUnit,
    background: ShapeComponent<Shape>?,
    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    lineCount: Int = DEF_LABEL_LINE_COUNT,
): TextComponent = TextComponent(
    color = color.toArgb(),
    textSize = textSize.pixelSize(LocalDensity.current),
    ellipsize = ellipsize,
    lineCount = lineCount,
    background = background,
)