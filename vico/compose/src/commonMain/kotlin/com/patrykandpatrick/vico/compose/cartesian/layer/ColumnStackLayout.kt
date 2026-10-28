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

import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlin.math.max
import kotlin.math.min

/**
 * Maps the series of a [ColumnCartesianLayerModel] to stacks. Stacks are ordered by their first
 * series. [seriesIndices] lists the series indices stack by stack, and the series of the stack with
 * index `i` are at indices `stackStarts[i]..<stackStarts[i + 1]`.
 */
internal class ColumnStackLayout(
  val stackIndices: IntArray,
  val seriesIndices: IntArray,
  val stackStarts: IntArray,
) {
  val stackCount: Int
    get() = stackStarts.size - 1

  fun getStackSize(stackIndex: Int): Int = stackStarts[stackIndex + 1] - stackStarts[stackIndex]

  inline fun forEachSeriesIndex(stackIndex: Int, action: (seriesIndex: Int) -> Unit) {
    for (index in stackStarts[stackIndex]..<stackStarts[stackIndex + 1]) {
      action(seriesIndices[index])
    }
  }
}

internal fun ColumnCartesianLayer.StackKeyProvider.getLayout(
  seriesKeys: List<Any>,
  extraStore: ExtraStore,
): ColumnStackLayout {
  val stackIndicesByKey = mutableMapOf<Any, Int>()
  val stackIndices = IntArray(seriesKeys.size)
  var stackCount = 0
  seriesKeys.forEachIndexed { seriesIndex, seriesKey ->
    stackIndices[seriesIndex] =
      stackIndicesByKey.getOrPut(getStackKey(seriesKey, seriesIndex, extraStore)) { stackCount++ }
  }
  val stackStarts = IntArray(stackCount + 1)
  stackIndices.forEach { stackIndex -> stackStarts[stackIndex + 1]++ }
  for (stackIndex in 1..stackCount) stackStarts[stackIndex] += stackStarts[stackIndex - 1]
  val seriesIndices = IntArray(seriesKeys.size)
  val nextPositions = stackStarts.copyOf()
  stackIndices.forEachIndexed { seriesIndex, stackIndex ->
    seriesIndices[nextPositions[stackIndex]++] = seriesIndex
  }
  return ColumnStackLayout(stackIndices, seriesIndices, stackStarts)
}

/**
 * Returns the _y_-range spanned by the stacks of [layout]. A single-series stack contributes its
 * _y_-values. A multi-series stack contributes, for each _x_-value, the sum of its positive
 * _y_-values and the sum of its negative _y_-values.
 */
internal fun ColumnCartesianLayerModel.getYRange(
  layout: ColumnStackLayout
): ClosedFloatingPointRange<Double> {
  if (layout.stackCount == series.size) return minY..maxY
  if (layout.stackCount == 1) return minAggregateY..maxAggregateY
  var minY = Double.POSITIVE_INFINITY
  var maxY = Double.NEGATIVE_INFINITY
  val sums = mutableMapOf<Double, DoubleArray>()
  for (stackIndex in 0..<layout.stackCount) {
    if (layout.getStackSize(stackIndex) == 1) {
      layout.forEachSeriesIndex(stackIndex) { seriesIndex ->
        series[seriesIndex].forEach { entry ->
          minY = min(minY, entry.y)
          maxY = max(maxY, entry.y)
        }
      }
      continue
    }
    layout.forEachSeriesIndex(stackIndex) { seriesIndex ->
      series[seriesIndex].forEach { entry ->
        sums.getOrPut(entry.x) { DoubleArray(2) }[if (entry.y < 0) 0 else 1] += entry.y
      }
    }
    sums.values.forEach { (negativeSum, positiveSum) ->
      minY = min(minY, negativeSum)
      maxY = max(maxY, positiveSum)
    }
    sums.clear()
  }
  return minY..maxY
}

/**
 * Holds the unzoomed horizontal geometry of a [ColumnStackLayout]: each stack is as wide as its
 * widest column, and stacks are positioned the stack spacing apart. [seriesKeys], [extraStore], and
 * [density] identify the inputs.
 */
internal class ColumnStackGeometry(
  val seriesKeys: List<Any>,
  val extraStore: ExtraStore,
  val density: Float,
  val layout: ColumnStackLayout,
  val thicknesses: Array<Dp>,
  val offsets: FloatArray,
  val width: Float,
)
