/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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
import android.graphics.RectF
import com.patrykandpatrick.vico.core.DEF_SHADOW_COLOR
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.shape.shadow.ComponentShadow
import com.patrykandpatrick.vico.core.context.DrawContext

/**
 * TODO
 *
 * @property color the background color.
 * @property dynamicShader an optional [DynamicShader] to apply to the line.
 * @property strokeWidthDp the stroke width.
 * @property strokeColor the stroke color.
 */
public open class PathComponent(
    public var color: Int = Color.BLACK,
    public var dynamicShader: DynamicShader? = null,
    public var strokeWidthDp: Float = 0f,
    public var strokeColor: Int = Color.TRANSPARENT,
) {

    protected val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected val strokePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected val shadowProperties: ComponentShadow = ComponentShadow()

    protected val path: Path = Path()

    /**
     * TODO
     */
    public val pathBuilder: PathBuilderHelper by lazy {

        object : PathBuilderHelper(path) {

            override fun setUp(context: DrawContext) = with(context) {
                path.rewind()

                paint.color = color
                strokePaint.color = strokeColor
                strokePaint.strokeWidth = strokeWidthDp.pixels
            }

            override fun draw(context: DrawContext): Unit = with(context) {
                applyShader(context, pathBuilderBounds)
                shadowProperties.maybeUpdateShadowLayer(
                    context = context,
                    paint = paint,
                    backgroundColor = color,
                )

                if (color != Color.TRANSPARENT) {
                    canvas.drawPath(path, paint)
                }

                if (strokeColor != Color.TRANSPARENT && strokeWidthDp > 0f) {
                    canvas.drawPath(path, strokePaint)
                }
            }
        }
    }

    init {
        strokePaint.style = Paint.Style.STROKE
    }

    protected fun applyShader(
        context: DrawContext,
        rect: RectF,
    ) {
        dynamicShader
            ?.provideShader(context, rect.left, rect.top, rect.right, rect.bottom)
            ?.let { shader -> paint.shader = shader }
    }

    /**
     * Instructs the [PathComponent] to draw itself.
     *
     * @param context the [DrawContext] to draw on.
     * @param buildPath a lambda that allows building the path.
     */
    public inline fun draw(
        context: DrawContext,
        buildPath: PathBuilder.() -> Unit,
    ) {
        pathBuilder.setUp(context)
        buildPath(pathBuilder)
        if (pathBuilder.isEmpty) return

        pathBuilder.draw(context)
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
    ): PathComponent = apply {
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
    public fun clearShadow(): PathComponent = apply {
        shadowProperties.apply {
            this.radius = 0f
            this.dx = 0f
            this.dy = 0f
            this.color = 0
        }
    }
}
