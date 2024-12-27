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

package com.patrykandpatrick.vico.compose.cartesian.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent

/** Creates and remembers a [DefaultCartesianMarker]. */
@Composable
public fun rememberDefaultCartesianMarker(
  label: TextComponent,
  valueFormatter: DefaultCartesianMarker.ValueFormatter = remember {
    DefaultCartesianMarker.ValueFormatter.default()
  },
  labelPosition: DefaultCartesianMarker.LabelPosition = DefaultCartesianMarker.LabelPosition.Top,
  indicator: ((Color) -> Component)? = null,
  indicatorSize: Dp = Defaults.MARKER_INDICATOR_SIZE.dp,
  guideline: LineComponent? = null,
): DefaultCartesianMarker =
  remember(label, valueFormatter, labelPosition, indicator, indicatorSize, guideline) {
    DefaultCartesianMarker(
      label = label,
      valueFormatter = valueFormatter,
      labelPosition = labelPosition,
      indicator = if (indicator != null) ({ indicator(Color(it)) }) else null,
      indicatorSizeDp = indicatorSize.value,
      guideline = guideline,
    )
  }
