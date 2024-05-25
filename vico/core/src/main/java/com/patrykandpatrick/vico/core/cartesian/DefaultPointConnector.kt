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

import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.half
import kotlin.math.abs

/**
 * The default implementation of [LineCartesianLayer.LineSpec.PointConnector]. This uses cubic
 * bezier curves.
 *
 * @property cubicStrength the strength of the cubic bezier curve between each point on the line.
 */
public class DefaultPointConnector(private val cubicStrength: Float = Defaults.CUBIC_STRENGTH) :
  LineCartesianLayer.LineSpec.PointConnector {
  public override fun connect(
    path: Path,
    prevX: Float,
    prevY: Float,
    x: Float,
    y: Float,
    horizontalDimensions: HorizontalDimensions,
    bounds: RectF,
  ) {
    path.horizontalCubicTo(
      prevX = prevX,
      prevY = prevY,
      x = x,
      y = y,
      curvature =
        abs(x - prevX).half *
          cubicStrength *
          (abs(x = y - prevY) / bounds.bottom * CUBIC_Y_MULTIPLIER).coerceAtMost(maximumValue = 1f),
    )
  }

  private companion object {
    const val CUBIC_Y_MULTIPLIER = 4
  }
}

private fun Path.horizontalCubicTo(
  prevX: Float,
  prevY: Float,
  x: Float,
  y: Float,
  curvature: Float,
) {
  val directionMultiplier = if (x >= prevX) 1f else -1f
  cubicTo(
    prevX + directionMultiplier * curvature,
    prevY,
    x - directionMultiplier * curvature,
    y,
    x,
    y,
  )
}
