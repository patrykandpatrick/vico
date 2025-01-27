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

package com.patrykandpatrick.vico.multiplatform.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.common.component.LineComponent

/**
 * Houses default chart colors.
 *
 * @param candlestickCartesianLayerColors houses default [CandlestickCartesianLayer.Candle] colors.
 * @param columnCartesianLayerColors used for [ColumnCartesianLayer]&#0020;[LineComponent]s.
 * @param lineCartesianLayerColors used for [LineCartesianLayer.Line]s.
 * @param lineColor used for [HorizontalAxis] and [VerticalAxis] lines.
 * @param textColor used for [HorizontalAxis] and [VerticalAxis] labels.
 */
public data class VicoTheme(
  val candlestickCartesianLayerColors: CandlestickCartesianLayerColors,
  val columnCartesianLayerColors: List<Color>,
  val lineCartesianLayerColors: List<Color> = columnCartesianLayerColors,
  val lineColor: Color,
  val textColor: Color,
) {
  /**
   * Houses default [CandlestickCartesianLayer.Candle] colors.
   *
   * @property bullish used for bullish [CandlestickCartesianLayer.Candle]s.
   * @property neutral used for neutral [CandlestickCartesianLayer.Candle]s.
   * @property bearish used for bearish [CandlestickCartesianLayer.Candle]s.
   */
  public data class CandlestickCartesianLayerColors(
    val bullish: Color,
    val neutral: Color,
    val bearish: Color,
  ) {
    internal companion object {
      fun fromDefaultColors(defaultColors: DefaultColors): CandlestickCartesianLayerColors =
        CandlestickCartesianLayerColors(
          Color(defaultColors.bullishCandleColor),
          Color(defaultColors.neutralCandleColor),
          Color(defaultColors.bearishCandleColor),
        )
    }
  }

  internal companion object {
    fun fromDefaultColors(defaultColors: DefaultColors) =
      VicoTheme(
        candlestickCartesianLayerColors =
          CandlestickCartesianLayerColors.fromDefaultColors(defaultColors),
        columnCartesianLayerColors = defaultColors.cartesianLayerColors.map(::Color),
        lineColor = Color(defaultColors.lineColor),
        textColor = Color(defaultColors.textColor),
      )
  }
}

private val LocalVicoTheme = staticCompositionLocalOf<VicoTheme?> { null }

/** The current [VicoTheme]. */
public val vicoTheme: VicoTheme
  @Composable
  get() =
    LocalVicoTheme.current
      ?: getDefaultColors().let { remember(it) { VicoTheme.fromDefaultColors(it) } }

/** Provides a [VicoTheme]. */
@Composable
public fun ProvideVicoTheme(theme: VicoTheme, content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalVicoTheme provides theme, content)
}
