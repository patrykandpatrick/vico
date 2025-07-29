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

package com.patrykandpatrick.vico.compose.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.mapKeysAndValues
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider

/** Creates a [Fill]. */
public fun fill(color: Color): Fill = Fill(color.toArgb())

/** Creates a [Fill]. */
public fun fill(shaderProvider: ShaderProvider): Fill = Fill(shaderProvider)

/** Creates a [Fill] */
public fun <T : Number> fill(
  colors: (ExtraStore) -> Map<T, Color>,
  alpha: (ExtraStore) -> Float = { 1f },
  verticalAxisPosition: Axis.Position.Vertical? = null,
): Fill =
  Fill(
    colors = { extraStore ->
      colors(extraStore).mapKeysAndValues { key, color -> key.toDouble() to color.toArgb() }
    },
    alpha = alpha,
    verticalAxisPosition = verticalAxisPosition,
  )
