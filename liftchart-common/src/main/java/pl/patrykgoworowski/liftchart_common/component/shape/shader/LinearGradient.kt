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

    override fun createShader(parentBounds: RectF): Shader =
        LinearGradient(
            parentBounds.left,
            parentBounds.top,
            parentBounds.right,
            parentBounds.top,
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

    override fun createShader(parentBounds: RectF): Shader =
        LinearGradient(
            parentBounds.left,
            parentBounds.top,
            parentBounds.left,
            parentBounds.bottom,
            colors,
            positions,
            Shader.TileMode.CLAMP,
        )
}