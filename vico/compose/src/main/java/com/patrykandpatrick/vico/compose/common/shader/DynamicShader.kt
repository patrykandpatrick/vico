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

@file:Suppress("UnusedReceiverParameter")

package com.patrykandpatrick.vico.compose.common.shader

import android.graphics.Shader
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.translationMatrix
import com.patrykandpatrick.vico.core.common.DrawingContext
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.shader.CacheableDynamicShader
import com.patrykandpatrick.vico.core.common.shader.ComponentShader
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shader.LinearGradientShader

/**
 * Creates a [ComponentShader] out of the provided [component].
 *
 * @property component used as a pattern in the [Shader].
 * @property componentSize the size of the [component].
 * @property checkeredArrangement whether the [component] will be arranged in a checkered pattern.
 * @property tileXMode the horizontal tiling mode for the [component].
 * @property tileYMode the vertical tiling mode for the [component].
 */
public fun DynamicShader.Companion.component(
  component: Component,
  componentSize: Dp,
  checkeredArrangement: Boolean = true,
  tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
  tileYMode: Shader.TileMode = tileXMode,
): ComponentShader =
  ComponentShader(
    component = component,
    componentSizeDp = componentSize.value,
    checkeredArrangement = checkeredArrangement,
    tileXMode = tileXMode,
    tileYMode = tileYMode,
  )

/**
 * Creates a [DynamicShader] with a horizontal gradient. [colors] houses the gradient colors, and
 * [positions] specifies the color offsets (between 0 and 1), with `null` producing an even
 * distribution.
 */
public fun DynamicShader.Companion.horizontalGradient(
  colors: Array<Color>,
  positions: FloatArray? = null,
): DynamicShader =
  LinearGradientShader(IntArray(colors.size) { colors[it].toArgb() }, positions, true)

/**
 * Creates a [DynamicShader] with a vertical gradient. [colors] houses the gradient colors, and
 * [positions] specifies the color offsets (between 0 and 1), with `null` producing an even
 * distribution.
 */
public fun DynamicShader.Companion.verticalGradient(
  colors: Array<Color>,
  positions: FloatArray? = null,
): DynamicShader =
  LinearGradientShader(IntArray(colors.size) { colors[it].toArgb() }, positions, false)

/** Converts this [Brush] to a [DynamicShader]. */
public fun Brush.toDynamicShader(): DynamicShader =
  object : CacheableDynamicShader() {
    override fun createShader(
      context: DrawingContext,
      left: Float,
      top: Float,
      right: Float,
      bottom: Float,
    ): Shader {
      val paint = Paint()
      applyTo(size = Size(right - left, bottom - top), p = paint, alpha = 1f)
      return paint.shader!!.apply { setLocalMatrix(translationMatrix(left, top)) }
    }
  }
