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

package com.patrykandpatrick.vico.core.context

public interface PreMeasureContext : Extras {
    /**
     * The pixel density.
     */
    public val density: Float

    /**
     * The number of pixels corresponding to this number of density-independent pixels.
     */
    public val Float.pixels: Float
        get() = this * density

    /**
     * The number of pixels corresponding to this number of density-independent pixels, with decimal values discarded.
     */
    public val Float.wholePixels: Int
        get() = pixels.toInt()

    /**
     * Converts the provided dimension from dp to px.
     */
    public fun dpToPx(dp: Float): Float = dp * density

    /**
     * Converts the provided dimension from sp to px.
     */
    public fun spToPx(sp: Float): Float

    /**
     * Whether the layout direction is left-to-right.
     */
    public val isLtr: Boolean

    /**
     * A multiplier used to ensure support for both left-to-right and right-to-left layouts. Values such as translation
     * deltas are multiplied by this value. [layoutDirectionMultiplier] is equal to `1f` if [isLtr] is `true`, and `-1f`
     * otherwise.
     */
    public val layoutDirectionMultiplier: Float
        get() = if (isLtr) 1f else -1f

    /**
     * Removes all stored extras.
     *
     * @see Extras.clearExtras
     */
    public fun reset()
}
