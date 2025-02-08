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

package com.patrykandpatrick.vico.compose.m3.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.common.VicoTheme
import com.patrykandpatrick.vico.compose.common.getDefaultColors

/** Creates and remembers a [VicoTheme] based on [MaterialTheme.colorScheme]. */
@Composable
public fun rememberM3VicoTheme(
  candlestickCartesianLayerColors: VicoTheme.CandlestickCartesianLayerColors =
    VicoTheme.CandlestickCartesianLayerColors.fromDefaultColors(getDefaultColors()),
  columnCartesianLayerColors: List<Color> =
    MaterialTheme.colorScheme.run { listOf(primary, secondary, tertiary) },
  lineCartesianLayerColors: List<Color> = columnCartesianLayerColors,
  lineColor: Color = MaterialTheme.colorScheme.outline,
  textColor: Color = MaterialTheme.colorScheme.onBackground,
): VicoTheme =
  remember(
    candlestickCartesianLayerColors,
    columnCartesianLayerColors,
    lineCartesianLayerColors,
    lineColor,
    textColor,
  ) {
    VicoTheme(
      candlestickCartesianLayerColors,
      columnCartesianLayerColors,
      lineCartesianLayerColors,
      lineColor,
      textColor,
    )
  }
