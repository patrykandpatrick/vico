package pl.patrykgoworowski.liftchart_compose.component.shape.shader

import android.graphics.RectF
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import pl.patrykgoworowski.liftchart_common.component.shape.shader.CacheableDynamicShader
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader

class BrushDynamicShader(
    private val shader: (bounds: RectF) -> Brush
) : CacheableDynamicShader() {

    private val tempPaint = Paint()

    override fun createShader(parentBounds: RectF): android.graphics.Shader {
        shader(parentBounds)
            .applyTo(
                size = Size(parentBounds.width(), parentBounds.height()),
                p = tempPaint,
                alpha = 1f
            )
        return requireNotNull(tempPaint.shader)
    }

}

fun brushShader(
    shader: (bounds: RectF) -> Brush
): DynamicShader = object : CacheableDynamicShader() {

    private val tempPaint = Paint()

    override fun createShader(parentBounds: RectF): android.graphics.Shader {
        shader(parentBounds)
            .applyTo(
                size = Size(parentBounds.width(), parentBounds.height()),
                p = tempPaint,
                alpha = 1f
            )
        return requireNotNull(tempPaint.shader)
    }

}