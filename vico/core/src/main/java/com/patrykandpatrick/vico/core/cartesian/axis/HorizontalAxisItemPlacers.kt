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

package com.patrykandpatrick.vico.core.cartesian.axis

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.roundedToNearest
import kotlin.math.ceil
import kotlin.math.floor

private const val LABEL_OVERFLOW_SIZE = 2
private const val SEGMENTED_TICK_OVERFLOW_SIZE = 1

private val CartesianChartRanges.measuredLabelValues
  get() = buildList {
    add(minX)
    if (xLength < xStep) return@buildList
    add(minX + xStep * floor(xLength / xStep))
    if (xLength >= 2 * xStep) add(minX + xStep * (xLength.half / xStep).roundedToNearest)
  }

private fun CartesianDrawingContext.getLabelValues(
  visibleXRange: ClosedFloatingPointRange<Double>,
  fullXRange: ClosedFloatingPointRange<Double>,
  offset: Int = 0,
  spacing: Int = 1,
): List<Double> {
  val remainder = ((visibleXRange.start - ranges.minX) / ranges.xStep - offset) % spacing
  val firstValue = visibleXRange.start + (spacing - remainder) % spacing * ranges.xStep
  val minXOffset = ranges.minX % ranges.xStep
  val values = mutableListOf<Double>()
  var multiplier = -LABEL_OVERFLOW_SIZE
  var hasEndOverflow = false
  while (true) {
    var potentialValue = firstValue + multiplier++ * spacing * ranges.xStep
    potentialValue =
      ranges.xStep * ((potentialValue - minXOffset) / ranges.xStep).roundedToNearest + minXOffset
    if (potentialValue < ranges.minX || potentialValue == fullXRange.start) continue
    if (potentialValue > ranges.maxX || potentialValue == fullXRange.endInclusive) break
    values += potentialValue
    if (
      potentialValue > visibleXRange.endInclusive && hasEndOverflow.also { hasEndOverflow = true }
    ) {
      break
    }
  }
  return values
}

internal abstract class BaseHorizontalAxisItemPlacer(private val shiftExtremeLines: Boolean) :
  HorizontalAxis.ItemPlacer {
  override fun getShiftExtremeLines(context: CartesianDrawingContext) = shiftExtremeLines

  override fun getHeightMeasurementLabelValues(
    context: CartesianMeasuringContext,
    horizontalDimensions: HorizontalDimensions,
    fullXRange: ClosedFloatingPointRange<Double>,
    maxLabelWidth: Float,
  ) = context.ranges.measuredLabelValues
}

internal class AlignedHorizontalAxisItemPlacer(
  private val spacing: Int,
  private val offset: Int,
  private val shiftExtremeLines: Boolean,
  private val addExtremeLabelPadding: Boolean,
) : BaseHorizontalAxisItemPlacer(shiftExtremeLines) {
  init {
    require(spacing > 0) { "`spacing` must be positive." }
    require(offset >= 0) { "`offset` must be nonnegative." }
  }

  override fun getFirstLabelValue(context: CartesianMeasuringContext, maxLabelWidth: Float) =
    if (addExtremeLabelPadding) context.ranges.minX + offset * context.ranges.xStep else null

  override fun getLastLabelValue(context: CartesianMeasuringContext, maxLabelWidth: Float) =
    if (addExtremeLabelPadding) {
      with(context.ranges) { maxX - (xLength - xStep * offset) % (xStep * spacing) }
    } else {
      null
    }

  override fun getLabelValues(
    context: CartesianDrawingContext,
    visibleXRange: ClosedFloatingPointRange<Double>,
    fullXRange: ClosedFloatingPointRange<Double>,
    maxLabelWidth: Float,
  ) =
    context.getLabelValues(
      visibleXRange = visibleXRange,
      fullXRange = fullXRange,
      offset = offset,
      spacing =
        spacing *
          if (addExtremeLabelPadding && maxLabelWidth != 0f) {
            ceil(maxLabelWidth / (context.horizontalDimensions.xSpacing * spacing)).toInt()
          } else {
            1
          },
    )

  override fun getWidthMeasurementLabelValues(
    context: CartesianMeasuringContext,
    horizontalDimensions: HorizontalDimensions,
    fullXRange: ClosedFloatingPointRange<Double>,
  ) = if (addExtremeLabelPadding) context.ranges.measuredLabelValues else emptyList()

  override fun getStartHorizontalAxisInset(
    context: CartesianMeasuringContext,
    horizontalDimensions: HorizontalDimensions,
    tickThickness: Float,
    maxLabelWidth: Float,
  ): Float {
    val tickSpace = if (shiftExtremeLines) tickThickness else tickThickness.half
    return (tickSpace - horizontalDimensions.unscalableStartPadding).coerceAtLeast(0f)
  }

  override fun getEndHorizontalAxisInset(
    context: CartesianMeasuringContext,
    horizontalDimensions: HorizontalDimensions,
    tickThickness: Float,
    maxLabelWidth: Float,
  ): Float {
    val tickSpace = if (shiftExtremeLines) tickThickness else tickThickness.half
    return (tickSpace - horizontalDimensions.unscalableEndPadding).coerceAtLeast(0f)
  }
}

internal class SegmentedHorizontalAxisItemPlacer(private val shiftExtremeLines: Boolean) :
  BaseHorizontalAxisItemPlacer(shiftExtremeLines) {
  override fun getLabelValues(
    context: CartesianDrawingContext,
    visibleXRange: ClosedFloatingPointRange<Double>,
    fullXRange: ClosedFloatingPointRange<Double>,
    maxLabelWidth: Float,
  ) = context.getLabelValues(visibleXRange, fullXRange)

  override fun getWidthMeasurementLabelValues(
    context: CartesianMeasuringContext,
    horizontalDimensions: HorizontalDimensions,
    fullXRange: ClosedFloatingPointRange<Double>,
  ) = emptyList<Double>()

  override fun getLineValues(
    context: CartesianDrawingContext,
    visibleXRange: ClosedFloatingPointRange<Double>,
    fullXRange: ClosedFloatingPointRange<Double>,
    maxLabelWidth: Float,
  ) =
    with(context) {
      val remainder = (visibleXRange.start - ranges.minX + ranges.xStep.half) % ranges.xStep
      val firstValue = visibleXRange.start + (ranges.xStep - remainder) % ranges.xStep
      var multiplier = -SEGMENTED_TICK_OVERFLOW_SIZE
      val values = mutableListOf<Double>()
      while (true) {
        val potentialValue = firstValue + multiplier++ * ranges.xStep
        if (potentialValue < ranges.minX - ranges.xStep.half) continue
        if (potentialValue > ranges.maxX + ranges.xStep.half) break
        values += potentialValue
        if (potentialValue > visibleXRange.endInclusive) break
      }
      values
    }

  override fun getStartHorizontalAxisInset(
    context: CartesianMeasuringContext,
    horizontalDimensions: HorizontalDimensions,
    tickThickness: Float,
    maxLabelWidth: Float,
  ) = if (shiftExtremeLines) tickThickness else tickThickness.half

  override fun getEndHorizontalAxisInset(
    context: CartesianMeasuringContext,
    horizontalDimensions: HorizontalDimensions,
    tickThickness: Float,
    maxLabelWidth: Float,
  ) = if (shiftExtremeLines) tickThickness else tickThickness.half
}
