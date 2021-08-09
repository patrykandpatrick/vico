package pl.patrykgoworowski.liftchart_common.component.shape.shader

import android.graphics.RectF
import android.graphics.Shader

fun interface DynamicShader {

    fun provideShader(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        parentBounds: RectF,
    ): Shader

}