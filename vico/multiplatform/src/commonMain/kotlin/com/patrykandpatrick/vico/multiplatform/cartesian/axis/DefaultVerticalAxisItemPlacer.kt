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

package com.patrykandpatrick.vico.multiplatform.cartesian.axis

import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.multiplatform.common.Position
import com.patrykandpatrick.vico.multiplatform.common.data.CacheStore
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.getDivisors
import com.patrykandpatrick.vico.multiplatform.common.half
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

internal class DefaultVerticalAxisItemPlacer(
  private val mode: Mode,
  private val shiftTopLines: Boolean,
) : VerticalAxis.ItemPlacer {
  override fun getShiftTopLines(context: CartesianDrawingContext): Boolean = shiftTopLines

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
  ): List<Double> = mode.getLabelValues(context, axisHeight, maxLabelHeight, position)

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
      !mode.insetsRequired(context) -> 0f
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
  ): Float =
    when {
      !mode.insetsRequired(context) -> 0f
      verticalLabelPosition == Position.Vertical.Top -> maxLineThickness
      verticalLabelPosition == Position.Vertical.Center ->
        (max(maxLabelHeight, maxLineThickness) + maxLineThickness).half
      else -> maxLabelHeight + maxLineThickness.half
    }

  sealed interface Mode {
    fun getSimpleLabelValues(
      context: CartesianMeasuringContext,
      axisHeight: Float,
      maxLabelHeight: Float,
      position: Axis.Position.Vertical,
    ): List<Double>

    fun getMixedLabelValues(
      context: CartesianMeasuringContext,
      axisHeight: Float,
      maxLabelHeight: Float,
      position: Axis.Position.Vertical,
    ): List<Double>

    fun insetsRequired(context: CartesianMeasuringContext): Boolean = true

    fun getLabelValues(
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

    class Step(private val step: (ExtraStore) -> Double?) : Mode {
      private fun CartesianMeasuringContext.getStepOrThrow() =
        step(model.extraStore)?.also { require(it > 0) { "`step` must return a positive value." } }

      private fun getPartialLabelValues(
        context: CartesianMeasuringContext,
        minY: Double,
        maxY: Double,
        freeHeight: Float,
        maxLabelHeight: Float,
        multiplier: Int = 1,
      ): List<Double> {
        val requestedStep = context.getStepOrThrow()
        return context.cacheStore.getOrSet(
          cacheKeyNamespace,
          requestedStep,
          maxY,
          minY,
          freeHeight,
          maxLabelHeight,
          multiplier,
        ) {
          val values = mutableListOf<Double>()
          val requestedOrDefaultStep = requestedStep ?: 10.0.pow(floor(log10(maxY)) - 1)
          val step =
            if (maxLabelHeight != 0f) {
              val minStep = (maxY - minY) / floor(freeHeight / maxLabelHeight)
              ((maxY - minY) / requestedOrDefaultStep)
                .takeIf { it == floor(it) }
                ?.toInt()
                ?.getDivisors(includeDividend = false)
                ?.firstOrNull { it * requestedOrDefaultStep >= minStep }
                ?.let { it * requestedOrDefaultStep }
                ?: (ceil(minStep / requestedOrDefaultStep) * requestedOrDefaultStep)
            } else {
              requestedOrDefaultStep
            }
          repeat(((maxY - minY) / step).toInt()) { values += multiplier * (minY + (it + 1) * step) }
          values
        }
      }

      override fun getSimpleLabelValues(
        context: CartesianMeasuringContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: Axis.Position.Vertical,
      ): List<Double> =
        context.ranges.getYRange(position).run {
          if (maxY > 0) {
            getPartialLabelValues(context, minY, maxY, axisHeight, maxLabelHeight) + minY
          } else {
            getPartialLabelValues(
              context = context,
              minY = abs(maxY),
              maxY = abs(minY),
              freeHeight = axisHeight,
              maxLabelHeight = maxLabelHeight,
              multiplier = -1,
            ) + maxY
          }
        }

      override fun getMixedLabelValues(
        context: CartesianMeasuringContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: Axis.Position.Vertical,
      ): List<Double> =
        context.ranges.getYRange(position).run {
          val topLabelValues =
            getPartialLabelValues(
              context = context,
              minY = 0.0,
              maxY = maxY,
              freeHeight = (maxY / length).toFloat() * axisHeight,
              maxLabelHeight = maxLabelHeight,
            )
          val bottomLabelValues =
            getPartialLabelValues(
              context = context,
              minY = 0.0,
              maxY = abs(minY),
              freeHeight = (-minY / length).toFloat() * axisHeight,
              maxLabelHeight = maxLabelHeight,
              multiplier = -1,
            )
          topLabelValues + bottomLabelValues + 0.0
        }

      private companion object {
        val cacheKeyNamespace = CacheStore.KeyNamespace()
      }
    }

    class Count(private val count: (ExtraStore) -> Int?) : Mode {
      private fun CartesianMeasuringContext.getCountOrThrow() =
        count(model.extraStore)?.also {
          require(it >= 0) { "`count` must return a nonnegative value." }
        }

      override fun getSimpleLabelValues(
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

      override fun getMixedLabelValues(
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
          if (requestedItemCount != null) (requestedItemCount - 1) * topHeight / axisHeight
          else null
        val maxBottomItemCount =
          if (requestedItemCount != null) (requestedItemCount - 1) * bottomHeight / axisHeight
          else null
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

      override fun insetsRequired(context: CartesianMeasuringContext): Boolean =
        context.getCountOrThrow() != 0
    }
  }
}
