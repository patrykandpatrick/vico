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

package com.patrykandpatrick.vico.shared.cartesian.data

import com.patrykandpatrick.vico.shared.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.shared.common.lerp
import com.patrykandpatrick.vico.shared.common.orZero

/** Houses [LineCartesianLayer] drawing information. [opacity] is the lines’ opacity. */
public class LineCartesianLayerDrawingModel(
  private val entries: List<Map<Double, Entry>>,
  seriesKeys: List<Any>,
  public val opacity: Float = 1f,
) : CartesianLayerDrawingModel<LineCartesianLayerDrawingModel.Entry>(entries, seriesKeys) {
  public constructor(
    entries: List<Map<Double, Entry>>,
    opacity: Float = 1f,
  ) : this(entries, entries.indices.toList(), opacity)

  override fun transform(
    entries: List<Map<Double, Entry>>,
    from: CartesianLayerDrawingModel<Entry>?,
    fraction: Float,
  ): CartesianLayerDrawingModel<Entry> =
    LineCartesianLayerDrawingModel(
      entries,
      seriesKeys,
      (from as LineCartesianLayerDrawingModel?)?.opacity.orZero.lerp(opacity, fraction),
    )

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LineCartesianLayerDrawingModel &&
        entries == other.entries &&
        seriesKeys == other.seriesKeys &&
        opacity == other.opacity

  override fun hashCode(): Int {
    var result = entries.hashCode()
    result = 31 * result + seriesKeys.hashCode()
    result = 31 * result + opacity.hashCode()
    return result
  }

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

    override fun equals(other: Any?): Boolean = this === other || other is Entry && y == other.y

    override fun hashCode(): Int = y.hashCode()
  }
}
