/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.common.path

import com.patrykandpatrick.vico.core.common.Point

/**
 * Abstracts the path building process.
 */
public interface PathBuilder {
    /**
     * Returns the last x coordinate of the path.
     */
    public val lastX: Float

    /**
     * Returns the last y coordinate of the path.
     */
    public val lastY: Float

    /**
     * Returns the last point of the path.
     */
    public val lastPoint: Point

    /**
     * Moves the path to the provided coordinates.
     */
    public fun moveTo(
        x: Float,
        y: Float,
    )

    /**
     * A convenience method for [moveTo] that takes a [Point] as an argument.
     */
    public fun moveTo(point: Point) {
        moveTo(point.x, point.y)
    }

    /**
     * Adds a line to the path from the last point to the provided coordinates.
     */
    public fun lineTo(
        x: Float,
        y: Float,
    )

    /**
     * A convenience method for [lineTo] that takes a [Point] as an argument.
     */
    public fun lineTo(point: Point) {
        lineTo(point.x, point.y)
    }

    /**
     * Similar to [moveTo] but relative to the provided coordinates.
     */
    public fun rLineTo(
        x: Float,
        y: Float,
    )

    /**
     * A convenience method for [rLineTo] that takes a [Point] as an argument.
     */
    public fun rLineTo(point: Point) {
        rLineTo(point.x, point.y)
    }

    /**
     * Closes the path.
     */
    public fun close()
}
