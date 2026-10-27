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
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/** Maps each series of a [ColumnCartesianLayerModel] to a stack, ordered by first occurrence. */
internal class ColumnStackLayout(
  val stackIndices: IntArray,
  val firstSeriesIndices: IntArray,
  val stackIndexBySeriesKey: Map<Any, Int>,
) {
  val stackCount: Int
    get() = firstSeriesIndices.size
}

/**
 * Creates a [ColumnStackLayout], calling [stackKey] once per series. Series with equal non-null
 * results share a stack, and a `null` result gives a series its own stack.
 */
internal fun createColumnStackLayout(
  seriesKeys: List<Any>,
  stackKey: (seriesKey: Any) -> Any?,
): ColumnStackLayout {
  val indicesByKey = mutableMapOf<Any, Int>()
  val firstSeriesIndices = mutableListOf<Int>()
  val stackIndices = IntArray(seriesKeys.size)
  seriesKeys.forEachIndexed { seriesIndex, seriesKey ->
    val key = stackKey(seriesKey)
    stackIndices[seriesIndex] =
      if (key == null) {
        firstSeriesIndices.size.also { firstSeriesIndices += seriesIndex }
      } else {
        indicesByKey.getOrPut(key) {
          firstSeriesIndices.size.also { firstSeriesIndices += seriesIndex }
        }
      }
  }
  return ColumnStackLayout(
    stackIndices,
    firstSeriesIndices.toIntArray(),
    seriesKeys.indices.associate { seriesKeys[it] to stackIndices[it] },
  )
}

/**
 * Returns the _y_ range spanned by the stacks of [layout], summing positive and negative values
 * separately for each stack and _x_ value.
 */
internal fun ColumnCartesianLayerModel.getStackedYRange(
  layout: ColumnStackLayout
): ClosedFloatingPointRange<Double> {
  val sumsByStack = Array(layout.stackCount) { mutableMapOf<Double, DoubleArray>() }
  series.forEachIndexed { seriesIndex, entries ->
    val sums = sumsByStack[layout.stackIndices[seriesIndex]]
    entries.forEach { entry ->
      val totals = sums.getOrPut(entry.x) { DoubleArray(2) }
      totals[if (entry.y < 0) 0 else 1] += entry.y
    }
  }
  var minY = 0.0
  var maxY = 0.0
  sumsByStack.forEach { sums ->
    sums.values.forEach { totals ->
      minY = minOf(minY, totals[0])
      maxY = maxOf(maxY, totals[1])
    }
  }
  return minY..maxY
}

/**
 * Caches the last [ColumnStackLayout], keyed by the series keys and the selector. Range updates run
 * on a background thread while drawing runs on the main thread, so the cache is an immutable
 * snapshot published atomically.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class ColumnStackResolver {
  private class Snapshot(
    val seriesKeys: List<Any>,
    val stackKey: (seriesKey: Any) -> Any?,
    val layout: ColumnStackLayout,
  )

  private val snapshot = AtomicReference<Snapshot?>(null)

  fun resolve(seriesKeys: List<Any>, stackKey: (seriesKey: Any) -> Any?): ColumnStackLayout {
    snapshot.load()?.let { cached ->
      if (cached.stackKey == stackKey && cached.seriesKeys == seriesKeys) return cached.layout
    }
    val layout = createColumnStackLayout(seriesKeys, stackKey)
    snapshot.store(Snapshot(seriesKeys.toList(), stackKey, layout))
    return layout
  }
}

/**
 * Holds the intrinsic horizontal geometry of a [ColumnStackLayout]: each stack is as wide as its
 * widest column, and stacks are [columnSpacing] apart. Pixel values are unzoomed.
 */
internal class ColumnStackGeometry(
  val layout: ColumnStackLayout,
  val stackKey: (seriesKey: Any) -> Any?,
  val columnSpacing: Dp,
  val density: Float,
  val extraStore: ExtraStore,
  val widestThicknesses: Array<Dp>,
  val offsets: FloatArray,
  val collectionWidth: Float,
)
