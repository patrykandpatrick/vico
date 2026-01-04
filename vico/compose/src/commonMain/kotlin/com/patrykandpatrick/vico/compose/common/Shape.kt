/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.translate
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

internal fun Shape.outline(
  density: Density,
  layoutDirection: LayoutDirection,
  path: Path,
  left: Float,
  top: Float,
  right: Float,
  bottom: Float,
) {
  val width = right - left
  val height = bottom - top
  if (width == 0f || height == 0f) return
  with(path) {
    when (val outline = createOutline(Size(width, height), layoutDirection, density)) {
      is Outline.Rectangle -> addRect(Rect(left, top, right, bottom))
      is Outline.Rounded -> addRoundRect(outline.roundRect.translate(Offset(left, top)))
      is Outline.Generic -> addPath(outline.path, Offset(left, top))
    }
  }
}
