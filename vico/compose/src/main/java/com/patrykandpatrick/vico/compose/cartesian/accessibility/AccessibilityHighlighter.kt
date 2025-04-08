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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker

/**
 * Displays accessibility-focused highlight indicators for a set of chart marker targets.
 *
 * This function renders invisible highlight elements for each target in the chart to support
 * accessibility tools like screen readers (e.g., TalkBack). Each marker is rendered with a semantic
 * `contentDescription`, allowing users to navigate data points via accessibility focus.
 *
 * @param targets the list of marker targets representing data points to be highlighted for
 *   accessibility
 * @param context holds environment data
 * @param xSpacing the horizontal spacing between data points, used to calculate highlight width
 * @param modifier the modifier to be applied to the [AccessibilityHighlighter].
 * @param contentDescriptionProvider provides the content description for the highlight elements.
 */
@Composable
internal fun AccessibilityHighlighter(
  targets: List<CartesianMarker.Target>,
  context: CartesianDrawingContext,
  xSpacing: Float,
  modifier: Modifier = Modifier,
  contentDescriptionProvider: DefaultCartesianMarker.ContentDescriptionProvider =
    DefaultCartesianMarker.ContentDescriptionProvider.default(),
) {
  val groupedTargets = targets.groupBy { it.canvasX }

  Box(modifier) {
    for ((x, targetsGroup) in groupedTargets) {
      Highlighter(
        xSpacing = xSpacing,
        canvasX = x,
        canvasTopY = context.layerBounds.top,
        height = context.layerBounds.height(),
        contentDescription =
          contentDescriptionProvider
            .getContentDescription(context = context, targets = targetsGroup)
            .toString(),
      )
    }
  }
}

@Composable
private fun Highlighter(
  xSpacing: Float,
  canvasX: Float,
  canvasTopY: Float,
  height: Float,
  contentDescription: String?,
) {
  val width = xSpacing.pxToDp()

  Box(
    modifier =
      Modifier.offset(x = canvasX.pxToDp() - width / 2, y = canvasTopY.pxToDp())
        .border(width = borderWidth, color = Color.Transparent)
        .size(width = width, height = height.pxToDp())
        .semantics { contentDescription?.let { this.contentDescription = it } }
  )
}

@Composable private fun Float.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

private val borderWidth = 2.dp
