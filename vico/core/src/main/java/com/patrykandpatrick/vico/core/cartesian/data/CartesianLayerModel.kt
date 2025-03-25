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

package com.patrykandpatrick.vico.core.cartesian.data

import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.common.data.CartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.gcdWith
import kotlin.math.abs

/** Stores a [CartesianLayer]’s data. */
public interface CartesianLayerModel {
  /** Identifies this [CartesianLayerModel]. */
  public val id: Int

  /** The minimum _x_ value. */
  public val minX: Double

  /** The maximum _x_ value. */
  public val maxX: Double

  /** The minimum _y_ value. */
  public val minY: Double

  /** The maximum _y_ value. */
  public val maxY: Double

  /** Stores auxiliary data, including [CartesianLayerDrawingModel]s. */
  public val extraStore: ExtraStore

  /** Returns the greatest common divisor of the _x_ values’ differences. */
  public fun getXDeltaGcd(): Double

  /** Creates a copy of this [CartesianLayerModel] with the given [ExtraStore]. */
  public fun copy(extraStore: ExtraStore): CartesianLayerModel

  override fun equals(other: Any?): Boolean

  override fun hashCode(): Int

  /** Represents a single entity in a [CartesianLayerModel]. */
  public interface Entry {
    /** The _x_ coordinate. */
    public val x: Double

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
  }

  /**
   * Stores the minimum amount of data required to create a [CartesianLayerModel] and facilitates
   * this creation.
   */
  public interface Partial {
    /** Creates a full [CartesianLayerModel] with the given [ExtraStore] from this [Partial]. */
    public fun complete(extraStore: ExtraStore = ExtraStore.Empty): CartesianLayerModel
  }
}

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
  (start..end).forEach { action(this[it], getOrNull(it + 1)) }
}

internal fun <T : CartesianLayerModel.Entry> List<T>.getSliceIndices(
  layerXRangeStart: Double,
  layerXRangeEnd: Double,
  visibleXRangeStart: Double,
  visibleXRangeEnd: Double,
): Triple<Int, Int, Int> {
  var firstInLayerXRange = 0
  var lastInLayerXRange = 0
  var firstVisible = 0
  var lastVisible = 0
  for (entry in this) {
    when {
      entry.x > layerXRangeEnd || lastInLayerXRange == lastIndex -> break
      entry.x < layerXRangeStart -> {
        firstInLayerXRange++
        firstVisible++
        lastVisible++
      }
      entry.x < visibleXRangeStart -> {
        firstVisible++
        lastVisible++
      }
      entry.x <= visibleXRangeEnd -> lastVisible++
    }
    lastInLayerXRange++
  }
  firstVisible = (firstVisible - 1).coerceAtLeast(firstInLayerXRange)
  lastVisible = (lastVisible + 1).coerceAtMost(lastInLayerXRange)
  return Triple(firstInLayerXRange, firstVisible, lastVisible)
}
