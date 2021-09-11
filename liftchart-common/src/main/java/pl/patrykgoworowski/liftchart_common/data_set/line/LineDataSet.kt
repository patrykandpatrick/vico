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

package pl.patrykgoworowski.liftchart_common.data_set.line

import android.graphics.*
import pl.patrykgoworowski.liftchart_common.axis.model.MutableDataSetModel
import pl.patrykgoworowski.liftchart_common.component.Component
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader
import pl.patrykgoworowski.liftchart_common.constants.DEF_LINE_CHART_SPACING
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.BaseDataSet
import pl.patrykgoworowski.liftchart_common.data_set.renderer.RendererViewState
import pl.patrykgoworowski.liftchart_common.data_set.segment.MutableSegmentProperties
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import pl.patrykgoworowski.liftchart_common.extension.*
import pl.patrykgoworowski.liftchart_common.marker.Marker
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
    private val linePath = Path()
    override val markerLocationMap = HashMap<Float, MutableList<Marker.EntryModel>>()

    private val lineBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val lineBackgroundPath = Path()

    private val segmentProperties = MutableSegmentProperties()

    private val scaledSpacing: Float
        get() = spacing * drawScale

    private val scaledPointSize: Float
        get() = pointSize * drawScale

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
        rendererViewState: RendererViewState
    ) {
        markerLocationMap.clear()
        if (model.entryCollections.isEmpty()) return
        val (touchPoint, scrollX) = rendererViewState
        linePath.rewind()
        lineBackgroundPath.rewind()

        val lineBackgroundShader = lineBackgroundShader

        val clipRestoreCount = canvas.save()
        canvas.clipRect(
            bounds.left,
            bounds.top - pointSize.half,
            bounds.right,
            bounds.bottom + pointSize.half
        )

        calculateDrawSegmentSpecIfNeeded(model)
        updateMaxScrollAmount(model.getEntriesLength(), segmentProperties.segmentWidth)

        var cubicCurvature: Float
        val minYorZero = minY ?: 0f
        val minX = minX ?: model.minX
        val maxX = maxX ?: model.maxX
        val maxY = maxY ?: model.maxY
        var prevX = bounds.left
        var prevY = bounds.bottom

        val heightMultiplier = bounds.height() / (maxY - minYorZero)

        val (segmentSize, spacing) = segmentProperties

        val drawingStart = bounds.left + spacing.half - scrollX + segmentSize.half

        forEachPoint(
            model = model,
            segmentSize = segmentSize,
            spacing = spacing,
            drawingStart = drawingStart,
            heightMultiplier = heightMultiplier,
            minX = minX,
            maxX = maxX,
            step = model.step,
        ) { entry, x, y ->
            if (linePath.isEmpty) {
                linePath.moveTo(x, y)
                if (lineBackgroundShader != null) {
                    lineBackgroundPath.moveTo(x, bounds.bottom)
                    lineBackgroundPath.lineTo(x, y)
                }
            } else {
                cubicCurvature = spacing * cubicStrength *
                        min(1f, abs((y - prevY) / bounds.bottom) * 4)
                linePath.cubicTo(prevX + cubicCurvature, prevY, x - cubicCurvature, y, x, y)
                if (lineBackgroundShader != null) {
                    lineBackgroundPath.cubicTo(
                        prevX + cubicCurvature,
                        prevY, x - cubicCurvature, y, x, y
                    )
                }
            }
            prevX = x
            prevY = y

            if (touchPoint != null) {
                markerLocationMap.updateList(ceil(x)) {
                    add(
                        Marker.EntryModel(
                            PointF(ceil(x), y.between(bounds.top, bounds.bottom)),
                            entry,
                            linePaint.color,
                        )
                    )
                }
            }
        }

        if (lineBackgroundShader != null) {
            lineBackgroundPaint.shader = lineBackgroundShader.provideShader(bounds)
            lineBackgroundPath.lineTo(prevX, bounds.bottom)
            lineBackgroundPath.close()
            canvas.drawPath(lineBackgroundPath, lineBackgroundPaint)
        }
        canvas.drawPath(linePath, linePaint)

        val point = point

        if (point != null) {
            forEachPoint(
                model = model,
                segmentSize = segmentSize,
                spacing = spacing,
                drawingStart = drawingStart,
                heightMultiplier = heightMultiplier,
                minX = minX,
                maxX = maxX,
                step = model.step,
            ) { _, x, y ->
                point.draw(
                    canvas = canvas,
                    left = x - pointSize.half,
                    top = y - pointSize.half,
                    right = x + pointSize.half,
                    bottom = y + pointSize.half,
                )
            }
        }

        canvas.restoreToCount(clipRestoreCount)
    }

    private inline fun forEachPoint(
        model: EntryModel,
        segmentSize: Float,
        spacing: Float,
        drawingStart: Float,
        heightMultiplier: Float,
        minX: Float,
        maxX: Float,
        step: Float,
        action: (entry: DataEntry, x: Float, y: Float) -> Unit,
    ) {
        var x: Float
        var y: Float

        model.entryCollections.forEach { collection ->
            collection.forEach forEach2@{ entry ->
                if (entry.x !in (minX - step)..(maxX + step)) return@forEach2

                x = drawingStart +
                        (segmentSize + spacing) * (entry.x - minX) / model.step
                y = bounds.bottom - entry.y * heightMultiplier
                action(entry, x, y)
            }
        }
    }

    override fun drawMarker(
        canvas: Canvas,
        model: EntryModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
        marker: Marker?
    ) {
        val touchPoint = rendererViewState.markerTouchPoint
        if (touchPoint == null || marker == null) return
        markerLocationMap.getClosestMarkerEntryPositionModel(touchPoint)?.let { markerModel ->
            marker.draw(
                canvas,
                bounds,
                markerModel,
            )
        }
    }

    override fun getMeasuredWidth(model: EntryModel): Int {
        val length = model.getEntriesLength()
        val segmentWidth = getSegmentSize(false)
        return (segmentWidth * length + spacing * length).roundToInt()
    }

    private fun getSegmentSize(scaled: Boolean = true): Float =
        if (scaled) scaledPointSize else pointSize

    override fun getSegmentProperties(model: EntryModel): SegmentProperties {
        calculateDrawSegmentSpecIfNeeded(model)
        return segmentProperties.apply {
            contentWidth = getSegmentSize()
            marginWidth = scaledSpacing
        }
    }

    override fun setToAxisModel(axisModel: MutableDataSetModel, model: EntryModel) {
        axisModel.minY = minY ?: min(model.minY, 0f)
        axisModel.maxY = maxY ?: model.maxY
        axisModel.minX = minX ?: model.minX
        axisModel.maxX = maxX ?: model.maxX
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
}
