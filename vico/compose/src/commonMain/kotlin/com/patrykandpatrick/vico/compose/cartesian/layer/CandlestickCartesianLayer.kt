/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.cartesian.layer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.*
import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel.Change
import com.patrykandpatrick.vico.compose.cartesian.getVisibleXRange
import com.patrykandpatrick.vico.compose.cartesian.marker.CandlestickCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.half
import kotlin.math.max

/**
 * Draws the content of candlestick charts.
 *
 * @property model the data.
 * @property candleProvider provides the [Candle]s.
 * @property minCandleBodyHeight the minimum height of the candle bodies.
 * @property candleSpacing the spacing between neighboring candles.
 * @property scaleCandleWicks whether the candle wicks should be scaled based on the zoom factor.
 * @property rangeProvider defines the _x_ and _y_ ranges.
 * @property verticalAxisPosition the position of the [VerticalAxis] with which the
 *   [CandlestickCartesianLayer] should be associated. Use this for independent [CartesianLayer]
 *   scaling.
 * @property drawingModelInterpolator interpolates the [CandlestickCartesianLayerDrawingModel]s.
 * @property key identifies this [CandlestickCartesianLayer] across data updates. See
 *   [AnimatedCartesianLayer.key].
 */
@Stable
public open class CandlestickCartesianLayer(
  override val model: CandlestickCartesianLayerModel,
  protected val candleProvider: CandleProvider,
  protected val minCandleBodyHeight: Dp = Defaults.MIN_CANDLE_BODY_HEIGHT_DP.dp,
  protected val candleSpacing: Dp = Defaults.CANDLE_SPACING_DP.dp,
  protected val scaleCandleWicks: Boolean = false,
  protected val rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
  protected val verticalAxisPosition: Axis.Position.Vertical? = null,
  override val drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<CandlestickCartesianLayerDrawingModel> =
    CartesianLayerDrawingModelInterpolator.candlestick(),
  override val key: Any,
) :
  BaseCartesianLayer<CandlestickCartesianLayerModel>(),
  AnimatedCartesianLayer<CandlestickCartesianLayerModel, CandlestickCartesianLayerDrawingModel> {
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
    /** The width of the [Candle]. */
    public val width: Dp
      get() = maxOf(body.thickness, topWick.thickness, bottomWick.thickness)

    internal companion object
  }

  private val _markerTargets = mutableMapOf<Double, List<CandlestickCartesianLayerMarkerTarget>>()

  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

  /** The widest [Candle], computed once per [CandlestickCartesianLayer]. */
  private val widestCandleWidth: Dp by lazy { getWidestCandleWidth(model.series) }

  /** The widest candle per exiting series, computed once per exit. Owned by the main thread. */
  private val exitingSeriesWidths = mutableMapOf<Pair<Any, Any?>, Dp>()

  private fun getWidestCandleWidth(series: List<CandlestickCartesianLayerModel.Entry>): Dp =
    series.maxOfOrNull { candleProvider.getCandle(it).width } ?: 0.dp

  override fun drawInternal(context: CartesianDrawingContext): Unit =
    with(context) {
      _markerTargets.clear()
      val drawingModel = getDrawingModel(this@CandlestickCartesianLayer)
      val drawingModelSeriesIndex = drawingModel.seriesKeys.indexOf(model.seriesKey)
      drawingModel.seriesKeys.forEachIndexed { index, key ->
        if (index == drawingModelSeriesIndex) return@forEachIndexed
        val series = drawingModel[index].values.map { it.entry }
        drawSeries(
          series,
          key,
          drawingModel[index],
          exitingSeriesWidths.getOrPut(key to series.firstOrNull()) {
            getWidestCandleWidth(series)
          },
          isExiting = true,
        )
      }
      drawSeries(
        model.series,
        model.seriesKey,
        drawingModel.getOrNull(drawingModelSeriesIndex),
        widestCandleWidth,
        isExiting = false,
      )
    }

  private fun CartesianDrawingContext.drawSeries(
    series: List<CandlestickCartesianLayerModel.Entry>,
    key: Any,
    drawingModelSeries: Map<Double, CandlestickCartesianLayerDrawingModel.Entry>?,
    widestCandleWidth: Dp,
    isExiting: Boolean,
  ) {
    val yRange = ranges.getYRange(verticalAxisPosition)
    val halfMaxCandleWidth = widestCandleWidth.pixels.half

    val drawingStart =
      layerBounds.getStart(isLtr) +
        (layerDimensions.startPadding - halfMaxCandleWidth * zoom) * layoutDirectionMultiplier -
        scroll

    var bodyCenterX: Float
    var candle: Candle
    val minBodyHeight = minCandleBodyHeight.pixels
    val visibleXRange = getVisibleXRange()

    val visibleIndices = series.getSliceIndices(visibleXRange.start, visibleXRange.endInclusive)
    if (visibleIndices.isEmpty()) return

    series.subList(visibleIndices.first, visibleIndices.last + 1).forEach { entry ->
      candle = candleProvider.getCandle(entry)
      val candleInfo = drawingModelSeries?.get(entry.x) ?: entry.toCandleInfo(yRange)
      val xSpacingMultiplier = ((entry.x - ranges.minX) / ranges.xStep).toFloat()
      bodyCenterX =
        drawingStart +
          layoutDirectionMultiplier * layerDimensions.xSpacing * xSpacingMultiplier +
          halfMaxCandleWidth * zoom

      var bodyBottomY = layerBounds.bottom - candleInfo.bodyBottomY * layerBounds.height
      var bodyTopY = layerBounds.bottom - candleInfo.bodyTopY * layerBounds.height
      val bottomWickY = layerBounds.bottom - candleInfo.bottomWickY * layerBounds.height
      val topWickY = layerBounds.bottom - candleInfo.topWickY * layerBounds.height

      if (bodyBottomY - bodyTopY < minBodyHeight) {
        bodyBottomY = (bodyBottomY + bodyTopY).half + minBodyHeight.half
        bodyTopY = bodyBottomY - minBodyHeight
      }

      if (!isExiting) {
        updateMarkerTargets(
          entry,
          key,
          bodyCenterX,
          bodyBottomY,
          bodyTopY,
          bottomWickY,
          topWickY,
          candle,
        )
      }

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
    key: Any,
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
          seriesKey = key,
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
              width = candle.body.thickness.pixels,
              height = limitedBodyBottomCanvasY - limitedBodyTopCanvasY,
              side = if (entry.absoluteChange == Change.Bearish) 1 else -1,
            ),
          closingColor =
            candle.body.effectiveStrokeFill.extractColor(
              context = this,
              width = candle.body.thickness.pixels,
              height = limitedBodyBottomCanvasY - limitedBodyTopCanvasY,
              side = if (entry.absoluteChange == Change.Bearish) -1 else 1,
            ),
          lowColor =
            candle.bottomWick.effectiveStrokeFill.extractColor(
              context = this,
              width = candle.bottomWick.thickness.pixels,
              height = lowCanvasY - limitedBodyBottomCanvasY,
              side = -1,
            ),
          highColor =
            candle.topWick.effectiveStrokeFill.extractColor(
              context = this,
              width = candle.topWick.thickness.pixels,
              height = highCanvasY - limitedBodyTopCanvasY,
            ),
        ),
      )
  }

  override fun updateChartRanges(chartRanges: MutableCartesianChartRanges) {
    // Initialize the per-model cache here, which runs off the main thread for asynchronous updates.
    widestCandleWidth
    chartRanges.tryUpdate(
      rangeProvider,
      model,
      model.minX,
      model.maxX,
      model.minY,
      model.maxY,
      verticalAxisPosition,
    )
  }

  override fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
  ) {
    with(context) {
      val candleWidth = widestCandleWidth.pixels
      val xSpacing = candleWidth + candleSpacing.pixels
      dimensions.ensureValuesAtLeast(
        xSpacing = xSpacing,
        scalableStartPadding = candleWidth.half + layerPadding.scalableStart.pixels,
        scalableEndPadding = candleWidth.half + layerPadding.scalableEnd.pixels,
        unscalableStartPadding = layerPadding.unscalableStart.pixels,
        unscalableEndPadding = layerPadding.unscalableEnd.pixels,
      )
    }
  }

  private fun CandlestickCartesianLayerModel.Entry.toCandleInfo(
    yRange: CartesianChartRanges.YRange,
  ) =
    CandlestickCartesianLayerDrawingModel.Entry(
      bodyBottomY = ((minOf(opening, closing) - yRange.minY) / yRange.length).toFloat(),
      bodyTopY = ((max(opening, closing) - yRange.minY) / yRange.length).toFloat(),
      bottomWickY = ((low - yRange.minY) / yRange.length).toFloat(),
      topWickY = ((high - yRange.minY) / yRange.length).toFloat(),
      entry = this,
    )

  /**
   * Compares the configuration and data (not the drawing caches), so that an equal
   * [CandlestickCartesianLayer] doesn’t cause the chart to be recreated. This applies to
   * [CandlestickCartesianLayer] itself; instances of subclasses compare by identity unless the
   * subclass overrides `equals` and `hashCode` (covering the settings it adds).
   */
  override fun equals(other: Any?): Boolean =
    this === other ||
      this::class == CandlestickCartesianLayer::class &&
        other is CandlestickCartesianLayer &&
        other::class == CandlestickCartesianLayer::class &&
        model == other.model &&
        candleProvider == other.candleProvider &&
        minCandleBodyHeight == other.minCandleBodyHeight &&
        candleSpacing == other.candleSpacing &&
        scaleCandleWicks == other.scaleCandleWicks &&
        rangeProvider == other.rangeProvider &&
        verticalAxisPosition == other.verticalAxisPosition &&
        drawingModelInterpolator == other.drawingModelInterpolator &&
        key == other.key

  override fun hashCode(): Int {
    if (this::class != CandlestickCartesianLayer::class) return super.hashCode()
    var result = model.hashCode()
    result = 31 * result + candleProvider.hashCode()
    result = 31 * result + minCandleBodyHeight.hashCode()
    result = 31 * result + candleSpacing.hashCode()
    result = 31 * result + scaleCandleWicks.hashCode()
    result = 31 * result + rangeProvider.hashCode()
    result = 31 * result + verticalAxisPosition.hashCode()
    result = 31 * result + drawingModelInterpolator.hashCode()
    result = 31 * result + key.hashCode()
    return result
  }

  override fun createDrawingModel(
    ranges: CartesianChartRanges,
  ): CandlestickCartesianLayerDrawingModel {
    val yRange = ranges.getYRange(verticalAxisPosition)
    return CandlestickCartesianLayerDrawingModel(
      entries = listOf(model.series.associate { it.x to it.toCandleInfo(yRange) }),
      seriesKeys = listOf(model.seriesKey),
    )
  }

  /**
   * Provides [Candle]s to [CandlestickCartesianLayer]s. Implementations must be pure functions of
   * their arguments and of the values they capture; to change the style, pass a new
   * [CandleProvider]. The [CandlestickCartesianLayer] determines the widest [Candle] by calling
   * [getCandle] for every entry once per [CandlestickCartesianLayerModel], possibly off the main
   * thread.
   *
   * In a composable function, a lambda converted to [CandleProvider] is memoized while the values
   * it captures are unchanged (compared via `equals` for stable types and by identity otherwise),
   * so it needs no explicit `remember`.
   */
  public fun interface CandleProvider {
    /** Returns the [Candle] for [entry]. */
    public fun getCandle(entry: CandlestickCartesianLayerModel.Entry): Candle

    /** Provides access to [CandleProvider] factory functions. */
    public companion object {
      internal data class Absolute(val bullish: Candle, val neutral: Candle, val bearish: Candle) :
        CandleProvider {

        override fun getCandle(entry: CandlestickCartesianLayerModel.Entry) =
          when (entry.absoluteChange) {
            Change.Bullish -> bullish
            Change.Neutral -> neutral
            Change.Bearish -> bearish
          }
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

        override fun getCandle(entry: CandlestickCartesianLayerModel.Entry) =
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
      }
    }
  }
}

internal fun LineComponent.asWick(): LineComponent =
  copy(
    fill = effectiveStrokeFill,
    thickness = Defaults.WICK_DEFAULT_WIDTH_DP.dp,
    shape = RectangleShape,
    strokeThickness = 0.dp,
  )

/**
 * Creates and remembers a [CandlestickCartesianLayer] that displays [model]. A new
 * [CandlestickCartesianLayer] is created whenever an argument changes. Its
 * [AnimatedCartesianLayer.key] is tied to the composition slot, so data updates are animated.
 */
@Composable
public fun rememberCandlestickCartesianLayer(
  model: CandlestickCartesianLayerModel,
  candleProvider: CandlestickCartesianLayer.CandleProvider =
    CandlestickCartesianLayer.CandleProvider.absolute(),
  minCandleBodyHeight: Dp = Defaults.MIN_CANDLE_BODY_HEIGHT_DP.dp,
  candleSpacing: Dp = Defaults.CANDLE_SPACING_DP.dp,
  scaleCandleWicks: Boolean = false,
  rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
  verticalAxisPosition: Axis.Position.Vertical? = null,
  drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<CandlestickCartesianLayerDrawingModel> =
    CartesianLayerDrawingModelInterpolator.candlestick(),
): CandlestickCartesianLayer {
  val key = rememberCartesianLayerKey()
  return remember(
    model,
    candleProvider,
    minCandleBodyHeight,
    candleSpacing,
    scaleCandleWicks,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
  ) {
    CandlestickCartesianLayer(
      model,
      candleProvider,
      minCandleBodyHeight,
      candleSpacing,
      scaleCandleWicks,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
      key,
    )
  }
}
