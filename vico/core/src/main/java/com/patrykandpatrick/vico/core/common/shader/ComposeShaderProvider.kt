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

import android.graphics.BlendMode
import android.graphics.ComposeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import com.patrykandpatrick.vico.core.common.DrawingContext

internal data class ComposeShaderProvider(
  private val first: ShaderProvider,
  private val second: ShaderProvider,
  private val mode: Mode,
) : ShaderProvider {
  override fun getShader(
    context: DrawingContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) =
    mode.createShader(
      first.getShader(context, left, top, right, bottom),
      second.getShader(context, left, top, right, bottom),
    )

  interface Mode {
    fun createShader(first: Shader, second: Shader): ComposeShader

    @RequiresApi(Build.VERSION_CODES.Q)
    data class Blend(private val mode: BlendMode) : Mode {
      override fun createShader(first: Shader, second: Shader) = ComposeShader(first, second, mode)
    }

    data class PorterDuff(private val mode: android.graphics.PorterDuff.Mode) : Mode {
      override fun createShader(first: Shader, second: Shader) = ComposeShader(first, second, mode)
    }
  }
}
