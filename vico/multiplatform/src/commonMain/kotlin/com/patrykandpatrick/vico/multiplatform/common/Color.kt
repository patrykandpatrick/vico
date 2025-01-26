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

package com.patrykandpatrick.vico.multiplatform.common

private const val ALPHA_BIT_SHIFT = 24
private const val COLOR_MASK = 0xff

internal val Int.alpha: Int
  get() = extractColorChannel(ALPHA_BIT_SHIFT)

private fun Int.extractColorChannel(bitShift: Int): Int = this shr bitShift and COLOR_MASK

internal val Long.alpha: Float
  get() =
    if (this and 0x3fL == 0L) (this shr 56 and 0xff) / 255.0f else (this shr 6 and 0x3ff) / 1023.0f
