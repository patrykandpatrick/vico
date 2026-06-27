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

package com.patrykandpatrick.vico.compose.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

/**
 * Hosts a chart, sizing it via [chartAreaHeight]. [content] should fill the [Box] (e.g., with a
 * `Modifier.fillMaxSize` [androidx.compose.foundation.Canvas]).
 */
@Composable
internal fun ChartHostBox(
  modifier: Modifier,
  chartAreaHeight: Dp,
  measureExtras: ((widthPx: Int) -> Float)?,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(
    modifier = modifier.fillMaxWidth().chartAreaHeight(chartAreaHeight, measureExtras),
    content = content,
  )
}

/**
 * Sizes the chart so that its coordinate system is [chartAreaHeight] tall and [measureExtras] (the
 * height of the legend, the marker, etc.) is added on top. This applies only when the height isn’t
 * already fixed by an outer modifier (e.g., [androidx.compose.foundation.layout.height]), in which
 * case that height is honored and the extras are subtracted from the coordinate system instead.
 */
private fun Modifier.chartAreaHeight(
  chartAreaHeight: Dp,
  measureExtras: ((widthPx: Int) -> Float)?,
): Modifier = layout { measurable, constraints ->
  val height =
    if (constraints.hasFixedHeight) {
      constraints.maxHeight
    } else {
      val extras =
        if (measureExtras != null && constraints.hasBoundedWidth) {
          measureExtras(constraints.maxWidth).roundToInt()
        } else {
          0
        }
      (chartAreaHeight.roundToPx() + extras).coerceIn(constraints.minHeight, constraints.maxHeight)
    }
  val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
  layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}
