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

package com.patrykandpatrick.vico.compose.cartesian.data

import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.common.gcdWith
import kotlin.math.abs

/**
 * Stores a [CartesianLayer]’s data. [CartesianLayerModel]s are immutable values. They can be
 * created anywhere, including off the main thread, and are passed to [CartesianLayer]s.
 *
 * Chart hosts compare [CartesianLayerModel]s to determine whether a chart’s data has changed, so
 * [equals] must be structural, and [hashCode] should be cheap (e.g., computed once and cached).
 */
public interface CartesianLayerModel {
  /** The minimum _x_-value. */
  public val minX: Double

  /** The maximum _x_-value. */
  public val maxX: Double

  /** The minimum _y_-value. */
  public val minY: Double

  /** The maximum _y_-value. */
  public val maxY: Double

  /**
   * Whether this [CartesianLayerModel] has no entries. An empty model is valid: its layer draws
   * nothing, the extents above are all 0, and, having no extents of its own, its range provider
   * receives the ranges established by the chart’s other layers, or zeros (see
   * [CartesianLayerRangeProvider]).
   */
  public val isEmpty: Boolean
    get() = false

  /** Returns the greatest common divisor of the _x_-values’ differences. */
  public fun getXDeltaGcd(): Double

  override fun equals(other: Any?): Boolean

  override fun hashCode(): Int

  /** Represents a single entity in a [CartesianLayerModel]. */
  public interface Entry {
    /** The _x_-coordinate. */
    public val x: Double

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
  }
}

/**
 * Returns whether [other] holds the same data as this [CartesianLayerModel]. Identity is checked
 * first, then the (cached) hash codes, and only then are the models compared structurally.
 */
internal fun CartesianLayerModel.isSameDataAs(other: CartesianLayerModel?): Boolean =
  this === other || other != null && hashCode() == other.hashCode() && this == other

internal fun List<CartesianLayerModel.Entry>.getXDeltaGcd(): Double {
  if (isEmpty()) return 1.0
  val iterator = iterator()
  var prevX = iterator.next().x
  var gcd: Double? = null
  while (iterator.hasNext()) {
    val x = iterator.next().x
    val delta = abs(x - prevX)
    prevX = x
    if (delta == 0.0) continue
    if (gcd != null) {
      gcd = gcd.gcdWith(delta)
      require(gcd != 0.0) {
        "The x-values are too precise. The maximum precision is four decimal places."
      }
    } else {
      gcd = delta
    }
  }
  return gcd ?: 1.0
}

internal inline fun <T : CartesianLayerModel.Entry> List<T>.forEachIn(
  minX: Double,
  maxX: Double,
  padding: Int = 0,
  action: (T, T?) -> Unit,
) {
  var start = 0
  var end = 0
  for (entry in this) {
    when {
      entry.x < minX -> start++
      entry.x > maxX -> break
    }
    end++
  }
  start = (start - padding).coerceAtLeast(0)
  end = (end + padding).coerceAtMost(lastIndex)
  for (i in start..end) action(this[i], getOrNull(i + 1))
}

internal fun <T : CartesianLayerModel.Entry> List<T>.getSliceIndices(
  visibleXRangeStart: Double,
  visibleXRangeEnd: Double,
  visiblePadding: Int? = 1,
): IntRange {
  if (isEmpty()) return IntRange.EMPTY
  if (visiblePadding == null) return indices

  var firstVisible = size
  for (index in indices) {
    if (this[index].x >= visibleXRangeStart) {
      firstVisible = index
      break
    }
  }

  var lastVisibleExclusive = size
  for (index in firstVisible until size) {
    if (this[index].x > visibleXRangeEnd) {
      lastVisibleExclusive = index
      break
    }
  }

  val start = (firstVisible - visiblePadding).coerceAtLeast(0)
  val endExclusive = (lastVisibleExclusive + visiblePadding).coerceAtMost(size)
  return if (endExclusive <= start) IntRange.EMPTY else start..<endExclusive
}
