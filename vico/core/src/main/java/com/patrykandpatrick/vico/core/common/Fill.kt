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

package com.patrykandpatrick.vico.core.common

import android.graphics.Color
import com.patrykandpatrick.vico.core.common.shader.DynamicShader

/**
 * Stores fill properties.
 *
 * @property color the color. If [shader] is not `null`, this is [Color.BLACK].
 * @property shader the [DynamicShader].
 */
public class Fill private constructor(public val color: Int, public val shader: DynamicShader?) {
  /** Creates a color [Fill]. */
  public constructor(color: Int) : this(color = color, shader = null)

  /** Creates a [DynamicShader]&#0020;[Fill]. */
  public constructor(shader: DynamicShader) : this(Color.BLACK, shader)

  override fun equals(other: Any?): Boolean =
    this === other || other is Fill && color == other.color && shader == other.shader

  override fun hashCode(): Int = 31 * color + shader?.hashCode().orZero

  /** Houses [Fill] singletons. */
  public companion object {
    /** A black [Fill]. */
    public val Black: Fill = Fill(Color.BLACK)

    /** A transparent [Fill]. */
    public val Transparent: Fill = Fill(Color.TRANSPARENT)
  }
}
