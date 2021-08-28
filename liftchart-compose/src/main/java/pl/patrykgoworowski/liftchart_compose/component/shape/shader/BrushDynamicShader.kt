package pl.patrykgoworowski.liftchart_compose.component.shape.shader

import android.graphics.RectF
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import pl.patrykgoworowski.liftchart_common.component.shape.shader.CacheableDynamicShader
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader

public class BrushDynamicShader(
    private val shader: (bounds: RectF) -> Brush
) : CacheableDynamicShader() {

    private val tempPaint = Paint()

    override fun createShader(bounds: RectF): android.graphics.Shader {
        shader(bounds)
            .applyTo(
                size = Size(bounds.width(), bounds.height()),
                p = tempPaint,
                alpha = 1f
            )
        return requireNotNull(tempPaint.shader)
    }

}

public fun brushShader(
    shader: (bounds: RectF) -> Brush
): DynamicShader = object : CacheableDynamicShader() {

    private val tempPaint = Paint()

    override fun createShader(bounds: RectF): android.graphics.Shader {
        shader(bounds)
            .applyTo(
                size = Size(bounds.width(), bounds.height()),
                p = tempPaint,
                alpha = 1f
            )
        return requireNotNull(tempPaint.shader)
    }

}