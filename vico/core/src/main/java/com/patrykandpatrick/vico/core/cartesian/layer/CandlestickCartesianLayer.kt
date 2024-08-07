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

import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CandlestickCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.CandlestickCartesianLayerModel.Entry.Change
import com.patrykandpatrick.vico.core.cartesian.data.ChartValues
import com.patrykandpatrick.vico.core.cartesian.data.MutableChartValues
import com.patrykandpatrick.vico.core.cartesian.data.forEachIn
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer.Candle
import com.patrykandpatrick.vico.core.cartesian.marker.CandlestickCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore
import com.patrykandpatrick.vico.core.common.getStart
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlin.math.max

/**
 * Draws the content of candlestick charts.
 *
 * @property candles provides the [Candle]s.
 * @property minCandleBodyHeightDp the minimum height of the candle bodies (in dp).
 * @property candleSpacingDp the spacing between neighboring candles.
 * @property scaleCandleWicks whether the candle wicks should be scaled based on the zoom factor.
 * @property axisValueOverrider overrides the _x_ and _y_ ranges.
 * @property verticalAxisPosition the position of the [VerticalAxis] with which the
 *   [CandlestickCartesianLayer] should be associated. Use this for independent [CartesianLayer]
 *   scaling.
 */
public open class CandlestickCartesianLayer(
  public var candles: CandleProvider,
  public var minCandleBodyHeightDp: Float = Defaults.MIN_CANDLE_BODY_HEIGHT_DP,
  public var candleSpacingDp: Float = Defaults.CANDLE_SPACING_DP,
  public var scaleCandleWicks: Boolean = false,
  public var axisValueOverrider: AxisValueOverrider = AxisValueOverrider.auto(),
  public var verticalAxisPosition: Axis.Position.Vertical? = null,
  public var drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<
      CandlestickCartesianLayerDrawingModel.CandleInfo,
      CandlestickCartesianLayerDrawingModel,
    > =
    CartesianLayerDrawingModelInterpolator.default(),
) : BaseCartesianLayer<CandlestickCartesianLayerModel>() {
  /**
   * Defines a candle style.
   *
   * @param body used for the body.
   * @param topWick used for the top wick.
   * @param bottomWick used for the bottom wick.
   */
  public data class Candle(
    public val body: LineComponent,
    public val topWick: LineComponent = body.asWick(),
    public val bottomWick: LineComponent = topWick,
  ) {
    /** The width of the [Candle] (in dp). */
    public val widthDp: Float
      get() = maxOf(body.thicknessDp, topWick.thicknessDp, bottomWick.thicknessDp)

    // Empty companion object is needed for extension functions.
    /** @suppress */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) public companion object
  }

  private val _markerTargets = mutableMapOf<Double, List<CandlestickCartesianLayerMarkerTarget>>()

  /** Holds information on the [CandlestickCartesianLayer]’s horizontal dimensions. */
  protected val horizontalDimensions: MutableHorizontalDimensions = MutableHorizontalDimensions()

  protected val drawingModelKey: ExtraStore.Key<CandlestickCartesianLayerDrawingModel> =
    ExtraStore.Key()

  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

  override fun drawInternal(
    context: CartesianDrawingContext,
    model: CandlestickCartesianLayerModel,
  ): Unit =
    with(context) {
      _markerTargets.clear()
      drawChartInternal(
        chartValues = chartValues,
        model = model,
        drawingModel = model.extraStore.getOrNull(drawingModelKey),
      )
    }

  private fun CartesianDrawingContext.drawChartInternal(
    chartValues: ChartValues,
    model: CandlestickCartesianLayerModel,
    drawingModel: CandlestickCartesianLayerDrawingModel?,
  ) {
    val yRange = chartValues.getYRange(verticalAxisPosition)
    val halfMaxCandleWidth = candles.getWidestCandle(model.extraStore).widthDp.half.pixels

    val drawingStart =
      layerBounds.getStart(isLtr) +
        (horizontalDimensions.startPadding - halfMaxCandleWidth * zoom) *
          layoutDirectionMultiplier - scroll

    var bodyCenterX: Float
    var candle: Candle
    val minBodyHeight = minCandleBodyHeightDp.pixels

    model.series.forEachIn(chartValues.minX..chartValues.maxX) { entry, _ ->
      candle = candles.getCandle(entry, model.extraStore)
      val candleInfo = drawingModel?.entries?.get(entry.x) ?: entry.toCandleInfo(yRange)
      val xSpacingMultiplier = ((entry.x - chartValues.minX) / chartValues.xStep).toFloat()
      bodyCenterX =
        drawingStart +
          layoutDirectionMultiplier * horizontalDimensions.xSpacing * xSpacingMultiplier +
          halfMaxCandleWidth * zoom

      var bodyBottomY = layerBounds.bottom - candleInfo.bodyBottomY * layerBounds.height()
      var bodyTopY = layerBounds.bottom - candleInfo.bodyTopY * layerBounds.height()
      val bottomWickY = layerBounds.bottom - candleInfo.bottomWickY * layerBounds.height()
      val topWickY = layerBounds.bottom - candleInfo.topWickY * layerBounds.height()

      if (bodyBottomY - bodyTopY < minBodyHeight) {
        bodyBottomY = (bodyBottomY + bodyTopY).half + minBodyHeight.half
        bodyTopY = bodyBottomY - minBodyHeight
      }

      if (
        candle.body.intersectsVertical(
          context = this,
          top = bodyTopY,
          bottom = bodyBottomY,
          centerX = bodyCenterX,
          boundingBox = layerBounds,
          thicknessScale = zoom,
        )
      ) {
        updateMarkerTargets(
          entry,
          bodyCenterX,
          bodyBottomY,
          bodyTopY,
          bottomWickY,
          topWickY,
          candle,
        )

        candle.body.drawVertical(this, bodyTopY, bodyBottomY, bodyCenterX, zoom)

        candle.topWick.drawVertical(
          context = this,
          top = topWickY,
          bottom = bodyTopY,
          centerX = bodyCenterX,
          thicknessScale = if (scaleCandleWicks) zoom else 1f,
        )

        candle.bottomWick.drawVertical(
          context = this,
          top = bodyBottomY,
          bottom = bottomWickY,
          centerX = bodyCenterX,
          thicknessScale = if (scaleCandleWicks) zoom else 1f,
        )
      }
    }
  }

  protected open fun CartesianDrawingContext.updateMarkerTargets(
    entry: CandlestickCartesianLayerModel.Entry,
    canvasX: Float,
    bodyBottomCanvasY: Float,
    bodyTopCanvasY: Float,
    lowCanvasY: Float,
    highCanvasY: Float,
    candle: Candle,
  ) {
    if (canvasX <= layerBounds.left - 1 || canvasX >= layerBounds.right + 1) return
    val limitedBodyBottomCanvasY = bodyBottomCanvasY.coerceIn(layerBounds.top, layerBounds.bottom)
    val limitedBodyTopCanvasY = bodyTopCanvasY.coerceIn(layerBounds.top, layerBounds.bottom)
    _markerTargets[entry.x] =
      listOf(
        CandlestickCartesianLayerMarkerTarget(
          x = entry.x,
          canvasX = canvasX,
          entry = entry,
          openingCanvasY =
            if (entry.absoluteChange == Change.Bullish) limitedBodyBottomCanvasY
            else limitedBodyTopCanvasY,
          closingCanvasY =
            if (entry.absoluteChange == Change.Bullish) limitedBodyTopCanvasY
            else limitedBodyBottomCanvasY,
          lowCanvasY = lowCanvasY.coerceIn(layerBounds.top, layerBounds.bottom),
          highCanvasY = highCanvasY.coerceIn(layerBounds.top, layerBounds.bottom),
          openingColor = candle.body.solidOrStrokeColor,
          closingColor = candle.body.solidOrStrokeColor,
          lowColor = candle.bottomWick.solidOrStrokeColor,
          highColor = candle.topWick.solidOrStrokeColor,
        )
      )
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
    context: CartesianMeasuringContext,
    horizontalDimensions: MutableHorizontalDimensions,
    model: CandlestickCartesianLayerModel,
  ) {
    with(context) {
      val candleWidth = candles.getWidestCandle(model.extraStore).widthDp.pixels
      val xSpacing = candleWidth + candleSpacingDp.pixels
      when (val horizontalLayout = horizontalLayout) {
        is HorizontalLayout.Segmented ->
          horizontalDimensions.ensureSegmentedValues(xSpacing, chartValues)
        is HorizontalLayout.FullWidth -> {
          horizontalDimensions.ensureValuesAtLeast(
            xSpacing = xSpacing,
            scalableStartPadding =
              candleWidth.half + horizontalLayout.scalableStartPaddingDp.pixels,
            scalableEndPadding = candleWidth.half + horizontalLayout.scalableEndPaddingDp.pixels,
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

  override suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {
    drawingModelInterpolator.transform(fraction)?.let { extraStore[drawingModelKey] = it }
      ?: extraStore.remove(drawingModelKey)
  }

  private fun CandlestickCartesianLayerModel.Entry.toCandleInfo(yRange: ChartValues.YRange) =
    CandlestickCartesianLayerDrawingModel.CandleInfo(
      bodyBottomY = ((minOf(opening, closing) - yRange.minY) / yRange.length).toFloat(),
      bodyTopY = ((max(opening, closing) - yRange.minY) / yRange.length).toFloat(),
      bottomWickY = ((low - yRange.minY) / yRange.length).toFloat(),
      topWickY = ((high - yRange.minY) / yRange.length).toFloat(),
    )

  private fun CandlestickCartesianLayerModel.toDrawingModel(
    chartValues: ChartValues
  ): CandlestickCartesianLayerDrawingModel {
    val yRange = chartValues.getYRange(verticalAxisPosition)
    return CandlestickCartesianLayerDrawingModel(
      series.associate { it.x to it.toCandleInfo(yRange) }
    )
  }

  /** Provides [Candle]s to [CandlestickCartesianLayer]s. */
  public interface CandleProvider {
    /** Returns the [Candle] for the given [CandlestickCartesianLayerModel.Entry]. */
    public fun getCandle(
      entry: CandlestickCartesianLayerModel.Entry,
      extraStore: ExtraStore,
    ): Candle

    /** Returns the widest [Candle]. */
    public fun getWidestCandle(extraStore: ExtraStore): Candle

    /** Provides access to [CandleProvider] factory functions. */
    public companion object {
      internal data class Absolute(val bullish: Candle, val neutral: Candle, val bearish: Candle) :
        CandleProvider {
        private val candles = listOf(bullish, neutral, bearish)

        override fun getCandle(
          entry: CandlestickCartesianLayerModel.Entry,
          extraStore: ExtraStore,
        ) =
          when (entry.absoluteChange) {
            Change.Bullish -> bullish
            Change.Neutral -> neutral
            Change.Bearish -> bearish
          }

        override fun getWidestCandle(extraStore: ExtraStore) = candles.maxBy { it.widthDp }
      }

      internal data class AbsoluteRelative(
        val absolutelyBullishRelativelyBullish: Candle,
        val absolutelyBullishRelativelyNeutral: Candle,
        val absolutelyBullishRelativelyBearish: Candle,
        val absolutelyNeutralRelativelyBullish: Candle,
        val absolutelyNeutralRelativelyNeutral: Candle,
        val absolutelyNeutralRelativelyBearish: Candle,
        val absolutelyBearishRelativelyBullish: Candle,
        val absolutelyBearishRelativelyNeutral: Candle,
        val absolutelyBearishRelativelyBearish: Candle,
      ) : CandleProvider {
        private val candles =
          listOf(
            absolutelyBullishRelativelyBullish,
            absolutelyBullishRelativelyNeutral,
            absolutelyBullishRelativelyBearish,
            absolutelyNeutralRelativelyBullish,
            absolutelyNeutralRelativelyNeutral,
            absolutelyNeutralRelativelyBearish,
            absolutelyBearishRelativelyBullish,
            absolutelyBearishRelativelyNeutral,
            absolutelyBearishRelativelyBearish,
          )

        override fun getCandle(
          entry: CandlestickCartesianLayerModel.Entry,
          extraStore: ExtraStore,
        ) =
          when (entry.absoluteChange) {
            Change.Bullish ->
              when (entry.relativeChange) {
                Change.Bullish -> absolutelyBullishRelativelyBullish
                Change.Neutral -> absolutelyBullishRelativelyNeutral
                Change.Bearish -> absolutelyBullishRelativelyBearish
              }
            Change.Neutral ->
              when (entry.relativeChange) {
                Change.Bullish -> absolutelyNeutralRelativelyBullish
                Change.Neutral -> absolutelyNeutralRelativelyNeutral
                Change.Bearish -> absolutelyNeutralRelativelyBearish
              }
            Change.Bearish ->
              when (entry.relativeChange) {
                Change.Bullish -> absolutelyBearishRelativelyBullish
                Change.Neutral -> absolutelyBearishRelativelyNeutral
                Change.Bearish -> absolutelyBearishRelativelyBearish
              }
          }

        override fun getWidestCandle(extraStore: ExtraStore) = candles.maxBy { it.widthDp }
      }
    }
  }
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun LineComponent.asWick(): LineComponent =
  copy(
    color = solidOrStrokeColor,
    thicknessDp = Defaults.WICK_DEFAULT_WIDTH_DP,
    shape = Shape.Rectangle,
    strokeThicknessDp = 0f,
  )

/**
 * Switches between three [Candle]s based on [CandlestickCartesianLayerModel.Entry.absoluteChange].
 */
public fun CandlestickCartesianLayer.CandleProvider.Companion.absolute(
  bullish: Candle,
  neutral: Candle,
  bearish: Candle,
): CandlestickCartesianLayer.CandleProvider =
  CandlestickCartesianLayer.CandleProvider.Companion.Absolute(bullish, neutral, bearish)

/**
 * Switches between nine [Candle]s based on [CandlestickCartesianLayerModel.Entry.absoluteChange]
 * and [CandlestickCartesianLayerModel.Entry.relativeChange].
 */
public fun CandlestickCartesianLayer.CandleProvider.Companion.absoluteRelative(
  absolutelyBullishRelativelyBullish: Candle,
  absolutelyBullishRelativelyNeutral: Candle,
  absolutelyBullishRelativelyBearish: Candle,
  absolutelyNeutralRelativelyBullish: Candle,
  absolutelyNeutralRelativelyNeutral: Candle,
  absolutelyNeutralRelativelyBearish: Candle,
  absolutelyBearishRelativelyBullish: Candle,
  absolutelyBearishRelativelyNeutral: Candle,
  absolutelyBearishRelativelyBearish: Candle,
): CandlestickCartesianLayer.CandleProvider =
  CandlestickCartesianLayer.CandleProvider.Companion.AbsoluteRelative(
    absolutelyBullishRelativelyBullish,
    absolutelyBullishRelativelyNeutral,
    absolutelyBullishRelativelyBearish,
    absolutelyNeutralRelativelyBullish,
    absolutelyNeutralRelativelyNeutral,
    absolutelyNeutralRelativelyBearish,
    absolutelyBearishRelativelyBullish,
    absolutelyBearishRelativelyNeutral,
    absolutelyBearishRelativelyBearish,
  )
