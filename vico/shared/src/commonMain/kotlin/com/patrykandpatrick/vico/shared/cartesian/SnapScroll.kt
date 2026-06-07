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

package com.patrykandpatrick.vico.shared.cartesian

import com.patrykandpatrick.vico.shared.common.rangeWith
import kotlin.math.abs
import kotlin.math.roundToInt

internal fun getSnapDelta(
  value: Float,
  maxValue: Float,
  xSnapStep: Double?,
  xStep: Double,
  xSpacing: Float,
  startPadding: Float,
  targetValue: Float = value,
): Float? {
  val snapTarget =
    getSnapTarget(
      maxValue = maxValue,
      xSnapStep = xSnapStep,
      xStep = xStep,
      xSpacing = xSpacing,
      startPadding = startPadding,
      targetValue = targetValue,
    ) ?: return null
  val delta = snapTarget - value
  return if (delta == 0f) null else delta
}

internal fun getSnapTarget(
  maxValue: Float,
  xSnapStep: Double?,
  xStep: Double,
  xSpacing: Float,
  startPadding: Float,
  targetValue: Float,
): Float? {
  xSnapStep ?: return null
  val windowPx = (xSnapStep / xStep).toFloat() * xSpacing
  if (windowPx <= 0f) return null
  val coercedTargetValue = targetValue.coerceIn(0f.rangeWith(maxValue))
  val closestStepTarget =
    startPadding + ((coercedTargetValue - startPadding) / windowPx).roundToInt() * windowPx
  return listOf(0f, closestStepTarget, maxValue)
    .filter { it in 0f.rangeWith(maxValue) }
    .minBy { abs(it - coercedTargetValue) }
}
