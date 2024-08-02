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
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.common.orZero
import kotlin.math.max
import kotlin.math.min

/** An implementation of [ChartValues] whose every property is mutable. */
public class MutableChartValues : ChartValues {
  private var _minX: Double? = null

  private var _maxX: Double? = null

  private var _xStep: Double? = null

  internal var yRanges: MutableMap<Axis.Position.Vertical?, MutableYRange> = mutableMapOf()

  override val minX: Double
    get() = _minX.orZero

  override val maxX: Double
    get() = _maxX.orZero

  override val xStep: Double
    get() = _xStep ?: 1.0

  override fun getYRange(axisPosition: Axis.Position.Vertical?): ChartValues.YRange =
    yRanges[axisPosition] ?: yRanges.getValue(null)

  override var model: CartesianChartModel = CartesianChartModel.empty

  /** Updates [MutableChartValues.xStep] and [MutableChartValues.model]. */
  public fun update(xStep: Double, model: CartesianChartModel) {
    _xStep = xStep
    this.model = model
  }

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
    _xStep = null
    model = CartesianChartModel.empty
  }

  /** A mutable implementation of [ChartValues.YRange]. */
  public class MutableYRange(override var minY: Double, override var maxY: Double) :
    ChartValues.YRange {
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

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun MutableChartValues.toImmutable(): ChartValues =
  object : ChartValues {
    private val yRanges = this@toImmutable.yRanges
    override val minX: Double = this@toImmutable.minX
    override val maxX: Double = this@toImmutable.maxX
    override val xStep: Double = this@toImmutable.xStep
    override val model: CartesianChartModel = this@toImmutable.model.toImmutable()

    override fun getYRange(axisPosition: Axis.Position.Vertical?): ChartValues.YRange =
      yRanges[axisPosition] ?: yRanges.getValue(null)
  }
