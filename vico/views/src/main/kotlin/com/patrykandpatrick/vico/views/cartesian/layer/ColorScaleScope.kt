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

package com.patrykandpatrick.vico.views.cartesian.layer

import com.patrykandpatrick.vico.views.common.data.ExtraStore

/** Builds color-scale entries for cartesian layer fills. */
public class ColorScaleScope internal constructor(public val extraStore: ExtraStore) {
  private val entries = linkedMapOf<Number, Int>()

  /** Adds a color-scale entry. */
  public infix fun Int.at(value: Number) {
    entries[value] = this
  }

  internal fun build(): Map<Number, Int> = entries.toMap()
}

internal fun buildColorScale(
  extraStore: ExtraStore,
  colors: ColorScaleScope.() -> Unit,
): Map<Number, Int> = ColorScaleScope(extraStore).apply(colors).build()
