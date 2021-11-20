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

package pl.patrykgoworowski.vico.core.component.shape.shadow

import android.graphics.Paint
import pl.patrykgoworowski.vico.core.draw.DrawContext

@Suppress("ComplexCondition")
public data class ComponentShadow(
    var radius: Float = 0f,
    var dx: Float = 0f,
    var dy: Float = 0f,
    var color: Int = 0,
) {
    private var laRadius: Float = 0f
    private var laDx: Float = 0f
    private var laDy: Float = 0f
    private var laColor: Int = 0
    private var laDensity: Float = 0f

    public fun maybeUpdateShadowLayer(context: DrawContext, paint: Paint) = with(context) {
        if (shouldUpdateShadowLayer()) {
            if (color == 0 || (radius == 0f && dx == 0f && dy == 0f)) {
                paint.clearShadowLayer()
            } else {
                paint.setShadowLayer(
                    radius.pixels,
                    dx.pixels,
                    dy.pixels,
                    color,
                )
            }
        }
    }

    private fun DrawContext.shouldUpdateShadowLayer(): Boolean {
        return if (
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
}
