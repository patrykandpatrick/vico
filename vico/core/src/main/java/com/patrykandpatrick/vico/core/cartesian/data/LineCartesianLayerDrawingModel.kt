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

import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.data.CartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.common.lerp
import com.patrykandpatrick.vico.core.common.orZero

/** Houses [LineCartesianLayer] drawing information. [opacity] is the lines’ opacity. */
public class LineCartesianLayerDrawingModel(
  private val entries: List<Map<Double, Entry>>,
  public val opacity: Float = 1f,
) : CartesianLayerDrawingModel<LineCartesianLayerDrawingModel.Entry>(entries) {
  override fun transform(
    entries: List<Map<Double, Entry>>,
    from: CartesianLayerDrawingModel<Entry>?,
    fraction: Float,
  ): CartesianLayerDrawingModel<Entry> =
    LineCartesianLayerDrawingModel(
      entries,
      (from as LineCartesianLayerDrawingModel?)?.opacity.orZero.lerp(opacity, fraction),
    )

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LineCartesianLayerDrawingModel &&
        entries == other.entries &&
        opacity == other.opacity

  override fun hashCode(): Int = 31 * entries.hashCode() + opacity.hashCode()

  /**
   * Houses positional information for a [LineCartesianLayer]’s point. [y] expresses the distance of
   * the point from the bottom of the [LineCartesianLayer] as a fraction of the
   * [LineCartesianLayer]’s height.
   */
  public class Entry(public val y: Float) : CartesianLayerDrawingModel.Entry {
    override fun transform(
      from: CartesianLayerDrawingModel.Entry?,
      fraction: Float,
    ): CartesianLayerDrawingModel.Entry {
      val oldY = (from as? Entry)?.y.orZero
      return Entry(oldY.lerp(y, fraction))
    }

    /**
     * Transforms this entry's fractional values from a global Y-range to a local Y-range.
     * @param globalYRange The global Y-range this entry's values are relative to.
     * @param localYRange The target local Y-range.
     * @return A new [Entry] with fractional values relative to the [localYRange].
     */
    public fun transform(
      globalYRange: CartesianChartRanges.YRange,
      localYRange: CartesianChartRanges.YRange,
    ): Entry {
      if (localYRange.length == 0.0) return this

      val yValue = this.y * globalYRange.length + globalYRange.minY
      val localFraction = ((yValue - localYRange.minY) / localYRange.length).toFloat()
      return Entry(y = localFraction)
    }

    override fun equals(other: Any?): Boolean = this === other || other is Entry && y == other.y

    override fun hashCode(): Int = y.hashCode()
  }
}
