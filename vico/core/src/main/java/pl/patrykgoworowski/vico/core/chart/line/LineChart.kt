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

package pl.patrykgoworowski.vico.core.chart.line

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import pl.patrykgoworowski.vico.core.DefaultDimens
import pl.patrykgoworowski.vico.core.axis.model.MutableChartModel
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.shape.extension.horizontalCubicTo
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.chart.forEachIn
import pl.patrykgoworowski.vico.core.chart.put
import pl.patrykgoworowski.vico.core.chart.BaseChart
import pl.patrykgoworowski.vico.core.chart.segment.MutableSegmentProperties
import pl.patrykgoworowski.vico.core.chart.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.entry.ChartEntry
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.context.MeasureContext
import pl.patrykgoworowski.vico.core.marker.Marker
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min

/**
 * [LineChart] displays data as a continuous line.
 *
 * @param point the optional [Component] that can be drawn at given x,y coordinate above the line.
 * @param pointSizeDp the size in dp unit of the [point].
 * @param spacingDp the spacing in dp unit between each [point].
 * @param lineThicknessDp the thickness of the line in dp unit.
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
     * The width of the line in dp.
     */
    public var lineWidth: Float by linePaint::strokeWidth

    /**
     * The optional [DynamicShader] that can style a space between bottom of the line and bottom of the chart.
     */
    public var lineBackgroundShader: DynamicShader? = null

    /**
     * The stroke cap for the line.
     */
    public var lineStrokeCap: Paint.Cap by linePaint::strokeCap

    /**
     * The strength of cubic bezier curve between each point of data entry.
     */
    public var cubicStrength: Float = 1f

    override fun drawChart(
        context: ChartDrawContext,
        model: ChartEntryModel,
    ): Unit = with(context) {
        resetTempData()
        val lineBackgroundShader = lineBackgroundShader
        linePaint.strokeWidth = lineThicknessDp.pixels

        val clipRestoreCount = saveCanvas()
        setUpClipBounds()

        val (cellWidth, spacing, _) = segmentProperties

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

            entryLocationMap.put(
                x = ceil(x),
                y = y.coerceIn(bounds.top, bounds.bottom),
                entry = entry,
                color = lineColor
            )
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
            model.forEachPoint(segmentProperties, drawingStart) { _, x, y ->
                point.drawPoint(context, x, y, pointSizeDp.pixels.half)
            }
        }
        canvas.restoreToCount(clipRestoreCount)
    }

    private fun ChartDrawContext.setUpClipBounds() {
        clipRect(
            left = bounds.left,
            top = bounds.top - pointSizeDp.pixels.half,
            right = bounds.right,
            bottom = bounds.bottom + pointSizeDp.pixels.half
        )
    }

    private fun resetTempData() {
        entryLocationMap.clear()
        linePath.rewind()
        lineBackgroundPath.rewind()
    }

    private inline fun ChartEntryModel.forEachPoint(
        segment: SegmentProperties,
        drawingStart: Float,
        action: (entry: ChartEntry, x: Float, y: Float) -> Unit,
    ) {
        var x: Float
        var y: Float
        val heightMultiplier = bounds.height() / (drawMaxY - this@LineChart.minY.orZero)

        entries.forEach { collection ->
            collection.forEachIn((drawMinX - stepX)..(drawMaxX + stepX)) { entry ->
                x = drawingStart + (segment.cellWidth + segment.marginWidth) *
                    (entry.x - drawMinX) / stepX
                y = bounds.bottom - entry.y * heightMultiplier
                action(entry, x, y)
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
