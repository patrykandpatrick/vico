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

package com.patrykandpatrick.vico.views.cartesian

import android.graphics.Color
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer.Candle
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.component.LineComponent

private fun LineComponent.copyWithColor(color: Int) =
  copy(
    color = if (this.color == Color.TRANSPARENT) this.color else color,
    strokeColor = if (this.strokeColor == Color.TRANSPARENT) this.color else color,
  )

internal fun Candle.Companion.sharpFilledCandle(
  color: Int,
  thicknessDp: Float = Defaults.CANDLE_BODY_WIDTH_DP,
): Candle {
  val filledBody = LineComponent(color, thicknessDp)
  return Candle(body = filledBody)
}

internal fun Candle.Companion.sharpHollowCandle(
  color: Int,
  thicknessDp: Float = Defaults.CANDLE_BODY_WIDTH_DP,
  strokeThicknessDp: Float = Defaults.HOLLOW_CANDLE_STROKE_THICKNESS_DP,
): Candle {
  val hollowBody =
    LineComponent(
      color = Color.TRANSPARENT,
      thicknessDp = thicknessDp,
      strokeColor = color,
      strokeThicknessDp = strokeThicknessDp,
    )

  return Candle(body = hollowBody)
}

internal fun Candle.copyWithColor(color: Int) =
  Candle(
    body = body.copyWithColor(color),
    topWick = topWick.copyWithColor(color),
    bottomWick = bottomWick.copyWithColor(color),
  )
