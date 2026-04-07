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

package com.patrykandpatrick.vico.compose.pie

import com.patrykandpatrick.vico.compose.common.lerp
import com.patrykandpatrick.vico.compose.common.orZero
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel
import kotlin.math.max

private const val MAX_SWEEP_DEGREES = 360f - 0.001f

internal class PieChartDrawingModel(val slices: List<SliceInfo>) {
  internal fun transform(from: PieChartDrawingModel?, fraction: Float): PieChartDrawingModel {
    val oldSlices = from?.slices.orEmpty()
    val sliceCount = max(oldSlices.size, slices.size)
    return PieChartDrawingModel(
      List(sliceCount) { index ->
        slices.getOrNull(index)?.transform(oldSlices.getOrNull(index), fraction)
          ?: checkNotNull(oldSlices.getOrNull(index)).transform(null, 1f - fraction)
      }
    )
  }

  internal class SliceInfo(
    val degrees: Float,
    val sliceOpacity: Float = 1f,
    val labelOpacity: Float = 1f,
  ) {
    internal fun transform(from: SliceInfo?, fraction: Float): SliceInfo {
      val oldDegrees = from?.degrees.orZero
      return SliceInfo(
        degrees = oldDegrees.lerp(degrees, fraction).coerceAtMost(MAX_SWEEP_DEGREES),
        sliceOpacity =
          when {
            from == null || from.degrees == 0f -> fraction
            degrees == 0f -> 1f - fraction
            else -> 1f
          },
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
          sliceOpacity == other.sliceOpacity &&
          labelOpacity == other.labelOpacity

    override fun hashCode(): Int {
      var result = degrees.hashCode()
      result = 31 * result + sliceOpacity.hashCode()
      result = 31 * result + labelOpacity.hashCode()
      return result
    }
  }

  override fun equals(other: Any?): Boolean =
    this === other || other is PieChartDrawingModel && slices == other.slices

  override fun hashCode(): Int = slices.hashCode()
}

internal fun PieChartModel.toDrawingModel(): PieChartDrawingModel =
  PieChartDrawingModel(
    entries.map { entry ->
      PieChartDrawingModel.SliceInfo(
        degrees =
          if (sum == 0f) 0f else (entry.value / sum * 360f).coerceAtMost(MAX_SWEEP_DEGREES)
      )
    }
  )
