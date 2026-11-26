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

import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer

/**
 * Houses drawing information for a [ColumnCartesianLayer]. [opacity] is the columns’ opacity. While
 * a series is exiting, its key is in [seriesKeys] but not in the [ColumnCartesianLayerModel].
 */
public class ColumnCartesianLayerDrawingModel
internal constructor(
  private val entries: List<Map<Double, Entry>>,
  seriesKeys: List<Any>,
  public val opacity: Float,
  override val seriesSources: List<Any?>?,
) : CartesianLayerDrawingModel<ColumnCartesianLayerDrawingModel.Entry>(entries, seriesKeys) {
  /** Creates a [ColumnCartesianLayerDrawingModel]. */
  public constructor(
    entries: List<Map<Double, Entry>>,
    seriesKeys: List<Any>,
    opacity: Float = 1f,
  ) : this(entries, seriesKeys, opacity, null)

  override fun withSeriesSources(seriesSources: List<Any?>): ColumnCartesianLayerDrawingModel =
    ColumnCartesianLayerDrawingModel(entries, seriesKeys, opacity, seriesSources)

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is ColumnCartesianLayerDrawingModel &&
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
   * Houses positional information for a [ColumnCartesianLayer]’s column. [height] expresses the
   * column’s height as a fraction of the [ColumnCartesianLayer]’s height. [entry] is the
   * [ColumnCartesianLayerModel.Entry] that’s drawn, which lets exiting series be drawn after
   * they’ve been removed from the model.
   */
  public class Entry(public val height: Float, public val entry: ColumnCartesianLayerModel.Entry) :
    CartesianLayerDrawingModel.Entry {
    override fun equals(other: Any?): Boolean =
      this === other || other is Entry && height == other.height && entry == other.entry

    override fun hashCode(): Int = 31 * height.hashCode() + entry.hashCode()
  }
}
