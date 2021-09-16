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

package pl.patrykgoworowski.vico.core.dataset.column

import android.graphics.Canvas
import pl.patrykgoworowski.vico.core.axis.model.MutableDataSetModel
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.constants.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.vico.core.constants.DEF_MERGED_BAR_SPACING
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.forEachIn
import pl.patrykgoworowski.vico.core.dataset.put
import pl.patrykgoworowski.vico.core.dataset.renderer.BaseDataSet
import pl.patrykgoworowski.vico.core.dataset.renderer.RendererViewState
import pl.patrykgoworowski.vico.core.dataset.segment.MutableSegmentProperties
import pl.patrykgoworowski.vico.core.dataset.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.extension.between
import pl.patrykgoworowski.vico.core.extension.dp
import pl.patrykgoworowski.vico.core.extension.getRepeating
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.inClip
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.extension.set
import pl.patrykgoworowski.vico.core.marker.Marker
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

public open class ColumnDataSet(
    public var columns: List<LineComponent>,
    public var spacing: Float = DEF_MERGED_BAR_SPACING.dp,
    public var innerSpacing: Float = DEF_MERGED_BAR_INNER_SPACING.dp,
    public var mergeMode: MergeMode = MergeMode.Grouped
) : BaseDataSet<EntryModel>() {

    constructor(
        column: LineComponent,
        spacing: Float = DEF_MERGED_BAR_SPACING.dp,
    ) : this(columns = listOf(column), spacing = spacing)

    constructor() : this(emptyList())

    private val heightMap = HashMap<Float, Float>()
    override val markerLocationMap = HashMap<Float, MutableList<Marker.EntryModel>>()

    override var maxScrollAmount: Float = 0f
    override var zoom: Float? = null
        set(value) {
            field = value
            isScaleCalculated = false
        }

    private var drawScale: Float = 1f
    private var isScaleCalculated = false
    private var scaledSpacing = spacing
    private var scaledInnerSpacing = innerSpacing

    private val segmentProperties = MutableSegmentProperties()

    override fun getMeasuredWidth(model: EntryModel): Int {
        val length = model.getEntriesLength()
        val segmentWidth = getSegmentSize(model.entryCollections.size, false)
        return (segmentWidth * length + spacing * length).roundToInt()
    }

    override fun setBounds(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number
    ) {
        bounds.set(left, top, right, bottom)
        isScaleCalculated = false
    }

    override fun drawDataSet(
        canvas: Canvas,
        model: EntryModel,
        segmentProperties: SegmentProperties,
        viewState: RendererViewState,
    ) = canvas.inClip(bounds) {

        markerLocationMap.clear()
        calculateDrawSegmentSpecIfNeeded(model)
        updateMaxScrollAmount(model.getEntriesLength(), segmentProperties.segmentWidth)

        val heightMultiplier = bounds.height() / ((maxY ?: mergeMode.getMaxY(model)) - minY.orZero)

        var drawingStart: Float

        var height: Float
        var columnCenterX: Float
        var column: LineComponent
        var columnTop: Float
        var columnBottom: Float
        val bottomCompensation = if (minY.orZero < 0f) (minY.orZero * heightMultiplier) else 0f

        val defaultSegmentSize = getSegmentSize(model.entryCollections.size, scaled = true)

        val (segmentSize, spacing) = segmentProperties

        model.entryCollections.forEachIndexed { index, entryCollection ->

            column = columns.getRepeating(index)
            column.setParentBounds(bounds)
            drawingStart = getDrawingStart(
                entryCollectionIndex = index,
                segmentCompensation = (segmentSize - defaultSegmentSize) / 2,
                spacing = spacing
            ) - viewState.horizontalScroll

            entryCollection.forEachIn(model.drawMinX..model.drawMaxX) { entry ->
                height = entry.y * heightMultiplier
                columnCenterX = drawingStart +
                        (segmentSize + spacing) * (entry.x - model.minX) / model.step

                when (mergeMode) {
                    MergeMode.Stack -> {
                        val cumulatedHeight = heightMap.getOrElse(entry.x) { 0f }
                        columnBottom = (bounds.bottom + bottomCompensation - cumulatedHeight)
                            .between(bounds.top, bounds.bottom)
                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        columnCenterX += segmentSize.half
                        heightMap[entry.x] = cumulatedHeight + height
                    }
                    MergeMode.Grouped -> {
                        columnBottom = (bounds.bottom + bottomCompensation)
                            .between(bounds.top, bounds.bottom)
                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        columnCenterX += column.scaledThickness.half
                    }
                }

                if (column.intersectsVertical(columnTop, columnBottom, columnCenterX, bounds)) {
                    if (viewState.markerTouchPoint != null) {
                        markerLocationMap.put(
                            x = ceil(columnCenterX),
                            y = columnTop.between(bounds.top, bounds.bottom),
                            entry = entry,
                            color = column.color
                        )
                    }

                    column.drawVertical(canvas, columnTop, columnBottom, columnCenterX)
                }
            }
        }
        heightMap.clear()
    }

    override fun setToAxisModel(axisModel: MutableDataSetModel, model: EntryModel) {
        axisModel.minY = minY ?: min(model.minY, 0f)
        axisModel.maxY = maxY ?: mergeMode.getMaxY(model)
        axisModel.minX = minX ?: model.minX
        axisModel.maxX = maxX ?: model.maxX
        axisModel.entryModel = model
    }

    override fun getSegmentProperties(model: EntryModel): SegmentProperties {
        calculateDrawSegmentSpecIfNeeded(model)
        return segmentProperties.apply {
            contentWidth = getSegmentSize(entryCollectionSize = model.entryCollections.size)
            marginWidth = scaledSpacing
        }
    }

    private fun getSegmentSize(entryCollectionSize: Int, scaled: Boolean = true): Float =
        when (mergeMode) {
            MergeMode.Stack -> columns.maxOf { if (scaled) it.scaledThickness else it.thickness }
            MergeMode.Grouped -> {
                val innerSpacing = if (scaled) scaledInnerSpacing else innerSpacing
                getCumulatedThickness(entryCollectionSize, scaled) +
                        (innerSpacing * (entryCollectionSize - 1))
            }
        }

    private fun getDrawingStart(
        entryCollectionIndex: Int,
        segmentCompensation: Float,
        spacing: Float,
    ): Float {
        val baseLeft = bounds.left + spacing.half + segmentCompensation
        return when (mergeMode) {
            MergeMode.Stack -> baseLeft
            MergeMode.Grouped -> baseLeft + (getCumulatedThickness(entryCollectionIndex, true) +
                    (scaledInnerSpacing * entryCollectionIndex))
        }
    }

    private fun getCumulatedThickness(
        count: Int,
        scaled: Boolean,
    ): Float {
        var thickness = 0f
        for (i in 0 until count) {
            thickness += if (scaled) columns.getRepeating(i).scaledThickness
            else columns.getRepeating(i).thickness
        }
        return thickness
    }

    private fun calculateDrawSegmentSpecIfNeeded(model: EntryModel) {
        if (isScaleCalculated) return
        val measuredWidth = getMeasuredWidth(model)
        if (isHorizontalScrollEnabled) {
            drawScale = zoom ?: 1f
            maxScrollAmount = maxOf(0f, measuredWidth * drawScale - bounds.width())
        } else {
            maxScrollAmount = 0f
            drawScale = minOf(bounds.width() / measuredWidth, 1f)
        }
        columns.forEach { column -> column.thicknessScale = drawScale }
        scaledSpacing = spacing * drawScale
        scaledInnerSpacing = innerSpacing * drawScale
        isScaleCalculated = true
    }

    private fun updateMaxScrollAmount(
        entryCollectionSize: Int,
        segmentWidth: Float,
    ) {
        maxScrollAmount = if (isHorizontalScrollEnabled) maxOf(
            a = 0f,
            b = (segmentWidth * entryCollectionSize) - bounds.width()
        ) else 0f
    }
}
