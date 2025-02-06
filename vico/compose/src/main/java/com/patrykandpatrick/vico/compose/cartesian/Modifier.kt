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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.patrykandpatrick.vico.compose.common.detectZoomGestures
import com.patrykandpatrick.vico.core.cartesian.marker.PointerEvent
import com.patrykandpatrick.vico.core.common.Point

private const val BASE_SCROLL_ZOOM_DELTA = 0.1f

private fun Offset.toPoint() = Point(x, y)

internal fun Modifier.pointerInput(
  scrollState: VicoScrollState,
  onPointerStateChange: ((PointerEvent?) -> Unit)?,
  onZoom: ((Float, Offset) -> Unit)?,
  consumeMoveEvents: Boolean,
) =
  scrollable(
      state = scrollState.scrollableState,
      orientation = Orientation.Horizontal,
      enabled = scrollState.scrollEnabled,
      reverseDirection = true,
    )
    .pointerInput(onZoom, onPointerStateChange) {
      awaitPointerEventScope {
        while (true) {
          val event = awaitPointerEvent()
          val pointerPosition = event.changes.first().position.toPoint()
          when {
            event.type == PointerEventType.Scroll && scrollState.scrollEnabled && onZoom != null ->
              onZoom(
                1 - event.changes.first().scrollDelta.y * BASE_SCROLL_ZOOM_DELTA,
                event.changes.first().position,
              )
            onPointerStateChange == null -> continue
            event.type == PointerEventType.Press ->
              onPointerStateChange(PointerEvent.Press(pointerPosition))
            event.type == PointerEventType.Release ->
              onPointerStateChange(PointerEvent.Release(pointerPosition))
            event.type == PointerEventType.Move && !scrollState.scrollEnabled -> {
              val changes = event.changes.first()
              if (consumeMoveEvents) changes.consume()
              onPointerStateChange(PointerEvent.Move(pointerPosition))
            }
          }
        }
      }
    }
    .then(
      if (scrollState.scrollEnabled && onZoom != null) {
        Modifier.pointerInput(onPointerStateChange, onZoom) {
          detectZoomGestures { centroid, zoom ->
            onPointerStateChange?.invoke(PointerEvent.Zoom(centroid.toPoint()))
            onZoom(zoom, centroid)
          }
        }
      } else {
        Modifier
      }
    )
