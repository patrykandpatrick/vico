/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.common.component

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.dimension.Dimensions
import com.patrykandpatrick.vico.core.common.dimension.emptyDimensions
import com.patrykandpatrick.vico.core.common.extension.alpha
import com.patrykandpatrick.vico.core.common.extension.half
import com.patrykandpatrick.vico.core.common.extension.round
import com.patrykandpatrick.vico.core.common.extension.withOpacity
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.core.common.shape.Shapes
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
) : PaintComponent<ShapeComponent>(),
    Component,
    Margins by DefaultMargins() {
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

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
        opacity: Float,
    ): Unit =
        with(context) {
            if (left == right || top == bottom) return // Skip drawing shape that will be invisible.
            path.rewind()
            applyShader(context, left, top, right, bottom)
            val centerX = (left + right).half
            val centerY = (top + bottom).half
            componentShadow.maybeUpdateShadowLayer(
                context = this,
                paint = paint,
                backgroundColor = color,
                opacity = opacity,
            )

            val strokeWidth = strokeWidthDp.pixels
            strokePaint.strokeWidth = strokeWidth

            fun drawShape(
                paint: Paint,
                isStroke: Boolean,
            ) {
                val strokeCompensation = if (isStroke) strokeWidth.half else 0f

                shape.drawShape(
                    context = context,
                    paint = paint,
                    path = path,
                    left =
                        minOf(
                            left + margins.startDp.pixels + strokeWidth.half,
                            centerX - strokeCompensation,
                        ).round,
                    top =
                        minOf(
                            top + margins.topDp.pixels + strokeWidth.half,
                            centerY - strokeCompensation,
                        ).round,
                    right =
                        maxOf(
                            right - margins.endDp.pixels - strokeWidth.half,
                            centerX + strokeCompensation,
                        ).round,
                    bottom =
                        maxOf(
                            bottom - margins.bottomDp.pixels - strokeWidth.half,
                            centerY + strokeCompensation,
                        ).round,
                )
            }

            paint.withOpacity(opacity) { paint ->
                drawShape(paint = paint, isStroke = false)
            }
            if (strokeWidth > 0f && strokeColor.alpha > 0) drawShape(paint = strokePaint, isStroke = true)
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
}
