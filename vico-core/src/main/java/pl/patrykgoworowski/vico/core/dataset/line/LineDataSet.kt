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

package pl.patrykgoworowski.vico.core.dataset.line

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import pl.patrykgoworowski.vico.core.axis.model.MutableDataSetModel
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.shape.extension.horizontalCubicTo
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.constants.DEF_LINE_CHART_SPACING
import pl.patrykgoworowski.vico.core.dataset.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.forEachIn
import pl.patrykgoworowski.vico.core.dataset.put
import pl.patrykgoworowski.vico.core.dataset.renderer.BaseDataSet
import pl.patrykgoworowski.vico.core.dataset.segment.MutableSegmentProperties
import pl.patrykgoworowski.vico.core.dataset.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.entry.DataEntry
import pl.patrykgoworowski.vico.core.extension.between
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.extension.set
import pl.patrykgoworowski.vico.core.layout.MeasureContext
import pl.patrykgoworowski.vico.core.marker.Marker
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

public open class LineDataSet(
    public var point: Component? = null,
    public var pointSizeDp: Float = 6f,
    public var spacingDp: Float = DEF_LINE_CHART_SPACING,
    public var lineWidthDp: Float = 2f,
    lineColor: Int = Color.LTGRAY,
) : BaseDataSet<EntryModel>() {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = lineColor
        strokeCap = Paint.Cap.ROUND
    }
    private val lineBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePath = Path()
    private val lineBackgroundPath = Path()

    private val segmentProperties = MutableSegmentProperties()

    override val markerLocationMap = HashMap<Float, MutableList<Marker.EntryModel>>()

    public var lineColor: Int by linePaint::color
    public var lineWidth: Float by linePaint::strokeWidth
    public var lineBackgroundShader: DynamicShader? = null
    public var lineStrokeCap: Paint.Cap by linePaint::strokeCap

    public var cubicStrength = 1f

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
        context: ChartDrawContext,
        model: EntryModel,
    ) = with(context) {
        resetTempData()
        val lineBackgroundShader = lineBackgroundShader
        linePaint.strokeWidth = lineWidthDp.pixels

        val clipRestoreCount = context.canvas.save()
        canvas.setUpClipBounds()

        val (cellWidth, spacing, segmentWidth) = segmentProperties

        context.calculateDrawSegmentSpecIfNeeded(model)
        updateMaxScrollAmount(model.getEntriesLength(), segmentWidth)

        var cubicCurvature: Float
        var prevX = bounds.left
        var prevY = bounds.bottom

        val drawingStart = bounds.left + spacing.half - horizontalScroll + cellWidth.half

        model.forEachPoint(segmentProperties, drawingStart) { entry, x, y ->
            if (linePath.isEmpty) {
                linePath.moveTo(x, y)
                if (lineBackgroundShader != null) {
                    lineBackgroundPath.moveTo(x, bounds.bottom)
                    lineBackgroundPath.lineTo(x, y)
                }
            } else {
                cubicCurvature = spacing * cubicStrength *
                        min(1f, abs((y - prevY) / bounds.bottom) * CUBIC_Y_MULTIPLIER)
                linePath.horizontalCubicTo(prevX, prevY, x, y, cubicCurvature)
                if (lineBackgroundShader != null) {
                    lineBackgroundPath.horizontalCubicTo(prevX, prevY, x, y, cubicCurvature)
                }
            }
            prevX = x
            prevY = y

            if (markerTouchPoint != null) {
                markerLocationMap.put(
                    x = ceil(x),
                    y = y.between(bounds.top, bounds.bottom),
                    entry = entry,
                    color = lineColor
                )
            }
        }

        if (lineBackgroundShader != null) {
            lineBackgroundPaint.shader = lineBackgroundShader.provideShader(context, bounds)
            lineBackgroundPath.lineTo(prevX, bounds.bottom)
            lineBackgroundPath.close()
            canvas.drawPath(lineBackgroundPath, lineBackgroundPaint)
        }
        canvas.drawPath(linePath, linePaint)

        point?.let { point ->
            model.forEachPoint(segmentProperties, drawingStart) { _, x, y ->
                point.drawPoint(context, x, y, pointSizeDp.pixels.half)
            }
        }
        canvas.restoreToCount(clipRestoreCount)
    }

    private fun Canvas.setUpClipBounds() {
        clipRect(
            bounds.left,
            bounds.top - pointSizeDp.half,
            bounds.right,
            bounds.bottom + pointSizeDp.half
        )
    }

    private fun resetTempData() {
        markerLocationMap.clear()
        linePath.rewind()
        lineBackgroundPath.rewind()
    }

    private inline fun EntryModel.forEachPoint(
        segment: SegmentProperties,
        drawingStart: Float,
        action: (entry: DataEntry, x: Float, y: Float) -> Unit,
    ) {
        var x: Float
        var y: Float
        val heightMultiplier = bounds.height() / (drawMaxY - this@LineDataSet.minY.orZero)

        entryCollections.forEach { collection ->
            collection.forEachIn((drawMinX - step)..(drawMaxX + step)) { entry ->
                x = drawingStart + (segment.cellWidth + segment.marginWidth) *
                        (entry.x - drawMinX) / step
                y = bounds.bottom - entry.y * heightMultiplier
                action(entry, x, y)
            }
        }
    }

    override fun getMeasuredWidth(context: MeasureContext, model: EntryModel): Int = with(context) {
        val length = model.getEntriesLength()
        (pointSizeDp.pixels * length + spacingDp.pixels * length).roundToInt()
    }

    override fun getSegmentProperties(
        context: MeasureContext,
        model: EntryModel,
    ): SegmentProperties = with(context) {
        context.calculateDrawSegmentSpecIfNeeded(model)
        segmentProperties.apply {
            cellWidth = pointSizeDp.pixels * drawScale
            marginWidth = spacingDp.pixels * drawScale
        }
    }

    override fun setToAxisModel(axisModel: MutableDataSetModel, model: EntryModel) {
        axisModel.minY = minY ?: min(model.minY, 0f)
        axisModel.maxY = maxY ?: model.maxY
        axisModel.minX = minX ?: model.minX
        axisModel.maxX = maxX ?: model.maxX
        axisModel.entryModel = model
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

    companion object {
        private const val CUBIC_Y_MULTIPLIER = 4
    }
}
