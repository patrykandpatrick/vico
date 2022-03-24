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

package pl.patrykgoworowski.vico.core.axis

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.chart.insets.ChartInsetter
import pl.patrykgoworowski.vico.core.chart.insets.Insets
import pl.patrykgoworowski.vico.core.collections.cacheInList
import pl.patrykgoworowski.vico.core.context.MeasureContext

/**
 * A class that manages the axes used in a chart.
 * It supports axes placed at the start, top, end, and bottom of the chart.
 * This class is responsible for setting bounds and drawing each axis.
 *
 * @see AxisRenderer
 */
public open class AxisManager {

    internal val axisCache = ArrayList<AxisRenderer<*>>(MAX_AXIS_COUNT)

    /**
     * The [AxisRenderer] for the start axis.
     */
    public var startAxis: AxisRenderer<AxisPosition.Vertical.Start>? by cacheInList()

    /**
     * The [AxisRenderer] for the top axis.
     */
    public var topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? by cacheInList()

    /**
     * The [AxisRenderer] for the end axis.
     */
    public var endAxis: AxisRenderer<AxisPosition.Vertical.End>? by cacheInList()

    /**
     * The [AxisRenderer] for the bottom axis.
     */
    public var bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? by cacheInList()

    /**
     * Adds axes ([AxisRenderer]s) to the list of [ChartInsetter]s, which can influence the chart layout.
     *
     * @param destination the output [MutableList] where the [ChartInsetter]s will be stored.
     *
     * @see ChartInsetter
     */
    public fun addInsetters(destination: MutableList<ChartInsetter>) {
        startAxis?.let(destination::add)
        topAxis?.let(destination::add)
        endAxis?.let(destination::add)
        bottomAxis?.let(destination::add)
    }

    /**
     * Sets the axes managed by this [AxisManager].
     */
    public fun setAxes(
        startAxis: AxisRenderer<AxisPosition.Vertical.Start>? = this.startAxis,
        topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? = this.topAxis,
        endAxis: AxisRenderer<AxisPosition.Vertical.End>? = this.endAxis,
        bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? = this.bottomAxis,
    ) {
        this.startAxis = startAxis
        this.topAxis = topAxis
        this.endAxis = endAxis
        this.bottomAxis = bottomAxis
    }

    /**
     * Sets the bounds of each axis.
     *
     * @param measureContext the measuring context that holds data used for component measurements.
     * @param contentBounds the bounds in which the chart contents must be drawn.
     * @param insets the final insets. These store the maximum insets for each side, as specified by the
     * [ChartInsetter]s used by this chart. Axes should take them into account while setting their bounds to avoid being
     * drawn incorrectly.
     */
    public fun setAxesBounds(
        measureContext: MeasureContext,
        contentBounds: RectF,
        insets: Insets,
    ) {
        startAxis?.setStartAxisBounds(
            context = measureContext,
            contentBounds = contentBounds,
            insets = insets,
        )
        topAxis?.setTopAxisBounds(
            contentBounds = contentBounds,
            insets = insets,
        )
        endAxis?.setEndAxisBounds(
            context = measureContext,
            contentBounds = contentBounds,
            insets = insets,
        )
        bottomAxis?.setBottomAxisBounds(
            contentBounds = contentBounds,
            insets = insets,
        )
        setRestrictedBounds()
    }

    private fun AxisRenderer<AxisPosition.Vertical.Start>.setStartAxisBounds(
        context: MeasureContext,
        contentBounds: RectF,
        insets: Insets,
    ): Unit = with(context) {
        setBounds(
            left = if (isLtr) {
                contentBounds.left
            } else {
                contentBounds.right - insets.end
            },
            top = contentBounds.top + insets.top,
            right = if (isLtr) {
                contentBounds.left + insets.start
            } else {
                contentBounds.right
            },
            bottom = contentBounds.bottom - insets.bottom
        )
    }

    private fun AxisRenderer<AxisPosition.Horizontal.Top>.setTopAxisBounds(
        contentBounds: RectF,
        insets: Insets,
    ) {
        setBounds(
            left = contentBounds.left + insets.start,
            top = contentBounds.top,
            right = contentBounds.right - insets.end,
            bottom = contentBounds.top + insets.top
        )
    }

    private fun AxisRenderer<AxisPosition.Vertical.End>.setEndAxisBounds(
        context: MeasureContext,
        contentBounds: RectF,
        insets: Insets,
    ): Unit = with(context) {
        setBounds(
            left = if (isLtr) {
                contentBounds.right - insets.end
            } else {
                contentBounds.left
            },
            top = contentBounds.top + insets.top,
            right = if (isLtr) {
                contentBounds.right
            } else {
                contentBounds.left + insets.end
            },
            bottom = contentBounds.bottom - insets.bottom
        )
    }

    private fun AxisRenderer<AxisPosition.Horizontal.Bottom>.setBottomAxisBounds(
        contentBounds: RectF,
        insets: Insets,
    ) {
        setBounds(
            left = contentBounds.left + insets.start,
            top = contentBounds.bottom - insets.bottom,
            right = contentBounds.right - insets.end,
            bottom = contentBounds.bottom
        )
    }

    private fun setRestrictedBounds() {
        startAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
        topAxis?.setRestrictedBounds(startAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
        endAxis?.setRestrictedBounds(topAxis?.bounds, startAxis?.bounds, bottomAxis?.bounds)
        bottomAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, startAxis?.bounds)
    }

    /**
     * Called before the [pl.patrykgoworowski.vico.core.chart.Chart] is drawn.
     * This forwards a call to all [Axis] subclasses that causes them to be drawn behind the chart.
     *
     * @param context a drawing context that holds the information necessary to draw the axes.
     *
     * @see Axis.drawBehindChart
     */
    public fun drawBehindChart(context: ChartDrawContext) {
        axisCache.forEach { axis ->
            axis.drawBehindChart(context)
        }
    }

    /**
     * Called after [pl.patrykgoworowski.vico.core.chart.Chart] is drawn.
     * This forwards a call to all [Axis] subclasses that causes them to be drawn above the chart.
     *
     * @param context the drawing context that holds the information necessary to draw the axes.
     *
     * @see Axis.drawAboveChart
     */
    public fun drawAboveChart(context: ChartDrawContext) {
        axisCache.forEach { axis ->
            axis.drawAboveChart(context)
        }
    }

    public companion object {
        private const val MAX_AXIS_COUNT = 4
    }
}
