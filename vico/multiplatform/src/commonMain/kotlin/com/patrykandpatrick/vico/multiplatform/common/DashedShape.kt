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

package com.patrykandpatrick.vico.multiplatform.common

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

/**
 * [DashedShape] draws a dashed line by interchangeably drawing the provided [shape] and leaving a
 * gap.
 *
 * @property shape the base [Shape] from which to create the [DashedShape].
 * @property dashLength the dash length.
 * @property gapLength the gap length.
 * @property fitStrategy the [DashedShape.FitStrategy] to use for the dashes.
 */
public class DashedShape(
  public val shape: Shape = RectangleShape,
  public val dashLength: Dp = Defaults.DASHED_SHAPE_DASH_LENGTH.dp,
  public val gapLength: Dp = Defaults.DASHED_SHAPE_GAP_LENGTH.dp,
  public val fitStrategy: FitStrategy = FitStrategy.Resize,
) : Shape {
  private var drawDashLength = 0f
  private var drawGapLength = 0f

  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    val path = Path()
    if (size.width > size.height) {
      outlineHorizontalDashes(density, layoutDirection, path, size)
    } else {
      outlineVerticalDashes(density, layoutDirection, path, size)
    }
    return Outline.Generic(path)
  }

  private fun outlineHorizontalDashes(
    density: Density,
    layoutDirection: LayoutDirection,
    path: Path,
    size: Size,
  ) {
    calculateDrawLengths(density, size.width)

    var index = 0
    var drawnLength = 0f
    while (size.width - drawnLength > 0) {
      drawnLength +=
        if (index % 2 == 0) {
          shape.outline(
            density = density,
            layoutDirection = layoutDirection,
            path = path,
            left = drawnLength,
            top = 0f,
            right = drawnLength + drawDashLength,
            bottom = size.height,
          )
          drawDashLength
        } else {
          drawGapLength
        }
      index++
    }
  }

  private fun outlineVerticalDashes(
    density: Density,
    layoutDirection: LayoutDirection,
    path: Path,
    size: Size,
  ) {
    calculateDrawLengths(density, size.height)

    var index = 0
    var drawnLength = 0f
    while (size.height - drawnLength > 0) {
      drawnLength +=
        if (index % 2 == 0) {
          shape.outline(
            density = density,
            layoutDirection = layoutDirection,
            path = path,
            left = 0f,
            top = drawnLength,
            right = size.width,
            bottom = drawnLength + drawDashLength,
          )
          drawDashLength
        } else {
          drawGapLength
        }
      index++
    }
  }

  private fun calculateDrawLengths(density: Density, length: Float): Unit =
    with(density) { calculateDrawLengths(dashLength.toPx(), gapLength.toPx(), length) }

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

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is DashedShape &&
        shape == other.shape &&
        dashLength == other.dashLength &&
        gapLength == other.gapLength &&
        fitStrategy == other.fitStrategy

  override fun hashCode(): Int {
    var result = shape.hashCode()
    result = 31 * result + dashLength.hashCode()
    result = 31 * result + gapLength.hashCode()
    result = 31 * result + fitStrategy.hashCode()
    return result
  }

  /** Defines how a [DashedShape] is to be rendered. */
  public enum class FitStrategy {
    /**
     * The [DashedShape] will slightly increase or decrease the [DashedShape.dashLength] and
     * [DashedShape.gapLength] values so that the dashes fit perfectly without being cut off.
     */
    Resize,

    /**
     * The [DashedShape] will use the exact [DashedShape.dashLength] and [DashedShape.gapLength]
     * values provided. As a result, the [DashedShape] may not fit within its bounds or be cut off.
     */
    Fixed,
  }
}
