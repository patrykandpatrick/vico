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

import android.graphics.Path
import com.patrykandpatrick.vico.core.common.Defaults.MARKER_TICK_SIZE
import com.patrykandpatrick.vico.core.common.MeasuringContext
import com.patrykandpatrick.vico.core.common.doubled
import com.patrykandpatrick.vico.core.common.half

/**
 * A [CorneredShape] extension that additionally draws a triangular tick.
 *
 * @property tickSizeDp the size of the tick (in dp).
 */
public open class MarkerCorneredShape(
  topLeft: Corner,
  topRight: Corner,
  bottomRight: Corner,
  bottomLeft: Corner,
  public val tickSizeDp: Float = MARKER_TICK_SIZE,
) : CorneredShape(topLeft, topRight, bottomRight, bottomLeft) {
  /** The tick’s _x_ coordinate. If this is null, the tick isn’t drawn. */
  public var tickX: Float? = null

  /** Specifies the tick position. */
  public var tickPosition: TickPosition = TickPosition.Bottom

  public constructor(
    all: Corner,
    tickSizeDp: Float = MARKER_TICK_SIZE,
  ) : this(all, all, all, all, tickSizeDp)

  public constructor(
    base: CorneredShape,
    tickSizeDp: Float = MARKER_TICK_SIZE,
  ) : this(
    topLeft = base.topLeft,
    topRight = base.topRight,
    bottomRight = base.bottomRight,
    bottomLeft = base.bottomLeft,
    tickSizeDp = tickSizeDp,
  )

  override fun outline(
    context: MeasuringContext,
    path: Path,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    with(context) {
      super.outline(context, path, left, top, right, bottom)

      val tickX = tickX ?: return

      val tickSize = context.dpToPx(tickSizeDp)
      val availableCornerSize = minOf(right - left, bottom - top)
      val cornerScale = getCornerScale(right - left, bottom - top, density)

      val minLeft = left + bottomLeft.getSize(availableCornerSize, density) * cornerScale
      val maxLeft = right - bottomRight.getSize(availableCornerSize, density) * cornerScale

      val coercedTickSize = tickSize.coerceAtMost((maxLeft - minLeft).half.coerceAtLeast(0f))

      (tickX - coercedTickSize)
        .takeIf { minLeft < maxLeft }
        ?.coerceIn(minLeft, maxLeft - coercedTickSize.doubled)
        ?.also { tickBaseLeft ->
          val tickBaseY =
            when (tickPosition) {
              TickPosition.Top -> top
              TickPosition.Bottom -> bottom
            }
          val tickDirection =
            when (tickPosition) {
              TickPosition.Top -> -1
              TickPosition.Bottom -> 1
            }
          path.moveTo(tickBaseLeft, tickBaseY)
          path.lineTo(tickX, tickBaseY + tickDirection * tickSize)
          path.lineTo(tickBaseLeft + coercedTickSize.doubled, tickBaseY)
        }

      path.close()
    }
  }

  override fun equals(other: Any?): Boolean =
    super.equals(other) && other is MarkerCorneredShape && tickSizeDp == other.tickSizeDp

  override fun hashCode(): Int = super.hashCode() * 31 + tickSizeDp.hashCode()

  /** Specifies the position of a [MarkerCorneredShape]’s tick. */
  public enum class TickPosition {
    /** Positions the tick at the top of the [MarkerCorneredShape]. */
    Top,

    /** Positions the tick at the bottom of the [MarkerCorneredShape]. */
    Bottom,
  }
}
