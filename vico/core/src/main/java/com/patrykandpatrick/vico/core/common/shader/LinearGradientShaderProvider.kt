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

package com.patrykandpatrick.vico.core.common.shader

import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.common.DrawingContext
import java.util.Objects

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class LinearGradientShaderProvider(
  private val colors: IntArray,
  private val positions: FloatArray?,
  private val isHorizontal: Boolean,
) : CachingShaderProvider() {
  override fun createShader(
    context: DrawingContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ): Shader =
    if (isHorizontal) {
      LinearGradient(left, top, right, top, colors, positions, Shader.TileMode.CLAMP)
    } else {
      LinearGradient(left, top, left, bottom, colors, positions, Shader.TileMode.CLAMP)
    }

  override fun createKey(left: Float, top: Float, right: Float, bottom: Float): String =
    "$this$left,$top,$right,$bottom"

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LinearGradientShaderProvider &&
        colors.contentEquals(other.colors) &&
        positions.contentEquals(other.positions)

  override fun hashCode(): Int = Objects.hash(colors, positions)

  @OptIn(ExperimentalStdlibApi::class)
  override fun toString(): String =
    "LinearGradientShader(colors=" +
      "${colors.joinToString(prefix = "[", postfix = "]") { it.toHexString(HexFormat.UpperCase) } }, " +
      "positions=${positions?.joinToString(prefix = "[", postfix = "]") { it.toString() }}, " +
      "isHorizontal=$isHorizontal)"
}
