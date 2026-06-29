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

package com.patrykandpatrick.vico.compose.cartesian.axis

import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.data.CacheStore
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.getDivisors
import com.patrykandpatrick.vico.compose.common.half
import kotlin.math.*

internal class StepVerticalAxisItemPlacer(
  private val step: (ExtraStore) -> Double?,
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
      verticalLabelPosition == Position.Vertical.Top -> maxLineThickness
      verticalLabelPosition == Position.Vertical.Center ->
        (max(maxLabelHeight, maxLineThickness) + maxLineThickness).half
      else -> maxLabelHeight + maxLineThickness.half
    }

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

  private fun getSimpleLabelValues(
    context: CartesianMeasuringContext,
    axisHeight: Float,
    maxLabelHeight: Float,
    position: Axis.Position.Vertical,
  ) =
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

  private fun getMixedLabelValues(
    context: CartesianMeasuringContext,
    axisHeight: Float,
    maxLabelHeight: Float,
    position: Axis.Position.Vertical,
  ) =
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
