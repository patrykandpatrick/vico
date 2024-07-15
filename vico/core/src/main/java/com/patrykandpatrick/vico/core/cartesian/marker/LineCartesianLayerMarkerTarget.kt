/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer

/** Houses information on a set of [LineCartesianLayer] points to be marked. */
public interface LineCartesianLayerMarkerTarget : CartesianMarker.Target {
  /** Holds [Point] instances, each of which houses information on a marked point. */
  public val points: List<Point>

  /**
   * Houses information on a [LineCartesianLayer] point to be marked.
   *
   * @param entry the [LineCartesianLayerModel.Entry].
   * @param canvasY the point’s pixel _y_ coordinate.
   * @param color the [LineCartesianLayer.Line]’s color for the point.
   */
  public data class Point(
    val entry: LineCartesianLayerModel.Entry,
    val canvasY: Float,
    val color: Int,
  )
}

internal data class MutableLineCartesianLayerMarkerTarget(
  override val x: Double,
  override val canvasX: Float,
  override val points: MutableList<LineCartesianLayerMarkerTarget.Point> = mutableListOf(),
) : LineCartesianLayerMarkerTarget
