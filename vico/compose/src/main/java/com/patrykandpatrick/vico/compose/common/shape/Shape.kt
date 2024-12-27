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
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Defaults.MARKER_TICK_SIZE
import com.patrykandpatrick.vico.core.common.MeasuringContext
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import com.patrykandpatrick.vico.core.common.shape.MarkerCorneredShape
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

/** A [Dp] version of [CorneredShape.rounded]. */
public fun CorneredShape.Companion.rounded(
  topLeft: Dp = 0.dp,
  topRight: Dp = 0.dp,
  bottomRight: Dp = 0.dp,
  bottomLeft: Dp = 0.dp,
): CorneredShape = rounded(topLeft.value, topRight.value, bottomRight.value, bottomLeft.value)

/** A [Dp] version of [CorneredShape.rounded]. */
public fun CorneredShape.Companion.rounded(all: Dp = 0.dp): CorneredShape = rounded(all.value)

/** A [Dp] version of [CorneredShape.cut]. */
public fun CorneredShape.Companion.cut(
  topLeft: Dp = 0.dp,
  topRight: Dp = 0.dp,
  bottomRight: Dp = 0.dp,
  bottomLeft: Dp = 0.dp,
): CorneredShape = cut(topLeft.value, topRight.value, bottomRight.value, bottomLeft.value)

/** A [Dp] version of [CorneredShape.cut]. */
public fun CorneredShape.Companion.cut(all: Dp = 0.dp): CorneredShape = cut(all.value)

/** Creates a [MarkerCorneredShape]. */
public fun markerCorneredShape(
  topLeft: CorneredShape.Corner,
  topRight: CorneredShape.Corner,
  bottomRight: CorneredShape.Corner,
  bottomLeft: CorneredShape.Corner,
  tickSize: Dp = MARKER_TICK_SIZE.dp,
): MarkerCorneredShape =
  MarkerCorneredShape(topLeft, topRight, bottomRight, bottomLeft, tickSize.value)

/** Creates a [MarkerCorneredShape]. */
public fun markerCorneredShape(
  all: CorneredShape.Corner,
  tickSize: Dp = MARKER_TICK_SIZE.dp,
): MarkerCorneredShape = MarkerCorneredShape(all, tickSize.value)

/** Creates a [MarkerCorneredShape]. */
public fun markerCorneredShape(
  base: CorneredShape,
  tickSize: Dp = MARKER_TICK_SIZE.dp,
): MarkerCorneredShape = MarkerCorneredShape(base, tickSize.value)

/** Creates a [DashedShape]. */
public fun dashedShape(
  shape: Shape = Shape.Rectangle,
  dashLength: Dp = Defaults.DASHED_SHAPE_DASH_LENGTH.dp,
  gapLength: Dp = Defaults.DASHED_SHAPE_GAP_LENGTH.dp,
  fitStrategy: DashedShape.FitStrategy = DashedShape.FitStrategy.Resize,
): DashedShape = DashedShape(shape, dashLength.value, gapLength.value, fitStrategy)
