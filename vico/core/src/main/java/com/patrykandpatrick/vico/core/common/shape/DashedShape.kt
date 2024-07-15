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

package com.patrykandpatrick.vico.core.common.shape

import android.graphics.Paint
import android.graphics.Path
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.DrawContext
import kotlin.math.ceil

/**
 * [DashedShape] draws a dashed line by interchangeably drawing the provided [shape] and leaving a
 * gap.
 *
 * @property shape the base [Shape] from which to create the [DashedShape].
 * @property dashLengthDp the dash length in dp.
 * @property gapLengthDp the gap length in dp.
 * @property fitStrategy the [DashedShape.FitStrategy] to use for the dashes.
 */
public class DashedShape(
  public val shape: Shape = Shape.Rectangle,
  public val dashLengthDp: Float = Defaults.DASH_LENGTH,
  public val gapLengthDp: Float = Defaults.DASH_GAP,
  public val fitStrategy: FitStrategy = FitStrategy.Resize,
) : Shape {
  private var drawDashLength = dashLengthDp
  private var drawGapLength = gapLengthDp

  override fun draw(
    context: DrawContext,
    paint: Paint,
    path: Path,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    if (right - left > bottom - top) {
      drawHorizontalDashes(context, paint, path, left, top, right, bottom)
    } else {
      drawVerticalDashes(context, paint, path, left, top, right, bottom)
    }
  }

  @Deprecated(
    "Use `draw`.",
    replaceWith = ReplaceWith("draw(context, paint, path, left, top, right, bottom)"),
  )
  override fun drawShape(
    context: DrawContext,
    paint: Paint,
    path: Path,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    draw(context, paint, path, left, top, right, bottom)
  }

  private fun drawHorizontalDashes(
    context: DrawContext,
    paint: Paint,
    path: Path,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    calculateDrawLengths(context, right - left)

    var index = 0
    var drawnLength = 0f
    while (right - left - drawnLength > 0) {
      drawnLength +=
        if (index % 2 == 0) {
          path.reset()
          shape.draw(
            context = context,
            paint = paint,
            path = path,
            left = left + drawnLength,
            top = top,
            right = left + drawnLength + drawDashLength,
            bottom = bottom,
          )
          drawDashLength
        } else {
          drawGapLength
        }
      index++
    }
  }

  private fun drawVerticalDashes(
    context: DrawContext,
    paint: Paint,
    path: Path,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    calculateDrawLengths(context, bottom - top)

    var index = 0
    var drawnLength = 0f
    while (bottom - top - drawnLength > 0) {
      drawnLength +=
        if (index % 2 == 0) {
          path.reset()
          shape.draw(
            context = context,
            paint = paint,
            path = path,
            left = left,
            top = top + drawnLength,
            right = right,
            bottom = top + drawnLength + drawDashLength,
          )
          drawDashLength
        } else {
          drawGapLength
        }
      index++
    }
  }

  private fun calculateDrawLengths(context: DrawContext, length: Float): Unit =
    with(context) { calculateDrawLengths(dashLengthDp.pixels, gapLengthDp.pixels, length) }

  private fun calculateDrawLengths(dashLength: Float, gapLength: Float, length: Float) {
    if (dashLength == 0f && gapLength == 0f) {
      drawDashLength = length
      return
    }
    when (fitStrategy) {
      FitStrategy.Resize ->
        when {
          length < dashLength + gapLength -> {
            drawDashLength = length
            drawGapLength = 0f
          }
          else -> {
            val gapAndDashLength = gapLength + dashLength
            val ratio = length / (dashLength + ceil(length / gapAndDashLength) * gapAndDashLength)
            drawDashLength = dashLength * ratio
            drawGapLength = gapLength * ratio
          }
        }
      FitStrategy.Fixed -> {
        drawDashLength = dashLength
        drawGapLength = gapLength
      }
    }
  }

  /** Defines how a [DashedShape] is to be rendered. */
  public enum class FitStrategy {
    /**
     * The [DashedShape] will slightly increase or decrease the [DashedShape.dashLengthDp] and
     * [DashedShape.gapLengthDp] values so that the dashes fit perfectly without being cut off.
     */
    Resize,

    /**
     * The [DashedShape] will use the exact [DashedShape.dashLengthDp] and [DashedShape.gapLengthDp]
     * values provided. As a result, the [DashedShape] may not fit within its bounds or be cut off.
     */
    Fixed,
  }
}
