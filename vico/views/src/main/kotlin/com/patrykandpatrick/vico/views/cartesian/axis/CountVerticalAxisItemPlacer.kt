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

package com.patrykandpatrick.vico.views.cartesian.axis

import com.patrykandpatrick.vico.views.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.views.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.views.common.Position
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import com.patrykandpatrick.vico.views.common.half
import kotlin.math.max

internal class CountVerticalAxisItemPlacer(
  private val count: (ExtraStore) -> Int?,
  private val shiftTopLines: Boolean,
) : VerticalAxis.ItemPlacer {
  override fun getShiftTopLines(context: CartesianDrawingContext) = shiftTopLines

  override fun getLabelValues(
    context: CartesianDrawingContext,
    axisHeight: Float,
    maxLabelHeight: Float,
    position: Axis.Position.Vertical,
  ) = getWidthMeasurementLabelValues(context, axisHeight, maxLabelHeight, position)

  override fun getWidthMeasurementLabelValues(
    context: CartesianMeasuringContext,
    axisHeight: Float,
    maxLabelHeight: Float,
    position: Axis.Position.Vertical,
  ) =
    if (context.ranges.getYRange(position).run { minY * maxY } >= 0) {
      getSimpleLabelValues(context, axisHeight, maxLabelHeight, position)
    } else {
      getMixedLabelValues(context, axisHeight, maxLabelHeight, position)
    }

  override fun getHeightMeasurementLabelValues(
    context: CartesianMeasuringContext,
    position: Axis.Position.Vertical,
  ): List<Double> {
    val yRange = context.ranges.getYRange(position)
    return listOf(yRange.minY, (yRange.minY + yRange.maxY).half, yRange.maxY)
  }

  override fun getTopLayerMargin(
    context: CartesianMeasuringContext,
    verticalLabelPosition: Position.Vertical,
    maxLabelHeight: Float,
    maxLineThickness: Float,
  ) =
    when {
      !insetsRequired(context) -> 0f
      verticalLabelPosition == Position.Vertical.Top ->
        maxLabelHeight + (if (shiftTopLines) maxLineThickness else -maxLineThickness).half
      verticalLabelPosition == Position.Vertical.Center ->
        (max(maxLabelHeight, maxLineThickness) +
            if (shiftTopLines) maxLineThickness else -maxLineThickness)
          .half
      else -> if (shiftTopLines) maxLineThickness else 0f
    }

  override fun getBottomLayerMargin(
    context: CartesianMeasuringContext,
    verticalLabelPosition: Position.Vertical,
    maxLabelHeight: Float,
    maxLineThickness: Float,
  ) =
    when {
      !insetsRequired(context) -> 0f
      verticalLabelPosition == Position.Vertical.Top -> maxLineThickness
      verticalLabelPosition == Position.Vertical.Center ->
        (max(maxLabelHeight, maxLineThickness) + maxLineThickness).half
      else -> maxLabelHeight + maxLineThickness.half
    }

  private fun CartesianMeasuringContext.getCountOrThrow() =
    count(model.extraStore)?.also {
      require(it >= 0) { "`count` must return a nonnegative value." }
    }

  private fun insetsRequired(context: CartesianMeasuringContext) = context.getCountOrThrow() != 0

  private fun getSimpleLabelValues(
    context: CartesianMeasuringContext,
    axisHeight: Float,
    maxLabelHeight: Float,
    position: Axis.Position.Vertical,
  ): List<Double> {
    val values = mutableListOf<Double>()
    val requestedItemCount = context.getCountOrThrow()
    if (requestedItemCount == 0) return values
    val yRange = context.ranges.getYRange(position)
    values += yRange.minY
    if (requestedItemCount == 1) return values
    if (maxLabelHeight == 0f) {
      values += yRange.maxY
      return values
    }
    var extraItemCount = (axisHeight / maxLabelHeight).toInt()
    if (requestedItemCount != null)
      extraItemCount = extraItemCount.coerceAtMost(requestedItemCount - 1)
    val step = yRange.length / extraItemCount
    repeat(extraItemCount) { values += yRange.minY + (it + 1) * step }
    return values
  }

  private fun getMixedLabelValues(
    context: CartesianMeasuringContext,
    axisHeight: Float,
    maxLabelHeight: Float,
    position: Axis.Position.Vertical,
  ): List<Double> {
    val values = mutableListOf<Double>()
    val requestedItemCount = context.getCountOrThrow()
    if (requestedItemCount == 0) return values
    values += 0.0
    if (requestedItemCount == 1) return values
    val yRange = context.ranges.getYRange(position)
    if (maxLabelHeight == 0f) {
      values += yRange.minY
      values += yRange.maxY
      return values
    }
    val topHeight = yRange.maxY / yRange.length * axisHeight
    val bottomHeight = -yRange.minY / yRange.length * axisHeight
    val maxTopItemCount =
      if (requestedItemCount != null) (requestedItemCount - 1) * topHeight / axisHeight else null
    val maxBottomItemCount =
      if (requestedItemCount != null) (requestedItemCount - 1) * bottomHeight / axisHeight else null
    val topItemCountByHeight = topHeight / maxLabelHeight
    val bottomItemCountByHeight = bottomHeight / maxLabelHeight
    var topItemCount =
      topItemCountByHeight
        .let { if (maxTopItemCount != null) it.coerceAtMost(maxTopItemCount) else it }
        .toInt()
    var bottomItemCount =
      bottomItemCountByHeight
        .let { if (maxBottomItemCount != null) it.coerceAtMost(maxBottomItemCount) else it }
        .toInt()
    if (requestedItemCount == null || topItemCount + bottomItemCount + 1 < requestedItemCount) {
      val isTopNotDenser = topItemCount / topHeight <= bottomItemCount / bottomHeight
      val isTopFillable = topItemCountByHeight - topItemCount >= 1
      val isBottomFillable = bottomItemCountByHeight - bottomItemCount >= 1
      if (isTopFillable && (isTopNotDenser || !isBottomFillable)) {
        topItemCount++
      } else if (isBottomFillable) {
        bottomItemCount++
      }
    }
    if (topItemCount != 0) {
      val step = yRange.maxY / topItemCount
      repeat(topItemCount) { values += (it + 1) * step }
    }
    if (bottomItemCount != 0) {
      val step = yRange.minY / bottomItemCount
      repeat(bottomItemCount) { values += (it + 1) * step }
    }
    return values
  }
}
