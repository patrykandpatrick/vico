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

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.dimensions.Dimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions
import pl.patrykgoworowski.vico.core.shape.Shape
import pl.patrykgoworowski.vico.core.shape.Shapes.rectShape

public open class LineComponent(
    color: Int,
    public var thickness: Float = 2f,
    shape: Shape = rectShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
) : ShapeComponent<Shape>(shape, color, dynamicShader, margins) {

    var thicknessScale: Float = 1f

    val scaledThickness: Float
        get() = thickness * thicknessScale

    public open fun drawHorizontal(
        canvas: Canvas,
        left: Float,
        right: Float,
        centerY: Float,
    ) {
        draw(
            canvas = canvas,
            left = left,
            top = centerY - scaledThickness / 2,
            right = right,
            bottom = centerY + scaledThickness / 2
        )
    }

    public open fun fitsInHorizontal(
        left: Float,
        right: Float,
        centerY: Float,
        boundingBox: RectF
    ): Boolean = fitsIn(
        left = left,
        top = centerY - scaledThickness / 2,
        right = right,
        bottom = centerY + scaledThickness / 2,
        boundingBox = boundingBox
    )

    public open fun drawVertical(
        canvas: Canvas,
        top: Float,
        bottom: Float,
        centerX: Float,
    ) {
        draw(
            canvas = canvas,
            left = centerX - scaledThickness / 2,
            top = top,
            right = centerX + scaledThickness / 2,
            bottom = bottom
        )
    }

    public open fun fitsInVertical(
        top: Float,
        bottom: Float,
        centerX: Float,
        boundingBox: RectF
    ): Boolean = fitsIn(
        left = centerX - scaledThickness / 2,
        top = top,
        right = centerX + scaledThickness / 2,
        bottom = bottom,
        boundingBox = boundingBox
    )

    public open fun intersectsVertical(
        top: Float,
        bottom: Float,
        centerX: Float,
        boundingBox: RectF
    ): Boolean = intersects(
        left = centerX - scaledThickness / 2,
        top = top,
        right = centerX + scaledThickness / 2,
        bottom = bottom,
        boundingBox = boundingBox
    )

    override fun updateDrawBounds(left: Float, top: Float, right: Float, bottom: Float) {
        val centerX = left + (right - left) / 2
        val centerY = top + (bottom - top) / 2
        drawBounds.set(
            minOf(left + margins.start, centerX),
            minOf(top + margins.top, centerY),
            maxOf(right - margins.end, centerX),
            maxOf(bottom - margins.bottom, centerY)
        )
    }
}
