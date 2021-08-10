package pl.patrykgoworowski.liftchart_compose.component.shape.shader

import android.graphics.LinearGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import pl.patrykgoworowski.liftchart_common.component.shape.shader.CacheableDynamicShader
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader

fun horizontalGradient(
    colors: Array<Color>,
    positions: FloatArray? = null,
): DynamicShader = object : CacheableDynamicShader() {

    override fun createShader(parentBounds: RectF): Shader =
        LinearGradient(
            parentBounds.left,
            parentBounds.top,
            parentBounds.right,
            parentBounds.top,
            IntArray(colors.size) { index -> colors[index].toArgb() },
            positions,
            Shader.TileMode.CLAMP,
        )
}

@Composable
fun verticalGradient(
    colors: Array<Color>,
    positions: FloatArray? = null,
): DynamicShader = object : CacheableDynamicShader() {

    override fun createShader(parentBounds: RectF): Shader =
        LinearGradient(
            parentBounds.left,
            parentBounds.top,
            parentBounds.left,
            parentBounds.bottom,
            IntArray(colors.size) { index -> colors[index].toArgb() },
            positions,
            Shader.TileMode.CLAMP,
        )
}