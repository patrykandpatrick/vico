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

package pl.patrykgoworowski.vico.core.axis

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.chart.insets.ChartInsetter
import pl.patrykgoworowski.vico.core.dimensions.BoundsAware

/**
 * The interface defining minimal set of properties and functions required by other parts of the library to draw
 * an axis.
 */
public interface AxisRenderer<Position : AxisPosition> : BoundsAware, ChartInsetter {

    /**
     * Defines a position of axis relative to the [pl.patrykgoworowski.vico.core.chart.Chart].
     */
    public val position: Position

    /**
     * Called before [pl.patrykgoworowski.vico.core.chart.Chart] is drawn.
     * Subclasses should rely on this function to draw themselves, unless they want to draw something above the chart.
     *
     * @param context Drawing context holding information necessary to draw axis.
     *
     * @see drawAboveChart
     */
    public fun drawBehindChart(context: ChartDrawContext)

    /**
     * Called after [pl.patrykgoworowski.vico.core.chart.Chart] is drawn.
     * Subclasses can use this function to draw something in the chart’s bounds, that can’t be covered by the chart.
     *
     * @param context Drawing context holding information necessary to draw axis.
     */
    public fun drawAboveChart(context: ChartDrawContext)

    /**
     * The bounds ([RectF]) passed here define rectangle coordinates where the [AxisRenderer] shouldn’t draw anything.
     */
    public fun setRestrictedBounds(vararg bounds: RectF?)
}
