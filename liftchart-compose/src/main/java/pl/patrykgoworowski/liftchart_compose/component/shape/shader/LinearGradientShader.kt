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

    override fun createShader(bounds: RectF): Shader =
        LinearGradient(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.top,
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

    override fun createShader(bounds: RectF): Shader =
        LinearGradient(
            bounds.left,
            bounds.top,
            bounds.left,
            bounds.bottom,
            IntArray(colors.size) { index -> colors[index].toArgb() },
            positions,
            Shader.TileMode.CLAMP,
        )
}