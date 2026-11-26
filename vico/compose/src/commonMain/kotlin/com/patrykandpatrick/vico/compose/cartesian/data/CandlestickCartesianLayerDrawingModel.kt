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

import com.patrykandpatrick.vico.compose.cartesian.layer.CandlestickCartesianLayer

/**
 * Houses drawing information for a [CandlestickCartesianLayer]. There’s one series per
 * [CandlestickCartesianLayerModel], identified by [CandlestickCartesianLayerModel.key]; when the
 * key changes, the previous series exits while the new one enters. [opacity] is the candles’
 * opacity.
 */
public class CandlestickCartesianLayerDrawingModel(
  private val entries: List<Map<Double, Entry>>,
  seriesKeys: List<Any>,
  public val opacity: Float = 1f,
) : CartesianLayerDrawingModel<CandlestickCartesianLayerDrawingModel.Entry>(entries, seriesKeys) {
  override fun equals(other: Any?): Boolean =
    this === other ||
      other is CandlestickCartesianLayerDrawingModel &&
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
   * Houses positional information for a [CandlestickCartesianLayer]’s candle. Each position is
   * stored as a distance from the bottom of the [CandlestickCartesianLayer]. The distances are
   * expressed as fractions of the [CandlestickCartesianLayer]’s height.
   *
   * @property bodyBottomY the position of the body’s bottom edge.
   * @property bodyTopY the position of the body’s top edge.
   * @property bottomWickY the position of the bottom wick’s bottom edge.
   * @property topWickY the position of the top wick’s top edge.
   * @property entry the [CandlestickCartesianLayerModel.Entry] that’s drawn.
   */
  public class Entry(
    public val bodyBottomY: Float,
    public val bodyTopY: Float,
    public val bottomWickY: Float,
    public val topWickY: Float,
    public val entry: CandlestickCartesianLayerModel.Entry,
  ) : CartesianLayerDrawingModel.Entry {
    override fun equals(other: Any?): Boolean =
      this === other ||
        other is Entry &&
          bodyBottomY == other.bodyBottomY &&
          bodyTopY == other.bodyTopY &&
          bottomWickY == other.bottomWickY &&
          topWickY == other.topWickY &&
          entry == other.entry

    override fun hashCode(): Int {
      var result = bodyBottomY.hashCode()
      result = 31 * result + bodyTopY.hashCode()
      result = 31 * result + bottomWickY.hashCode()
      result = 31 * result + topWickY.hashCode()
      result = 31 * result + entry.hashCode()
      return result
    }
  }
}
