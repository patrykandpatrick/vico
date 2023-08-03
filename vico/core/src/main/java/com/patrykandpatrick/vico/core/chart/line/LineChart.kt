/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart.line

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.chart.BaseChart
import com.patrykandpatrick.vico.core.chart.DefaultPointConnector
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.forEachInRelativelyIndexed
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec
import com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec.PointConnector
import com.patrykandpatrick.vico.core.chart.put
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.component.text.inBounds
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.doubled
import com.patrykandpatrick.vico.core.extension.getRepeating
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.rangeWith
import com.patrykandpatrick.vico.core.formatter.DecimalFormatValueFormatter
import com.patrykandpatrick.vico.core.formatter.ValueFormatter
import com.patrykandpatrick.vico.core.marker.Marker
import kotlin.math.max
import kotlin.math.min

/**
 * [LineChart] displays data as a continuous line.
 *
 * @param lines a [List] of [LineSpec]s defining the style of each line.
 * @param spacingDp the spacing between each [LineSpec.point] (in dp).
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 */
public open class LineChart(
    public var lines: List<LineSpec> = listOf(LineSpec()),
    public var spacingDp: Float = DefaultDimens.POINT_SPACING,
    public var targetVerticalAxisPosition: AxisPosition.Vertical? = null
) : BaseChart<ChartEntryModel>() {

    /**
     * Creates a [LineChart] with a common style for all lines.
     *
     * @param line a [LineSpec] defining the style of each line.
     * @param spacingDp the spacing between each [LineSpec.point] (in dp).
     * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
     * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
     */
    public constructor(
        line: LineSpec,
        spacingDp: Float,
        targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    ) : this(listOf(line), spacingDp, targetVerticalAxisPosition)

    /**
     * Defines the appearance of a line in a line chart.
     *
     * @param lineColor the color of the line.
     * @param lineThicknessDp the thickness of the line (in dp).
     * @param lineBackgroundShader an optional [DynamicShader] to use for the area below the line.
     * @param lineCap the stroke cap for the line.
     * @param point an optional [Component] that can be drawn at a given point on the line.
     * @param pointSizeDp the size of the [point] (in dp).
     * @param dataLabel an optional [TextComponent] to use for data labels.
     * @param dataLabelVerticalPosition the vertical position of data labels relative to the line.
     * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
     * @param dataLabelRotationDegrees the rotation of data labels (in degrees).
     * @param pointConnector the [PointConnector] for the line.
     */
    public open class LineSpec(
        lineColor: Int = Color.LTGRAY,
        public var lineThicknessDp: Float = DefaultDimens.LINE_THICKNESS,
        public var lineBackgroundShader: DynamicShader? = null,
        public var lineCap: Paint.Cap = Paint.Cap.ROUND,
        public var point: Component? = null,
        public var pointSizeDp: Float = DefaultDimens.POINT_SIZE,
        public var dataLabel: TextComponent? = null,
        public var dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
        public var dataLabelValueFormatter: ValueFormatter = DecimalFormatValueFormatter(),
        public var dataLabelRotationDegrees: Float = 0f,
        public var pointConnector: PointConnector = DefaultPointConnector(),
    ) {

        /**
         * Defines the appearance of a line in a line chart.
         *
         * @param lineColor the color of the line.
         * @param lineThicknessDp the thickness of the line (in dp).
         * @param lineBackgroundShader an optional [DynamicShader] to use for the area below the line.
         * @param lineCap the stroke cap for the line.
         * @param cubicStrength the strength of the cubic bezier curve between each point on the line.
         * @param point an optional [Component] that can be drawn at a given point on the line.
         * @param pointSizeDp the size of the [point] (in dp).
         * @param dataLabel an optional [TextComponent] to use for data labels.
         * @param dataLabelVerticalPosition the vertical position of data labels relative to the line.
         * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
         * @param dataLabelRotationDegrees the rotation of data labels in degrees.
         */
        @Deprecated(
            message = """Rather than using this constructor and its `cubicStrength` parameter, use the primary
                constructor and provide a `DefaultPointConnector` instance with a custom `cubicStrength` via the
                `pointConnector` parameter.""",
            replaceWith = ReplaceWith(
                expression = """LineSpec(
                        lineColor = lineColor,
                        lineThicknessDp = lineThicknessDp,
                        lineBackgroundShader = lineBackgroundShader,
                        lineCap = lineCap,
                        point = point,
                        pointSizeDp = pointSizeDp,
                        dataLabel = dataLabel,
                        dataLabelVerticalPosition = dataLabelVerticalPosition,
                        dataLabelValueFormatter = dataLabelValueFormatter,
                        dataLabelRotationDegrees = dataLabelRotationDegrees,
                        pointConnector = DefaultPointConnector(cubicStrength = cubicStrength),
                    )""",
                imports = arrayOf("com.patrykandpatrick.vico.core.chart.DefaultPointConnector"),
            ),
            level = DeprecationLevel.ERROR,
        )
        public constructor(
            lineColor: Int = Color.LTGRAY,
            lineThicknessDp: Float = DefaultDimens.LINE_THICKNESS,
            lineBackgroundShader: DynamicShader? = null,
            lineCap: Paint.Cap = Paint.Cap.ROUND,
            cubicStrength: Float,
            point: Component? = null,
            pointSizeDp: Float = DefaultDimens.POINT_SIZE,
            dataLabel: TextComponent? = null,
            dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
            dataLabelValueFormatter: ValueFormatter = DecimalFormatValueFormatter(),
            dataLabelRotationDegrees: Float = 0f,
        ) : this(
            lineColor = lineColor,
            lineThicknessDp = lineThicknessDp,
            lineBackgroundShader = lineBackgroundShader,
            lineCap = lineCap,
            point = point,
            pointSizeDp = pointSizeDp,
            dataLabel = dataLabel,
            dataLabelVerticalPosition = dataLabelVerticalPosition,
            dataLabelValueFormatter = dataLabelValueFormatter,
            dataLabelRotationDegrees = dataLabelRotationDegrees,
            pointConnector = DefaultPointConnector(cubicStrength = cubicStrength),
        )

        /**
         * Returns `true` if the [lineBackgroundShader] is not null, and `false` otherwise.
         */
        public val hasLineBackgroundShader: Boolean
            get() = lineBackgroundShader != null

        protected val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = lineColor
            strokeCap = lineCap
        }

        protected val lineBackgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        /**
         * The color of the line.
         */
        public var lineColor: Int by linePaint::color

        /**
         * The stroke cap for the line.
         */
        public var lineStrokeCap: Paint.Cap by linePaint::strokeCap

        /**
         * Draws a [point] at the given [x] and [y] coordinates, using the provided [context].
         *
         * @see Component
         */
        public fun drawPoint(
            context: DrawContext,
            x: Float,
            y: Float,
        ): Unit = with(context) {
            point?.drawPoint(context, x, y, pointSizeDp.pixels.half)
        }

        /**
         * Draws the line.
         */
        public fun drawLine(context: DrawContext, path: Path): Unit = with(context) {
            linePaint.strokeWidth = lineThicknessDp.pixels
            canvas.drawPath(path, linePaint)
        }

        /**
         * Draws the line background.
         */
        public fun drawBackgroundLine(context: DrawContext, bounds: RectF, path: Path): Unit = with(context) {
            lineBackgroundPaint.shader = lineBackgroundShader
                ?.provideShader(
                    context = context,
                    left = bounds.left,
                    top = bounds.top,
                    right = bounds.right,
                    bottom = bounds.bottom,
                )

            canvas.drawPath(path, lineBackgroundPaint)
        }

        /**
         * Defines the shape of a line in a line chart by specifying how points are to be connected.
         *
         * @see DefaultPointConnector
         */
        public interface PointConnector {

            /**
             * Draws a line between two points.
             */
            public fun connect(
                path: Path,
                prevX: Float,
                prevY: Float,
                x: Float,
                y: Float,
                horizontalDimensions: HorizontalDimensions,
                bounds: RectF,
            )
        }
    }

    /**
     * The [Path] used to draw the lines, each of which corresponds to a [LineSpec].
     */
    protected val linePath: Path = Path()

    /**
     * The [Path] used to draw the backgrounds of the lines, each of which corresponds to a [LineSpec].
     */
    protected val lineBackgroundPath: Path = Path()

    /**
     * Holds information on the [LineChart]’s horizontal dimensions.
     */
    protected val horizontalDimensions: MutableHorizontalDimensions = MutableHorizontalDimensions()

    override val entryLocationMap: HashMap<Float, MutableList<Marker.EntryModel>> = HashMap()
    override var markerMoveWithPoint: Boolean = false

    override fun drawChart(
        context: ChartDrawContext,
        model: ChartEntryModel,
    ): Unit = with(context) {
        resetTempData()

        model.entries.forEachIndexed { entryListIndex, entries ->

            linePath.rewind()
            lineBackgroundPath.rewind()
            val component = lines.getRepeating(entryListIndex)

            var prevX = bounds.getStart(isLtr = isLtr)
            var prevY = bounds.bottom

            val drawingStartAlignmentCorrection = layoutDirectionMultiplier * horizontalDimensions.startPadding

            val drawingStart = bounds.getStart(isLtr = isLtr) + drawingStartAlignmentCorrection - horizontalScroll

            forEachPointWithinBoundsIndexed(
                entries = entries,
                drawingStart = drawingStart,
            ) { entryIndex, entry, x, y, _, _ ->
                if (linePath.isEmpty) {
                    linePath.moveTo(x, y)
                    if (component.hasLineBackgroundShader) {
                        lineBackgroundPath.moveTo(x, bounds.bottom)
                        lineBackgroundPath.lineTo(x, y)
                    }
                } else {
                    component.pointConnector.connect(
                        path = linePath,
                        prevX = prevX,
                        prevY = prevY,
                        x = x,
                        y = y,
                        horizontalDimensions = horizontalDimensions,
                        bounds = bounds,
                    )
                    if (component.hasLineBackgroundShader) {
                        component.pointConnector.connect(
                            path = lineBackgroundPath,
                            prevX = prevX,
                            prevY = prevY,
                            x = x,
                            y = y,
                            horizontalDimensions = horizontalDimensions,
                            bounds = bounds,
                        )
                    }
                }
                prevX = x
                prevY = y

                if (x in bounds.left..bounds.right) {
                    entryLocationMap.put(
                        x = x,
                        y = y.coerceIn(bounds.top, bounds.bottom),
                        entry = entry,
                        color = component.lineColor,
                        index = entryIndex,
                    )
                }
            }

            if (component.hasLineBackgroundShader) {
                lineBackgroundPath.lineTo(prevX, bounds.bottom)
                lineBackgroundPath.close()
                component.drawBackgroundLine(context, bounds, lineBackgroundPath)
            }
            component.drawLine(context, linePath)

            drawPointsAndDataLabels(
                lineSpec = component,
                entries = entries,
                drawingStart = drawingStart,
            )
        }
    }

    /**
     * Draws a line’s points ([LineSpec.point]) and their corresponding data labels ([LineSpec.dataLabel]).
     */
    protected open fun ChartDrawContext.drawPointsAndDataLabels(
        lineSpec: LineSpec,
        entries: List<ChartEntry>,
        drawingStart: Float,
    ) {
        if (lineSpec.point == null && lineSpec.dataLabel == null) return
        val chartValues = chartValuesManager.getChartValues(targetVerticalAxisPosition)

        forEachPointWithinBoundsIndexed(
            entries = entries,
            drawingStart = drawingStart,
        ) { _, chartEntry, x, y, previousX, nextX ->

            if (lineSpec.point != null) lineSpec.drawPoint(context = this, x = x, y = y)

            lineSpec.dataLabel.takeIf {
                horizontalLayout is HorizontalLayout.Segmented ||
                    chartEntry.x != chartValues.minX && chartEntry.x != chartValues.maxX ||
                    chartEntry.x == chartValues.minX && horizontalDimensions.startPadding > 0 ||
                    chartEntry.x == chartValues.maxX && horizontalDimensions.endPadding > 0
            }?.let { textComponent ->

                val distanceFromLine = maxOf(
                    a = lineSpec.lineThicknessDp,
                    b = lineSpec.pointSizeDpOrZero,
                ).half.pixels

                val text = lineSpec.dataLabelValueFormatter.formatValue(
                    value = chartEntry.y,
                    chartValues = chartValues,
                )
                val maxWidth = getMaxDataLabelWidth(chartEntry, x, previousX, nextX)
                val verticalPosition = lineSpec.dataLabelVerticalPosition.inBounds(
                    bounds = bounds,
                    distanceFromPoint = distanceFromLine,
                    componentHeight = textComponent.getHeight(
                        context = this,
                        text = text,
                        width = maxWidth,
                        rotationDegrees = lineSpec.dataLabelRotationDegrees,
                    ),
                    y = y,
                )
                val dataLabelY = y + when (verticalPosition) {
                    VerticalPosition.Top -> -distanceFromLine
                    VerticalPosition.Center -> 0f
                    VerticalPosition.Bottom -> distanceFromLine
                }
                textComponent.drawText(
                    context = this,
                    textX = x,
                    textY = dataLabelY,
                    text = text,
                    verticalPosition = verticalPosition,
                    maxTextWidth = maxWidth,
                    rotationDegrees = lineSpec.dataLabelRotationDegrees,
                )
            }
        }
    }

    protected fun ChartDrawContext.getMaxDataLabelWidth(
        entry: ChartEntry,
        x: Float,
        previousX: Float?,
        nextX: Float?,
    ): Int {
        val chartValues = chartValuesManager.getChartValues(targetVerticalAxisPosition)
        return when {
            previousX != null && nextX != null -> min(x - previousX, nextX - x)

            previousX == null && nextX == null ->
                min(horizontalDimensions.startPadding, horizontalDimensions.endPadding).doubled

            nextX != null -> {
                val extraSpace = when (horizontalLayout) {
                    is HorizontalLayout.Segmented -> horizontalDimensions.xSpacing.half
                    is HorizontalLayout.FullWidth -> horizontalDimensions.startPadding
                }
                ((entry.x - chartValues.minX) / chartValues.xStep * horizontalDimensions.xSpacing + extraSpace)
                    .doubled
                    .coerceAtMost(nextX - x)
            }

            else -> {
                val extraSpace = when (horizontalLayout) {
                    is HorizontalLayout.Segmented -> horizontalDimensions.xSpacing.half
                    is HorizontalLayout.FullWidth -> horizontalDimensions.endPadding
                }
                ((chartValues.maxX - entry.x) / chartValues.xStep * horizontalDimensions.xSpacing + extraSpace)
                    .doubled
                    .coerceAtMost(x - previousX!!)
            }
        }.toInt()
    }

    /**
     * Clears the temporary data saved during a single [drawChart] run.
     */
    protected fun resetTempData() {
        entryLocationMap.clear()
        linePath.rewind()
        lineBackgroundPath.rewind()
    }

    /**
     * Performs the given [action] for each [ChartEntry] in [entries] that lies within the chart’s bounds.
     */
    protected open fun ChartDrawContext.forEachPointWithinBoundsIndexed(
        entries: List<ChartEntry>,
        drawingStart: Float,
        action: (index: Int, entry: ChartEntry, x: Float, y: Float, previousX: Float?, nextX: Float?) -> Unit,
    ) {
        val chartValues = chartValuesManager.getChartValues(targetVerticalAxisPosition)

        val minX = chartValues.minX
        val maxX = chartValues.maxX
        val minY = chartValues.minY
        val maxY = chartValues.maxY
        val xStep = chartValues.xStep

        var x: Float = Float.NEGATIVE_INFINITY
        var nextX: Float? = null
        var y: Float

        var prevEntry: ChartEntry? = null
        var lastEntry: ChartEntry? = null

        val heightMultiplier = bounds.height() / (maxY - minY)

        val boundsStart = bounds.getStart(isLtr = isLtr)
        val boundsEnd = boundsStart + layoutDirectionMultiplier * bounds.width()

        fun getDrawX(entry: ChartEntry): Float = drawingStart + layoutDirectionMultiplier *
            horizontalDimensions.xSpacing * (entry.x - minX) / xStep

        fun getDrawY(entry: ChartEntry): Float =
            bounds.bottom - (entry.y - minY) * heightMultiplier

        entries.forEachInRelativelyIndexed(minX - xStep..maxX + xStep) { index, entry, next ->

            val previousX = x.takeIf { it.isFinite() }
            x = nextX ?: getDrawX(entry)
            nextX = next?.let(::getDrawX)
            y = getDrawY(entry)

            when {
                isLtr && x < boundsStart || isLtr.not() && x > boundsStart -> {
                    prevEntry = entry
                }

                x in boundsStart.rangeWith(other = boundsEnd) -> {
                    prevEntry?.also {
                        action(index, it, getDrawX(it), getDrawY(it), previousX, nextX)
                        prevEntry = null
                    }
                    action(index, entry, x, y, previousX, nextX)
                }

                (isLtr && x > boundsEnd || isLtr.not() && x < boundsEnd) && lastEntry == null -> {
                    action(index, entry, x, y, previousX, nextX)
                    lastEntry = entry
                }
            }
        }
    }

    override fun getHorizontalDimensions(
        context: MeasureContext,
        model: ChartEntryModel,
    ): HorizontalDimensions = with(context) {
        val maxPointSize = lines.maxOf { it.pointSizeDpOrZero }.pixels
        horizontalDimensions.apply {
            xSpacing = maxPointSize + spacingDp.pixels
            when (val horizontalLayout = horizontalLayout) {
                is HorizontalLayout.Segmented -> {
                    scalableStartPadding = xSpacing.half
                    scalableEndPadding = scalableStartPadding
                }
                is HorizontalLayout.FullWidth -> {
                    scalableStartPadding = horizontalLayout.startPaddingDp.pixels
                    scalableEndPadding = horizontalLayout.endPaddingDp.pixels
                    unscalableStartPadding = maxPointSize.half
                    unscalableEndPadding = unscalableStartPadding
                }
            }
        }
    }

    override fun updateChartValues(chartValuesManager: ChartValuesManager, model: ChartEntryModel, xStep: Float?) {
        @Suppress("DEPRECATION_ERROR")
        chartValuesManager.tryUpdate(
            minX = axisValuesOverrider?.getMinX(model) ?: minX ?: model.minX,
            maxX = axisValuesOverrider?.getMaxX(model) ?: maxX ?: model.maxX,
            minY = axisValuesOverrider?.getMinY(model) ?: minY ?: min(model.minY, 0f),
            maxY = axisValuesOverrider?.getMaxY(model) ?: maxY ?: model.maxY,
            xStep = xStep ?: model.xGcd,
            chartEntryModel = model,
            axisPosition = targetVerticalAxisPosition,
        )
    }

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions,
    ): Unit = with(context) {
        outInsets.setVertical(
            value = lines.maxOf {
                if (it.point != null) max(a = it.lineThicknessDp, b = it.pointSizeDp) else it.lineThicknessDp
            }.pixels,
        )
    }
}
