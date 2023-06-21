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

package com.patrykandpatrick.vico.core.chart.dimensions

import com.patrykandpatrick.vico.core.chart.Chart

/**
 * Holds information on a [Chart]’s horizontal dimensions.
 */
public interface HorizontalDimensions {
    /**
     * The distance between neighboring major entries (in pixels).
     */
    public val xSpacing: Float

    /**
     * The distance between the start of the content area and the first entry (in pixels).
     */
    public val startPadding: Float

    /**
     * The distance between the end of the content area and the last entry (in pixels).
     */
    public val endPadding: Float

    /**
     * Creates a new [HorizontalDimensions] instance by multiplying this one’s values by the given factor.
     */
    public fun scaled(scale: Float): HorizontalDimensions =
        HorizontalDimensions(xSpacing * scale, startPadding * scale, endPadding * scale)
}

/**
 * Creates a [HorizontalDimensions] instance.
 */
public fun HorizontalDimensions(
    xSpacing: Float,
    startPadding: Float,
    endPadding: Float,
): HorizontalDimensions = object : HorizontalDimensions {
    override val xSpacing: Float = xSpacing
    override val startPadding: Float = startPadding
    override val endPadding: Float = endPadding
}

/**
 * The total horizontal padding (in pixels).
 */
public val HorizontalDimensions.padding: Float
    get() = startPadding + endPadding
