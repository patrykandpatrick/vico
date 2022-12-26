/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.core.component.shape

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.patrykandpatrick.vico.core.DEF_SHADOW_COLOR
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.dimension.setMargins
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.shape.shadow.ComponentShadow
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.debug.DebugHelper
import com.patrykandpatrick.vico.core.dimensions.Dimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions
import com.patrykandpatrick.vico.core.extension.alpha
import com.patrykandpatrick.vico.core.extension.half
import kotlin.properties.Delegates

/**
 * [ShapeComponent] is a [Component] that draws a shape.
 *
 * @param shape the [Shape] that will be drawn.
 * @param color the color of the shape.
 * @param dynamicShader an optional [Shader] provider used as the shape’s background.
 * @param margins the [Component]’s margins.
 * @param strokeWidthDp the width of the shape’s stroke (in dp).
 * @param strokeColor the color of the stroke.
 */
public open class ShapeComponent(
    public val shape: Shape = Shapes.rectShape,
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
    public var strokeColor: Int by Delegates.observable(strokeColor) { _, _, value -> strokePaint.color = value }

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
        val centerX = (left + right).half
        val centerY = (top + bottom).half
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
                bottom = maxOf(bottom - margins.bottomDp.pixels - strokeWidth.half, centerY),
            )
        }

        drawShape(paint)
        if (strokeWidth > 0f && strokeColor.alpha > 0) drawShape(strokePaint)

        DebugHelper.drawDebugBounds(
            context = context,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
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
     * Applies a drop shadow.
     *
     * @param radius the blur radius.
     * @param dx the horizontal offset.
     * @param dy the vertical offset.
     * @param color the shadow color.
     * @param applyElevationOverlay whether to apply an elevation overlay to the shape.
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
     * Removes this [ShapeComponent]’s drop shadow.
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
