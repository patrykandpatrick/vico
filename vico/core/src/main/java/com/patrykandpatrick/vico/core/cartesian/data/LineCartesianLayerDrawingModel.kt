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
import com.patrykandpatrick.vico.core.common.data.DrawingModel
import com.patrykandpatrick.vico.core.common.lerp
import com.patrykandpatrick.vico.core.common.orZero

/**
 * Houses drawing information for a [LineCartesianLayer]. [opacity] is the lines’ opacity. [zeroY],
 * restricted to the interval [0, 1], specifies the position of the zero line (_y_ = 0) from the top
 * of the [LineCartesianLayer] as a fraction of the [LineCartesianLayer]’s height.
 */
public class LineCartesianLayerDrawingModel(
  private val pointInfo: List<Map<Float, PointInfo>>,
  public val zeroY: Float,
  public val opacity: Float = 1f,
) : DrawingModel<LineCartesianLayerDrawingModel.PointInfo>(pointInfo) {
  override fun transform(
    drawingInfo: List<Map<Float, PointInfo>>,
    from: DrawingModel<PointInfo>?,
    fraction: Float,
  ): DrawingModel<PointInfo> {
    val oldOpacity = (from as LineCartesianLayerDrawingModel?)?.opacity.orZero
    val oldZeroY = from?.zeroY ?: zeroY
    return LineCartesianLayerDrawingModel(
      drawingInfo,
      oldZeroY.lerp(zeroY, fraction),
      oldOpacity.lerp(opacity, fraction),
    )
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LineCartesianLayerDrawingModel &&
        pointInfo == other.pointInfo &&
        zeroY == other.zeroY &&
        opacity == other.opacity

  override fun hashCode(): Int {
    var result = pointInfo.hashCode()
    result = 31 * result + zeroY.hashCode()
    result = 31 * result + opacity.hashCode()
    return result
  }

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
