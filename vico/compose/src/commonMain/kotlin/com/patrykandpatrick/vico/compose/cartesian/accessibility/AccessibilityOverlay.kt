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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.compose.common.getStart
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Immutable
internal data class AccessibilityMapping(
  val layerBounds: Rect,
  val xSpacing: Float,
  val startPadding: Float,
  val minX: Double,
  val xStep: Double,
  val layoutDirectionMultiplier: Float,
) {
  val isLtr: Boolean
    get() = layoutDirectionMultiplier > 0f

  fun visibleXRange(scroll: Float): ClosedFloatingPointRange<Double> {
    val fullRangeStart = minX - (startPadding / xSpacing) * xStep
    val start = fullRangeStart + (layoutDirectionMultiplier * scroll / xSpacing) * xStep
    val end = start + (layerBounds.width / xSpacing) * xStep
    return start..end
  }

  fun canvasX(x: Double, scroll: Float): Float {
    val drawingStart =
      layerBounds.getStart(isLtr) + layoutDirectionMultiplier * startPadding - scroll
    return drawingStart +
      layoutDirectionMultiplier * xSpacing * ((x - minX) / xStep).toFloat()
  }
}

internal fun AccessibilityMapping(
  layerBounds: Rect,
  layerDimensions: CartesianLayerDimensions,
  context: CartesianMeasuringContext,
): AccessibilityMapping =
  AccessibilityMapping(
    layerBounds = layerBounds,
    xSpacing = layerDimensions.xSpacing,
    startPadding = layerDimensions.startPadding,
    minX = context.ranges.minX,
    xStep = context.ranges.xStep,
    layoutDirectionMultiplier = context.layoutDirectionMultiplier.toFloat(),
  )

@Composable
internal fun AccessibilityOverlay(
  chart: CartesianChart,
  model: CartesianChartModel,
  mapping: State<AccessibilityMapping?>,
  scrollState: VicoScrollState,
  measuringContext: State<CartesianMeasuringContext>,
) {
  val provider = chart.descriptionProvider ?: return
  if (model.models.isEmpty()) return

  val density = LocalDensity.current
  val allXValues = remember(chart, model) { chart.getXValues(model) }
  if (allXValues.isEmpty()) return

  val minIntervalWidthPx = with(density) { 48.dp.toPx() }

  val intervals by
    remember(allXValues, mapping, scrollState, minIntervalWidthPx) {
      derivedStateOf {
        val currentMapping = mapping.value ?: return@derivedStateOf emptyList<ChartInterval>()
        val scroll = scrollState.value
        val layerBounds = currentMapping.layerBounds
        if (layerBounds.isEmpty || currentMapping.xSpacing <= 0f || currentMapping.xStep <= 0.0) {
          return@derivedStateOf emptyList<ChartInterval>()
        }
        val visibleXRange = currentMapping.visibleXRange(scroll)
        if (
          visibleXRange.start > allXValues.last() || visibleXRange.endInclusive < allXValues.first()
        ) {
          return@derivedStateOf emptyList<ChartInterval>()
        }

        var fromIndex = allXValues.binarySearch(visibleXRange.start)
        if (fromIndex < 0) {
          fromIndex = -fromIndex - 1
        } else {
          while (fromIndex > 0 && allXValues[fromIndex - 1] >= visibleXRange.start) {
            fromIndex -= 1
          }
        }

        var toIndex = allXValues.binarySearch(visibleXRange.endInclusive)
        if (toIndex < 0) {
          toIndex = -toIndex - 2
        } else {
          while (
            toIndex < allXValues.size - 1 && allXValues[toIndex + 1] <= visibleXRange.endInclusive
          ) {
            toIndex += 1
          }
        }

        if (fromIndex > toIndex || fromIndex >= allXValues.size || toIndex < 0) {
          return@derivedStateOf emptyList<ChartInterval>()
        }

        val visibleCount = toIndex - fromIndex + 1
        val maxIntervalCount = (layerBounds.width / minIntervalWidthPx).toInt().coerceAtLeast(1)
        val intervalCount = min(visibleCount, maxIntervalCount)
        if (intervalCount <= 0) return@derivedStateOf emptyList<ChartInterval>()

        List(intervalCount) { i ->
          val sliceStart = fromIndex + (i * visibleCount) / intervalCount
          val sliceEnd =
            (fromIndex + ((i + 1) * visibleCount) / intervalCount - 1).coerceAtLeast(sliceStart)
          val startX = allXValues[sliceStart]
          val endX = allXValues[sliceEnd]
          val startXBound =
            if (sliceStart == 0) {
              startX - currentMapping.xStep / 2.0
            } else {
              (allXValues[sliceStart - 1] + startX) / 2.0
            }
          val endXBound =
            if (sliceEnd == allXValues.lastIndex) {
              endX + currentMapping.xStep / 2.0
            } else {
              (endX + allXValues[sliceEnd + 1]) / 2.0
            }
          ChartInterval(
            startX = startX,
            endX = endX,
            startXBound = startXBound,
            endXBound = endXBound,
          )
        }
      }
    }

  if (intervals.isEmpty()) return

  Layout(
    content = {
      for (interval in intervals) {
        Box(
          modifier =
            Modifier.semantics {
              val aggregation = chart.getAggregation(model, interval.startX..interval.endX)
              val description = provider.getIntervalDescription(measuringContext.value, aggregation)
              if (description.isNotEmpty()) {
                contentDescription = description.toString()
              }
            }
        )
      }
    },
    modifier = Modifier.fillMaxSize(),
  ) { measurables, constraints ->
    val currentMapping = mapping.value
    val layerBounds = currentMapping?.layerBounds ?: Rect.Zero
    val scroll = scrollState.value

    val placeableData = measurables.mapIndexed { index, measurable ->
      val interval = intervals[index]
      val c1 = currentMapping?.canvasX(interval.startXBound, scroll) ?: 0f
      val c2 = currentMapping?.canvasX(interval.endXBound, scroll) ?: 0f
      val minX = min(c1, c2)
      val maxX = max(c1, c2)
      val leftInt = minX.coerceIn(layerBounds.left, layerBounds.right).roundToInt()
      val rightInt = maxX.coerceIn(layerBounds.left, layerBounds.right).roundToInt()
      val widthPx = (rightInt - leftInt).coerceAtLeast(1)
      val heightPx = layerBounds.height.roundToInt().coerceAtLeast(1)
      val placeable = measurable.measure(Constraints.fixed(widthPx, heightPx))
      Triple(placeable, leftInt, layerBounds.top.roundToInt())
    }

    layout(constraints.maxWidth, constraints.maxHeight) {
      placeableData.forEach { (placeable, leftInt, topInt) ->
        placeable.place(leftInt, topInt)
      }
    }
  }
}

private data class ChartInterval(
  val startX: Double,
  val endX: Double,
  val startXBound: Double,
  val endXBound: Double,
)
