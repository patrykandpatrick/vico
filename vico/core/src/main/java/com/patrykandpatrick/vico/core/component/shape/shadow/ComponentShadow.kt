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

package com.patrykandpatrick.vico.core.component.shape.shadow

import android.graphics.Paint
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.extension.applyElevationOverlayToColor

/**
 * A class that stores shadow properties.
 *
 * @property radius the blur radius.
 * @property dx the horizontal offset.
 * @property dy the vertical offset.
 * @property color the shadow color.
 * @property applyElevationOverlay whether to apply an elevation overlay to the component casting the shadow.
 */
@Suppress("ComplexCondition")
public data class ComponentShadow(
    var radius: Float = 0f,
    var dx: Float = 0f,
    var dy: Float = 0f,
    var color: Int = 0,
    var applyElevationOverlay: Boolean = false,
) {
    private var laRadius: Float = 0f
    private var laDx: Float = 0f
    private var laDy: Float = 0f
    private var laColor: Int = 0
    private var laDensity: Float = 0f

    /**
     * Checks whether the applied shadow layer needs to be updated.
     */
    public fun maybeUpdateShadowLayer(
        context: DrawContext,
        paint: Paint,
        backgroundColor: Int,
    ): Unit = with(context) {
        if (shouldUpdateShadowLayer()) {
            updateShadowLayer(paint, backgroundColor)
        }
    }

    private fun DrawContext.updateShadowLayer(
        paint: Paint,
        backgroundColor: Int,
    ) {
        if (color == 0 || radius == 0f && dx == 0f && dy == 0f) {
            paint.clearShadowLayer()
        } else {
            paint.color = if (applyElevationOverlay) {
                applyElevationOverlayToColor(color = backgroundColor, elevationDp = radius)
            } else {
                backgroundColor
            }
            paint.setShadowLayer(
                radius.pixels,
                dx.pixels,
                dy.pixels,
                color,
            )
        }
    }

    private fun DrawContext.shouldUpdateShadowLayer(): Boolean =
        if (
            radius != laRadius ||
            dx != laDx ||
            dy != laDy ||
            color != laColor ||
            density != laDensity
        ) {
            laRadius = radius
            laDx = dx
            laDy = dy
            laColor = color
            laDensity = density
            true
        } else {
            false
        }
}
