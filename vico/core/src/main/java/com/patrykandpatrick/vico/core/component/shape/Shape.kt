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

package com.patrykandpatrick.vico.core.component.shape

import android.graphics.Paint
import android.graphics.Path
import com.patrykandpatrick.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatrick.vico.core.context.DrawContext

/**
 * Defines a shape that can be drawn on a canvas.
 */
public interface Shape {

    /**
     * Draws the [Shape] on the canvas.
     *
     * @param context holds environment data.
     * @param paint the [Paint] used to draw the shape.
     * @param path the [Path] defining the shape.
     * @param left the _x_ coordinate of the left edge of the bounds in which the shape should be drawn.
     * @param top the _y_ coordinate of the top edge of the bounds in which the shape should be drawn.
     * @param right the _x_ coordinate of the right edge of the bounds in which the shape should be drawn.
     * @param bottom the _y_ coordinate of the bottom edge of the bounds in which the shape should be drawn.
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
