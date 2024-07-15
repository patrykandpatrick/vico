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

package com.patrykandpatrick.vico.core.cartesian.marker

import com.patrykandpatrick.vico.core.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer

/**
 * Houses information on a [CandlestickCartesianLayer] candle to be marked.
 *
 * @property entry the [CandlestickCartesianLayerModel.Entry].
 * @property openingCanvasY the pixel _y_ coordinate of the body edge corresponding to
 *   [CandlestickCartesianLayerModel.Entry.opening].
 * @property closingCanvasY the pixel _y_ coordinate of the body edge corresponding to
 *   [CandlestickCartesianLayerModel.Entry.closing].
 * @property lowCanvasY the pixel _y_ coordinate of the bottom wick’s bottom edge, which corresponds
 *   to [CandlestickCartesianLayerModel.Entry.low].
 * @property highCanvasY the pixel _y_ coordinate of the top wick’s top edge, which corresponds to
 *   [CandlestickCartesianLayerModel.Entry.high].
 * @property openingColor the color of [CandlestickCartesianLayer.Candle.body].
 * @property closingColor the color of [CandlestickCartesianLayer.Candle.body].
 * @property lowColor the color of [CandlestickCartesianLayer.Candle.bottomWick].
 * @property highColor the color of [CandlestickCartesianLayer.Candle.topWick].
 */
public data class CandlestickCartesianLayerMarkerTarget(
  override val x: Double,
  override val canvasX: Float,
  public val entry: CandlestickCartesianLayerModel.Entry,
  public val openingCanvasY: Float,
  public val closingCanvasY: Float,
  public val lowCanvasY: Float,
  public val highCanvasY: Float,
  public val openingColor: Int,
  public val closingColor: Int,
  public val lowColor: Int,
  public val highColor: Int,
) : CartesianMarker.Target
