/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.extension

import kotlin.math.roundToInt

const val PI_RAD = 180f

inline val Int.half: Int
    get() = this / 2

inline val Float.half: Float
    get() = this / 2

inline val Float.doubled: Float
    get() = this * 2

inline val Number?.orZeroInt: Int
    get() = this?.toInt() ?: 0

inline val Float?.orZero: Float
    get() = this ?: 0f

inline val Int?.orZero: Int
    get() = this ?: 0

inline val Float.round: Float
    get() = roundToInt().toFloat()

inline val Float.piRad: Float
    get() = this * PI_RAD

fun Float.between(min: Float, max: Float): Float =
    minOf(maxOf(this, min), max)

fun Int.hasFlag(flag: Int): Boolean = (this and flag) == flag

public fun firstNonNegativeOf(vararg floats: Float): Float? = floats.firstOrNull { it >= 0f }
