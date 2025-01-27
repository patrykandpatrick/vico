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

package com.patrykandpatrick.vico.multiplatform.cartesian.data

import com.patrykandpatrick.vico.multiplatform.cartesian.axis.Axis
import com.patrykandpatrick.vico.multiplatform.common.orZero
import kotlin.math.max
import kotlin.math.min

/** A [CartesianChartRanges] implementation whose every property is mutable. */
public class MutableCartesianChartRanges : CartesianChartRanges {
  private var _minX: Double? = null

  private var _maxX: Double? = null

  internal var yRanges: MutableMap<Axis.Position.Vertical?, MutableYRange> = mutableMapOf()

  override val minX: Double
    get() = _minX.orZero

  override val maxX: Double
    get() = _maxX.orZero

  override var xStep: Double = 1.0

  override fun getYRange(axisPosition: Axis.Position.Vertical?): CartesianChartRanges.YRange =
    yRanges[axisPosition] ?: yRanges.getValue(null)

  /**
   * Tries to update the stored values. A minimum value can only be decreased. A maximum value can
   * only be increased.
   */
  public fun tryUpdate(
    minX: Double,
    maxX: Double,
    minY: Double,
    maxY: Double,
    axisPosition: Axis.Position.Vertical?,
  ) {
    _minX = _minX?.coerceAtMost(minX) ?: minX
    _maxX = _maxX?.coerceAtLeast(maxX) ?: maxX
    yRanges[null]?.tryUpdate(minY, maxY) ?: run { yRanges[null] = MutableYRange(minY, maxY) }
    if (axisPosition != null) {
      yRanges[axisPosition]?.tryUpdate(minY, maxY)
        ?: run { yRanges[axisPosition] = MutableYRange(minY, maxY) }
    }
  }

  /** Clears all values. */
  public fun reset() {
    _minX = null
    _maxX = null
    yRanges = mutableMapOf()
    xStep = 1.0
  }

  /** A mutable implementation of [CartesianChartRanges.YRange]. */
  public class MutableYRange(override var minY: Double, override var maxY: Double) :
    CartesianChartRanges.YRange {
    override val length: Double
      get() = maxY - minY

    /**
     * Tries to update [MutableYRange.minY] and [MutableYRange.maxY]. [MutableYRange.minY] can only
     * be decreased. [MutableYRange.maxY] can only be increased.
     */
    public fun tryUpdate(minY: Double, maxY: Double) {
      this.minY = min(this.minY, minY)
      this.maxY = max(this.maxY, maxY)
    }
  }
}

internal fun MutableCartesianChartRanges.toImmutable(): CartesianChartRanges =
  object : CartesianChartRanges {
    private val yRanges = this@toImmutable.yRanges
    override val minX: Double = this@toImmutable.minX
    override val maxX: Double = this@toImmutable.maxX
    override val xStep: Double = this@toImmutable.xStep

    override fun getYRange(axisPosition: Axis.Position.Vertical?): CartesianChartRanges.YRange =
      yRanges[axisPosition] ?: yRanges.getValue(null)
  }
