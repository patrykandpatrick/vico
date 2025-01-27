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

@file:Suppress("NOTHING_TO_INLINE")

package com.patrykandpatrick.vico.multiplatform.common

/** Packs two [Float] values into one [Long] value for use in inline classes. */
internal inline fun packFloats(val1: Float, val2: Float): Long {
  val v1 = val1.toBits().toLong()
  val v2 = val2.toBits().toLong()
  return v1.shl(32) or (v2 and 0xFFFFFFFF)
}

/** Unpacks the first [Float] value in [packFloats] from its returned [Long]. */
internal inline fun unpackFloat1(value: Long) = Float.fromBits(value.shr(32).toInt())

/** Unpacks the second [Float] value in [packFloats] from its returned [Long]. */
internal inline fun unpackFloat2(value: Long) = Float.fromBits(value.and(0xFFFFFFFF).toInt())
