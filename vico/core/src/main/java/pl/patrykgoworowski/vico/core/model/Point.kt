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

package pl.patrykgoworowski.vico.core.model

import pl.patrykgoworowski.vico.core.util.packFloats
import pl.patrykgoworowski.vico.core.util.packInts
import pl.patrykgoworowski.vico.core.util.unpackFloat1
import pl.patrykgoworowski.vico.core.util.unpackFloat2

/**
 * Creates a new [Point] with the provided coordinates.
 */
public fun Point(x: Float, y: Float): Point = Point(packFloats(x, y))

/**
 * Creates a new [Point] with the provided coordinates.
 */
public fun Point(x: Int, y: Int): Point = Point(packInts(x, y))

/**
 * Represents a point in a coordinate system.
 */
@JvmInline
public value class Point internal constructor(private val packedValue: Long) {

    /**
     * The x coordinate.
     */
    public val x: Float
        get() = unpackFloat1(packedValue)

    /**
     * The y coordinate.
     */
    public val y: Float
        get() = unpackFloat2(packedValue)

    public operator fun component1(): Float = x

    public operator fun component2(): Float = y

    public fun copy(
        x: Float = this.x,
        y: Float = this.y,
    ): Point = Point(x, y)
}
