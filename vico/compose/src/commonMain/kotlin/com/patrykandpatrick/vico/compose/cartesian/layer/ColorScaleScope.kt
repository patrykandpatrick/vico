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

package com.patrykandpatrick.vico.compose.cartesian.layer

import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.common.data.ExtraStore

/** Builds color-scale entries for cartesian layer fills. */
public class ColorScaleScope internal constructor(public val extraStore: ExtraStore) {
  private val entries = linkedMapOf<Number, Color>()

  /** Adds a color-scale entry. */
  public infix fun Color.at(value: Number) {
    entries[value] = this
  }

  internal fun build(): Map<Number, Color> = entries.toMap()
}

internal fun buildColorScale(
  extraStore: ExtraStore,
  block: ColorScaleScope.() -> Unit,
): Map<Number, Color> = ColorScaleScope(extraStore).apply(block).build()
