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

package com.patrykandpatrick.vico.views.cartesian

import android.graphics.LinearGradient
import android.graphics.Shader
import com.patrykandpatrick.vico.views.cartesian.axis.Axis

internal object ColorScaleShader {
  fun create(
    context: CartesianDrawingContext,
    colors: Map<Number, Int>,
    left: Float = 0f,
    top: Float = context.layerBounds.top,
    right: Float = 0f,
    bottom: Float = context.layerBounds.bottom,
    verticalAxisPosition: Axis.Position.Vertical? = null,
  ): LinearGradient {
    val sortedMapEntries = colors.entries.sortedByDescending { it.key.toDouble() }
    val (safeColors, safePositions) =
      getSafeGradientData(sortedMapEntries, context, verticalAxisPosition)

    return LinearGradient(
      left,
      top,
      right,
      bottom,
      safeColors,
      safePositions,
      Shader.TileMode.CLAMP,
    )
  }

  private fun getPositions(
    context: CartesianDrawingContext,
    entries: List<Map.Entry<Number, Int>>,
    verticalAxisPosition: Axis.Position.Vertical?,
  ): FloatArray {
    val yRange = context.ranges.getYRange(verticalAxisPosition)
    val range = yRange.length
    if (range == 0.0) return getDegeneratePositions(entries.size)
    return FloatArray(entries.size) { index ->
      1f - ((entries[index].key.toDouble() - yRange.minY) / range).toFloat()
    }
  }

  private fun getColorsIntArray(entries: List<Map.Entry<Number, Int>>): IntArray =
    IntArray(entries.size) { index -> entries[index].value }

  private fun getSafeGradientData(
    entries: List<Map.Entry<Number, Int>>,
    context: CartesianDrawingContext,
    verticalAxisPosition: Axis.Position.Vertical?,
  ): Pair<IntArray, FloatArray> =
    when {
      entries.size >= 2 ->
        getColorsIntArray(entries) to getPositions(context, entries, verticalAxisPosition)
      entries.size == 1 -> {
        val color = getColorsIntArray(entries).first()
        intArrayOf(color, color) to floatArrayOf(0f, 1f)
      }
      else -> intArrayOf(0, 0) to floatArrayOf(0f, 1f)
    }

  private fun getDegeneratePositions(count: Int): FloatArray =
    when (count) {
      0 -> floatArrayOf()
      1 -> floatArrayOf(0f)
      else -> {
        val denominator = (count - 1).toFloat()
        FloatArray(count) { index -> index / denominator }
      }
    }
}
