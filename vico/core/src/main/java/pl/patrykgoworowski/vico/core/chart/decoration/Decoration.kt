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

package pl.patrykgoworowski.vico.core.chart.decoration

import android.graphics.RectF
import android.graphics.Canvas
import pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.chart.Chart
import pl.patrykgoworowski.vico.core.marker.Marker

/**
 * A [Decoration] is drawn between a [Chart] and a persistent [Marker] layers.
 *
 * An example of [Decoration] implementation is [ThresholdLine], which draws threshold lines above the [Chart].
 *
 * @see [ThresholdLine]
 */
public interface Decoration {

    /**
     * Called right after [Chart] finishes drawing itself.
     *
     * @param [context] A drawing context containing a [Canvas] and other data.
     * @param [bounds] A bounding box of the [Chart].
     */
    public fun draw(context: ChartDrawContext, bounds: RectF)
}
