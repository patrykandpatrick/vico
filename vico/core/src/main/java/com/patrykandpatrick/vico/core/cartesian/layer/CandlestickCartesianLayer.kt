/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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
import androidx.compose.runtime.Stable
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CandlestickCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.CandlestickCartesianLayerModel.Change
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.data.getSliceIndices
import com.patrykandpatrick.vico.core.cartesian.getVisibleXRange
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer.Candle
import com.patrykandpatrick.vico.core.cartesian.marker.CandlestickCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore
import com.patrykandpatrick.vico.core.common.extractColor
import com.patrykandpatrick.vico.core.common.getStart
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.shape.Shape
import java.util.Objects
import kotlin.math.max

/**
 * Draws the content of candlestick charts.
 *
 * @property candleProvider provides the [Candle]s.
 * @property minCandleBodyHeightDp the minimum height of the candle bodies (in dp).
 * @property candleSpacingDp the spacing between neighboring candles.
 * @property scaleCandleWicks whether the candle wicks should be scaled based on the zoom factor.
 * @property rangeProvider defines the _x_ and _y_ ranges.
 * @property verticalAxisPosition the position of the [VerticalAxis] with which the
 *   [CandlestickCartesianLayer] should be associated. Use this for independent [CartesianLayer]
 *   scaling.
 */
@Stable
public open class CandlestickCartesianLayer
protected constructor(
  protected val candleProvider: CandleProvider,
  protected val minCandleBodyHeightDp: Float,
  protected val candleSpacingDp: Float,
  protected val scaleCandleWicks: Boolean,
  protected val rangeProvider: CartesianLayerRangeProvider,
  protected val verticalAxisPosition: Axis.Position.Vertical?,
  protected val drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<
      CandlestickCartesianLayerDrawingModel.Entry,
      CandlestickCartesianLayerDrawingModel,
    >,
  protected val drawingModelKey: ExtraStore.Key<CandlestickCartesianLayerDrawingModel>,
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

  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

  /** Creates a [CandlestickCartesianLayer]. */
  public constructor(
    candleProvider: CandleProvider,
    minCandleBodyHeightDp: Float = Defaults.MIN_CANDLE_BODY_HEIGHT_DP,
    candleSpacingDp: Float = Defaults.CANDLE_SPACING_DP,
    scaleCandleWicks: Boolean = false,
    rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
    verticalAxisPosition: Axis.Position.Vertical? = null,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        CandlestickCartesianLayerDrawingModel.Entry,
        CandlestickCartesianLayerDrawingModel,
      > =
      CartesianLayerDrawingModelInterpolator.default(),
  ) : this(
    candleProvider,
    minCandleBodyHeightDp,
    candleSpacingDp,
    scaleCandleWicks,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
    ExtraStore.Key(),
  )

  override fun drawInternal(
    context: CartesianDrawingContext,
    model: CandlestickCartesianLayerModel,
  ): Unit =
    with(context) {
      _markerTargets.clear()
      drawChartInternal(model, ranges, extraStore.getOrNull(drawingModelKey))
    }

  private fun CartesianDrawingContext.drawChartInternal(
    model: CandlestickCartesianLayerModel,
    ranges: CartesianChartRanges,
    drawingModel: CandlestickCartesianLayerDrawingModel?,
  ) {
    val yRange = ranges.getYRange(verticalAxisPosition)
    val halfMaxCandleWidth = candleProvider.getWidestCandle(model.extraStore).widthDp.half.pixels

    val drawingStart =
      layerBounds.getStart(isLtr) +
        (layerDimensions.startPadding - halfMaxCandleWidth * zoom) * layoutDirectionMultiplier -
        scroll

    var bodyCenterX: Float
    var candle: Candle
    val minBodyHeight = minCandleBodyHeightDp.pixels
    val visibleXRange = getVisibleXRange()

    val (_, firstVisibleEntryIndex, lastVisibleEntryIndex) =
      model.series.getSliceIndices(
        ranges.minX,
        ranges.maxX,
        visibleXRange.start,
        visibleXRange.endInclusive,
      )

    model.series.subList(firstVisibleEntryIndex, lastVisibleEntryIndex + 1).forEach { entry ->
      candle = candleProvider.getCandle(entry, model.extraStore)
      val candleInfo = drawingModel?.entries?.get(entry.x) ?: entry.toCandleInfo(yRange)
      val xSpacingMultiplier = ((entry.x - ranges.minX) / ranges.xStep).toFloat()
      bodyCenterX =
        drawingStart +
          layoutDirectionMultiplier * layerDimensions.xSpacing * xSpacingMultiplier +
          halfMaxCandleWidth * zoom

      var bodyBottomY = layerBounds.bottom - candleInfo.bodyBottomY * layerBounds.height()
      var bodyTopY = layerBounds.bottom - candleInfo.bodyTopY * layerBounds.height()
      val bottomWickY = layerBounds.bottom - candleInfo.bottomWickY * layerBounds.height()
      val topWickY = layerBounds.bottom - candleInfo.topWickY * layerBounds.height()

      if (bodyBottomY - bodyTopY < minBodyHeight) {
        bodyBottomY = (bodyBottomY + bodyTopY).half + minBodyHeight.half
        bodyTopY = bodyBottomY - minBodyHeight
      }

      updateMarkerTargets(entry, bodyCenterX, bodyBottomY, bodyTopY, bottomWickY, topWickY, candle)

      candle.body.drawVertical(this, bodyCenterX, bodyTopY, bodyBottomY, zoom)

      candle.topWick.drawVertical(
        context = this,
        x = bodyCenterX,
        top = topWickY,
        bottom = bodyTopY,
        thicknessFactor = if (scaleCandleWicks) zoom else 1f,
      )

      candle.bottomWick.drawVertical(
        context = this,
        x = bodyCenterX,
        top = bodyBottomY,
        bottom = bottomWickY,
        thicknessFactor = if (scaleCandleWicks) zoom else 1f,
      )
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
          openingColor =
            candle.body.effectiveStrokeFill.extractColor(
              context = this,
              significantY =
                if (entry.absoluteChange == Change.Bearish) {
                  limitedBodyTopCanvasY
                } else {
                  limitedBodyBottomCanvasY
                },
              width = candle.body.thicknessDp.pixels,
              height = limitedBodyBottomCanvasY - limitedBodyTopCanvasY,
              side = if (entry.absoluteChange == Change.Bearish) 1 else -1,
            ),
          closingColor =
            candle.body.effectiveStrokeFill.extractColor(
              context = this,
              significantY =
                if (entry.absoluteChange == Change.Bearish) {
                  limitedBodyBottomCanvasY
                } else {
                  limitedBodyTopCanvasY
                },
              width = candle.body.thicknessDp.pixels,
              height = limitedBodyBottomCanvasY - limitedBodyTopCanvasY,
              side = if (entry.absoluteChange == Change.Bearish) -1 else 1,
            ),
          lowColor =
            candle.bottomWick.effectiveStrokeFill.extractColor(
              context = this,
              significantY = lowCanvasY,
              width = candle.bottomWick.thicknessDp.pixels,
              height = lowCanvasY - limitedBodyBottomCanvasY,
              side = -1,
            ),
          highColor =
            candle.topWick.effectiveStrokeFill.extractColor(
              context = this,
              significantY = highCanvasY,
              width = candle.topWick.thicknessDp.pixels,
              height = highCanvasY - limitedBodyTopCanvasY,
            ),
        )
      )
  }

  override fun updateChartRanges(
    chartRanges: MutableCartesianChartRanges,
    model: CandlestickCartesianLayerModel,
  ) {
    chartRanges.tryUpdate(
      rangeProvider.getMinX(model.minX, model.maxX, model.extraStore),
      rangeProvider.getMaxX(model.minX, model.maxX, model.extraStore),
      rangeProvider.getMinY(model.minY, model.maxY, model.extraStore),
      rangeProvider.getMaxY(model.minY, model.maxY, model.extraStore),
      verticalAxisPosition,
    )
  }

  override fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
    model: CandlestickCartesianLayerModel,
  ) {
    with(context) {
      val candleWidth = candleProvider.getWidestCandle(model.extraStore).widthDp.pixels
      val xSpacing = candleWidth + candleSpacingDp.pixels
      dimensions.ensureValuesAtLeast(
        xSpacing = xSpacing,
        scalableStartPadding = candleWidth.half + layerPadding.scalableStartDp.pixels,
        scalableEndPadding = candleWidth.half + layerPadding.scalableEndDp.pixels,
        unscalableStartPadding = layerPadding.unscalableStartDp.pixels,
        unscalableEndPadding = layerPadding.unscalableEndDp.pixels,
      )
    }
  }

  override fun prepareForTransformation(
    model: CandlestickCartesianLayerModel?,
    ranges: CartesianChartRanges,
    extraStore: MutableExtraStore,
  ) {
    drawingModelInterpolator.setModels(
      old = extraStore.getOrNull(drawingModelKey),
      new = model?.toDrawingModel(ranges),
    )
  }

  override suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {
    drawingModelInterpolator.transform(fraction)?.let { extraStore[drawingModelKey] = it }
      ?: extraStore.remove(drawingModelKey)
  }

  private fun CandlestickCartesianLayerModel.Entry.toCandleInfo(
    yRange: CartesianChartRanges.YRange
  ) =
    CandlestickCartesianLayerDrawingModel.Entry(
      bodyBottomY = ((minOf(opening, closing) - yRange.minY) / yRange.length).toFloat(),
      bodyTopY = ((max(opening, closing) - yRange.minY) / yRange.length).toFloat(),
      bottomWickY = ((low - yRange.minY) / yRange.length).toFloat(),
      topWickY = ((high - yRange.minY) / yRange.length).toFloat(),
    )

  private fun CandlestickCartesianLayerModel.toDrawingModel(
    ranges: CartesianChartRanges
  ): CandlestickCartesianLayerDrawingModel {
    val yRange = ranges.getYRange(verticalAxisPosition)
    return CandlestickCartesianLayerDrawingModel(
      series.associate { it.x to it.toCandleInfo(yRange) }
    )
  }

  /** Creates a new [CandlestickCartesianLayer] based on this one. */
  public fun copy(
    candleProvider: CandleProvider = this.candleProvider,
    minCandleBodyHeightDp: Float = this.minCandleBodyHeightDp,
    candleSpacingDp: Float = this.candleSpacingDp,
    scaleCandleWicks: Boolean = this.scaleCandleWicks,
    rangeProvider: CartesianLayerRangeProvider = this.rangeProvider,
    verticalAxisPosition: Axis.Position.Vertical? = this.verticalAxisPosition,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        CandlestickCartesianLayerDrawingModel.Entry,
        CandlestickCartesianLayerDrawingModel,
      > =
      this.drawingModelInterpolator,
  ): CandlestickCartesianLayer =
    CandlestickCartesianLayer(
      candleProvider,
      minCandleBodyHeightDp,
      candleSpacingDp,
      scaleCandleWicks,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
      drawingModelKey,
    )

  override fun equals(other: Any?): Boolean =
    other is CandlestickCartesianLayer &&
      candleProvider == other.candleProvider &&
      minCandleBodyHeightDp == other.minCandleBodyHeightDp &&
      candleSpacingDp == other.candleSpacingDp &&
      scaleCandleWicks == other.scaleCandleWicks &&
      rangeProvider == other.rangeProvider &&
      verticalAxisPosition == other.verticalAxisPosition &&
      drawingModelInterpolator == other.drawingModelInterpolator

  override fun hashCode(): Int =
    Objects.hash(
      candleProvider,
      minCandleBodyHeightDp,
      candleSpacingDp,
      scaleCandleWicks,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
    )

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
    fill = effectiveStrokeFill,
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
