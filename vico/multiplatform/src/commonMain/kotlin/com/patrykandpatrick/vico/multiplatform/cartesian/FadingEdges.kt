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

package com.patrykandpatrick.vico.multiplatform.cartesian

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.common.Defaults.FADING_EDGE_VISIBILITY_THRESHOLD_DP
import com.patrykandpatrick.vico.multiplatform.common.Defaults.FADING_EDGE_WIDTH_DP

/**
 * [FadingEdges] applies a horizontal fade to the edges of the chart area for scrollable charts.
 * This effect indicates that there’s more content beyond a given edge, and the user can scroll to
 * reveal it.
 *
 * @param startWidth the width of the fade overlay for the start edge.
 * @param endWidth the width of the fade overlay for the end edge.
 * @param visibilityThreshold the scroll distance over which the overlays fade in and out.
 * @param visibilityEasing used for the fading edges’ fade-in and fade-out animations. This is a
 *   mapping of the degree to which [visibilityThreshold] has been satisfied to the opacity of the
 *   fading edges.
 */
@Immutable
public open class FadingEdges(
  protected val startWidth: Dp = FadingEdgesDefaults.edgeWidth,
  protected val endWidth: Dp = FadingEdgesDefaults.edgeWidth,
  protected val visibilityThreshold: Dp = FadingEdgesDefaults.visibilityThreshold,
  protected val visibilityEasing: Easing = FadingEdgesDefaults.visibilityEasing,
) {
  private val paint = Paint().apply { blendMode = BlendMode.DstOut }

  /**
   * Creates a [FadingEdges] instance with fading edges of equal width.
   *
   * @param width the width of the fade overlays.
   * @param visibilityThreshold the scroll distance over which the overlays fade in and out.
   * @param visibilityEasing used for the fading edges’ fade-in and fade-out animations. This is a
   *   mapping of the degree to which [visibilityThreshold] has been satisfied to the opacity of the
   *   fading edges.
   */
  public constructor(
    width: Dp = FADING_EDGE_WIDTH_DP.dp,
    visibilityThreshold: Dp = FADING_EDGE_VISIBILITY_THRESHOLD_DP.dp,
    visibilityEasing: Easing = FastOutSlowInEasing,
  ) : this(
    startWidth = width,
    endWidth = width,
    visibilityThreshold = visibilityThreshold,
    visibilityEasing = visibilityEasing,
  )

  init {
    require(value = startWidth.value >= 0) { "`startWidth` must be nonnegative." }
    require(value = endWidth.value >= 0) { "`endWidth` must be nonnegative." }
  }

  internal fun draw(context: CartesianDrawingContext) {
    with(context) {
      val maxScroll = getMaxScrollDistance()
      var fadeAlphaFraction: Float

      if (scrollEnabled && startWidth.value > 0f && scroll > 0f) {
        fadeAlphaFraction = (scroll / visibilityThreshold.pixels).coerceAtMost(1f)

        drawFadingEdge(
          left = layerBounds.left,
          top = layerBounds.top,
          right = layerBounds.left + startWidth.pixels,
          bottom = layerBounds.bottom,
          direction = -1,
          alpha = visibilityEasing.transform(fadeAlphaFraction),
        )
      }

      if (scrollEnabled && endWidth.value > 0f && scroll < maxScroll) {
        fadeAlphaFraction = ((maxScroll - scroll) / visibilityThreshold.pixels).coerceAtMost(1f)

        drawFadingEdge(
          left = layerBounds.right - endWidth.pixels,
          top = layerBounds.top,
          right = layerBounds.right,
          bottom = layerBounds.bottom,
          direction = 1,
          alpha = visibilityEasing.transform(fadeAlphaFraction),
        )
      }
    }
  }

  private fun CartesianDrawingContext.drawFadingEdge(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    direction: Int,
    alpha: Float,
  ) {
    val rect = Rect(left, top, right, bottom)
    val faded = Color.Black.copy(alpha = alpha)
    Brush.horizontalGradient(
        colors =
          listOf(
            if (direction < 0) faded else Color.Transparent,
            if (direction < 0) Color.Transparent else faded,
          ),
        startX = left,
        endX = right,
        tileMode = TileMode.Clamp,
      )
      .applyTo(size = rect.size, p = paint, alpha = 1f)
    canvas.drawRect(rect, paint)
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is FadingEdges &&
        startWidth == other.startWidth &&
        endWidth == other.endWidth &&
        visibilityThreshold == other.visibilityThreshold &&
        visibilityEasing == other.visibilityEasing

  override fun hashCode(): Int {
    var result = startWidth.hashCode()
    result = 31 * result + endWidth.hashCode()
    result = 31 * result + visibilityThreshold.hashCode()
    result = 31 * result + visibilityEasing.hashCode()
    return result
  }
}

/** Creates and remembers a [FadingEdges] instance. */
@Composable
public fun rememberFadingEdges(
  startWidth: Dp = FadingEdgesDefaults.edgeWidth,
  endWidth: Dp = FadingEdgesDefaults.edgeWidth,
  visibilityThreshold: Dp = FadingEdgesDefaults.visibilityThreshold,
  visibilityEasing: Easing = FadingEdgesDefaults.visibilityEasing,
): FadingEdges =
  remember(startWidth, endWidth, visibilityThreshold, visibilityEasing) {
    FadingEdges(startWidth, endWidth, visibilityThreshold, visibilityEasing)
  }

/** Creates and remembers a [FadingEdges] instance. */
@Composable
public fun rememberFadingEdges(
  width: Dp = FadingEdgesDefaults.edgeWidth,
  visibilityThreshold: Dp = FadingEdgesDefaults.visibilityThreshold,
  visibilityEasing: Easing = FadingEdgesDefaults.visibilityEasing,
): FadingEdges =
  rememberFadingEdges(
    startWidth = width,
    endWidth = width,
    visibilityThreshold = visibilityThreshold,
    visibilityEasing = visibilityEasing,
  )

private object FadingEdgesDefaults {
  val edgeWidth = FADING_EDGE_WIDTH_DP.dp
  val visibilityThreshold = FADING_EDGE_VISIBILITY_THRESHOLD_DP.dp
  val visibilityEasing = FastOutSlowInEasing
}
