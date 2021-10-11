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
import pl.patrykgoworowski.vico.core.collections.cacheInList
import pl.patrykgoworowski.vico.core.dataset.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.layout.MeasureContext
import pl.patrykgoworowski.vico.core.dataset.insets.DataSetInsetter
import pl.patrykgoworowski.vico.core.dataset.insets.Insets
import pl.patrykgoworowski.vico.core.extension.half

public open class AxisManager {

    private val hasTopAxis: Boolean
        get() = topAxis != null

    private val hasBottomAxis: Boolean
        get() = bottomAxis != null

    public val MeasureContext.leftAxis: AxisRenderer<*>?
        get() = if (isLtr) startAxis else endAxis

    public val MeasureContext.rightAxis: AxisRenderer<*>?
        get() = if (isLtr) endAxis else startAxis

    internal val axisCache = ArrayList<AxisRenderer<*>>(MAX_AXIS_COUNT)

    public var startAxis: AxisRenderer<AxisPosition.Vertical.Start>? by cacheInList()
    public var topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? by cacheInList()
    public var endAxis: AxisRenderer<AxisPosition.Vertical.End>? by cacheInList()
    public var bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? by cacheInList()

    fun addInsetters(destination: MutableList<DataSetInsetter>) {
        startAxis?.let(destination::add)
        topAxis?.let(destination::add)
        endAxis?.let(destination::add)
        bottomAxis?.let(destination::add)
    }

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

    fun setAxesBounds(
        measureContext: MeasureContext,
        contentBounds: RectF,
        dataSetBounds: RectF,
        insets: Insets,
    ) {
        startAxis?.setStartAxisBounds(
            context = measureContext,
            contentBounds = contentBounds,
            dataSetBounds = dataSetBounds,
            insets = insets,
        )
        topAxis?.setTopAxisBounds(
            context = measureContext,
            contentBounds = contentBounds,
            dataSetBounds = dataSetBounds,
            insets = insets,
        )
        endAxis?.setEndAxisBounds(
            context = measureContext,
            contentBounds = contentBounds,
            dataSetBounds = dataSetBounds,
            insets = insets,
        )
        bottomAxis?.setBottomAxisBounds(
            context = measureContext,
            contentBounds = contentBounds,
            dataSetBounds = dataSetBounds,
            insets = insets,
        )
        setRestrictedBounds()
    }

    private fun AxisRenderer<AxisPosition.Vertical.Start>.setStartAxisBounds(
        context: MeasureContext,
        contentBounds: RectF,
        dataSetBounds: RectF,
        insets: Insets,
    ) = with(context) {
        setBounds(
            left = if (isLtr) {
                contentBounds.left
            } else {
                contentBounds.right - insets.end
            },
            top = contentBounds.top + insets.top,
            right = if (isLtr) {
                contentBounds.left + insets.start + axisThickness.half
            } else {
                contentBounds.right
            },
            bottom = contentBounds.bottom - insets.bottom
        )
        this@setStartAxisBounds.dataSetBounds.set(
            getHorizontalAxisLeftDrawBound(context, dataSetBounds, insets),
            dataSetBounds.top + context.maxAnyAxisLineThickness * if (hasTopAxis) 1 else -1,
            getHorizontalAxisRightDrawBound(context, dataSetBounds, insets),
            dataSetBounds.bottom + if (hasBottomAxis) 0f else context.maxAnyAxisLineThickness
        )
    }

    private fun AxisRenderer<AxisPosition.Horizontal.Top>.setTopAxisBounds(
        context: MeasureContext,
        contentBounds: RectF,
        dataSetBounds: RectF,
        insets: Insets,
    ) {
        setBounds(
            left = contentBounds.left + insets.start,
            top = contentBounds.top,
            right = contentBounds.right - insets.end,
            bottom = contentBounds.top + insets.top
        )
        this.dataSetBounds.set(
            getHorizontalAxisLeftDrawBound(context, dataSetBounds, insets),
            dataSetBounds.top,
            getHorizontalAxisRightDrawBound(context, dataSetBounds, insets),
            dataSetBounds.bottom
        )
    }

    private fun AxisRenderer<AxisPosition.Vertical.End>.setEndAxisBounds(
        context: MeasureContext,
        contentBounds: RectF,
        dataSetBounds: RectF,
        insets: Insets,
    ) = with(context) {
        setBounds(
            left = if (isLtr) {
                contentBounds.right - (insets.end + axisThickness.half)
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
        this@setEndAxisBounds.dataSetBounds.set(
            getHorizontalAxisLeftDrawBound(context, dataSetBounds, insets),
            dataSetBounds.top + context.maxAnyAxisLineThickness * if (hasTopAxis) 1 else -1,
            getHorizontalAxisRightDrawBound(context, dataSetBounds, insets),
            dataSetBounds.bottom + if (hasBottomAxis) 0f else context.maxAnyAxisLineThickness
        )
    }

    private fun AxisRenderer<AxisPosition.Horizontal.Bottom>.setBottomAxisBounds(
        context: MeasureContext,
        contentBounds: RectF,
        dataSetBounds: RectF,
        insets: Insets,
    ) = with(context) {
        setBounds(
            left = contentBounds.left + insets.start,
            top = contentBounds.bottom - insets.bottom,
            right = contentBounds.right - insets.end,
            bottom = contentBounds.bottom
        )
        this@setBottomAxisBounds.dataSetBounds.set(
            getHorizontalAxisLeftDrawBound(context, dataSetBounds, insets),
            dataSetBounds.top + if (hasTopAxis) axisThickness else 0f,
            getHorizontalAxisRightDrawBound(context, dataSetBounds, insets),
            dataSetBounds.bottom
        )
    }

    private fun setRestrictedBounds() {
        startAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
        topAxis?.setRestrictedBounds(startAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
        endAxis?.setRestrictedBounds(topAxis?.bounds, startAxis?.bounds, bottomAxis?.bounds)
        bottomAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, startAxis?.bounds)
    }

    fun drawBehindDataSet(context: ChartDrawContext) {
        axisCache.forEach { axis ->
            axis.drawBehindDataSet(context)
        }
    }

    fun drawAboveDataSet(context: ChartDrawContext) {
        axisCache.forEach { axis ->
            axis.drawAboveDataSet(context)
        }
    }

    companion object {
        private const val MAX_AXIS_COUNT = 4

        private fun AxisManager.getHorizontalAxisLeftDrawBound(
            context: MeasureContext,
            dataSetBounds: RectF,
            insets: Insets,
        ) = with(context) {
            dataSetBounds.left + (leftAxis?.run { axisThickness.half } ?: -insets.getLeft(isLtr))
        }

        private fun AxisManager.getHorizontalAxisRightDrawBound(
            context: MeasureContext,
            dataSetBounds: RectF,
            insets: Insets,
        ) = with(context) {
            dataSetBounds.right - (rightAxis?.run { axisThickness.half } ?: -insets.getRight(isLtr))
        }
    }
}
