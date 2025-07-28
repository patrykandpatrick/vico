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

package com.patrykandpatrick.vico.core.cartesian.marker

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext

/** Provides contentDescription for [CartesianMarker] * */
public fun interface ContentDescriptionProvider {

  /** Returns content description for given [CartesianMarker.Target]s */
  public fun getContentDescription(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>,
  ): String

  /** Houses a [ContentDescriptionProvider] factory function. */
  public companion object {
    /** Creates an instance of the default [ContentDescriptionProvider] implementation. */
    public fun default(): ContentDescriptionProvider = DefaultContentDescriptionProvider()
  }
}

internal class DefaultContentDescriptionProvider : ContentDescriptionProvider {

  override fun getContentDescription(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>,
  ): String {
    return buildString { targets.forEach { target -> append(target = target) } }
  }

  private fun StringBuilder.append(target: CartesianMarker.Target) {
    when (target) {
      is CandlestickCartesianLayerMarkerTarget -> {
        append("x ${target.x}.")
        append("opening ${target.entry.opening}.")
        append("closing ${target.entry.closing}.")
        append("low ${target.entry.low}.")
        append("high ${target.entry.high}.")
      }

      is ColumnCartesianLayerMarkerTarget -> {
        append("x ${target.x}.")
        target.columns.forEach { append("y ${it.entry.y}.") }
      }

      is LineCartesianLayerMarkerTarget -> {
        append("x ${target.x}.")
        target.points.forEach { append("y ${it.entry.y}.") }
      }

      else -> throw IllegalArgumentException("Unexpected `CartesianMarker.Target` implementation.")
    }
  }
}
