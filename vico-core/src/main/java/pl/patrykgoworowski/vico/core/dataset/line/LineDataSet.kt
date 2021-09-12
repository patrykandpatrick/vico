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
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.constants.DEF_LINE_CHART_SPACING
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.forEachIn
import pl.patrykgoworowski.vico.core.dataset.put
import pl.patrykgoworowski.vico.core.dataset.renderer.BaseDataSet
import pl.patrykgoworowski.vico.core.dataset.renderer.RendererViewState
import pl.patrykgoworowski.vico.core.dataset.segment.MutableSegmentProperties
import pl.patrykgoworowski.vico.core.dataset.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.entry.DataEntry
import pl.patrykgoworowski.vico.core.extension.between
import pl.patrykgoworowski.vico.core.extension.dp
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.extension.set
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.path.horizontalCubicTo
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

public open class LineDataSet(
    var point: Component? = null,
    var pointSize: Float = 6f.dp,
    var spacing: Float = DEF_LINE_CHART_SPACING.dp,
    lineWidth: Float = 2.dp,
    lineColor: Int = Color.LTGRAY,
) : BaseDataSet<EntryModel>() {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = lineWidth
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

    private var drawScale: Float = 1f
    private var isScaleCalculated = false

    override var maxScrollAmount: Float = 0f
    override var zoom: Float? = null
        set(value) {
            field = value
            isScaleCalculated = false
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
        viewState: RendererViewState
    ) {
        resetTempData()
        val lineBackgroundShader = lineBackgroundShader

        val clipRestoreCount = canvas.save()
        canvas.setUpClipBounds()

        calculateDrawSegmentSpecIfNeeded(model)
        updateMaxScrollAmount(model.getEntriesLength(), segmentProperties.segmentWidth)

        var cubicCurvature: Float
        var prevX = bounds.left
        var prevY = bounds.bottom

        val (segmentSize, spacing) = segmentProperties

        val drawingStart = bounds.left + spacing.half - viewState.horizontalScroll + segmentSize.half

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

            if (viewState.markerTouchPoint != null) {
                markerLocationMap.put(
                    x = ceil(x),
                    y = y.between(bounds.top, bounds.bottom),
                    entry = entry,
                    color = lineColor
                )
            }
        }

        if (lineBackgroundShader != null) {
            lineBackgroundPaint.shader = lineBackgroundShader.provideShader(bounds)
            lineBackgroundPath.lineTo(prevX, bounds.bottom)
            lineBackgroundPath.close()
            canvas.drawPath(lineBackgroundPath, lineBackgroundPaint)
        }
        canvas.drawPath(linePath, linePaint)

        point?.let { point ->
            model.forEachPoint(segmentProperties, drawingStart) { _, x, y ->
                point.drawPoint(canvas, x, y, pointSize.half)
            }
        }
        canvas.restoreToCount(clipRestoreCount)
    }

    private fun Canvas.setUpClipBounds() {
        clipRect(
            bounds.left,
            bounds.top - pointSize.half,
            bounds.right,
            bounds.bottom + pointSize.half
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
                x = drawingStart + (segment.contentWidth + segment.marginWidth) *
                        (entry.x - drawMinX) / step
                y = bounds.bottom - entry.y * heightMultiplier
                action(entry, x, y)
            }
        }
    }

    override fun getMeasuredWidth(model: EntryModel): Int {
        val length = model.getEntriesLength()
        val segmentWidth = pointSize
        return (segmentWidth * length + spacing * length).roundToInt()
    }

    override fun getSegmentProperties(model: EntryModel): SegmentProperties {
        calculateDrawSegmentSpecIfNeeded(model)
        return segmentProperties.apply {
            contentWidth = pointSize * drawScale
            marginWidth = spacing * drawScale
        }
    }

    override fun setToAxisModel(axisModel: MutableDataSetModel, model: EntryModel) {
        axisModel.minY = minY ?: min(model.minY, 0f)
        axisModel.maxY = maxY ?: model.maxY
        axisModel.minX = minX ?: model.minX
        axisModel.maxX = maxX ?: model.maxX
        axisModel.entryModel = model
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

    companion object {
        private const val CUBIC_Y_MULTIPLIER = 4
    }
}
