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

package com.patrykandpatrick.vico.multiplatform.common.shape

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.multiplatform.common.MeasuringContext

/** Defines a shape. */
@Immutable
public fun interface Shape {
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
    public val Rectangle: Shape = Shape { _, path, left, top, right, bottom ->
      path.moveTo(left, top)
      path.lineTo(right, top)
      path.lineTo(right, bottom)
      path.lineTo(left, bottom)
      path.close()
    }
  }
}

/**
 * Converts this [androidx.compose.ui.graphics.Shape] to an instance of
 * [com.patrykandpatrick.vico.multiplatform.common.shape.Shape].
 */
public fun androidx.compose.ui.graphics.Shape.toVicoShape(): Shape =
  Shape { context, path, left, top, right, bottom ->
    val outline =
      createOutline(
        size = Size(width = right - left, height = bottom - top),
        layoutDirection = if (context.isLtr) LayoutDirection.Ltr else LayoutDirection.Rtl,
        density = context.density,
      )
    when (outline) {
      is Outline.Rectangle ->
        path.addRect(Rect(left, top, right, bottom), Path.Direction.CounterClockwise)
      is Outline.Rounded -> path.addRoundRect(outline.roundRect, Path.Direction.CounterClockwise)
      is Outline.Generic -> path.addPath(outline.path, Offset(left, top))
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
      val path = Path()
      outline(
        density = density.density,
        path = path,
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height,
      )
      return Outline.Generic(path)
    }
  }
