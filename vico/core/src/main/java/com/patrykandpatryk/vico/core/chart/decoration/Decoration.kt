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

package com.patrykandpatryk.vico.core.chart.decoration

import android.graphics.Canvas
import android.graphics.RectF
import com.patrykandpatryk.vico.core.chart.Chart
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.marker.Marker

/**
 * A [Decoration] is drawn between a [Chart] and persistent [Marker] layers.
 *
 * An example [Decoration] implementation is [ThresholdLine], which draws threshold lines above the [Chart].
 *
 * @see [ThresholdLine]
 */
public interface Decoration {

    /**
     * Called before the [Chart] starts drawing itself.
     *
     * @param [context] the drawing context containing the [Canvas] and other data.
     * @param [bounds] the bounding box of the [Chart].
     */
    public fun onDrawBehindChart(context: ChartDrawContext, bounds: RectF): Unit = Unit

    /**
     * Called immediately after the [Chart] finishes drawing itself.
     *
     * @param [context] the drawing context containing the [Canvas] and other data.
     * @param [bounds] the bounding box of the [Chart].
     */
    public fun onDrawAboveChart(context: ChartDrawContext, bounds: RectF): Unit = Unit
}
