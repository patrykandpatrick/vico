/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.component.shape

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.properties.Delegates
import pl.patrykgoworowski.vico.core.DEF_SHADOW_COLOR
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.dimension.setMargins
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.component.shape.shadow.ComponentShadow
import pl.patrykgoworowski.vico.core.context.DrawContext
import pl.patrykgoworowski.vico.core.debug.DebugHelper
import pl.patrykgoworowski.vico.core.dimensions.Dimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions
import pl.patrykgoworowski.vico.core.extension.alpha
import pl.patrykgoworowski.vico.core.extension.half

/**
 * [ShapeComponent] is a [Component] that draws a shape.
 *
 * @param shape the [Shape] that will be drawn.
 * @param color the color of the shape.
 * @param dynamicShader the optional [android.graphics.Shader] provider used as shape’s background.
 * @param margins the margins that will inset the shape.
 * @param strokeWidthDp the width of the shape’s stroke in dp unit.
 * @param strokeColor the color of the stroke.
 */
public open class ShapeComponent(
    public val shape: Shape,
    color: Int = Color.BLACK,
    public val dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
    public val strokeWidthDp: Float = 0f,
    strokeColor: Int = Color.TRANSPARENT,
) : Component() {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowProperties: ComponentShadow = ComponentShadow()

    protected val path: Path = Path()

    /**
     * The color of the shape.
     */
    public var color: Int by Delegates.observable(color) { _, _, value -> paint.color = value }

    /**
     * The color of the stroke.
     */
    public var strokeColor: Int by Delegates.observable(color) { _, _, value -> strokePaint.color = value }

    init {
        paint.color = color

        with(strokePaint) {
            this.color = strokeColor
            style = Paint.Style.STROKE
        }

        setMargins(margins)
    }

    override fun draw(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Unit = with(context) {
        if (left == right || top == bottom) return // Skip drawing shape that will be invisible.
        path.rewind()
        applyShader(context, left, top, right, bottom)
        val centerX = left + (right - left) / 2
        val centerY = top + (bottom - top) / 2
        shadowProperties.maybeUpdateShadowLayer(context = this, paint = paint, backgroundColor = color)

        val strokeWidth = strokeWidthDp.pixels
        strokePaint.strokeWidth = strokeWidth

        fun drawShape(paint: Paint) {
            shape.drawShape(
                context = context,
                paint = paint,
                path = path,
                left = minOf(left + margins.startDp.pixels + strokeWidth.half, centerX),
                top = minOf(top + margins.topDp.pixels + strokeWidth.half, centerY),
                right = maxOf(right - margins.endDp.pixels - strokeWidth.half, centerX),
                bottom = maxOf(bottom - margins.bottomDp.pixels - strokeWidth.half, centerY)
            )
        }

        drawShape(paint)
        if (strokeWidth > 0f && strokeColor.alpha > 0) drawShape(strokePaint)

        DebugHelper.drawDebugBounds(
            context = context,
            left = left,
            top = top,
            right = right,
            bottom = bottom
        )
    }

    protected fun applyShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        dynamicShader
            ?.provideShader(context, left, top, right, bottom)
            ?.let { shader -> paint.shader = shader }
    }

    /**
     * Sets a shadow layer.
     *
     * @param radius the blur radius.
     * @param dx the horizontal offset.
     * @param dy the vertical offset.
     * @param color the shadow color.
     * @param applyElevationOverlay whether to apply an elevation overlay to the component casting the shadow.
     */
    public fun setShadow(
        radius: Float,
        dx: Float = 0f,
        dy: Float = 0f,
        color: Int = DEF_SHADOW_COLOR,
        applyElevationOverlay: Boolean = false,
    ): ShapeComponent = apply {
        shadowProperties.apply {
            this.radius = radius
            this.dx = dx
            this.dy = dy
            this.color = color
            this.applyElevationOverlay = applyElevationOverlay
        }
    }

    /**
     * Removes the shadow layer from the [ShapeComponent].
     */
    public fun clearShadow(): ShapeComponent = apply {
        shadowProperties.apply {
            this.radius = 0f
            this.dx = 0f
            this.dy = 0f
            this.color = 0
        }
    }
}
