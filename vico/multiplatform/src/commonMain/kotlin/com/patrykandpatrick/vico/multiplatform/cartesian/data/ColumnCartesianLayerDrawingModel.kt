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

import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.common.data.CartesianLayerDrawingModel
import com.patrykandpatrick.vico.multiplatform.common.lerp
import com.patrykandpatrick.vico.multiplatform.common.orZero

/** Houses drawing information for a [ColumnCartesianLayer]. [opacity] is the columns’ opacity. */
public class ColumnCartesianLayerDrawingModel(
  private val entries: List<Map<Double, Entry>>,
  public val opacity: Float = 1f,
) : CartesianLayerDrawingModel<ColumnCartesianLayerDrawingModel.Entry>(entries) {
  override fun transform(
    entries: List<Map<Double, Entry>>,
    from: CartesianLayerDrawingModel<Entry>?,
    fraction: Float,
  ): CartesianLayerDrawingModel<Entry> {
    val oldOpacity = (from as ColumnCartesianLayerDrawingModel?)?.opacity.orZero
    return ColumnCartesianLayerDrawingModel(entries, oldOpacity.lerp(opacity, fraction))
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is ColumnCartesianLayerDrawingModel &&
        entries == other.entries &&
        opacity == other.opacity

  override fun hashCode(): Int = 31 * entries.hashCode() + opacity.hashCode()

  /**
   * Houses positional information for a [ColumnCartesianLayer]’s column. [height] expresses the
   * column’s height as a fraction of the [ColumnCartesianLayer]’s height.
   */
  public class Entry(public val height: Float) : CartesianLayerDrawingModel.Entry {
    override fun transform(
      from: CartesianLayerDrawingModel.Entry?,
      fraction: Float,
    ): CartesianLayerDrawingModel.Entry {
      val oldHeight = (from as? Entry)?.height.orZero
      return Entry(oldHeight.lerp(height, fraction))
    }

    /**
     * Transforms this entry's fractional values from a global Y-range to a local Y-range.
     * @param globalYRange The global Y-range this entry's values are relative to.
     * @param localYRange The target local Y-range.
     * @return A new [Entry] with its height re-calculated for the [localYRange].
     */
    public fun transform(
      globalYRange: CartesianChartRanges.YRange,
      localYRange: CartesianChartRanges.YRange,
    ): Entry {
      if (localYRange.length == 0.0 || globalYRange.length == 0.0) return this
      val newHeight = this.height * (globalYRange.length / localYRange.length).toFloat()
      return Entry(newHeight)
    }

    override fun equals(other: Any?): Boolean =
      this === other || other is Entry && height == other.height

    override fun hashCode(): Int = height.hashCode()
  }
}
