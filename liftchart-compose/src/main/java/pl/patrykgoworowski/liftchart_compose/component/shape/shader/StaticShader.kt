package pl.patrykgoworowski.liftchart_compose.component.shape.shader

import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader

class StaticShader(private val brush: Brush) : DynamicShader {

    private var shader: Shader? = null

    override fun provideShader(
        bounds: RectF
    ): Shader = shader ?: kotlin.run {
        val tempPaint = Paint()
        brush.applyTo(
            size = Size(bounds.width(), bounds.height()),
            p = tempPaint,
            alpha = 1f,
        )
        requireNotNull(tempPaint.shader)
    }.also { shader = it }

}

val Brush.dynamic: DynamicShader
    get() = StaticShader(this)