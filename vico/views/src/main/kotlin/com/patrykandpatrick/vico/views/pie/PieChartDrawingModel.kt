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

package com.patrykandpatrick.vico.views.pie

import com.patrykandpatrick.vico.views.common.lerp
import com.patrykandpatrick.vico.views.common.orZero
import java.util.Objects

internal class PieChartDrawingModel(val slices: List<SliceInfo>) {
  class SliceInfo(
    val degrees: Float,
    val value: Float?,
    val sliceOpacity: Float = 1f,
    val labelOpacity: Float = 1f,
  ) {
    internal fun transform(from: SliceInfo?, fraction: Float): SliceInfo {
      val oldDegrees = from?.degrees.orZero
      val oldValue = from?.value
      val sliceOpacity =
        when {
          from == null || from.degrees == 0f -> fraction
          degrees == 0f -> 1f - fraction
          else -> 1f
        }
      return SliceInfo(
        degrees = oldDegrees.lerp(degrees, fraction),
        value = if (fraction < 0.5f) oldValue else value,
        sliceOpacity = sliceOpacity,
        labelOpacity =
          when {
            from == null || from.degrees == 0f -> fraction
            degrees == 0f -> 1f - fraction
            else -> 1f
          },
      )
    }

    override fun equals(other: Any?): Boolean =
      this === other ||
        other is SliceInfo &&
          degrees == other.degrees &&
          value == other.value &&
          sliceOpacity == other.sliceOpacity &&
          labelOpacity == other.labelOpacity

    override fun hashCode(): Int = Objects.hash(degrees, value, sliceOpacity, labelOpacity)
  }

  override fun equals(other: Any?): Boolean =
    this === other || other is PieChartDrawingModel && slices == other.slices

  override fun hashCode(): Int = Objects.hash(slices)
}
