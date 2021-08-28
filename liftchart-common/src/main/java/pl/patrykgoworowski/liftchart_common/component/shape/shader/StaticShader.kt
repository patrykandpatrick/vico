package pl.patrykgoworowski.liftchart_common.component.shape.shader

import android.graphics.RectF
import android.graphics.Shader

class StaticShader(private val shader: Shader) : DynamicShader {

    override fun provideShader(
        bounds: RectF
    ): Shader = shader

}

val Shader.dynamic: DynamicShader
    get() = StaticShader(this)