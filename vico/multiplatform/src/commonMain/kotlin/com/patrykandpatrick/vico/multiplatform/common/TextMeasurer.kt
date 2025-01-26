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

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints

internal fun TextMeasurer.measure(
  text: CharSequence,
  style: TextStyle,
  overflow: TextOverflow,
  maxLines: Int,
  width: Int,
  height: Int,
): TextLayoutResult {
  val constraints = Constraints(maxWidth = width, maxHeight = height)
  return if (text is AnnotatedString) {
    measure(
      text = text,
      style = style,
      overflow = overflow,
      maxLines = maxLines,
      constraints = constraints,
    )
  } else {
    measure(
      text = text.toString(),
      style = style,
      overflow = overflow,
      maxLines = maxLines,
      constraints = constraints,
    )
  }
}

internal val TextLayoutResult.bounds: Rect
  get() = Rect(0f, 0f, size.width.toFloat(), size.height.toFloat())
