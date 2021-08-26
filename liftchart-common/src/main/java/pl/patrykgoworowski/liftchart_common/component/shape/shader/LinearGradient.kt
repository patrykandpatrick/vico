package pl.patrykgoworowski.liftchart_common.component.shape.shader

import android.graphics.LinearGradient
import android.graphics.RectF
import android.graphics.Shader

fun horizontalGradient(
    vararg colors: Int,
) = horizontalGradient(colors)

fun horizontalGradient(
    colors: IntArray,
    positions: FloatArray? = null,
): DynamicShader = object : CacheableDynamicShader() {

    override fun createShader(bounds: RectF): Shader =
        LinearGradient(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.top,
            colors,
            positions,
            Shader.TileMode.CLAMP,
        )
}

fun verticalGradient(
    vararg colors: Int,
) = verticalGradient(colors)

fun verticalGradient(
    colors: IntArray,
    positions: FloatArray? = null,
): DynamicShader = object : CacheableDynamicShader() {

    override fun createShader(bounds: RectF): Shader =
        LinearGradient(
            bounds.left,
            bounds.top,
            bounds.left,
            bounds.bottom,
            colors,
            positions,
            Shader.TileMode.CLAMP,
        )
}