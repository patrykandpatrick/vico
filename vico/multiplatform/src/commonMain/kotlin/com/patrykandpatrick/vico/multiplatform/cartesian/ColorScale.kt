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

package com.patrykandpatrick.vico.multiplatform.cartesian

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.Axis
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore

/**
 * TODO
 *
 * @param verticalAxisPosition TODO
 * @param colors TODO
 */
public class ColorScale(
  private val verticalAxisPosition: Axis.Position.Vertical? = null,
  private val colors: (ExtraStore) -> Map<Double, Color>,
  private val alpha: (ExtraStore) -> Float = { 1f },
) {

  public fun getColorScaleShader(
    context: CartesianDrawingContext,
    translationY: Float = 0f,
  ): Shader =
    ColorScaleShader.create(
      context = context,
      colors = colors(context.extraStore),
      alpha = alpha(context.extraStore),
      from = Offset(x = 0f, y = context.layerBounds.top - translationY),
      to = Offset(x = 0f, y = context.layerBounds.bottom - translationY),
      verticalAxisPosition = verticalAxisPosition,
    )
}
