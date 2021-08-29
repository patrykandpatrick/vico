package pl.patrykgoworowski.liftchart_compose.component.shape.shader


import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.liftchart_common.component.Component
import pl.patrykgoworowski.liftchart_compose.extension.pixels

@Composable
fun componentShader(
    component: Component,
    componentSize: Dp,
    checkeredArrangement: Boolean = true,
    tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
    tileYMode: Shader.TileMode = tileXMode,
) = pl.patrykgoworowski.liftchart_common.component.shape.shader.componentShader(
    component = component,
    componentSize = componentSize.pixels,
    checkeredArrangement = checkeredArrangement,
    tileXMode = tileXMode,
    tileYMode = tileYMode,
)