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

package com.patrykandpatrick.vico.core.cartesian

import android.graphics.Shader
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.common.data.ExtraStore

/**
 * TODO
 *
 * @param verticalAxisPosition TODO
 * @param colors TODO
 */
public class ColorScale(
  private val colors: (ExtraStore) -> Map<Double, Int>,
  private val alpha: (ExtraStore) -> Float = { 1f },
  private val verticalAxisPosition: Axis.Position.Vertical? = null,
) {

  public fun getColorScaleShader(context: CartesianDrawingContext): Shader =
    ColorScaleShader.create(
      context = context,
      colors = colors(context.extraStore),
      alpha = alpha(context.extraStore),
      verticalAxisPosition = verticalAxisPosition,
    )
}
