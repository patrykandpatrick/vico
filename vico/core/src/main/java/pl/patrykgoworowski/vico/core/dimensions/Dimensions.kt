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

package pl.patrykgoworowski.vico.core.dimensions

/**
 * Defines the size of each edge of a rectangle.
 * Used to store e.g. padding, or margin values.
 */
public interface Dimensions {

    /**
     * The value on the start edge in dp unit.
     */
    public val startDp: Float

    /**
     * The value on the top edge in dp unit.
     */
    public val topDp: Float

    /**
     * The value on the end edge in dp unit.
     */
    public val endDp: Float

    /**
     * The value on the bottom edge in dp unit.
     */
    public val bottomDp: Float

    /**
     * Returns a dimension of the left edge depending on layout orientation.
     *
     * @param isLtr whether the device layout is left-to-right.
     */
    public fun getLeftDp(isLtr: Boolean): Float = if (isLtr) startDp else endDp

    /**
     * Returns a dimension of the right edge depending on layout orientation.
     *
     * @param isLtr whether the device layout is left-to-right.
     */
    public fun getRightDp(isLtr: Boolean): Float = if (isLtr) endDp else startDp
}
