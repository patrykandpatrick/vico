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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.patrykandpatrick.vico.compose.common.detectZoomGestures
import com.patrykandpatrick.vico.core.common.Point

internal fun Modifier.chartTouchEvent(
  setTouchPoint: ((Point?) -> Unit)?,
  isScrollEnabled: Boolean,
  scrollState: VicoScrollState,
  onZoom: ((Float, Offset) -> Unit)?,
): Modifier =
  scrollable(
      state = scrollState.scrollableState,
      orientation = Orientation.Horizontal,
      reverseDirection = true,
      enabled = isScrollEnabled,
    )
    .then(
      if (setTouchPoint != null) {
        pointerInput(setTouchPoint) {
          awaitPointerEventScope {
            while (true) {
              val event = awaitPointerEvent()
              when (event.type) {
                PointerEventType.Press -> setTouchPoint(event.changes.first().position.point)
                PointerEventType.Release -> setTouchPoint(null)
                PointerEventType.Move ->
                  if (!isScrollEnabled) setTouchPoint(event.changes.first().position.point)
              }
            }
          }
        }
      } else {
        Modifier
      }
    )
    .then(
      if (isScrollEnabled && onZoom != null) {
        pointerInput(setTouchPoint, onZoom) {
          detectZoomGestures { centroid, zoom ->
            setTouchPoint?.invoke(null)
            onZoom(zoom, centroid)
          }
        }
      } else {
        Modifier
      }
    )

private val Offset.point: Point
  get() = Point(x, y)
