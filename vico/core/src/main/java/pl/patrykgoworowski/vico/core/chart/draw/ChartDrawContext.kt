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

package pl.patrykgoworowski.vico.core.chart.draw

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.chart.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.context.DrawContext
import pl.patrykgoworowski.vico.core.model.Point

/**
 * The extension of [DrawContext] which holds additional data required to render the chart.
 */
public interface ChartDrawContext : DrawContext {

    /**
     * The bounds in which the [pl.patrykgoworowski.vico.core.chart.Chart] will be drawn.
     */
    public val chartBounds: RectF

    /**
     * Holds information about the width of each individual segment on the x-axis.
     */
    public val segmentProperties: SegmentProperties

    /**
     * The point inside the chart’s coordinates where physical touch is occurring.
     */
    public val markerTouchPoint: Point?
}
