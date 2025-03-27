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

package com.patrykandpatrick.vico.compose.cartesian.accessibility

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.cartesian.marker.CandlestickCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget

/**
 * Displays accessibility-focused highlight indicators for a set of chart marker targets.
 *
 * This function renders invisible highlight elements for each target in the chart to support
 * accessibility tools like screen readers (e.g., TalkBack). Each marker is rendered with a semantic
 * `contentDescription`, allowing users to navigate data points via accessibility focus.
 *
 * The function delegates rendering to specific implementations based on the target type:
 * - [ColumnCartesianLayerMarkerTarget]
 * - [LineCartesianLayerMarkerTarget]
 * - [CandlestickCartesianLayerMarkerTarget]
 *
 * @param targets the list of marker targets representing data points to be highlighted for
 *   accessibility
 * @param canvasHeight the full height of the canvas, used to compute highlighter size
 * @param xSpacing the horizontal spacing between data points, used to calculate highlight width
 * @param modifier the modifier to be applied to the [AccessibilityHighlighter].
 */
@Composable
internal fun AccessibilityHighlighter(
  targets: List<CartesianMarker.Target>,
  canvasHeight: Float,
  xSpacing: Float,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    targets.forEach { target ->
      when (target) {
        is ColumnCartesianLayerMarkerTarget -> {
          ColumnCartesianMarkerHighlighter(
            target = target,
            canvasHeight = canvasHeight,
            xSpacing = xSpacing,
          )
        }

        is LineCartesianLayerMarkerTarget -> {
          LineCartesianMarkerHighlighter(
            target = target,
            canvasHeight = canvasHeight,
            xSpacing = xSpacing,
          )
        }

        is CandlestickCartesianLayerMarkerTarget ->
          CandlestickCartesianMarkerHighlighter(target = target, xSpacing = xSpacing)
      }
    }
  }
}

@Composable
private fun ColumnCartesianMarkerHighlighter(
  target: ColumnCartesianLayerMarkerTarget,
  xSpacing: Float,
  canvasHeight: Float,
) {
  Box {
    target.columns.forEach { column ->
      Highlighter(
        xSpacing = xSpacing,
        canvasX = target.canvasX,
        canvasY = column.canvasY,
        height = canvasHeight,
        contentDescription = column.entry.contentDescription,
      )
    }
  }
}

@Composable
private fun LineCartesianMarkerHighlighter(
  target: LineCartesianLayerMarkerTarget,
  xSpacing: Float,
  canvasHeight: Float,
) {
  Box {
    target.points.forEach { point ->
      Highlighter(
        xSpacing = xSpacing,
        canvasX = target.canvasX,
        canvasY = point.canvasY,
        height = canvasHeight,
        contentDescription = point.entry.contentDescription,
      )
    }
  }
}

@Composable
private fun CandlestickCartesianMarkerHighlighter(
  target: CandlestickCartesianLayerMarkerTarget,
  xSpacing: Float,
) {
  Highlighter(
    xSpacing = xSpacing,
    canvasX = target.canvasX,
    canvasY = target.highCanvasY,
    height = target.lowCanvasY,
    contentDescription = target.entry.contentDescription,
  )
}

@Composable
private fun Highlighter(
  xSpacing: Float,
  canvasX: Float,
  canvasY: Float,
  height: Float,
  contentDescription: String?,
) {
  val width = xSpacing.pxToDp()

  Box(
    modifier =
      Modifier.graphicsLayer {
          translationX = canvasX - width.toPx() / 2
          translationY = canvasY
        }
        .border(width = borderWidth, color = Color.Transparent)
        .size(width = width, height = height.pxToDp() - canvasY.pxToDp())
        .semantics { contentDescription?.let { this.contentDescription = it } }
  )
}

@Composable private fun Float.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

private val borderWidth = 2.dp
