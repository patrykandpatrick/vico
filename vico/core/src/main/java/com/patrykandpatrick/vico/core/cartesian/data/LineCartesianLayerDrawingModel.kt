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

package com.patrykandpatrick.vico.core.cartesian.data

import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.data.CartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.common.lerp
import com.patrykandpatrick.vico.core.common.orZero

/** Houses [LineCartesianLayer] drawing information. [opacity] is the lines’ opacity. */
public class LineCartesianLayerDrawingModel(
  private val pointInfo: List<Map<Double, PointInfo>>,
  public val opacity: Float = 1f,
) : CartesianLayerDrawingModel<LineCartesianLayerDrawingModel.PointInfo>(pointInfo) {
  override fun transform(
    drawingInfo: List<Map<Double, PointInfo>>,
    from: CartesianLayerDrawingModel<PointInfo>?,
    fraction: Float,
  ): CartesianLayerDrawingModel<PointInfo> =
    LineCartesianLayerDrawingModel(
      drawingInfo,
      (from as LineCartesianLayerDrawingModel?)?.opacity.orZero.lerp(opacity, fraction),
    )

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LineCartesianLayerDrawingModel &&
        pointInfo == other.pointInfo &&
        opacity == other.opacity

  override fun hashCode(): Int = 31 * pointInfo.hashCode() + opacity.hashCode()

  /**
   * Houses positional information for a [LineCartesianLayer]’s point. [y] expresses the distance of
   * the point from the bottom of the [LineCartesianLayer] as a fraction of the
   * [LineCartesianLayer]’s height.
   */
  public class PointInfo(public val y: Float) : DrawingInfo {
    override fun transform(from: DrawingInfo?, fraction: Float): DrawingInfo {
      val oldY = (from as? PointInfo)?.y.orZero
      return PointInfo(oldY.lerp(y, fraction))
    }

    override fun equals(other: Any?): Boolean = this === other || other is PointInfo && y == other.y

    override fun hashCode(): Int = y.hashCode()
  }
}
