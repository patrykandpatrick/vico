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

package com.patrykandpatrick.vico.core.component.shape.shader

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.model.Point

/**
 * A [DynamicShader] that can apply two different [DynamicShader]s to the top and bottom part of the area.
 *
 * @property dividerYFraction the fraction of the height of the area to apply the style to, that the divider is at.
 */
public abstract class HorizontalSplitShader(
    public var dividerYFraction: Float = 0f,
) : CacheableDynamicShader() {
    private val paint = Paint()

    override fun createShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Shader {
        val height = (bottom - top).toInt()
        val width = (right - left).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (dividerYFraction > 0f) {
            applyTopTo(paint, context, left, 0f, right, height * dividerYFraction)
            canvas.drawRect(0f, 0f, width.toFloat(), height * dividerYFraction, paint)
        }
        if (dividerYFraction < 1f) {
            applyBottomTo(paint, context, left, height * dividerYFraction, right, height.toFloat())
            canvas.drawRect(0f, height * dividerYFraction, width.toFloat(), height.toFloat(), paint)
        }
        val matrix = Matrix().apply { postTranslate(left, top) }
        return BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
            setLocalMatrix(matrix)
        }
    }

    /**
     * Applies the top style to the [Paint] object for the top part of the area.
     *
     * @param paint the [Paint] object to apply the style to.
     * @param drawContext the [DrawContext] for the current draw operation.
     * @param left the left coordinate of the area to apply the style to.
     * @param top the top coordinate of the area to apply the style to.
     * @param right the right coordinate of the area to apply the style to.
     * @param bottom the bottom coordinate of the area to apply the style to.
     */
    public abstract fun applyTopTo(
        paint: Paint,
        drawContext: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    )

    /**
     * Applies the bottom style to the [Paint] object for the bottom part of the area.
     *
     * @param paint the [Paint] object to apply the style to.
     * @param drawContext the [DrawContext] for the current draw operation.
     * @param left the left coordinate of the area to apply the style to.
     * @param top the top coordinate of the area to apply the style to.
     * @param right the right coordinate of the area to apply the style to.
     * @param bottom the bottom coordinate of the area to apply the style to.
     */
    public abstract fun applyBottomTo(
        paint: Paint,
        drawContext: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    )

    override fun createKey(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): String = super.createKey(left, top, right, bottom) + ",$dividerYFraction"

    override fun applyTo(
        paint: Paint,
        drawContext: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        zeroLineYFraction: Float,
    ) {
        dividerYFraction = zeroLineYFraction
        super.applyTo(paint, drawContext, left, top, right, bottom, zeroLineYFraction)
    }

    override fun getColorAt(
        point: Point,
        drawContext: DrawContext,
        rectF: RectF,
        zeroLineYFraction: Float,
    ): Int {
        dividerYFraction = zeroLineYFraction
        return super.getColorAt(point, drawContext, rectF, zeroLineYFraction)
    }

    /**
     * A [HorizontalSplitShader] with a solid color for the top and bottom part of the area.
     *
     * @param colorTop the color for the top part of the area.
     * @param colorBottom the color for the bottom part of the area.
     * @param dividerYFraction the fraction of the height of the area to apply the style to, that the divider is at.
     */
    public class Solid(
        public var colorTop: Int,
        public var colorBottom: Int,
        dividerYFraction: Float = 0f,
    ) : HorizontalSplitShader(dividerYFraction) {
        override fun applyTopTo(
            paint: Paint,
            drawContext: DrawContext,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
        ) {
            paint.color = colorTop
            paint.shader = null
        }

        override fun applyBottomTo(
            paint: Paint,
            drawContext: DrawContext,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
        ) {
            paint.color = colorBottom
            paint.shader = null
        }

        override fun createKey(
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
        ): String = super.createKey(left, top, right, bottom) + ",$colorTop,$colorBottom"

        override fun equals(other: Any?): Boolean =
            when {
                this === other -> true
                other !is Solid -> false
                else ->
                    colorTop == other.colorTop &&
                        colorBottom == other.colorBottom &&
                        dividerYFraction == other.dividerYFraction
            }

        override fun hashCode(): Int {
            var result = colorTop
            result = 31 * result + colorBottom
            result = 31 * result + dividerYFraction.hashCode()
            return result
        }
    }

    /**
     * A [HorizontalSplitShader] with a [DynamicShader] for the top and bottom part of the area.
     *
     * @param topShader the [DynamicShader] for the top part of the area.
     * @param bottomShader the [DynamicShader] for the bottom part of the area.
     * @param dividerYFraction the fraction of the height of the area to apply the style to, that the divider is at.
     */
    public class Double(
        public var topShader: DynamicShader,
        public var bottomShader: DynamicShader,
        dividerYFraction: Float = 0f,
    ) : HorizontalSplitShader(dividerYFraction) {
        override fun applyTopTo(
            paint: Paint,
            drawContext: DrawContext,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
        ) {
            paint.shader = topShader.provideShader(drawContext, left, top, right, bottom)
        }

        override fun applyBottomTo(
            paint: Paint,
            drawContext: DrawContext,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
        ) {
            paint.shader = bottomShader.provideShader(drawContext, left, top, right, bottom)
        }

        override fun createKey(
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
        ): String = super.createKey(left, top, right, bottom) + ",${topShader.hashCode()},${bottomShader.hashCode()}"

        override fun equals(other: Any?): Boolean =
            when {
                this === other -> true
                other !is Double -> false
                else ->
                    topShader == other.topShader &&
                        bottomShader == other.bottomShader &&
                        dividerYFraction == other.dividerYFraction
            }

        override fun hashCode(): Int = 31 * topShader.hashCode() + bottomShader.hashCode()
    }
}
