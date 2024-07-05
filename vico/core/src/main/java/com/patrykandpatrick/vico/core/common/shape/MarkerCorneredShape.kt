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
import com.patrykandpatrick.vico.core.common.Defaults.MARKER_TICK_SIZE
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.doubled
import com.patrykandpatrick.vico.core.common.half

/**
 * [MarkerCorneredShape] is an extension of [CorneredShape] that supports drawing a triangular tick
 * at a given point.
 *
 * @param topLeft specifies a [Corner] for the top left of the [Shape].
 * @param topRight specifies a [Corner] for the top right of the [Shape].
 * @param bottomLeft specifies a [Corner] for the bottom left of the [Shape].
 * @param bottomRight specifies a [Corner] for the bottom right of the [Shape].
 * @param tickSizeDp the size of the tick (in dp).
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
    set(value) {
      field = value
      tickPositionSet = true
    }

  private var tickPositionSet = false

  public constructor(
    all: Corner,
    tickSizeDp: Float = MARKER_TICK_SIZE,
  ) : this(all, all, all, all, tickSizeDp)

  public constructor(
    corneredShape: CorneredShape,
    tickSizeDp: Float = MARKER_TICK_SIZE,
  ) : this(
    topLeft = corneredShape.topLeft,
    topRight = corneredShape.topRight,
    bottomRight = corneredShape.bottomRight,
    bottomLeft = corneredShape.bottomLeft,
    tickSizeDp = tickSizeDp,
  )

  override fun draw(
    context: DrawContext,
    paint: Paint,
    path: Path,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    with(context) {
      val tickX = tickX ?: @Suppress("DEPRECATION") context.extraStore.getOrNull(tickXKey)
      if (tickX != null) {
        val tickPosition =
          if (tickPositionSet) {
            tickPosition
          } else {
            (@Suppress("DEPRECATION") context.extraStore.getOrNull(tickPositionKey)) ?: tickPosition
          }
        createPath(
          context = context,
          path = path,
          left = left,
          top = top,
          right = right,
          bottom = bottom,
        )
        val tickSize = context.dpToPx(tickSizeDp)
        val availableCornerSize = minOf(right - left, bottom - top)
        val cornerScale = getCornerScale(right - left, bottom - top, density)

        val minLeft = left + bottomLeft.getCornerSize(availableCornerSize, density) * cornerScale
        val maxLeft = right - bottomRight.getCornerSize(availableCornerSize, density) * cornerScale

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
        context.canvas.drawPath(path, paint)
      } else {
        super.draw(context, paint, path, left, top, right, bottom)
      }
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

  /** Specifies the position of a [MarkerCorneredShape]’s tick. */
  public enum class TickPosition {
    /** Positions the tick at the top of the [MarkerCorneredShape]. */
    Top,

    /** Positions the tick at the bottom of the [MarkerCorneredShape]. */
    Bottom,
  }

  public companion object {
    /** Used to store and retrieve the _x_ coordinate of a [MarkerCorneredShape]’s tick. */
    @Deprecated("Use the `tickX` instance property.")
    public val tickXKey: ExtraStore.Key<Float> = ExtraStore.Key()

    /** Used to store and retrieve a [MarkerCorneredShape]’s [TickPosition]. */
    @Deprecated("Use the `tickPosition` instance property.")
    public val tickPositionKey: ExtraStore.Key<TickPosition> = ExtraStore.Key()
  }
}
