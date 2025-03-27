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
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.ContentDescriptionProvider

@Composable
internal fun AccessibilityHighlighter(
  targets: List<CartesianMarker.Target>,
  context: CartesianDrawingContext,
  contentDescriptionProvider: ContentDescriptionProvider,
) {
  val groupedTargets = targets.groupBy(CartesianMarker.Target::canvasX)

  Box {
    groupedTargets.forEach { (x, targetGroup) ->
      Highlighter(
        context.layerDimensions.xSpacing,
        x,
        context.layerBounds.top,
        context.layerBounds.height(),
        contentDescriptionProvider.getContentDescription(context, targetGroup),
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
    Modifier.graphicsLayer {
        translationX = canvasX - width.toPx() / 2
        translationY = canvasTopY
      }
      .border(borderWidth, Color.Transparent)
      .size(width, height.pxToDp())
      .semantics {
        if (contentDescription != null) {
          this.contentDescription = contentDescription
        }
      }
  )
}

@Composable private fun Float.pxToDp() = LocalDensity.current.run { toDp() }

private val borderWidth = 2.dp
