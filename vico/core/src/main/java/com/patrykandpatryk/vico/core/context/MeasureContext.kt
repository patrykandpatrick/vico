/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.core.context

import android.graphics.RectF
import com.patrykandpatryk.vico.core.chart.values.ChartValues
import com.patrykandpatryk.vico.core.chart.values.ChartValuesManager

/**
 * [MeasureContext] holds data used by various chart components during the measuring and drawing phases.
 */
public interface MeasureContext : Extras {

    /**
     * The bounds of the canvas that will be used to draw the chart and its components.
     */
    public val canvasBounds: RectF

    /**
     * Manages [ChartValues] used in the chart.
     *
     * @see [ChartValuesManager]
     */
    public val chartValuesManager: ChartValuesManager

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
     * The scale of the chart. Used to handle zooming in and out.
     */
    public val chartScale: Float

    /**
     * A multiplier used to ensure support for both left-to-right and right-to-left layouts.
     * Values such as translation deltas are multiplied by this value.
     * [layoutDirectionMultiplier] is equal to `1f` if [isLtr] is `true`, and `-1f` otherwise.
     */
    public val layoutDirectionMultiplier: Float
        get() = if (isLtr) 1f else -1f

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
     * Converts the given [dp] value to pixels.
     */
    public fun toPixels(dp: Float): Float = dp * density

    /**
     * Converts the given [sp] value to a font size in pixels.
     */
    public fun toFontSize(sp: Float): Float = sp * fontScale
}
