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

package com.patrykandpatrick.vico.views.common.gesture

import android.graphics.RectF
import android.view.ScaleGestureDetector

internal class ChartScaleGestureListener(
  private val getLayerBounds: () -> RectF?,
  private val onZoom: (focusX: Float, zoomChange: Float) -> Unit,
) : ScaleGestureDetector.OnScaleGestureListener {
  override fun onScale(detector: ScaleGestureDetector): Boolean {
    onZoom(detector.focusX, detector.scaleFactor)
    return true
  }

  override fun onScaleBegin(detector: ScaleGestureDetector): Boolean =
    getLayerBounds()?.contains(detector.focusX, detector.focusY) == true

  override fun onScaleEnd(detector: ScaleGestureDetector) = Unit
}
