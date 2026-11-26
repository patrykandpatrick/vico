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

import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.common.orZero
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

  internal val hasXRange: Boolean
    get() = _minX != null

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

  /**
   * Extends each zero-length _y_-range upward by 1, so that no consumer divides by a zero length
   * (e.g., for a constant series under [CartesianLayerRangeProvider.Intrinsic]).
   */
  internal fun ensureNonZeroYLengths() {
    yRanges.values.forEach { range -> if (range.length == 0.0) range.maxY = range.minY + 1 }
  }

  /** Clears all values. */
  public fun reset() {
    _minX = null
    _maxX = null
    yRanges = mutableMapOf()
    xStep = 1.0
  }

  override fun toString(): String =
    "MutableCartesianChartRanges(minX=$minX, maxX=$maxX, xStep=$xStep, yRanges=$yRanges)"

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

    override fun toString(): String = "MutableYRange(minY=$minY, maxY=$maxY, length=$length)"
  }
}

/**
 * Updates these ranges for a [CartesianLayerModel] via [rangeProvider]. For a non-empty model, the
 * range provider receives the model’s extents ([minX], [maxX], [minY], and [maxY]). An empty model
 * has no extents, so the range provider receives the ranges established so far by the other layers
 * (the _x_-range and the _y_-range for [axisPosition], or else the global one), or zeros where none
 * exist. Empty layers are processed after the non-empty ones, in layer order (see
 * `CartesianChart.updateRanges`).
 */
internal fun MutableCartesianChartRanges.tryUpdate(
  rangeProvider: CartesianLayerRangeProvider,
  model: CartesianLayerModel,
  minX: Double,
  maxX: Double,
  minY: Double,
  maxY: Double,
  axisPosition: Axis.Position.Vertical?,
) {
  var inputMinX = minX
  var inputMaxX = maxX
  var inputMinY = minY
  var inputMaxY = maxY
  if (model.isEmpty) {
    inputMinX = if (hasXRange) this.minX else 0.0
    inputMaxX = if (hasXRange) this.maxX else 0.0
    val yRange = yRanges[axisPosition] ?: yRanges[null]
    inputMinY = yRange?.minY ?: 0.0
    inputMaxY = yRange?.maxY ?: 0.0
  }
  tryUpdate(
    rangeProvider.getMinX(inputMinX, inputMaxX),
    rangeProvider.getMaxX(inputMinX, inputMaxX),
    rangeProvider.getMinY(inputMinY, inputMaxY),
    rangeProvider.getMaxY(inputMinY, inputMaxY),
    axisPosition,
  )
}

internal fun MutableCartesianChartRanges.toImmutable(): CartesianChartRanges =
  object : CartesianChartRanges {
    private val yRanges = this@toImmutable.yRanges
    override val minX: Double = this@toImmutable.minX
    override val maxX: Double = this@toImmutable.maxX
    override val xStep: Double = this@toImmutable.xStep

    override fun getYRange(axisPosition: Axis.Position.Vertical?): CartesianChartRanges.YRange =
      yRanges[axisPosition] ?: yRanges.getValue(null)

    override fun toString(): String =
      "CartesianChartRanges(minX=$minX, maxX=$maxX, xStep=$xStep, yRanges=$yRanges)"
  }
