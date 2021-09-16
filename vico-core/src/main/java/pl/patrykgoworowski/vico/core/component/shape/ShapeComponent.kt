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
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.vico.core.DEF_SHADOW_COLOR
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.dimension.setMargins
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.dimensions.Dimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions
import pl.patrykgoworowski.vico.core.path.Shape

public open class ShapeComponent<T : Shape>(
    public var shape: T,
    color: Int = Color.BLACK,
    public var dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
) : Component() {

    public val parentBounds = RectF()
    public val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val drawBounds: RectF = RectF()
    protected val path: Path = Path()

    public var color by paint::color

    init {
        paint.color = color
        setMargins(margins)
    }

    public fun setParentBounds(bounds: RectF) {
        parentBounds.set(bounds)
    }

    public fun setParentBounds(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        parentBounds.set(left, top, right, bottom)
    }

    override fun draw(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        if (left == right || top == bottom) return // Skip drawing shape that will be invisible.
        updateDrawBounds(left, top, right, bottom)
        path.reset()
        applyShader(parentBounds)
        shape.drawShape(canvas, paint, path, drawBounds)
    }

    protected fun applyShader(
        bounds: RectF,
    ) {
        dynamicShader
            ?.provideShader(bounds)
            ?.let { shader -> paint.shader = shader }
    }

    public open fun updateDrawBounds(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        drawBounds.set(
            left + margins.start,
            top + margins.top,
            right - margins.end,
            bottom - margins.bottom,
        )
    }

    public open fun fitsIn(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        boundingBox: RectF
    ): Boolean = boundingBox.contains(left, top, right, bottom)

    public open fun intersects(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        boundingBox: RectF
    ): Boolean = boundingBox.intersects(left, top, right, bottom)

    public fun setShadow(
        radius: Float,
        dx: Float = 0f,
        dy: Float = 0f,
        color: Int = DEF_SHADOW_COLOR,
    ) = apply {
        paint.setShadowLayer(radius, dx, dy, color)
    }
}
