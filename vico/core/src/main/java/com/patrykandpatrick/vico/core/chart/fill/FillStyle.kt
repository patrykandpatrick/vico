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

package com.patrykandpatrick.vico.core.chart.fill

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.shape.shader.HorizontalSplitShader
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.getOrPutExtra
import com.patrykandpatrick.vico.core.model.Point
import kotlin.math.roundToInt

/**
 * Defines an appearance of a component. It modifies the [Paint] object and applies either a color integer, or a
 * [android.graphics.Shader] to it.
 */
public interface FillStyle {

    /**
     * Applies the style to the [Paint] object.
     *
     * @param paint the [Paint] object to apply the style to.
     * @param drawContext the [DrawContext] for the current draw operation.
     * @param rectF the [RectF] holding coordinates of the area to apply the style to.
     * @param zeroLineYFraction the fraction of the height of the area to apply the style to, that the zero line is at.
     */
    public fun applyTo(paint: Paint, drawContext: DrawContext, rectF: RectF, zeroLineYFraction: Float) {
        applyTo(paint, drawContext, rectF.left, rectF.top, rectF.right, rectF.bottom, zeroLineYFraction)
    }

    /**
     * Applies the style to the [Paint] object.
     *
     * @param paint the [Paint] object to apply the style to.
     * @param drawContext the [DrawContext] for the current draw operation.
     * @param left the left coordinate of the area to apply the style to.
     * @param top the top coordinate of the area to apply the style to.
     * @param right the right coordinate of the area to apply the style to.
     * @param bottom the bottom coordinate of the area to apply the style to.
     * @param zeroLineYFraction the fraction of the height of the area to apply the style to, that the zero line is at.
     */
    public fun applyTo(
        paint: Paint,
        drawContext: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        zeroLineYFraction: Float,
    )

    /**
     * Gets the color of the style at the given point.
     *
     * @param point the [Point] to get the color at.
     * @param drawContext the [DrawContext] for the current draw operation.
     * @param rectF the [RectF] holding coordinates of the area to apply the style to.
     * @param zeroLineYFraction the fraction of the height of the area to apply the style to, that the zero line is at.
     */
    public fun getColorAt(point: Point, drawContext: DrawContext, rectF: RectF, zeroLineYFraction: Float): Int

    /**
     * A solid color fill style.
     *
     * @param color the color of the fill.
     */
    public class Solid(public val color: Int) : FillStyle {

        override fun applyTo(
            paint: Paint,
            drawContext: DrawContext,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            zeroLineYFraction: Float,
        ) {
            paint.color = color
            paint.shader = null
        }

        override fun getColorAt(point: Point, drawContext: DrawContext, rectF: RectF, zeroLineYFraction: Float): Int =
            color
    }

    /**
     * A shader fill style. It wraps a [DynamicShader] and uses it as the fill.
     *
     * @param dynamicShader the [DynamicShader] to use as the fill.
     */
    public open class Shader<Shader : DynamicShader>(public var dynamicShader: Shader) : FillStyle {

        private val bitmapKey: Any = Any()

        protected val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun applyTo(
            paint: Paint,
            drawContext: DrawContext,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            zeroLineYFraction: Float,
        ) {
            paint.shader = dynamicShader.provideShader(drawContext, left, top, right, bottom)
        }

        override fun getColorAt(point: Point, drawContext: DrawContext, rectF: RectF, zeroLineYFraction: Float): Int {
            val bitmap = drawContext.getOrPutExtra(bitmapKey) { dynamicShader.getBitmap(drawContext, paint, rectF) }
            return bitmap.getPixel(
                (point.x - rectF.left).toInt().coerceIn(0, rectF.width().toInt() - 1),
                (point.y - rectF.top).toInt().coerceIn(0, rectF.height().toInt() - 1),
            )
        }
    }

    /**
     * A split fill style. It uses two colors to fill the area, with the zero line being the divider.
     *
     * @param positiveColor the color to use for the positive area.
     * @param negativeColor the color to use for the negative area.
     */
    public class Split(
        positiveColor: Int,
        negativeColor: Int,
    ) : Shader<HorizontalSplitShader.Solid>(HorizontalSplitShader.Solid(positiveColor, negativeColor)) {

        public var positiveColor: Int by dynamicShader::colorTop

        public var negativeColor: Int by dynamicShader::colorBottom

        override fun applyTo(
            paint: Paint,
            drawContext: DrawContext,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            zeroLineYFraction: Float,
        ) {
            dynamicShader.dividerYFraction = zeroLineYFraction
            super.applyTo(paint, drawContext, left, top, right, bottom, zeroLineYFraction)
        }

        override fun getColorAt(point: Point, drawContext: DrawContext, rectF: RectF, zeroLineYFraction: Float): Int {
            dynamicShader.dividerYFraction = zeroLineYFraction
            return super.getColorAt(point, drawContext, rectF, zeroLineYFraction)
        }
    }

    /**
     * A split shader fill style. It wraps a [DynamicShader] and uses it as the fill. It uses two shaders to fill the
     * area, with the zero line being the divider.
     *
     * @param positiveShader the [DynamicShader] to use for the positive area.
     * @param negativeShader the [DynamicShader] to use for the negative area.
     */
    public class SplitShader(
        positiveShader: DynamicShader,
        negativeShader: DynamicShader,
    ) : Shader<HorizontalSplitShader.Double>(HorizontalSplitShader.Double(positiveShader, negativeShader)) {

        override fun applyTo(
            paint: Paint,
            drawContext: DrawContext,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            zeroLineYFraction: Float,
        ) {
            dynamicShader.dividerYFraction = zeroLineYFraction
            super.applyTo(paint, drawContext, left, top, right, bottom, zeroLineYFraction)
        }

        override fun getColorAt(point: Point, drawContext: DrawContext, rectF: RectF, zeroLineYFraction: Float): Int {
            dynamicShader.dividerYFraction = zeroLineYFraction
            return super.getColorAt(point, drawContext, rectF, zeroLineYFraction)
        }
    }

    public companion object {

        private fun DynamicShader.getBitmap(drawContext: DrawContext, paint: Paint, rectF: RectF): Bitmap {
            val width = rectF.width().roundToInt()
            val height = rectF.height().roundToInt()
            paint.shader = provideShader(drawContext, 0f, 0f, width.toFloat(), height.toFloat())
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            return bitmap
        }
    }
}
