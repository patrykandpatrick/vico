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

package com.patrykandpatrick.vico.core.entry

import com.patrykandpatrick.vico.core.chart.Chart

/**
 * The base for a single chart entry rendered by [Chart] subclasses.
 * It holds information about the location of the chart entry on the x-axis and y-axis.
 */
public interface ChartEntry {

    /**
     * The position of this [ChartEntry] on the x-axis.
     */
    public val x: Float

    /**
     * The position of this [ChartEntry] on the y-axis.
     */
    public val y: Float

    /**
     * @see x
     */
    public operator fun component1(): Float = x

    /**
     * @see y
     */
    public operator fun component2(): Float = y

    /**
     * Creates a copy of this [ChartEntry] implementation, but with a new [y] value.
     */
    public fun withY(y: Float): ChartEntry
}
