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

package com.patrykandpatrick.vico.multiplatform.cartesian.marker

import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.multiplatform.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.common.component.LineComponent

/** Houses information on a set of [ColumnCartesianLayer] columns to be marked. */
public interface ColumnCartesianLayerMarkerTarget : CartesianMarker.Target {
  /**
   * Holds [Column] instances, each of which houses information on a [ColumnCartesianLayer] column
   * to be marked.
   */
  public val columns: List<Column>

  /**
   * Houses information on a [ColumnCartesianLayer] column to be marked.
   *
   * @param entry the [ColumnCartesianLayerModel.Entry].
   * @param canvasY the pixel _y_ coordinate of the column’s top or bottom edge (depending on the
   *   sign of [ColumnCartesianLayerModel.Entry.y]).
   * @param color the column [LineComponent]’s color.
   */
  public data class Column(
    val entry: ColumnCartesianLayerModel.Entry,
    val canvasY: Float,
    val color: Color,
  )
}

internal data class MutableColumnCartesianLayerMarkerTarget(
  override val x: Double,
  override val canvasX: Float,
  override val columns: MutableList<ColumnCartesianLayerMarkerTarget.Column> = mutableListOf(),
) : ColumnCartesianLayerMarkerTarget
