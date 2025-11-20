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

package com.patrykandpatrick.vico.multiplatform.cartesian

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.Interaction
import com.patrykandpatrick.vico.multiplatform.common.Point
import com.patrykandpatrick.vico.multiplatform.common.detectZoomGestures

private const val BASE_SCROLL_ZOOM_DELTA = 0.1f

private fun Offset.toPoint() = Point(x, y)

@Composable internal expect fun Modifier.extraPointerInput(scrollState: VicoScrollState): Modifier

@Composable
internal fun Modifier.pointerInput(
  scrollState: VicoScrollState,
  onInteraction: ((Interaction) -> Unit)?,
  onZoom: ((Float, Offset) -> Unit)?,
  consumeMoveEvents: Boolean,
) =
  scrollable(
      state = scrollState.scrollableState,
      orientation = Orientation.Horizontal,
      enabled = scrollState.scrollEnabled,
      reverseDirection = true,
    )
    .pointerInput(onZoom, onInteraction) {
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
            onInteraction == null -> continue
            event.type == PointerEventType.Press ->
              onInteraction(Interaction.Press(pointerPosition))
            event.type == PointerEventType.Release ->
              onInteraction(Interaction.Release(pointerPosition))
            event.type == PointerEventType.Move && !scrollState.scrollEnabled -> {
              val changes = event.changes.first()
              if (consumeMoveEvents) changes.consume()
              onInteraction(Interaction.Move(pointerPosition))
            }
            event.type == PointerEventType.Enter -> {
              onInteraction(Interaction.Hover(pointerPosition))
            }
            event.type == PointerEventType.Move && scrollState.scrollEnabled -> {
              onInteraction(Interaction.Hover(pointerPosition))
            }
            event.type == PointerEventType.Exit -> {
              onInteraction(Interaction.Exit(pointerPosition))
            }
          }
        }
      }
    }
    .then(
      if (onInteraction != null) {
        Modifier.pointerInput(onInteraction) {
          detectTapGestures(
            onLongPress = { onInteraction(Interaction.LongPress(it.toPoint())) },
            onTap = { onInteraction(Interaction.Tap(it.toPoint())) },
          )
        }
      } else {
        Modifier
      }
    )
    .then(
      if (scrollState.scrollEnabled && onZoom != null) {
        Modifier.pointerInput(onInteraction, onZoom) {
          detectZoomGestures { centroid, zoom ->
            onInteraction?.invoke(Interaction.Zoom(centroid.toPoint()))
            onZoom(zoom, centroid)
          }
        }
      } else {
        Modifier
      }
    )
    .extraPointerInput(scrollState)
