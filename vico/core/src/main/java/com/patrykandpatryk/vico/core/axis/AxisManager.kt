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

package com.patrykandpatryk.vico.core.axis

import android.graphics.RectF
import com.patrykandpatryk.vico.core.chart.Chart
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.chart.insets.ChartInsetter
import com.patrykandpatryk.vico.core.chart.insets.Insets
import com.patrykandpatryk.vico.core.collections.cacheInList
import com.patrykandpatryk.vico.core.context.MeasureContext

/**
 * Manages a chart’s axes, setting their bounds and drawing them.
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
     * Adds the [AxisRenderer]s controlled by this [AxisManager] to the given [MutableList] of [ChartInsetter]s.
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
     * Sets each axis’s bounds.
     *
     * @param measureContext holds data used for component measurements.
     * @param contentBounds the bounds in which the content of the chart should be drawn.
     * @param insets the final insets, as specified by the associated chart’s [ChartInsetter]s. In order to be drawn
     * properly, axes should take these insets into account while setting their bounds.
     */
    public fun setAxesBounds(
        measureContext: MeasureContext,
        contentBounds: RectF,
        chartBounds: RectF,
        insets: Insets,
    ) {
        startAxis?.setStartAxisBounds(
            context = measureContext,
            contentBounds = contentBounds,
            chartBounds = chartBounds,
            insets = insets,
        )

        topAxis?.setTopAxisBounds(
            context = measureContext,
            contentBounds = contentBounds,
            insets = insets,
        )

        endAxis?.setEndAxisBounds(
            context = measureContext,
            contentBounds = contentBounds,
            chartBounds = chartBounds,
            insets = insets,
        )

        bottomAxis?.setBottomAxisBounds(
            context = measureContext,
            contentBounds = contentBounds,
            chartBounds = chartBounds,
            insets = insets,
        )

        setRestrictedBounds()
    }

    private fun AxisRenderer<AxisPosition.Vertical.Start>.setStartAxisBounds(
        context: MeasureContext,
        contentBounds: RectF,
        chartBounds: RectF,
        insets: Insets,
    ) {
        with(context) {
            setBounds(
                left = if (isLtr) contentBounds.left else contentBounds.right - insets.start,
                top = chartBounds.top,
                right = if (isLtr) contentBounds.left + insets.start else contentBounds.right,
                bottom = chartBounds.bottom,
            )
        }
    }

    private fun AxisRenderer<AxisPosition.Horizontal.Top>.setTopAxisBounds(
        context: MeasureContext,
        contentBounds: RectF,
        insets: Insets,
    ) {
        with(context) {
            setBounds(
                left = contentBounds.left + if (isLtr) insets.start else insets.end,
                top = contentBounds.top,
                right = contentBounds.right - if (isLtr) insets.end else insets.start,
                bottom = contentBounds.top + insets.top,
            )
        }
    }

    private fun AxisRenderer<AxisPosition.Vertical.End>.setEndAxisBounds(
        context: MeasureContext,
        contentBounds: RectF,
        chartBounds: RectF,
        insets: Insets,
    ) {
        with(context) {
            setBounds(
                left = if (isLtr) contentBounds.right - insets.end else contentBounds.left,
                top = chartBounds.top,
                right = if (isLtr) contentBounds.right else contentBounds.left + insets.end,
                bottom = chartBounds.bottom,
            )
        }
    }

    private fun AxisRenderer<AxisPosition.Horizontal.Bottom>.setBottomAxisBounds(
        context: MeasureContext,
        contentBounds: RectF,
        chartBounds: RectF,
        insets: Insets,
    ) {
        with(context) {
            setBounds(
                left = contentBounds.left + if (isLtr) insets.start else insets.end,
                top = chartBounds.bottom,
                right = contentBounds.right - if (isLtr) insets.end else insets.start,
                bottom = chartBounds.bottom + insets.bottom,
            )
        }
    }

    private fun setRestrictedBounds() {
        startAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
        topAxis?.setRestrictedBounds(startAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
        endAxis?.setRestrictedBounds(topAxis?.bounds, startAxis?.bounds, bottomAxis?.bounds)
        bottomAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, startAxis?.bounds)
    }

    /**
     * Called before the associated [Chart] is drawn. This forwards a call to all [Axis] subclasses that causes them to
     * be drawn behind the chart.
     *
     * @param context holds the information necessary to draw the axes.
     *
     * @see Axis.drawBehindChart
     */
    public fun drawBehindChart(context: ChartDrawContext) {
        axisCache.forEach { axis ->
            axis.drawBehindChart(context)
        }
    }

    /**
     * Called after the associated [Chart] is drawn. This forwards a call to all [Axis] subclasses that causes them to
     * be drawn above the chart.
     *
     * @param context holds the information necessary to draw the axes.
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
