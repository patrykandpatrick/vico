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
import android.graphics.RectF
import androidx.annotation.IntRange
import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.common.MeasuringContext
import com.patrykandpatrick.vico.core.common.piRad
import com.patrykandpatrick.vico.core.common.shape.CorneredShape.Corner
import com.patrykandpatrick.vico.core.common.shape.CorneredShape.CornerTreatment
import kotlin.math.absoluteValue

/**
 * A [Shape] implementation with customizable corners.
 *
 * @param topLeft specifies a [Corner] for the top left of the [Shape].
 * @param topRight specifies a [Corner] for the top right of the [Shape].
 * @param bottomLeft specifies a [Corner] for the bottom left of the [Shape].
 * @param bottomRight specifies a [Corner] for the bottom right of the [Shape].
 */
public open class CorneredShape(
  public val topLeft: Corner = Corner.Sharp,
  public val topRight: Corner = Corner.Sharp,
  public val bottomRight: Corner = Corner.Sharp,
  public val bottomLeft: Corner = Corner.Sharp,
) : Shape {
  private val Float.nonZero: Float
    get() = if (this == 0f) 1f else this

  protected fun getCornerScale(width: Float, height: Float, density: Float): Float {
    val availableSize = minOf(width, height)
    val tL = topLeft.getSize(availableSize, density)
    val tR = topRight.getSize(availableSize, density)
    val bR = bottomRight.getSize(availableSize, density)
    val bL = bottomLeft.getSize(availableSize, density)
    return minOf(
      width / (tL + tR).nonZero,
      width / (bL + bR).nonZero,
      height / (tL + bL).nonZero,
      height / (tR + bR).nonZero,
    )
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public fun outline(
    density: Float,
    path: Path,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    val width = right - left
    val height = bottom - top
    if (width == 0f || height == 0f) return

    val size = minOf(width, height).absoluteValue
    val scale = getCornerScale(width, height, density).coerceAtMost(1f)

    val tL = topLeft.getSize(size, density) * scale
    val tR = topRight.getSize(size, density) * scale
    val bR = bottomRight.getSize(size, density) * scale
    val bL = bottomLeft.getSize(size, density) * scale

    path.moveTo(left, top + tL)
    topLeft.treatment.createCorner(
      path = path,
      position = CornerPosition.TopLeft,
      x1 = left,
      y1 = top + tL,
      x2 = left + tL,
      y2 = top,
    )

    path.lineTo(right - tR, top)
    topRight.treatment.createCorner(
      path = path,
      position = CornerPosition.TopRight,
      x1 = right - tR,
      y1 = top,
      x2 = right,
      y2 = top + tR,
    )

    path.lineTo(right, bottom - bR)
    bottomRight.treatment.createCorner(
      path = path,
      position = CornerPosition.BottomRight,
      x1 = right,
      y1 = bottom - bR,
      x2 = right - bR,
      y2 = bottom,
    )

    path.lineTo(left + bL, bottom)
    bottomLeft.treatment.createCorner(
      path = path,
      position = CornerPosition.BottomLeft,
      x1 = left + bL,
      y1 = bottom,
      x2 = left,
      y2 = bottom - bL,
    )
    path.close()
  }

  override fun outline(
    context: MeasuringContext,
    path: Path,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    outline(context.density, path, left, top, right, bottom)
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is CorneredShape &&
        topLeft == other.topLeft &&
        topRight == other.topRight &&
        bottomRight == other.bottomRight &&
        bottomLeft == other.bottomLeft

  override fun hashCode(): Int {
    var result = topLeft.hashCode()
    result = 31 * result + topRight.hashCode()
    result = 31 * result + bottomRight.hashCode()
    result = 31 * result + bottomLeft.hashCode()
    return result
  }

  /** Denotes a corner position. */
  public enum class CornerPosition {
    TopLeft,
    TopRight,
    BottomRight,
    BottomLeft,
  }

  /** Defines a corner shape. */
  public fun interface CornerTreatment {
    /** Adds a corner segment connecting ([x1], [y1]) and ([x2], [y2]) to [path]. */
    public fun createCorner(
      path: Path,
      position: CornerPosition,
      x1: Float,
      y1: Float,
      x2: Float,
      y2: Float,
    )

    /** Houses [CornerTreatment] singletons. */
    public companion object {
      /** Produces sharp corners. */
      public val Sharp: CornerTreatment = CornerTreatment { path, position, x1, y1, x2, y2 ->
        with(path) {
          when (position) {
            CornerPosition.TopLeft -> lineTo(x1, y2)
            CornerPosition.TopRight -> lineTo(x2, y1)
            CornerPosition.BottomRight -> lineTo(x1, y2)
            CornerPosition.BottomLeft -> lineTo(x2, y1)
          }
        }
      }

      /** Produces rounded corners. */
      public val Rounded: CornerTreatment = RoundedCornerTreatment

      /** Produces cut corners. */
      public val Cut: CornerTreatment = CornerTreatment { path, _, x1, y1, x2, y2 ->
        path.lineTo(x1, y1)
        path.lineTo(x2, y2)
      }
    }
  }

  /** Defines a corner style. */
  public sealed class Corner(internal val treatment: CornerTreatment) {
    internal abstract fun getSize(max: Float, density: Float): Float

    /** Produces absolutely sized corners. */
    public class Absolute(private val sizeDp: Float, shape: CornerTreatment) : Corner(shape) {
      override fun getSize(max: Float, density: Float) = sizeDp * density
    }

    /** Produces relatively sized corners. */
    public class Relative(
      @IntRange(0, 100) private val sizePercent: Int,
      treatment: CornerTreatment,
    ) : Corner(treatment) {
      init {
        require(sizePercent in 0..100) { "`sizePercent` must be in [0, 100]." }
      }

      override fun getSize(max: Float, density: Float) = max / 100 * sizePercent
    }

    /** Houses [Corner] singletons. */
    public companion object {
      /** Produces sharp corners. */
      public val Sharp: Corner = Absolute(sizeDp = 0f, shape = CornerTreatment.Sharp)

      /** Produces fully rounded corners. */
      public val Rounded: Corner = Relative(sizePercent = 100, treatment = CornerTreatment.Rounded)
    }
  }

  /** Houses [CorneredShape] singletons and factory functions. */
  public companion object {
    /** A [CorneredShape] with fully rounded corners. */
    public val Pill: CorneredShape = rounded(allPercent = 50)

    /** Creates a [CorneredShape] with rounded corners of the provided radii. */
    public fun rounded(
      topLeftDp: Float = 0f,
      topRightDp: Float = 0f,
      bottomRightDp: Float = 0f,
      bottomLeftDp: Float = 0f,
    ): CorneredShape =
      CorneredShape(
        Corner.Absolute(topLeftDp, CornerTreatment.Rounded),
        Corner.Absolute(topRightDp, CornerTreatment.Rounded),
        Corner.Absolute(bottomRightDp, CornerTreatment.Rounded),
        Corner.Absolute(bottomLeftDp, CornerTreatment.Rounded),
      )

    /** Creates a [CorneredShape] with rounded corners of the provided radius. */
    public fun rounded(allDp: Float): CorneredShape = rounded(allDp, allDp, allDp, allDp)

    /** Creates a [CorneredShape] with rounded corners of the provided radii. */
    public fun rounded(
      topLeftPercent: Int = 0,
      topRightPercent: Int = 0,
      bottomRightPercent: Int = 0,
      bottomLeftPercent: Int = 0,
    ): CorneredShape =
      CorneredShape(
        Corner.Relative(topLeftPercent, CornerTreatment.Rounded),
        Corner.Relative(topRightPercent, CornerTreatment.Rounded),
        Corner.Relative(bottomRightPercent, CornerTreatment.Rounded),
        Corner.Relative(bottomLeftPercent, CornerTreatment.Rounded),
      )

    /** Creates a [CorneredShape] with rounded corners of the provided radius. */
    public fun rounded(allPercent: Int): CorneredShape =
      rounded(allPercent, allPercent, allPercent, allPercent)

    /** Creates a [CorneredShape] with cut corners of the provided sizes. */
    public fun cut(
      topLeftDp: Float = 0f,
      topRightDp: Float = 0f,
      bottomRightDp: Float = 0f,
      bottomLeftDp: Float = 0f,
    ): CorneredShape =
      CorneredShape(
        Corner.Absolute(topLeftDp, CornerTreatment.Cut),
        Corner.Absolute(topRightDp, CornerTreatment.Cut),
        Corner.Absolute(bottomRightDp, CornerTreatment.Cut),
        Corner.Absolute(bottomLeftDp, CornerTreatment.Cut),
      )

    /** Creates a [CorneredShape] with cut corners of the provided size. */
    public fun cut(allDp: Float): CorneredShape = cut(allDp, allDp, allDp, allDp)

    /** Creates a [CorneredShape] with cut corners of the provided sizes. */
    public fun cut(
      topLeftPercent: Int = 0,
      topRightPercent: Int = 0,
      bottomRightPercent: Int = 0,
      bottomLeftPercent: Int = 0,
    ): CorneredShape =
      CorneredShape(
        Corner.Relative(topLeftPercent, CornerTreatment.Cut),
        Corner.Relative(topRightPercent, CornerTreatment.Cut),
        Corner.Relative(bottomRightPercent, CornerTreatment.Cut),
        Corner.Relative(bottomLeftPercent, CornerTreatment.Cut),
      )

    /** Creates a [CorneredShape] with cut corners of the provided size. */
    public fun cut(allPercent: Int): CorneredShape =
      cut(allPercent, allPercent, allPercent, allPercent)
  }
}

private object RoundedCornerTreatment : CornerTreatment {
  private val bounds = RectF()

  override fun createCorner(
    path: Path,
    position: CorneredShape.CornerPosition,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
  ) {
    val startAngle: Float
    when (position) {
      CorneredShape.CornerPosition.TopLeft -> {
        startAngle = 1f.piRad
        bounds.set(x1, y2, 2 * x2 - x1, 2 * y1 - y2)
      }
      CorneredShape.CornerPosition.TopRight -> {
        startAngle = 1.5f.piRad
        bounds.set(2 * x1 - x2, y1, x2, 2 * y2 - y1)
      }
      CorneredShape.CornerPosition.BottomRight -> {
        startAngle = 0f
        bounds.set(2 * x2 - x1, 2 * y1 - y2, x1, y2)
      }
      CorneredShape.CornerPosition.BottomLeft -> {
        startAngle = 0.5f.piRad
        bounds.set(x2, 2 * y2 - y1, 2 * x1 - x2, y1)
      }
    }
    path.arcTo(bounds, startAngle, 0.5f.piRad)
  }
}
