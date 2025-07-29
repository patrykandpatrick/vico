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

import android.graphics.LinearGradient
import android.graphics.Shader
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.common.copyColor

internal object ColorScaleShader {

  fun <T : Number, R : Number> create(
    context: CartesianDrawingContext,
    colors: Map<T, R>,
    alpha: Float = 1f,
    left: Float = 0f,
    top: Float = context.layerBounds.top,
    right: Float = 0f,
    bottom: Float = context.layerBounds.bottom,
    verticalAxisPosition: Axis.Position.Vertical? = null,
  ): LinearGradient {
    val sortedMapEntries = colors.entries.sortedByDescending { it.key.toDouble() }

    return LinearGradient(
      left,
      top,
      right,
      bottom,
      getColorsIntArray(sortedMapEntries, alpha),
      getPositions(context, sortedMapEntries, verticalAxisPosition),
      Shader.TileMode.CLAMP,
    )
  }

  private fun <T : Number> getPositions(
    context: CartesianDrawingContext,
    entries: List<Map.Entry<T, *>>,
    verticalAxisPosition: Axis.Position.Vertical?,
  ): FloatArray {
    val maxY = context.ranges.getYRange(verticalAxisPosition).maxY
    val minY = context.ranges.getYRange(verticalAxisPosition).minY
    return FloatArray(entries.size) { index ->
      1f - ((entries[index].key.toFloat() - minY) / (maxY - minY)).toFloat()
    }
  }

  private fun <T : Number, R : Number> getColorsIntArray(
    entries: List<Map.Entry<T, R>>,
    alpha: Float,
  ): IntArray =
    IntArray(entries.size) { index -> entries[index].value.toInt().copyColor(alpha = alpha) }
}
