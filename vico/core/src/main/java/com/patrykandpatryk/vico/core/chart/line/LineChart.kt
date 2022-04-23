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

package com.patrykandpatryk.vico.core.chart.line

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.axis.model.MutableChartModel
import com.patrykandpatryk.vico.core.chart.BaseChart
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.chart.forEachIn
import com.patrykandpatryk.vico.core.chart.put
import com.patrykandpatryk.vico.core.chart.segment.MutableSegmentProperties
import com.patrykandpatryk.vico.core.chart.segment.SegmentProperties
import com.patrykandpatryk.vico.core.component.Component
import com.patrykandpatryk.vico.core.component.shape.extension.horizontalCubicTo
import com.patrykandpatryk.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatryk.vico.core.context.MeasureContext
import com.patrykandpatryk.vico.core.entry.ChartEntry
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.extension.half
import com.patrykandpatryk.vico.core.extension.orZero
import com.patrykandpatryk.vico.core.marker.Marker
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min

/**
 * [LineChart] displays data as a continuous line.
 *
 * @param point an optional [Component] that can be drawn at a given point above the line.
 * @param pointSizeDp the size of the [point] in dp.
 * @param spacingDp the spacing between each [point] in dp.
 * @param lineThicknessDp the thickness of the line in dp.
 * @param lineColor the color of the line.
 */
public open class LineChart(
    public var point: Component? = null,
    public var pointSizeDp: Float = DefaultDimens.POINT_SIZE,
    public var spacingDp: Float = DefaultDimens.POINT_SPACING,
    public var lineThicknessDp: Float = DefaultDimens.LINE_THICKNESS,
    lineColor: Int = Color.LTGRAY,
) : BaseChart<ChartEntryModel>() {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = lineColor
        strokeCap = Paint.Cap.ROUND
    }
    private val lineBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePath = Path()
    private val lineBackgroundPath = Path()

    private val segmentProperties = MutableSegmentProperties()

    override val entryLocationMap: HashMap<Float, MutableList<Marker.EntryModel>> = HashMap()

    /**
     * The color of the line.
     */
    public var lineColor: Int by linePaint::color

    /**
     * An optional [DynamicShader] that can style the space between the line and the x-axis.
     */
    public var lineBackgroundShader: DynamicShader? = null

    /**
     * The stroke cap for the line.
     */
    public var lineStrokeCap: Paint.Cap by linePaint::strokeCap

    /**
     * The strength of the cubic bezier curve between each key point on the line.
     */
    public var cubicStrength: Float = 1f

    override fun drawChart(
        context: ChartDrawContext,
        model: ChartEntryModel,
    ): Unit = with(context) {
        resetTempData()
        val lineBackgroundShader = lineBackgroundShader
        linePaint.strokeWidth = lineThicknessDp.pixels

        val (cellWidth, spacing, _) = segmentProperties

        var cubicCurvature: Float
        var prevX = bounds.left
        var prevY = bounds.bottom

        val drawingStart = bounds.left + spacing.half - horizontalScroll + cellWidth.half

        model.forEachPointWithinBounds(segmentProperties, drawingStart) { entry, x, y ->
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

            if (x in bounds.left..bounds.right) {
                entryLocationMap.put(
                    x = ceil(x),
                    y = y.coerceIn(bounds.top, bounds.bottom),
                    entry = entry,
                    color = lineColor
                )
            }
        }

        if (lineBackgroundShader != null) {
            lineBackgroundPaint.shader = lineBackgroundShader
                .provideShader(context, bounds.left, bounds.top, bounds.right, bounds.bottom)
            lineBackgroundPath.lineTo(prevX, bounds.bottom)
            lineBackgroundPath.close()
            canvas.drawPath(lineBackgroundPath, lineBackgroundPaint)
        }
        canvas.drawPath(linePath, linePaint)

        point?.let { point ->
            model.forEachPointWithinBounds(segmentProperties, drawingStart) { _, x, y ->
                point.drawPoint(context, x, y, pointSizeDp.pixels.half)
            }
        }
    }

    private fun resetTempData() {
        entryLocationMap.clear()
        linePath.rewind()
        lineBackgroundPath.rewind()
    }

    private fun ChartEntryModel.forEachPointWithinBounds(
        segment: SegmentProperties,
        drawingStart: Float,
        action: (entry: ChartEntry, x: Float, y: Float) -> Unit,
    ) {
        var x: Float
        var y: Float

        var prevEntry: ChartEntry? = null
        var lastEntry: ChartEntry? = null

        val boundsStart = bounds.left
        val boundsEnd = bounds.left + bounds.width()

        val heightMultiplier = bounds.height() / (drawMaxY - drawMinY)

        fun getDrawX(entry: ChartEntry): Float =
            drawingStart + (segment.cellWidth + segment.marginWidth) * (entry.x - drawMinX) / stepX

        fun getDrawY(entry: ChartEntry): Float =
            bounds.bottom - (minY - drawMinY + entry.y) * heightMultiplier

        entries.firstOrNull()?.forEachIn(drawMinX - stepX..drawMaxX + stepX) { entry ->
            x = getDrawX(entry)
            y = getDrawY(entry)
            when {
                x < boundsStart -> {
                    prevEntry = entry
                }
                x in boundsStart..boundsEnd -> {
                    prevEntry?.also {
                        action(it, getDrawX(it), getDrawY(it))
                        prevEntry = null
                    }
                    action(entry, x, y)
                }
                x > boundsEnd && lastEntry == null -> {
                    action(entry, x, y)
                    lastEntry = entry
                }
            }
        }
    }

    override fun getSegmentProperties(
        context: MeasureContext,
        model: ChartEntryModel,
    ): SegmentProperties = with(context) {
        segmentProperties.set(cellWidth = pointSizeDp.pixels, marginWidth = spacingDp.pixels)
    }

    override fun setToChartModel(chartModel: MutableChartModel, model: ChartEntryModel) {
        chartModel.minY = minY ?: min(model.minY, 0f)
        chartModel.maxY = maxY ?: model.maxY
        chartModel.minX = minX ?: model.minX
        chartModel.maxX = maxX ?: model.maxX
        chartModel.chartEntryModel = model
    }

    private companion object {
        const val CUBIC_Y_MULTIPLIER = 4
    }
}
