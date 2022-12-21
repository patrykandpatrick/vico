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

package com.patrykandpatryk.vico.core.chart.decoration

import android.graphics.RectF
import com.patrykandpatryk.vico.core.chart.Chart
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext

/**
 * A [Decoration] presents additional information on a [Chart].
 *
 * An example [Decoration] implementation is [ThresholdLine].
 *
 * @see [ThresholdLine]
 */
public interface Decoration {

    /**
     * Called before the [Chart] starts drawing itself.
     *
     * @param [context] holds the information needed to draw the [Chart].
     * @param [bounds] the bounding box of the [Chart].
     */
    public fun onDrawBehindChart(context: ChartDrawContext, bounds: RectF): Unit = Unit

    /**
     * Called immediately after the [Chart] finishes drawing itself.
     *
     * @param [context] holds the information needed to draw the [Chart].
     * @param [bounds] the bounding box of the [Chart].
     */
    public fun onDrawAboveChart(context: ChartDrawContext, bounds: RectF): Unit = Unit
}
