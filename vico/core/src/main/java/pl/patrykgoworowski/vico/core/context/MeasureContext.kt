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

package pl.patrykgoworowski.vico.core.context

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.axis.model.ChartModel

/**
 * [MeasureContext] holds data used by various chart components during measure and draw phase.
 */
public interface MeasureContext : Extras {

    /**
     * The bounds of the canvas which will be used to draw the chart and its components.
     */
    public val canvasBounds: RectF

    /**
     * Holds information about values on both y-axis and x-axis.
     *
     * @see ChartModel
     */
    public val chartModel: ChartModel

    /**
     * The pixel density.
     */
    public val density: Float

    /**
     * The scale of fonts.
     */
    public val fontScale: Float

    /**
     * Whether the current device layout is left-to-right.
     */
    public val isLtr: Boolean

    /**
     * Whether horizontal scrolling is enabled.
     */
    public val isHorizontalScrollEnabled: Boolean

    /**
     * The current amount of horizontal scroll.
     */
    public val horizontalScroll: Float

    /**
     * The scale of the chart. Used to handle zooming in and out.
     */
    public val chartScale: Float

    /**
     * Converts the receiver [Float] to pixels.
     */
    public val Float.pixels: Float
        get() = this * density

    /**
     * Converts the receiver [Float] to pixels and discards decimal values.
     */
    public val Float.wholePixels: Int
        get() = pixels.toInt()

    /**
     * Converts given [dp] value to pixels.
     */
    public fun toPixels(dp: Float): Float = dp * density

    /**
     * Converts given [sp] value to the font size.
     */
    public fun toFontSize(sp: Float): Float = sp * fontScale
}
