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

package com.patrykandpatrick.vico.core.component.shape.shadow

import android.graphics.Paint
import com.patrykandpatrick.vico.core.DEF_SHADOW_COLOR
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.context.DrawContext

/**
 * A base class for components that use [android.graphics.Paint] to draw themselves.
 */
@Suppress("UNCHECKED_CAST")
public open class PaintComponent<C> protected constructor() {

    protected val componentShadow: ComponentShadow = ComponentShadow()

    /**
     * Checks whether the applied shadow layer needs to be updated.
     */
    protected fun maybeUpdateShadowLayer(
        context: DrawContext,
        paint: Paint,
        backgroundColor: Int,
    ): Unit = componentShadow.maybeUpdateShadowLayer(
        context = context,
        paint = paint,
        backgroundColor = backgroundColor,
    )

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
    ): C = apply {
        componentShadow.apply {
            this.radius = radius
            this.dx = dx
            this.dy = dy
            this.color = color
            this.applyElevationOverlay = applyElevationOverlay
        }
    } as C

    /**
     * Removes this [ShapeComponent]’s drop shadow.
     */
    public fun clearShadow(): C = apply {
        componentShadow.apply {
            this.radius = 0f
            this.dx = 0f
            this.dy = 0f
            this.color = 0
        }
    } as C
}
