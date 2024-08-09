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
import com.patrykandpatrick.vico.core.common.MeasuringContext

/** Defines a shape. */
public interface Shape {
  /**
   * Adds an outline of the [Shape] to [path]. [left], [top], [right], and [bottom] define the
   * outline bounds.
   */
  public fun outline(
    context: MeasuringContext,
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
        override fun outline(
          context: MeasuringContext,
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
  }
}
