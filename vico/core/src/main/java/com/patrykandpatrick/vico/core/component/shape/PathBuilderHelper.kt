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

package com.patrykandpatrick.vico.core.component.shape

import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.extension.updateIfExceeds
import com.patrykandpatrick.vico.core.model.Point

/**
 * A helper class for building paths.
 *
 * @param path the path to build.
 */
public abstract class PathBuilderHelper(
    public val path: Path = Path(),
) : PathBuilder {

    /**
     * The bounds of the path.
     */
    protected val pathBuilderBounds: RectF = RectF()

    override var lastX: Float = 0f

    override var lastY: Float = 0f

    override val lastPoint: Point
        get() = Point(lastX, lastY)

    /**
     * Checks whether the [path] is empty (contains no lines or curves)
     */
    public val isEmpty: Boolean
        get() = path.isEmpty

    /**
     * Called before the path is built.
     * This callback should be used to prepare the builder for building and drawing the path.
     */
    public abstract fun setUp(context: DrawContext)

    override fun moveTo(x: Float, y: Float) {
        pathBuilderBounds.updateIfExceeds(x, y)
        path.moveTo(x, y)

        lastX = x
        lastY = y
    }

    override fun lineTo(x: Float, y: Float) {
        pathBuilderBounds.updateIfExceeds(x, y)
        path.lineTo(x, y)

        lastX = x
        lastY = y
    }

    override fun rLineTo(x: Float, y: Float) {
        val correctedX = lastX + x
        val correctedY = lastY + y

        path.lineTo(correctedX, correctedY)

        lastX = correctedX
        lastY = correctedY
    }

    override fun close() {
        path.close()
    }

    /**
     * Draws the built path.
     */
    public abstract fun draw(context: DrawContext)
}
