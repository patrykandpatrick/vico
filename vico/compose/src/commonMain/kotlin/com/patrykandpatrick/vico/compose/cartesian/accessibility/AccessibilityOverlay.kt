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

package com.patrykandpatrick.vico.compose.cartesian.accessibility

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.getVisibleXRange
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerDimensions
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
internal fun AccessibilityOverlay(
  chart: CartesianChart,
  model: CartesianChartModel,
  ranges: CartesianChartRanges,
  layerDimensions: CartesianLayerDimensions,
  layerBounds: Rect,
  scroll: Float,
  zoom: Float,
  context: CartesianMeasuringContext,
  onIntervalFocused: (Double) -> Unit,
) {
  val provider = chart.contentDescriptionProvider ?: return
  if (layerBounds.isEmpty || model.models.isEmpty() || layerDimensions.xSpacing <= 0f) return

  val density = LocalDensity.current
  val visibleXRange = remember(context, layerDimensions, layerBounds, scroll, zoom) {
    context.getVisibleXRange(layerDimensions, layerBounds, scroll)
  }

  val visibleXValues = remember(model, visibleXRange) {
    model.getXValues().filter { it in visibleXRange }
  }

  if (visibleXValues.isEmpty()) return

  val minIntervalWidthPx = with(density) { 48.dp.toPx() }
  val maxIntervalCount = (layerBounds.width / minIntervalWidthPx).toInt().coerceAtLeast(1)
  val intervalCount = min(visibleXValues.size, maxIntervalCount)
  if (intervalCount <= 0) return

  val widthPx = layerBounds.width / intervalCount
  val topPx = layerBounds.top
  val heightPx = layerBounds.height

  Layout(
    content = {
      for (i in 0 until intervalCount) {
        val fromIndex = i * visibleXValues.size / intervalCount
        val toIndex = ((i + 1) * visibleXValues.size / intervalCount - 1).coerceAtLeast(fromIndex)
        val startX = visibleXValues[fromIndex]
        val endX = visibleXValues[toIndex]
        val intervalRange = startX..endX

        val intervalAggregation = remember(chart, model, intervalRange) {
          chart.getAggregation(model, intervalRange)
        }

        val description = remember(provider, context, intervalAggregation) {
          provider.getIntervalDescription(context, intervalAggregation).toString()
        }

        val midIndex = (fromIndex + toIndex) / 2
        val representativeX = visibleXValues[midIndex]

        Box(
          modifier = Modifier
            .size(
              width = with(density) { widthPx.toDp() },
              height = with(density) { heightPx.toDp() },
            )
            .onFocusChanged { focusState ->
              if (focusState.isFocused) {
                onIntervalFocused(representativeX)
              }
            }
            .focusable()
            .semantics {
              this.contentDescription = description
            }
        )
      }
    },
    modifier = Modifier.fillMaxSize(),
  ) { measurables, constraints ->
    val placeables = measurables.map { it.measure(constraints) }
    layout(constraints.maxWidth, constraints.maxHeight) {
      placeables.forEachIndexed { i, placeable ->
        val leftPx = if (context.isLtr) {
          layerBounds.left + i * widthPx
        } else {
          layerBounds.right - (i + 1) * widthPx
        }
        placeable.place(leftPx.roundToInt(), topPx.roundToInt())
      }
    }
  }
}
