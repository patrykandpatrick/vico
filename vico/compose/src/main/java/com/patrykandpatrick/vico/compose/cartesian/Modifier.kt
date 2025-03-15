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

private const val BASE_SCROLL_ZOOM_DELTA = 0.1f

private fun Offset.toPoint() = Point(x, y)

internal fun Modifier.pointerInput(
  scrollState: VicoScrollState,
  onPointerPositionChange: ((Point?) -> Unit)?,
  onZoom: ((Float, Offset) -> Unit)?,
  consumeMoveEvents: Boolean,
) =
  scrollable(
      state = scrollState.scrollableState,
      orientation = Orientation.Horizontal,
      enabled = scrollState.scrollEnabled,
      reverseDirection = true,
    )
    .pointerInput(onZoom, onPointerPositionChange) {
      awaitPointerEventScope {
        while (true) {
          val event = awaitPointerEvent()
          when {
            event.type == PointerEventType.Scroll && scrollState.scrollEnabled && onZoom != null ->
              onZoom(
                1 - event.changes.first().scrollDelta.y * BASE_SCROLL_ZOOM_DELTA,
                event.changes.first().position,
              )
            onPointerPositionChange == null -> continue
            event.type == PointerEventType.Press ->
              onPointerPositionChange(event.changes.first().position.toPoint())
            event.type == PointerEventType.Release -> onPointerPositionChange(null)
            event.type == PointerEventType.Move && !scrollState.scrollEnabled -> {
              val changes = event.changes.first()
              if (consumeMoveEvents) changes.consume()
              onPointerPositionChange(changes.position.toPoint())
            }
          }
        }
      }
    }
    .then(
      if (scrollState.scrollEnabled && onZoom != null) {
        Modifier.pointerInput(onPointerPositionChange, onZoom) {
          detectZoomGestures { centroid, zoom ->
            onPointerPositionChange?.invoke(null)
            onZoom(zoom, centroid)
          }
        }
      } else {
        Modifier
      }
    )
