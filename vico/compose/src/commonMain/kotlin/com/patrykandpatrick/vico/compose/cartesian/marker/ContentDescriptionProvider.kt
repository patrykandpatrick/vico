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

package com.patrykandpatrick.vico.compose.cartesian.marker

import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.common.format

/** Provides [CartesianChart] content descriptions for accessibility services. */
public fun interface ContentDescriptionProvider {
  /** Returns a content description for the given [CartesianMarker.Target]s. */
  public fun getContentDescription(
    context: CartesianMeasuringContext?,
    targets: List<CartesianMarker.Target>,
  ): String

  /** Houses a [ContentDescriptionProvider] singleton. */
  public companion object {
    /** The default [ContentDescriptionProvider] implementation. */
    public val default: ContentDescriptionProvider = DefaultContentDescriptionProvider
  }
}

internal object DefaultContentDescriptionProvider : ContentDescriptionProvider {
  override fun getContentDescription(
    context: CartesianMeasuringContext?,
    targets: List<CartesianMarker.Target>,
  ): String = buildString {
    targets.forEachIndexed { index, target ->
      appendTarget(target)
      if (index < targets.lastIndex) {
        append(" ")
      }
    }
  }

  private fun StringBuilder.appendTarget(target: CartesianMarker.Target) {
    when (target) {
      is CandlestickCartesianLayerMarkerTarget -> {
        append("x: ${target.x.format()}. ")
        append("Opening: ${target.entry.opening.format()}. ")
        append("Closing: ${target.entry.closing.format()}. ")
        append("Low: ${target.entry.low.format()}. ")
        append("High: ${target.entry.high.format()}.")
      }

      is ColumnCartesianLayerMarkerTarget -> {
        append("x: ${target.x.format()}. ")
        target.columns.forEachIndexed { index, column ->
          append("y: ${column.entry.y.format()}.")
          if (index < target.columns.lastIndex) {
            append(" ")
          }
        }
      }

      is LineCartesianLayerMarkerTarget -> {
        append("x: ${target.x.format()}. ")
        target.points.forEachIndexed { index, point ->
          append("y: ${point.entry.y.format()}.")
          if (index < target.points.lastIndex) {
            append(" ")
          }
        }
      }

      else -> throw IllegalArgumentException("Unexpected `CartesianMarker.Target` implementation.")
    }
  }
}
