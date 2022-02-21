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
import android.graphics.RectF
import pl.patrykgoworowski.vico.core.component.shape.Shapes.rectShape
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.dimensions.Dimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions
import pl.patrykgoworowski.vico.core.context.DrawContext
import pl.patrykgoworowski.vico.core.context.MeasureContext

public open class LineComponent(
    color: Int,
    public var thicknessDp: Float = 2f,
    shape: Shape = rectShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
    strokeWidthDp: Float = 0f,
    strokeColor: Int = Color.TRANSPARENT,
) : ShapeComponent(shape, color, dynamicShader, margins, strokeWidthDp, strokeColor) {

    private val MeasureContext.thickness: Float
        get() = thicknessDp.pixels

    public open fun drawHorizontal(
        context: DrawContext,
        left: Float,
        right: Float,
        centerY: Float,
        thicknessScale: Float = 1f
    ): Unit = with(context) {
        draw(
            context,
            left = left,
            top = centerY - thickness * thicknessScale / 2,
            right = right,
            bottom = centerY + thickness * thicknessScale / 2,
        )
    }

    public open fun fitsInHorizontal(
        context: DrawContext,
        left: Float,
        right: Float,
        centerY: Float,
        boundingBox: RectF,
        thicknessScale: Float = 1f,
    ): Boolean = with(context) {
        fitsIn(
            left = left,
            top = centerY - thickness * thicknessScale / 2,
            right = right,
            bottom = centerY + thickness * thicknessScale / 2,
            boundingBox = boundingBox,
        )
    }

    public open fun drawVertical(
        context: DrawContext,
        top: Float,
        bottom: Float,
        centerX: Float,
        thicknessScale: Float = 1f,
    ): Unit = with(context) {
        draw(
            context,
            left = centerX - thickness * thicknessScale / 2,
            top = top,
            right = centerX + thickness * thicknessScale / 2,
            bottom = bottom,
        )
    }

    public open fun fitsInVertical(
        context: DrawContext,
        top: Float,
        bottom: Float,
        centerX: Float,
        boundingBox: RectF,
        thicknessScale: Float = 1f,
    ): Boolean = with(context) {
        fitsIn(
            left = centerX - thickness * thicknessScale / 2,
            top = top,
            right = centerX + thickness * thicknessScale / 2,
            bottom = bottom,
            boundingBox = boundingBox,
        )
    }

    public open fun intersectsVertical(
        context: DrawContext,
        top: Float,
        bottom: Float,
        centerX: Float,
        boundingBox: RectF,
        thicknessScale: Float = 1f,
    ): Boolean = with(context) {
        intersects(
            left = centerX - thickness * thicknessScale / 2,
            top = top,
            right = centerX + thickness * thicknessScale / 2,
            bottom = bottom,
            boundingBox = boundingBox,
        )
    }
}
