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

package com.patrykandpatrick.vico.core.chart.candlestick

import android.graphics.Color
import com.patrykandpatrick.vico.core.DefaultDimens.REAL_BODY_MIN_HEIGHT_DP
import com.patrykandpatrick.vico.core.DefaultDimens.WICK_DEFAULT_WIDTH_DP
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.candlestickentry.CandlestickEntryModel
import com.patrykandpatrick.vico.core.candlestickentry.CandlestickEntryType
import com.patrykandpatrick.vico.core.chart.BaseChart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.forEachIn
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.put
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.round
import com.patrykandpatrick.vico.core.marker.Marker

/**
 * [CandlestickChart] displays data as vertical bars. It can draw multiple columns per segment.
 *
 * @param config TODO
 * @param minRealBodyHeightDp TODO
 * @param spacingDp the horizontal padding between the edges of chart segments and the columns they contain.
 * segments that contain a single column only.
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 */
public open class CandlestickChart(
    public var config: Config,
    public var minRealBodyHeightDp: Float = REAL_BODY_MIN_HEIGHT_DP,
    public var spacingDp: Float = 16f,
    public var targetVerticalAxisPosition: AxisPosition.Vertical? = null,
) : BaseChart<CandlestickEntryModel>() {

    /**
     * TODO
     *
     * @param realBody TODO
     * @param upperWick TODO
     * @param lowerWick TODO
     */
    public data class Candle(
        public val realBody: LineComponent,
        public val upperWick: LineComponent = realBody.copyAsWick(),
        public val lowerWick: LineComponent = upperWick,
    ) {

        /**
         * Returns the maximum thickness among all of the candle components.
         */
        val thicknessDp: Float
            get() = maxOf(
                realBody.thicknessDp,
                upperWick.thicknessDp,
                lowerWick.thicknessDp,
            )

        // Empty companion object is needed for extension functions.
        public companion object
    }

    private val heightMap = HashMap<Float, Pair<Float, Float>>()

    /**
     * Holds information on the [CandlestickChart]’s horizontal dimensions.
     */
    protected val horizontalDimensions: MutableHorizontalDimensions = MutableHorizontalDimensions()

    override val entryLocationMap: HashMap<Float, MutableList<Marker.EntryModel>> = HashMap()

    override fun drawChart(
        context: ChartDrawContext,
        model: CandlestickEntryModel,
    ): Unit = with(context) {
        entryLocationMap.clear()
        drawChartInternal(
            chartValues = chartValuesManager.getChartValues(axisPosition = targetVerticalAxisPosition),
            model = model,
        )
        heightMap.clear()
    }

    private fun ChartDrawContext.drawChartInternal(
        chartValues: ChartValues,
        model: CandlestickEntryModel,
    ) {
        val yRange = (chartValues.maxY - chartValues.minY).takeIf { it != 0f } ?: return
        val heightMultiplier = bounds.height() / yRange

        val drawingStart: Float = bounds.getStart(isLtr = isLtr) +
            horizontalDimensions.scaled(chartScale).startPadding -
            config.maxThicknessDp.half.pixels * layoutDirectionMultiplier * chartScale - horizontalScroll

        var bodyCenterX: Float
        var candle: Candle
        var bodyTop: Float
        var bodyBottom: Float
        val zeroLinePosition = (bounds.bottom + chartValues.minY / yRange * bounds.height()).round

        val minRealBodyHeight = minRealBodyHeightDp.pixels

        model.entries.forEachIn(range = chartValues.minX..chartValues.maxX) { entry ->

            candle = config.getCandle(entry.type)

            val xSpacingMultiplier = (entry.x - chartValues.minX) / chartValues.xStep
            bodyCenterX = drawingStart + layoutDirectionMultiplier * horizontalDimensions.xSpacing *
                xSpacingMultiplier + candle.thicknessDp.half.pixels * chartScale

            bodyBottom = (zeroLinePosition - minOf(entry.close, entry.open) * heightMultiplier).round
            bodyTop = (zeroLinePosition - maxOf(entry.close, entry.open) * heightMultiplier).round

            if (bodyBottom - bodyTop < minRealBodyHeight) {
                bodyBottom = (bodyBottom + bodyTop).half + minRealBodyHeight.half
                bodyTop = bodyBottom - minRealBodyHeight
            }

            if (candle.realBody.intersectsVertical(
                    context = this,
                    top = bodyTop,
                    bottom = bodyBottom,
                    centerX = bodyCenterX,
                    boundingBox = bounds,
                    thicknessScale = chartScale,
                )
            ) {
                listOf(entry.low, entry.open, entry.close, entry.high)
                    .forEach { entryY ->
                        updateMarkerLocationMap(
                            entry = entryOf(x = entry.x, y = entryY),
                            columnTop = zeroLinePosition - entryY * heightMultiplier,
                            columnCenterX = bodyCenterX,
                            realBody = candle.realBody,
                        )
                    }

                candle.realBody.drawVertical(this, bodyTop, bodyBottom, bodyCenterX, chartScale)

                candle.upperWick.drawVertical(
                    context = this,
                    top = zeroLinePosition - entry.high * heightMultiplier,
                    bottom = bodyTop,
                    centerX = bodyCenterX,
                    thicknessScale = chartScale,
                )

                candle.lowerWick.drawVertical(
                    context = this,
                    top = bodyBottom,
                    bottom = zeroLinePosition - entry.low * heightMultiplier,
                    centerX = bodyCenterX,
                    thicknessScale = chartScale,
                )
            }
        }
    }

    private fun updateMarkerLocationMap(
        entry: ChartEntry,
        columnTop: Float,
        columnCenterX: Float,
        realBody: LineComponent,
    ) {
        if (columnCenterX in bounds.left..bounds.right) {
            entryLocationMap.put(
                x = columnCenterX,
                y = columnTop.coerceIn(bounds.top, bounds.bottom),
                entry = entry,
                color = realBody.solidOrStrokeColor,
                index = 0,
            )
        }
    }

    override fun updateChartValues(
        chartValuesManager: ChartValuesManager,
        model: CandlestickEntryModel,
        xStep: Float?,
    ) {
        chartValuesManager.tryUpdate(
            minX = axisValuesOverrider?.getMinX(model) ?: model.minX,
            maxX = axisValuesOverrider?.getMaxX(model) ?: model.maxX,
            minY = axisValuesOverrider?.getMinY(model) ?: model.minY,
            maxY = axisValuesOverrider?.getMaxY(model) ?: model.maxY,
            xStep = xStep ?: model.xGcd,
            model = model,
            axisPosition = targetVerticalAxisPosition,
        )
    }

    override fun getHorizontalDimensions(
        context: MeasureContext,
        model: CandlestickEntryModel,
    ): HorizontalDimensions = with(context) {
        val columnCollectionWidth = config.maxThicknessDp.pixels
        horizontalDimensions.apply {
            xSpacing = columnCollectionWidth + spacingDp.pixels
            when (horizontalLayout) {
                is HorizontalLayout.Segmented -> {
                    scalableStartPadding = xSpacing.half
                    scalableEndPadding = scalableStartPadding
                }

                is HorizontalLayout.FullWidth -> {
                    scalableStartPadding = columnCollectionWidth.half + horizontalLayout.startPaddingDp.pixels
                    scalableEndPadding = columnCollectionWidth.half + horizontalLayout.endPaddingDp.pixels
                }
            }
        }
    }

    /**
     * TODO
     *
     * @param filledGreenCandle TODO
     * @param filledGrayCandle TODO
     * @param filledRedCandle TODO
     * @param crossGreenCandle TODO
     * @param crossGrayCandle TODO
     * @param crossRedCandle TODO
     * @param hollowGreenCandle TODO
     * @param hollowGrayCandle TODO
     * @param hollowRedCandle TODO
     */
    @Suppress("LongParameterList")
    public class Config(
        public val filledGreenCandle: Candle,
        public val filledGrayCandle: Candle,
        public val filledRedCandle: Candle,
        public val crossGreenCandle: Candle,
        public val crossGrayCandle: Candle,
        public val crossRedCandle: Candle,
        public val hollowGreenCandle: Candle,
        public val hollowGrayCandle: Candle,
        public val hollowRedCandle: Candle,
    ) {

        /**
         * TODO
         */
        public val maxThicknessDp: Float
            get() = maxOf(
                filledGreenCandle.realBody.thicknessDp,
                filledGrayCandle.realBody.thicknessDp,
                filledRedCandle.realBody.thicknessDp,
                crossGreenCandle.realBody.thicknessDp,
                crossGrayCandle.realBody.thicknessDp,
                crossRedCandle.realBody.thicknessDp,
                hollowGreenCandle.realBody.thicknessDp,
                hollowGrayCandle.realBody.thicknessDp,
                hollowRedCandle.realBody.thicknessDp,
            )

        /**
         * TODO
         */
        public fun getCandle(type: CandlestickEntryType): Candle =
            when (type) {
                is CandlestickEntryType.Filled ->
                    when (type.color) {
                        CandlestickEntryType.Color.Green -> filledGreenCandle
                        CandlestickEntryType.Color.Red -> filledRedCandle
                        CandlestickEntryType.Color.Gray -> filledGrayCandle
                    }

                is CandlestickEntryType.Cross ->
                    when (type.color) {
                        CandlestickEntryType.Color.Green -> crossGreenCandle
                        CandlestickEntryType.Color.Red -> crossRedCandle
                        CandlestickEntryType.Color.Gray -> crossGrayCandle
                    }

                is CandlestickEntryType.Hollow ->
                    when (type.color) {
                        CandlestickEntryType.Color.Green -> hollowGreenCandle
                        CandlestickEntryType.Color.Red -> hollowRedCandle
                        CandlestickEntryType.Color.Gray -> hollowGrayCandle
                    }
            }

        public companion object
    }
}

private fun LineComponent.copyAsWick(): LineComponent =
    copy(
        color = if (color == Color.TRANSPARENT) strokeColor else color,
        thicknessDp = WICK_DEFAULT_WIDTH_DP,
        strokeWidthDp = 0f,
    )
