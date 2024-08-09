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

@file:Suppress("Unused")

package com.patrykandpatrick.vico.compose.common.shape

import android.graphics.Matrix
import android.graphics.Path
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.common.Defaults.MARKER_TICK_SIZE
import com.patrykandpatrick.vico.core.common.MeasuringContext
import com.patrykandpatrick.vico.core.common.shape.Corner
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.CutCornerTreatment
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import com.patrykandpatrick.vico.core.common.shape.MarkerCorneredShape
import com.patrykandpatrick.vico.core.common.shape.RoundedCornerTreatment
import com.patrykandpatrick.vico.core.common.shape.Shape

private typealias ComposePath = androidx.compose.ui.graphics.AndroidPath

private const val RADII_ARRAY_SIZE = 8

private fun Path.addRoundRect(
  left: Float,
  top: Float,
  right: Float,
  bottom: Float,
  rect: RoundRect,
  radii: FloatArray,
) {
  radii[0] = rect.topLeftCornerRadius.x
  radii[1] = rect.topLeftCornerRadius.y
  radii[2] = rect.topRightCornerRadius.x
  radii[3] = rect.topRightCornerRadius.y
  radii[4] = rect.bottomRightCornerRadius.x
  radii[5] = rect.bottomRightCornerRadius.y
  radii[6] = rect.bottomLeftCornerRadius.x
  radii[7] = rect.bottomLeftCornerRadius.y
  addRoundRect(left, top, right, bottom, radii, Path.Direction.CCW)
}

/**
 * Converts this [androidx.compose.ui.graphics.Shape] to an instance of
 * [com.patrykandpatrick.vico.core.common.shape.Shape].
 */
public fun androidx.compose.ui.graphics.Shape.toVicoShape(): Shape =
  object : Shape {
    private val radii by lazy { FloatArray(RADII_ARRAY_SIZE) }
    private val matrix: Matrix by lazy { Matrix() }

    override fun outline(
      context: MeasuringContext,
      path: Path,
      left: Float,
      top: Float,
      right: Float,
      bottom: Float,
    ) {
      val outline =
        createOutline(
          size = Size(width = right - left, height = bottom - top),
          layoutDirection = if (context.isLtr) LayoutDirection.Ltr else LayoutDirection.Rtl,
          density = Density(context.density, 1f),
        )
      when (outline) {
        is Outline.Rectangle -> path.addRect(left, top, right, bottom, Path.Direction.CCW)
        is Outline.Rounded ->
          path.addRoundRect(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            rect = outline.roundRect,
            radii = radii,
          )
        is Outline.Generic -> {
          matrix.setTranslate(left, top)
          path.addPath(outline.path.asAndroidPath(), matrix)
        }
      }
    }
  }

/** Converts this [CorneredShape] to an instance of [androidx.compose.ui.graphics.Shape]. */
public fun CorneredShape.toComposeShape(): androidx.compose.ui.graphics.Shape =
  object : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
      size: Size,
      layoutDirection: LayoutDirection,
      density: Density,
    ): Outline {
      val path = ComposePath()

      outline(
        density = density.density,
        path = path.asAndroidPath(),
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height,
      )
      return Outline.Generic(path)
    }
  }

/** Creates a [CorneredShape] with rounded corners of the provided size. */
public fun Shape.Companion.rounded(all: Dp = 0.dp): CorneredShape =
  CorneredShape(
    Corner.Absolute(all.value, RoundedCornerTreatment),
    Corner.Absolute(all.value, RoundedCornerTreatment),
    Corner.Absolute(all.value, RoundedCornerTreatment),
    Corner.Absolute(all.value, RoundedCornerTreatment),
  )

/** Creates a [CorneredShape] with rounded corners of the provided sizes. */
public fun Shape.Companion.rounded(
  topLeft: Dp = 0.dp,
  topRight: Dp = 0.dp,
  bottomRight: Dp = 0.dp,
  bottomLeft: Dp = 0.dp,
): CorneredShape =
  CorneredShape(
    Corner.Absolute(topLeft.value, RoundedCornerTreatment),
    Corner.Absolute(topRight.value, RoundedCornerTreatment),
    Corner.Absolute(bottomRight.value, RoundedCornerTreatment),
    Corner.Absolute(bottomLeft.value, RoundedCornerTreatment),
  )

/** Creates a [CorneredShape] with cut corners of the provided size. */
public fun Shape.Companion.cut(all: Dp = 0.dp): CorneredShape =
  CorneredShape(
    Corner.Absolute(all.value, CutCornerTreatment),
    Corner.Absolute(all.value, CutCornerTreatment),
    Corner.Absolute(all.value, CutCornerTreatment),
    Corner.Absolute(all.value, CutCornerTreatment),
  )

/** Creates a [CorneredShape] with cut corners of the provided sizes. */
public fun Shape.Companion.cut(
  topLeft: Dp = 0.dp,
  topRight: Dp = 0.dp,
  bottomRight: Dp = 0.dp,
  bottomLeft: Dp = 0.dp,
): CorneredShape =
  CorneredShape(
    Corner.Absolute(topLeft.value, CutCornerTreatment),
    Corner.Absolute(topRight.value, CutCornerTreatment),
    Corner.Absolute(bottomRight.value, CutCornerTreatment),
    Corner.Absolute(bottomLeft.value, CutCornerTreatment),
  )

/**
 * Creates a [MarkerCorneredShape].
 *
 * @param topLeft the size and look of the top-left corner.
 * @param topRight the size and look of the top-right corner.
 * @param bottomRight the size and look of the bottom-right corner.
 * @param bottomLeft the size and look of the bottom-left corner.
 * @param tickSizeDp the tick size.
 */
public fun Shape.Companion.markerCornered(
  topLeft: Corner,
  topRight: Corner,
  bottomRight: Corner,
  bottomLeft: Corner,
  tickSizeDp: Dp = MARKER_TICK_SIZE.dp,
): MarkerCorneredShape =
  MarkerCorneredShape(
    topLeft = topLeft,
    topRight = topRight,
    bottomRight = bottomRight,
    bottomLeft = bottomLeft,
    tickSizeDp = tickSizeDp.value,
  )

/**
 * Creates a [MarkerCorneredShape].
 *
 * @param all the size and look of all corners.
 * @param tickSizeDp the tick size.
 */
public fun Shape.Companion.markerCornered(
  all: Corner,
  tickSizeDp: Dp = MARKER_TICK_SIZE.dp,
): MarkerCorneredShape =
  MarkerCorneredShape(
    topLeft = all,
    topRight = all,
    bottomRight = all,
    bottomLeft = all,
    tickSizeDp = tickSizeDp.value,
  )

/**
 * Creates a [MarkerCorneredShape] out of a regular [CorneredShape].
 *
 * @param corneredShape the base [CorneredShape].
 * @param tickSizeDp the tick size.
 */
public fun Shape.Companion.markerCornered(
  corneredShape: CorneredShape,
  tickSizeDp: Dp = MARKER_TICK_SIZE.dp,
): MarkerCorneredShape =
  MarkerCorneredShape(
    topLeft = corneredShape.topLeft,
    topRight = corneredShape.topRight,
    bottomRight = corneredShape.bottomRight,
    bottomLeft = corneredShape.bottomLeft,
    tickSizeDp = tickSizeDp.value,
  )

/**
 * Creates a [DashedShape].
 *
 * @param shape the [androidx.compose.ui.graphics.Shape] from which to create the [DashedShape].
 * @param dashLength the dash length.
 * @param gapLength the gap length.
 * @param fitStrategy the [DashedShape.FitStrategy] to use for the dashes.
 */
public fun Shape.Companion.dashed(
  shape: androidx.compose.ui.graphics.Shape,
  dashLength: Dp,
  gapLength: Dp,
  fitStrategy: DashedShape.FitStrategy =
    com.patrykandpatrick.vico.core.common.shape.DashedShape.FitStrategy.Resize,
): DashedShape =
  DashedShape(
    shape = shape.toVicoShape(),
    dashLengthDp = dashLength.value,
    gapLengthDp = gapLength.value,
    fitStrategy = fitStrategy,
  )

/**
 * Creates a [DashedShape].
 *
 * @param shape the [Shape] from which to create the [DashedShape].
 * @param dashLength the dash length.
 * @param gapLength the gap length.
 * @param fitStrategy the [DashedShape.FitStrategy] to use for the dashes.
 */
public fun Shape.Companion.dashed(
  shape: Shape,
  dashLength: Dp,
  gapLength: Dp,
  fitStrategy: DashedShape.FitStrategy =
    com.patrykandpatrick.vico.core.common.shape.DashedShape.FitStrategy.Resize,
): DashedShape =
  DashedShape(
    shape = shape,
    dashLengthDp = dashLength.value,
    gapLengthDp = gapLength.value,
    fitStrategy = fitStrategy,
  )
