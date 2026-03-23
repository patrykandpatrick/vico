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
    from: Offset = Offset(0f, context.layerBounds.top),
    to: Offset = Offset(0f, context.layerBounds.bottom),
    verticalAxisPosition: Axis.Position.Vertical? = null,
  ): Shader {
    val sortedColors = colors.getSortedColors()
    val positions = getPositions(context, colors.keys, verticalAxisPosition)
    val (safeColors, safePositions) = getSafeGradientData(sortedColors, positions)
    return LinearGradientShader(
      from = from,
      to = to,
      colors = safeColors,
      colorStops = safePositions,
    )
  }

  private fun Map<Number, Color>.getSortedColors(): List<Color> =
    entries.sortedByDescending { it.key.toDouble() }.map { it.value }

  private fun getPositions(
    context: CartesianDrawingContext,
    y: Collection<Number>,
    verticalAxisPosition: Axis.Position.Vertical?,
  ): List<Float> {
    val yRange = context.ranges.getYRange(verticalAxisPosition)
    val range = yRange.length
    if (range == 0.0) return getDegeneratePositions(y.size)
    return y.map { y -> 1f - ((y.toDouble() - yRange.minY) / range).toFloat() }.sorted()
  }

  private fun getSafeGradientData(
    colors: List<Color>,
    positions: List<Float>,
  ): Pair<List<Color>, List<Float>> =
    when {
      colors.size >= 2 && positions.size >= 2 -> Pair(colors, positions)
      colors.isNotEmpty() ->
        Pair(
          listOf(colors.first(), colors.first()),
          positions.firstOrNull()?.let { listOf(it, it) } ?: listOf(0f, 1f),
        )
      else -> Pair(listOf(Color.Transparent, Color.Transparent), listOf(0f, 1f))
    }

  private fun getDegeneratePositions(count: Int): List<Float> =
    when (count) {
      0 -> emptyList()
      1 -> listOf(0f)
      else -> {
        val step = 1f / (count - 1)
        List(count) { index -> index * step }
      }
    }
}
