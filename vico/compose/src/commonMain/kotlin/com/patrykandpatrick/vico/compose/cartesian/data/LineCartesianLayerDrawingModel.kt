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

import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer

/**
 * Houses [LineCartesianLayer] drawing information. [opacity] is the lines’ opacity. [sweepFraction]
 * is the fraction of the layer swept in from the start edge (0 = hidden, 1 = fully visible). While
 * a series is exiting, its key is in [seriesKeys] but not in the [LineCartesianLayerModel].
 */
public class LineCartesianLayerDrawingModel
internal constructor(
  private val entries: List<Map<Double, Entry>>,
  seriesKeys: List<Any>,
  public val opacity: Float,
  public val sweepFraction: Float,
  override val seriesSources: List<Any?>?,
) : CartesianLayerDrawingModel<LineCartesianLayerDrawingModel.Entry>(entries, seriesKeys) {
  /** Creates a [LineCartesianLayerDrawingModel]. */
  public constructor(
    entries: List<Map<Double, Entry>>,
    seriesKeys: List<Any>,
    opacity: Float = 1f,
    sweepFraction: Float = 1f,
  ) : this(entries, seriesKeys, opacity, sweepFraction, null)

  override val canCarrySeriesSources: Boolean
    get() = true

  override fun withSeriesSources(seriesSources: List<Any?>): LineCartesianLayerDrawingModel =
    LineCartesianLayerDrawingModel(entries, seriesKeys, opacity, sweepFraction, seriesSources)

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LineCartesianLayerDrawingModel &&
        entries == other.entries &&
        seriesKeys == other.seriesKeys &&
        opacity == other.opacity &&
        sweepFraction == other.sweepFraction

  override fun hashCode(): Int {
    var result = entries.hashCode()
    result = 31 * result + seriesKeys.hashCode()
    result = 31 * result + opacity.hashCode()
    result = 31 * result + sweepFraction.hashCode()
    return result
  }

  /**
   * Houses positional information for a [LineCartesianLayer]’s point. [y] expresses the distance of
   * the point from the bottom of the [LineCartesianLayer] as a fraction of the
   * [LineCartesianLayer]’s height. [entry] is the [LineCartesianLayerModel.Entry] that’s drawn,
   * which lets exiting series be drawn after they’ve been removed from the model.
   */
  public class Entry(public val y: Float, public val entry: LineCartesianLayerModel.Entry) :
    CartesianLayerDrawingModel.Entry {
    override fun equals(other: Any?): Boolean =
      this === other || other is Entry && y == other.y && entry == other.entry

    override fun hashCode(): Int = 31 * y.hashCode() + entry.hashCode()
  }
}
