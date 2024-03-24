/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.cartesian.layer

import android.graphics.Color
import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.draw.CartesianChartDrawContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.put
import com.patrykandpatrick.vico.core.cartesian.model.CandlestickCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.model.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.model.CandlestickCartesianLayerModel.Entry.Change
import com.patrykandpatrick.vico.core.cartesian.model.forEachInIndexed
import com.patrykandpatrick.vico.core.cartesian.values.ChartValues
import com.patrykandpatrick.vico.core.cartesian.values.MutableChartValues
import com.patrykandpatrick.vico.core.common.DefaultDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.DrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.ExtraStore
import com.patrykandpatrick.vico.core.common.MutableExtraStore
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.extension.getStart
import com.patrykandpatrick.vico.core.common.extension.half

/**
 * [CandlestickCartesianLayer] displays data as vertical bars. It can draw multiple columns per segment.
 *
 * @param candles TODO
 * @param minCandleBodyHeightDp TODO
 * @param spacingDp the horizontal padding between the edges of chart segments and the columns they contain.
 * segments that contain a single column only.
 * @param verticalAxisPosition the position of the [VerticalAxis] with which the [ColumnCartesianLayer] should be
 * associated. Use this for independent [CartesianLayer] scaling.
 */
public open class CandlestickCartesianLayer(
    public var candles: Candles,
    public var minCandleBodyHeightDp: Float = Defaults.MIN_CANDLE_BODY_HEIGHT_DP,
    public var spacingDp: Float = Defaults.CANDLESTICK_CHART_DEFAULT_SPACING_DP,
    public var verticalAxisPosition: AxisPosition.Vertical? = null,
    public var drawingModelInterpolator: DrawingModelInterpolator<
        CandlestickCartesianLayerDrawingModel.CandleInfo,
        CandlestickCartesianLayerDrawingModel,
    > = DefaultDrawingModelInterpolator(),
) : BaseCartesianLayer<CandlestickCartesianLayerModel>() {
    /**
     * TODO
     *
     * @param body TODO
     * @param topWick TODO
     * @param bottomWick TODO
     */
    public data class Candle(
        public val body: LineComponent,
        public val topWick: LineComponent = body.copyAsWick(),
        public val bottomWick: LineComponent = topWick,
    ) {
        internal val thicknessDp
            get() =
                maxOf(
                    body.thicknessDp,
                    topWick.thicknessDp,
                    bottomWick.thicknessDp,
                )

        // Empty companion object is needed for extension functions.
        public companion object
    }

    /**
     * Holds information on the [CandlestickCartesianLayer]’s horizontal dimensions.
     */
    protected val horizontalDimensions: MutableHorizontalDimensions = MutableHorizontalDimensions()

    protected val drawingModelKey: ExtraStore.Key<CandlestickCartesianLayerDrawingModel> = ExtraStore.Key()

    override val entryLocationMap: HashMap<Float, MutableList<CartesianMarker.EntryModel>> = HashMap()

    override fun drawInternal(
        context: CartesianChartDrawContext,
        model: CandlestickCartesianLayerModel,
    ): Unit =
        with(context) {
            entryLocationMap.clear()
            drawChartInternal(
                chartValues = chartValues,
                model = model,
                drawingModel = model.extraStore.getOrNull(drawingModelKey),
            )
        }

    private fun CartesianChartDrawContext.drawChartInternal(
        chartValues: ChartValues,
        model: CandlestickCartesianLayerModel,
        drawingModel: CandlestickCartesianLayerDrawingModel?,
    ) {
        val yRange = chartValues.getYRange(verticalAxisPosition)

        val drawingStart: Float =
            bounds.getStart(isLtr = isLtr) + (
                horizontalDimensions.startPadding -
                    candles.maxThicknessDp.half.pixels * zoom
            ) * layoutDirectionMultiplier - horizontalScroll

        var bodyCenterX: Float
        var candle: Candle
        val minBodyHeight = minCandleBodyHeightDp.pixels

        model.series.forEachInIndexed(range = chartValues.minX..chartValues.maxX) { index, entry, _ ->
            candle = candles.getCandle(entry)
            val candleInfo = drawingModel?.entries?.get(entry.x) ?: entry.toCandleInfo(yRange)

            val xSpacingMultiplier = (entry.x - chartValues.minX) / chartValues.xStep
            bodyCenterX = drawingStart + layoutDirectionMultiplier * horizontalDimensions.xSpacing *
                xSpacingMultiplier + candle.thicknessDp.half.pixels * zoom

            var bodyBottomY = bounds.bottom - candleInfo.bodyBottomY * bounds.height()
            var bodyTopY = bounds.bottom - candleInfo.bodyTopY * bounds.height()
            val bottomWickY = bounds.bottom - candleInfo.bottomWickY * bounds.height()
            val topWickY = bounds.bottom - candleInfo.topWickY * bounds.height()

            if (bodyBottomY - bodyTopY < minBodyHeight) {
                bodyBottomY = (bodyBottomY + bodyTopY).half + minBodyHeight.half
                bodyTopY = bodyBottomY - minBodyHeight
            }

            if (candle.body.intersectsVertical(
                    context = this,
                    top = bodyTopY,
                    bottom = bodyBottomY,
                    centerX = bodyCenterX,
                    boundingBox = bounds,
                    thicknessScale = zoom,
                )
            ) {
                updateMarkerLocationMap(
                    entry = entry,
                    entryX = bodyCenterX,
                    entryY = (bodyBottomY + bodyTopY).half,
                    body = candle.body,
                    entryIndex = index,
                )

                candle.body.drawVertical(this, bodyTopY, bodyBottomY, bodyCenterX, zoom)

                candle.topWick.drawVertical(
                    context = this,
                    top = topWickY,
                    bottom = bodyTopY,
                    centerX = bodyCenterX,
                    thicknessScale = zoom,
                )

                candle.bottomWick.drawVertical(
                    context = this,
                    top = bodyBottomY,
                    bottom = bottomWickY,
                    centerX = bodyCenterX,
                    thicknessScale = zoom,
                )
            }
        }
    }

    private fun updateMarkerLocationMap(
        entry: CandlestickCartesianLayerModel.Entry,
        entryX: Float,
        entryY: Float,
        entryIndex: Int,
        body: LineComponent,
    ) {
        if (entryX in bounds.left..bounds.right) {
            entryLocationMap.put(
                x = entryX,
                y = entryY.coerceIn(bounds.top, bounds.bottom),
                entry = entry,
                color = body.solidOrStrokeColor,
                index = entryIndex,
            )
        }
    }

    override fun updateChartValues(
        chartValues: MutableChartValues,
        model: CandlestickCartesianLayerModel,
    ) {
        chartValues.tryUpdate(
            axisValueOverrider.getMinX(model.minX, model.maxX, model.extraStore),
            axisValueOverrider.getMaxX(model.minX, model.maxX, model.extraStore),
            axisValueOverrider.getMinY(model.minY, model.maxY, model.extraStore),
            axisValueOverrider.getMaxY(model.minY, model.maxY, model.extraStore),
            verticalAxisPosition,
        )
    }

    override fun updateHorizontalDimensions(
        context: CartesianMeasureContext,
        horizontalDimensions: MutableHorizontalDimensions,
        model: CandlestickCartesianLayerModel,
    ) {
        with(context) {
            val columnCollectionWidth = candles.maxThicknessDp.pixels
            val xSpacing = columnCollectionWidth + spacingDp.pixels
            when (val horizontalLayout = horizontalLayout) {
                is HorizontalLayout.Segmented -> {
                    horizontalDimensions.ensureValuesAtLeast(
                        xSpacing = xSpacing,
                        scalableStartPadding = xSpacing.half,
                        scalableEndPadding = xSpacing.half,
                    )
                }

                is HorizontalLayout.FullWidth -> {
                    horizontalDimensions.ensureValuesAtLeast(
                        xSpacing = xSpacing,
                        scalableStartPadding =
                            columnCollectionWidth.half +
                                horizontalLayout.scalableStartPaddingDp.pixels,
                        scalableEndPadding = columnCollectionWidth.half + horizontalLayout.scalableEndPaddingDp.pixels,
                        unscalableStartPadding = horizontalLayout.unscalableStartPaddingDp.pixels,
                        unscalableEndPadding = horizontalLayout.unscalableEndPaddingDp.pixels,
                    )
                }
            }
        }
    }

    override fun prepareForTransformation(
        model: CandlestickCartesianLayerModel?,
        extraStore: MutableExtraStore,
        chartValues: ChartValues,
    ) {
        drawingModelInterpolator.setModels(
            old = extraStore.getOrNull(drawingModelKey),
            new = model?.toDrawingModel(chartValues),
        )
    }

    override suspend fun transform(
        extraStore: MutableExtraStore,
        fraction: Float,
    ) {
        drawingModelInterpolator
            .transform(fraction)
            ?.let { extraStore[drawingModelKey] = it }
            ?: extraStore.remove(drawingModelKey)
    }

    private fun CandlestickCartesianLayerModel.Entry.toCandleInfo(yRange: ChartValues.YRange) =
        CandlestickCartesianLayerDrawingModel.CandleInfo(
            bodyBottomY = (minOf(opening, closing) - yRange.minY) / yRange.length,
            bodyTopY = (maxOf(opening, closing) - yRange.minY) / yRange.length,
            bottomWickY = (low - yRange.minY) / yRange.length,
            topWickY = (high - yRange.minY) / yRange.length,
        )

    private fun CandlestickCartesianLayerModel.toDrawingModel(
        chartValues: ChartValues,
    ): CandlestickCartesianLayerDrawingModel {
        val yRange = chartValues.getYRange(verticalAxisPosition)
        return CandlestickCartesianLayerDrawingModel(series.associate { it.x to it.toCandleInfo(yRange) })
    }

    /**
     * TODO
     *
     * @param absolutelyBullishRelativelyBullish TODO
     * @param absolutelyBullishRelativelyNeutral TODO
     * @param absolutelyBullishRelativelyBearish TODO
     * @param absolutelyNeutralRelativelyBullish TODO
     * @param absolutelyNeutralRelativelyNeutral TODO
     * @param absolutelyNeutralRelativelyBearish TODO
     * @param absolutelyBearishRelativelyBullish TODO
     * @param absolutelyBearishRelativelyNeutral TODO
     * @param absolutelyBearishRelativelyBearish TODO
     */
    public class Candles(
        public val absolutelyBullishRelativelyBullish: Candle,
        public val absolutelyBullishRelativelyNeutral: Candle,
        public val absolutelyBullishRelativelyBearish: Candle,
        public val absolutelyNeutralRelativelyBullish: Candle,
        public val absolutelyNeutralRelativelyNeutral: Candle,
        public val absolutelyNeutralRelativelyBearish: Candle,
        public val absolutelyBearishRelativelyBullish: Candle,
        public val absolutelyBearishRelativelyNeutral: Candle,
        public val absolutelyBearishRelativelyBearish: Candle,
    ) {
        internal val maxThicknessDp
            get() =
                maxOf(
                    absolutelyBullishRelativelyBullish.body.thicknessDp,
                    absolutelyBullishRelativelyNeutral.body.thicknessDp,
                    absolutelyBullishRelativelyBearish.body.thicknessDp,
                    absolutelyNeutralRelativelyBullish.body.thicknessDp,
                    absolutelyNeutralRelativelyNeutral.body.thicknessDp,
                    absolutelyNeutralRelativelyBearish.body.thicknessDp,
                    absolutelyBearishRelativelyBullish.body.thicknessDp,
                    absolutelyBearishRelativelyNeutral.body.thicknessDp,
                    absolutelyBearishRelativelyBearish.body.thicknessDp,
                )

        internal fun getCandle(entry: CandlestickCartesianLayerModel.Entry) =
            when (entry.absoluteChange) {
                Change.Bullish ->
                    when (entry.relativeChange) {
                        Change.Bullish -> absolutelyBullishRelativelyBullish
                        Change.Bearish -> absolutelyBullishRelativelyBearish
                        Change.Neutral -> absolutelyBullishRelativelyNeutral
                    }

                Change.Bearish ->
                    when (entry.relativeChange) {
                        Change.Bullish -> absolutelyBearishRelativelyBullish
                        Change.Bearish -> absolutelyBearishRelativelyBearish
                        Change.Neutral -> absolutelyBearishRelativelyNeutral
                    }

                Change.Neutral ->
                    when (entry.relativeChange) {
                        Change.Bullish -> absolutelyNeutralRelativelyBullish
                        Change.Bearish -> absolutelyNeutralRelativelyBearish
                        Change.Neutral -> absolutelyNeutralRelativelyNeutral
                    }
            }

        public companion object
    }
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun LineComponent.copyAsWick(): LineComponent =
    copy(
        color = if (color == Color.TRANSPARENT) strokeColor else color,
        thicknessDp = Defaults.WICK_DEFAULT_WIDTH_DP,
        strokeWidthDp = 0f,
    )
