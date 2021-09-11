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

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.vico.core.axis.model.DataSetModel
import pl.patrykgoworowski.vico.core.collections.cacheInList
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.renderer.RendererViewState
import pl.patrykgoworowski.vico.core.dataset.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.dimensions.DataSetInsetter
import pl.patrykgoworowski.vico.core.dimensions.Dimensions
import pl.patrykgoworowski.vico.core.extension.half

public open class AxisManager {

    private val hasTopAxis: Boolean
        get() = topAxis != null

    private val hasBottomAxis: Boolean
        get() = bottomAxis != null

    private val leftAxis: AxisRenderer<*>?
        get() = if (isLTR) startAxis else endAxis

    private val rightAxis: AxisRenderer<*>?
        get() = if (isLTR) endAxis else startAxis

    internal val axisCache = ArrayList<AxisRenderer<*>>(MAX_AXIS_COUNT)

    internal open var isLTR: Boolean = true

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

    fun setAxesBounds(
        contentBounds: RectF,
        dataSetBounds: RectF,
        axesDimensions: Dimensions,
    ) {
        val horizontalAxisLeftDrawBound = dataSetBounds.left +
                (leftAxis?.axisThickness?.half ?: -axesDimensions.getLeft(isLTR))

        val horizontalAxisRightDrawBound = dataSetBounds.right -
                (rightAxis?.axisThickness?.half ?: -axesDimensions.getRight(isLTR))

        startAxis?.let { axis ->
            setStartAxisBounds(
                axis = axis,
                contentBounds = contentBounds,
                dataSetBounds = dataSetBounds,
                axesDimensions = axesDimensions,
                horizontalAxisLeftDrawBound = horizontalAxisLeftDrawBound,
                horizontalAxisRightDrawBound = horizontalAxisRightDrawBound,
            )
        }
        topAxis?.let { axis ->
            setTopAxisBounds(
                axis = axis,
                contentBounds = contentBounds,
                dataSetBounds = dataSetBounds,
                axesDimensions = axesDimensions,
                horizontalAxisLeftDrawBound = horizontalAxisLeftDrawBound,
                horizontalAxisRightDrawBound = horizontalAxisRightDrawBound,
            )
        }
        endAxis?.let { axis ->
            setEndAxisBounds(
                axis = axis,
                contentBounds = contentBounds,
                dataSetBounds = dataSetBounds,
                axesDimensions = axesDimensions,
                horizontalAxisLeftDrawBound = horizontalAxisLeftDrawBound,
                horizontalAxisRightDrawBound = horizontalAxisRightDrawBound,
            )
        }
        bottomAxis?.let { axis ->
            setBottomAxisBounds(
                axis = axis,
                contentBounds = contentBounds,
                dataSetBounds = dataSetBounds,
                axesDimensions = axesDimensions,
                horizontalAxisLeftDrawBound = horizontalAxisLeftDrawBound,
                horizontalAxisRightDrawBound = horizontalAxisRightDrawBound,
            )
        }
        setRestrictedBounds()
    }

    private fun setStartAxisBounds(
        axis: AxisRenderer<AxisPosition.Vertical.Start>,
        contentBounds: RectF,
        dataSetBounds: RectF,
        axesDimensions: Dimensions,
        horizontalAxisLeftDrawBound: Float,
        horizontalAxisRightDrawBound: Float,
    ) {
        axis.setBounds(
            left = if (isLTR) contentBounds.left else contentBounds.right - axesDimensions.end,
            top = contentBounds.top + axesDimensions.top,
            right = if (isLTR) {
                contentBounds.left + axesDimensions.start + axis.axisThickness.half
            } else {
                contentBounds.right
            },
            bottom = contentBounds.bottom - axesDimensions.bottom
        )
        axis.dataSetBounds.set(
            horizontalAxisLeftDrawBound,
            dataSetBounds.top + axis.maxAnyAxisLineThickness * if (hasTopAxis) 1 else -1,
            horizontalAxisRightDrawBound,
            dataSetBounds.bottom + if (hasBottomAxis) 0f else axis.maxAnyAxisLineThickness
        )
    }

    private fun setTopAxisBounds(
        axis: AxisRenderer<AxisPosition.Horizontal.Top>,
        contentBounds: RectF,
        dataSetBounds: RectF,
        axesDimensions: Dimensions,
        horizontalAxisLeftDrawBound: Float,
        horizontalAxisRightDrawBound: Float,
    ) {
        axis.setBounds(
            left = contentBounds.left + axesDimensions.start,
            top = contentBounds.top,
            right = contentBounds.right - axesDimensions.end,
            bottom = contentBounds.top + axesDimensions.top
        )
        axis.dataSetBounds.set(
            horizontalAxisLeftDrawBound,
            dataSetBounds.top,
            horizontalAxisRightDrawBound,
            dataSetBounds.bottom
        )
    }

    private fun setEndAxisBounds(
        axis: AxisRenderer<AxisPosition.Vertical.End>,
        contentBounds: RectF,
        dataSetBounds: RectF,
        axesDimensions: Dimensions,
        horizontalAxisLeftDrawBound: Float,
        horizontalAxisRightDrawBound: Float,
    ) {
        axis.setBounds(
            left = if (isLTR) {
                contentBounds.right - (axesDimensions.end + axis.axisThickness.half)
            } else {
                contentBounds.left
            },
            top = contentBounds.top + axesDimensions.top,
            right = if (isLTR) {
                contentBounds.right
            } else {
                contentBounds.left + axesDimensions.end
            },
            bottom = contentBounds.bottom - axesDimensions.bottom
        )
        axis.dataSetBounds.set(
            horizontalAxisLeftDrawBound,
            dataSetBounds.top + axis.maxAnyAxisLineThickness * if (hasTopAxis) 1 else -1,
            horizontalAxisRightDrawBound,
            dataSetBounds.bottom + if (hasBottomAxis) 0f else axis.maxAnyAxisLineThickness
        )
    }

    private fun setBottomAxisBounds(
        axis: AxisRenderer<AxisPosition.Horizontal.Bottom>,
        contentBounds: RectF,
        dataSetBounds: RectF,
        axesDimensions: Dimensions,
        horizontalAxisLeftDrawBound: Float,
        horizontalAxisRightDrawBound: Float,
    ) {
        axis.setBounds(
            left = contentBounds.left + axesDimensions.start,
            top = contentBounds.bottom - axesDimensions.bottom,
            right = contentBounds.right - axesDimensions.end,
            bottom = contentBounds.bottom
        )
        axis.dataSetBounds.set(
            horizontalAxisLeftDrawBound,
            dataSetBounds.top + if (hasTopAxis) axis.axisThickness else 0f,
            horizontalAxisRightDrawBound,
            dataSetBounds.bottom
        )
    }

    private fun setRestrictedBounds() {
        startAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
        topAxis?.setRestrictedBounds(startAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
        endAxis?.setRestrictedBounds(topAxis?.bounds, startAxis?.bounds, bottomAxis?.bounds)
        bottomAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, startAxis?.bounds)
    }

    fun drawBehindDataSet(
        canvas: Canvas,
        model: EntryModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
    ) {
        axisCache.forEach { axis ->
            axis.drawBehindDataSet(
                canvas,
                model,
                dataSetModel,
                segmentProperties,
                rendererViewState
            )
        }
    }

    fun drawAboveDataSet(
        canvas: Canvas,
        model: EntryModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
    ) {
        axisCache.forEach { axis ->
            axis.drawAboveDataSet(
                canvas,
                model,
                dataSetModel,
                segmentProperties,
                rendererViewState
            )
        }
    }

    companion object {
        private const val MAX_AXIS_COUNT = 4
    }
}
