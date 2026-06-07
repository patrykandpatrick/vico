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

package com.patrykandpatrick.vico.views.cartesian

import android.graphics.RectF
import com.patrykandpatrick.vico.views.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.views.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.views.common.MeasuringContext
import com.patrykandpatrick.vico.views.common.Point
import com.patrykandpatrick.vico.views.common.getStart

internal fun MeasuringContext.pointerPositionToX(
  pointerPosition: Point,
  layerDimensions: CartesianLayerDimensions,
  layerBounds: RectF,
  scrollValue: Float,
  ranges: CartesianChartRanges,
): Double {
  val drawingStart =
    layerBounds.getStart(isLtr) + layoutDirectionMultiplier * layerDimensions.startPadding
  val pointerX = pointerPosition.x - drawingStart + scrollValue
  return ranges.minX + pointerX * ranges.xStep / layerDimensions.xSpacing
}
