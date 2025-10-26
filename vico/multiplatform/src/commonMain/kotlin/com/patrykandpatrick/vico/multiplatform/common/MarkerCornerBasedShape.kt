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

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.common.Defaults.MARKER_TICK_SIZE

private fun CornerBasedShape.getTickBaseXRange(
  size: Size,
  layoutDirection: LayoutDirection,
  density: Density,
  tickPosition: MarkerCornerBasedShape.TickPosition,
): Pair<Float, Float> {
  val (startSize, endSize) =
    when (tickPosition) {
      MarkerCornerBasedShape.TickPosition.Top -> topStart to topEnd
      MarkerCornerBasedShape.TickPosition.Bottom -> bottomStart to bottomEnd
    }
  val (leftSize, rightSize) =
    if (layoutDirection == LayoutDirection.Ltr) startSize to endSize else endSize to startSize
  return density.run { leftSize.toPx(size, density) to size.width - rightSize.toPx(size, density) }
}

/**
 * A [CornerBasedShape] wrapper that additionally draws a triangular tick.
 *
 * @property base the base [CornerBasedShape].
 * @property tickSize the size of the tick.
 */
public open class MarkerCornerBasedShape(
  public val base: CornerBasedShape,
  public val tickSize: Dp = MARKER_TICK_SIZE.dp,
) : Shape {
  /**
   * The tick’s _x_-coordinate (relative to the center of the [MarkerCornerBasedShape]). If this is
   * null, the tick isn’t drawn.
   */
  public var tickX: Float? = null

  /** Specifies the tick position. */
  public var tickPosition: TickPosition = TickPosition.Bottom

  private val path = Path()

  private val tickPath = Path()

  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    val baseOutline = base.createOutline(size, layoutDirection, density)
    val tickTipX = size.width.half + (tickX ?: return baseOutline)
    val (minTickBaseX, maxTickBaseX) =
      base.getTickBaseXRange(size, layoutDirection, density, tickPosition)
    if (minTickBaseX >= maxTickBaseX) return baseOutline
    val tickSize = density.run { tickSize.toPx() }
    path.rewind()
    with(path) {
      when (baseOutline) {
        is Outline.Rectangle -> addRect(Rect(0f, 0f, size.width, size.height))
        is Outline.Rounded -> addRoundRect(baseOutline.roundRect)
        is Outline.Generic -> addPath(baseOutline.path)
      }
    }
    val coercedTickSize = tickSize.coerceAtMost((maxTickBaseX - minTickBaseX).half)
    val tickBaseLeft =
      (tickTipX - coercedTickSize).coerceIn(minTickBaseX, maxTickBaseX - coercedTickSize.doubled)
    val tickBaseY =
      when (tickPosition) {
        TickPosition.Top -> 0f
        TickPosition.Bottom -> size.height
      }
    val tickDirection =
      when (tickPosition) {
        TickPosition.Top -> -1
        TickPosition.Bottom -> 1
      }
    with(tickPath) {
      rewind()
      moveTo(tickBaseLeft, tickBaseY)
      lineTo(tickTipX, tickBaseY + tickDirection * tickSize)
      lineTo(tickBaseLeft + coercedTickSize.doubled, tickBaseY)
    }
    path.op(path, tickPath, PathOperation.Union)
    return Outline.Generic(path)
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is MarkerCornerBasedShape && base == other.base && tickSize == other.tickSize

  override fun hashCode(): Int = 31 * base.hashCode() + tickSize.hashCode()

  /** Specifies the position of a [MarkerCornerBasedShape]’s tick. */
  public enum class TickPosition {
    /** Positions the tick at the top of the [MarkerCornerBasedShape]. */
    Top,

    /** Positions the tick at the bottom of the [MarkerCornerBasedShape]. */
    Bottom,
  }
}
