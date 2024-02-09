/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.axis

import android.graphics.RectF
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.ChartInsetter
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.collections.cacheInList
import com.patrykandpatrick.vico.core.context.MeasureContext

internal class AxisManager {
    internal val axisCache = ArrayList<Axis<*>>(MAX_AXIS_COUNT)

    var startAxis: Axis<AxisPosition.Vertical.Start>? by cacheInList()
    var topAxis: Axis<AxisPosition.Horizontal.Top>? by cacheInList()
    var endAxis: Axis<AxisPosition.Vertical.End>? by cacheInList()
    var bottomAxis: Axis<AxisPosition.Horizontal.Bottom>? by cacheInList()

    fun addInsetters(destination: MutableList<ChartInsetter>) {
        startAxis?.let(destination::add)
        topAxis?.let(destination::add)
        endAxis?.let(destination::add)
        bottomAxis?.let(destination::add)
    }

    fun setAxesBounds(
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

    private fun Axis<AxisPosition.Vertical.Start>.setStartAxisBounds(
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

    private fun Axis<AxisPosition.Horizontal.Top>.setTopAxisBounds(
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

    private fun Axis<AxisPosition.Vertical.End>.setEndAxisBounds(
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

    private fun Axis<AxisPosition.Horizontal.Bottom>.setBottomAxisBounds(
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

    fun drawBehindChart(context: ChartDrawContext) {
        axisCache.forEach { axis ->
            axis.drawBehindChart(context)
        }
    }

    fun drawAboveChart(context: ChartDrawContext) {
        axisCache.forEach { axis ->
            axis.drawAboveChart(context)
        }
    }

    private companion object {
        const val MAX_AXIS_COUNT = 4
    }
}
