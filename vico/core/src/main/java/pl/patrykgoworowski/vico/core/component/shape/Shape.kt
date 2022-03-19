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

import android.graphics.Paint
import android.graphics.Path
import pl.patrykgoworowski.vico.core.annotation.LongParameterListDrawFunction
import pl.patrykgoworowski.vico.core.context.DrawContext

/**
 * Defines a shape that can be drawn on canvas.
 */
public interface Shape {

    /**
     * Draws the [Shape] on the canvas.
     *
     * @param context the drawing context holding data about environment, as well as Canvas to draw on.
     * @param paint the [Paint] used to draw the shape.
     * @param path the [Path] defining the shape.
     * @param left the left bound in which the shape should be drawn.
     * @param top the top bound in which the shape should be drawn.
     * @param right the right bound in which the shape should be drawn.
     * @param bottom the bottom bound in which the shape should be drawn.
     */
    @LongParameterListDrawFunction
    public fun drawShape(
        context: DrawContext,
        paint: Paint,
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    )
}
