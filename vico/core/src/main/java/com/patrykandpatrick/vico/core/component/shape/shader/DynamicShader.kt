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
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.getOrPutExtra
import com.patrykandpatrick.vico.core.model.Point
import kotlin.math.roundToInt

/**
 * [DynamicShader] creates [Shader] instances on demand.
 *
 * @see Shader
 */
public fun interface DynamicShader {
    private val bitmapPixelExtractionKey: Any
        get() = "bitmapPixelExtractionKey${hashCode()}"

    /**
     * Creates a [Shader] by using the provided [bounds].
     */
    public fun provideShader(
        context: DrawContext,
        bounds: RectF,
    ): Shader =
        provideShader(
            context = context,
            left = bounds.left,
            top = bounds.top,
            right = bounds.right,
            bottom = bounds.bottom,
        )

    /**
     * Creates a [Shader] by using the provided [left], [top], [right], and [bottom] bounds.
     */
    public fun provideShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Shader

    /**
     * Applies the [Shader] to the [Paint] object.
     *
     * @param paint the [Paint] object to apply the style to.
     * @param drawContext the [DrawContext] for the current draw operation.
     * @param rectF the [RectF] holding coordinates of the area to apply the style to.
     * @param zeroLineYFraction the fraction of the height of the area to apply the style to, that the zero line is at.
     */
    public fun applyTo(
        paint: Paint,
        drawContext: DrawContext,
        rectF: RectF,
        zeroLineYFraction: Float,
    ) {
        applyTo(paint, drawContext, rectF.left, rectF.top, rectF.right, rectF.bottom, zeroLineYFraction)
    }

    /**
     * Applies the [Shader] to the [Paint] object.
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
    ) {
        paint.shader = provideShader(drawContext, left, top, right, bottom)
    }

    /**
     * Gets the pixel color of the [Shader] at the given [point].
     *
     * @param point the [Point] to get the color at.
     * @param drawContext the [DrawContext] for the current draw operation.
     * @param rectF the [RectF] holding coordinates of the area to apply the style to.
     * @param zeroLineYFraction the fraction of the height of the area to apply the style to, that the zero line is at.
     */
    public fun getColorAt(
        point: Point,
        drawContext: DrawContext,
        rectF: RectF,
        zeroLineYFraction: Float,
    ): Int {
        val bitmap =
            drawContext.getOrPutExtra(bitmapPixelExtractionKey) {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                getBitmap(drawContext, paint, rectF)
            }
        return bitmap.getPixel(
            (point.x - rectF.left).toInt().coerceIn(0, rectF.width().toInt() - 1),
            (point.y - rectF.top).toInt().coerceIn(0, rectF.height().toInt() - 1),
        )
    }

    public companion object {
        private fun DynamicShader.getBitmap(
            drawContext: DrawContext,
            paint: Paint,
            rectF: RectF,
        ): Bitmap {
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
