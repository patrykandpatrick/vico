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
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import com.patrykandpatrick.vico.core.common.DrawContext

/** Defines a shape that can be drawn on a canvas. */
public interface Shape {
  /**
   * Draws the [Shape] on the canvas.
   *
   * @param context holds environment data.
   * @param paint the [Paint] used to draw the shape.
   * @param path the [Path] defining the shape.
   * @param left the _x_ coordinate of the left edge of the bounds in which the shape should be
   *   drawn.
   * @param top the _y_ coordinate of the top edge of the bounds in which the shape should be drawn.
   * @param right the _x_ coordinate of the right edge of the bounds in which the shape should be
   *   drawn.
   * @param bottom the _y_ coordinate of the bottom edge of the bounds in which the shape should be
   *   drawn.
   */
  @Suppress("DEPRECATION")
  public fun draw(
    context: DrawContext,
    paint: Paint,
    path: Path,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    drawShape(context, paint, path, left, top, right, bottom)
  }

  /**
   * Draws the [Shape] on the canvas.
   *
   * @param context holds environment data.
   * @param paint the [Paint] used to draw the shape.
   * @param path the [Path] defining the shape.
   * @param left the _x_ coordinate of the left edge of the bounds in which the shape should be
   *   drawn.
   * @param top the _y_ coordinate of the top edge of the bounds in which the shape should be drawn.
   * @param right the _x_ coordinate of the right edge of the bounds in which the shape should be
   *   drawn.
   * @param bottom the _y_ coordinate of the bottom edge of the bounds in which the shape should be
   *   drawn.
   */
  @Deprecated(
    message = "Use `draw`.",
    replaceWith = ReplaceWith("draw(context, paint, path, left, top, right, bottom)"),
  )
  public fun drawShape(
    context: DrawContext,
    paint: Paint,
    path: Path,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  )

  public companion object {
    /** A rectangle with sharp corners. */
    public val Rectangle: Shape =
      object : Shape {
        override fun draw(
          context: DrawContext,
          paint: Paint,
          path: Path,
          left: Float,
          top: Float,
          right: Float,
          bottom: Float,
        ) {
          path.moveTo(left, top)
          path.lineTo(right, top)
          path.lineTo(right, bottom)
          path.lineTo(left, bottom)
          path.close()
          context.canvas.drawPath(path, paint)
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
      }

    /** A shape whose each corner is fully rounded. */
    public val Pill: CorneredShape = rounded(allPercent = 50)

    /**
     * Creates a [Shape] with all corners rounded.
     *
     * @param allPercent the radius of each corner (in percent).
     */
    public fun rounded(allPercent: Int): CorneredShape =
      rounded(allPercent, allPercent, allPercent, allPercent)

    /**
     * Creates a [Shape] with all corners rounded.
     *
     * @param topLeftPercent the top-left corner radius (in percent).
     * @param topRightPercent the top-right corner radius (in percent).
     * @param bottomRightPercent the bottom-right corner radius (in percent).
     * @param bottomLeftPercent the bottom-left corner radius (in percent).
     */
    public fun rounded(
      topLeftPercent: Int = 0,
      topRightPercent: Int = 0,
      bottomRightPercent: Int = 0,
      bottomLeftPercent: Int = 0,
    ): CorneredShape =
      CorneredShape(
        Corner.Relative(topLeftPercent, RoundedCornerTreatment),
        Corner.Relative(topRightPercent, RoundedCornerTreatment),
        Corner.Relative(bottomRightPercent, RoundedCornerTreatment),
        Corner.Relative(bottomLeftPercent, RoundedCornerTreatment),
      )

    /** Creates a [CorneredShape] with rounded corners of the provided size. */
    public fun rounded(allDp: Float): Shape = rounded(allDp, allDp, allDp, allDp)

    /** Creates a [CorneredShape] with rounded corners of the provided sizes. */
    public fun rounded(
      topLeftDp: Float = 0f,
      topRightDp: Float = 0f,
      bottomRightDp: Float = 0f,
      bottomLeftDp: Float = 0f,
    ): CorneredShape =
      CorneredShape(
        Corner.Absolute(topLeftDp, RoundedCornerTreatment),
        Corner.Absolute(topRightDp, RoundedCornerTreatment),
        Corner.Absolute(bottomRightDp, RoundedCornerTreatment),
        Corner.Absolute(bottomLeftDp, RoundedCornerTreatment),
      )

    /**
     * Creates a [Shape] with all corners cut.
     *
     * @param allPercent the radius of each corner (in percent).
     */
    public fun cut(allPercent: Int): CorneredShape =
      cut(allPercent, allPercent, allPercent, allPercent)

    /**
     * Creates a [Shape] with all corners cut.
     *
     * @param topLeftPercent the top-left corner radius (in percent).
     * @param topRightPercent the top-right corner radius (in percent).
     * @param bottomRightPercent the bottom-right corner radius (in percent).
     * @param bottomLeftPercent the bottom-left corner radius (in percent).
     */
    public fun cut(
      topLeftPercent: Int = 0,
      topRightPercent: Int = 0,
      bottomRightPercent: Int = 0,
      bottomLeftPercent: Int = 0,
    ): CorneredShape =
      CorneredShape(
        Corner.Relative(topLeftPercent, CutCornerTreatment),
        Corner.Relative(topRightPercent, CutCornerTreatment),
        Corner.Relative(bottomRightPercent, CutCornerTreatment),
        Corner.Relative(bottomLeftPercent, CutCornerTreatment),
      )

    /** Creates a [CorneredShape] with cut corners of the provided size. */
    public fun cut(allDp: Float): Shape = cut(allDp, allDp, allDp, allDp)

    /** Creates a [CorneredShape] with cut corners of the provided sizes. */
    public fun cut(
      topLeftDp: Float = 0f,
      topRightDp: Float = 0f,
      bottomRightDp: Float = 0f,
      bottomLeftDp: Float = 0f,
    ): CorneredShape =
      CorneredShape(
        Corner.Absolute(topLeftDp, CutCornerTreatment),
        Corner.Absolute(topRightDp, CutCornerTreatment),
        Corner.Absolute(bottomRightDp, CutCornerTreatment),
        Corner.Absolute(bottomLeftDp, CutCornerTreatment),
      )

    /**
     * Creates a [Shape] out of a [Drawable].
     *
     * @param drawable the [Drawable] that will be used as a shape.
     * @param keepAspectRatio whether to keep the drawable’s aspect ratio, based on its intrinsic
     *   size.
     * @param otherShape used to fill the remaining space if the [drawable] doesn’t fill the entire
     *   bounds.
     */
    public fun drawable(
      drawable: Drawable,
      tintDrawable: Boolean = true,
      keepAspectRatio: Boolean = false,
      otherShape: Shape? = Rectangle,
    ): Shape =
      object : Shape {
        private val ratio: Float =
          drawable.intrinsicWidth.coerceAtLeast(1) /
            drawable.intrinsicHeight.coerceAtLeast(1).toFloat()

        override fun draw(
          context: DrawContext,
          paint: Paint,
          path: Path,
          left: Float,
          top: Float,
          right: Float,
          bottom: Float,
        ) {
          if (bottom - top == 0f || left - right == 0f) return
          val width = right - left
          val height = bottom - top

          var otherComponentLeft = left
          var otherComponentTop = top

          if (tintDrawable) drawable.setTintCompat(paint.color)

          if (height > width) {
            val drawableHeight = if (keepAspectRatio) width / ratio else height
            val topWithoutClipping = minOf(top, bottom - drawableHeight)
            drawable.setBounds(
              left = left,
              top = topWithoutClipping,
              right = right,
              bottom = topWithoutClipping + drawableHeight,
            )

            otherComponentTop = topWithoutClipping + drawableHeight
          } else {
            val drawableWidth = if (keepAspectRatio) height * ratio else width
            val leftWithoutClipping = minOf(left, right - drawableWidth)
            drawable.setBounds(
              left = leftWithoutClipping,
              top = top,
              right = leftWithoutClipping + drawableWidth,
              bottom = bottom,
            )

            otherComponentLeft = leftWithoutClipping + drawableWidth
          }

          drawable.draw(context.canvas)
          otherShape ?: return

          if (bottom - otherComponentTop > 0 && right - otherComponentLeft > 0) {
            otherShape.draw(
              context = context,
              paint = paint,
              path = path,
              left = otherComponentLeft,
              top = otherComponentTop,
              right = right,
              bottom = bottom,
            )
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
      }
  }
}

private fun Drawable.setTintCompat(tint: Int) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    setTint(tint)
  } else {
    colorFilter = PorterDuffColorFilter(tint, PorterDuff.Mode.SRC_IN)
  }
}
