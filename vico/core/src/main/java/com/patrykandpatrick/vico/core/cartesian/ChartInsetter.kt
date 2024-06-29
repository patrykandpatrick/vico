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

package com.patrykandpatrick.vico.core.cartesian

import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker

/**
 * Enables a component to add insets to [CartesianChart]s to make room for itself. This is used by
 * [Axis], [CartesianMarker], and the like.
 */
public interface ChartInsetter {
  /**
   * Called during the measurement phase, before [updateHorizontalInsets]. Both horizontal and
   * vertical insets can be requested from this function. The final inset for a given edge of the
   * associated [CartesianChart] is the largest of the insets requested for the edge.
   *
   * @param context holds data used for the measuring of components.
   * @param horizontalDimensions the [CartesianChart]’s [HorizontalDimensions].
   * @param insets used to store the requested insets.
   */
  public fun updateInsets(
    context: CartesianMeasureContext,
    horizontalDimensions: HorizontalDimensions,
    insets: Insets,
  ) {}

  /**
   * Called during the measurement phase, after [updateInsets]. Only horizontal insets can be
   * requested from this function. Unless the available height is of interest, [updateInsets] can be
   * used to set all insets. The final inset for a given edge of the associated [CartesianChart] is
   * the largest of the insets requested for the edge.
   *
   * @param context holds data used for the measuring of components.
   * @param freeHeight the available height. The vertical insets are considered here.
   * @param insets used to store the requested insets.
   */
  public fun updateHorizontalInsets(
    context: CartesianMeasureContext,
    freeHeight: Float,
    insets: HorizontalInsets,
  ) {}
}
