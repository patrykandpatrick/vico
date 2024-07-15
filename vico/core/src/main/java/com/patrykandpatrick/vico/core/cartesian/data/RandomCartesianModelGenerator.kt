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

package com.patrykandpatrick.vico.core.cartesian.data

import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.common.length
import com.patrykandpatrick.vico.core.common.random
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public object RandomCartesianModelGenerator {
  public val defaultX: IntProgression = 0..96
  public val defaultY: ClosedFloatingPointRange<Double> = 2.0..20.0
  public val defaultOpeningClosingRange: ClosedFloatingPointRange<Double> = 5.0..15.0
  public val defaultLowHighRange: ClosedFloatingPointRange<Double> = 0.5..5.0

  public fun getRandomColumnLayerModelPartial(
    seriesCount: Int = 1,
    x: IntProgression = defaultX,
    y: ClosedFloatingPointRange<Double> = defaultY,
  ): ColumnCartesianLayerModel.Partial =
    ColumnCartesianLayerModel.partial {
      repeat(seriesCount) { series(x.toList(), x.map { y.random() }) }
    }

  public fun getRandomLineLayerModelPartial(
    seriesCount: Int = 1,
    x: IntProgression = defaultX,
    y: ClosedFloatingPointRange<Double> = defaultY,
  ): LineCartesianLayerModel.Partial =
    LineCartesianLayerModel.partial {
      repeat(seriesCount) { series(x.toList(), x.map { y.random() }) }
    }

  public fun getRandomCandlestickLayerModelPartial(
    x: IntProgression = defaultX,
    openingClosingRange: ClosedFloatingPointRange<Double> = defaultOpeningClosingRange,
    lowHighRange: ClosedFloatingPointRange<Double> = defaultLowHighRange,
  ): CandlestickCartesianLayerModel.Partial {
    var previousClosingPrice: Double? = null
    val opening = mutableListOf<Double>()
    val closing = mutableListOf<Double>()
    val low = mutableListOf<Double>()
    val high = mutableListOf<Double>()
    val maxOpeningPriceDelta = 0.2 * openingClosingRange.length
    val changeOverrideThreshold = 0.2 * openingClosingRange.length
    val maxClosingPriceDelta = 0.8 * openingClosingRange.length
    for (i in x) {
      val openingPrice =
        if (previousClosingPrice != null) {
          previousClosingPrice +
            Random.nextFloat() *
              if (Random.nextBoolean()) {
                (openingClosingRange.endInclusive - previousClosingPrice).coerceAtMost(
                  maxOpeningPriceDelta
                )
              } else {
                (openingClosingRange.start - previousClosingPrice).coerceAtLeast(
                  -maxOpeningPriceDelta
                )
              }
        } else {
          openingClosingRange.random()
        }
      val isBullish =
        when {
          openingPrice - openingClosingRange.start < changeOverrideThreshold -> true
          openingClosingRange.endInclusive - openingPrice < changeOverrideThreshold -> false
          else -> Random.nextBoolean()
        }
      val closingPrice =
        openingPrice +
          Random.nextFloat() *
            if (isBullish) {
              (openingClosingRange.endInclusive - openingPrice).coerceAtMost(maxClosingPriceDelta)
            } else {
              (openingClosingRange.start - openingPrice).coerceAtLeast(-maxClosingPriceDelta)
            }
      opening += openingPrice
      closing += closingPrice
      low += min(openingPrice, closingPrice) - lowHighRange.random()
      high += max(openingPrice, closingPrice) + lowHighRange.random()
      previousClosingPrice = closingPrice
    }
    return CandlestickCartesianLayerModel.partial(x.toList(), opening, closing, low, high)
  }

  public fun getRandomModel(
    columnSeriesCount: Int = 1,
    lineSeriesCount: Int = 1,
    x: IntProgression = defaultX,
    y: ClosedFloatingPointRange<Double> = defaultY,
  ): CartesianChartModel =
    CartesianChartModel(
      buildList {
        add(getRandomColumnLayerModelPartial(columnSeriesCount, x, y).complete())
        add(getRandomLineLayerModelPartial(lineSeriesCount, x, y).complete())
        add(getRandomCandlestickLayerModelPartial(x).complete())
      }
    )
}
