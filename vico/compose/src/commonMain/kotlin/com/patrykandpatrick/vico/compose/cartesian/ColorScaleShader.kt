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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis

internal object ColorScaleShader {
  fun create(
    context: CartesianDrawingContext,
    colors: Map<Number, Color>,
    alpha: Float = 1f,
    from: Offset = Offset(0f, context.layerBounds.top),
    to: Offset = Offset(0f, context.layerBounds.bottom),
    verticalAxisPosition: Axis.Position.Vertical? = null,
  ): Shader =
    LinearGradientShader(
      from = from,
      to = to,
      colors = colors.getSortedColors(alpha),
      colorStops = getPositions(context, colors.keys, verticalAxisPosition),
    )

  private fun Map<Number, Color>.getSortedColors(alpha: Float): List<Color> =
    entries.sortedByDescending { it.key.toDouble() }.map { it.value.copy(alpha) }

  private fun getPositions(
    context: CartesianDrawingContext,
    y: Collection<Number>,
    verticalAxisPosition: Axis.Position.Vertical?,
  ): List<Float> {
    val maxY = context.ranges.getYRange(verticalAxisPosition).maxY
    val minY = context.ranges.getYRange(verticalAxisPosition).minY
    return y.map { y -> 1f - ((y.toFloat() - minY) / (maxY - minY)).toFloat() }.sorted()
  }
}
