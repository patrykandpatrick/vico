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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.patrykandpatrick.vico.compose.common.detectZoomGestures
import com.patrykandpatrick.vico.core.cartesian.marker.Interaction
import com.patrykandpatrick.vico.core.common.Point

private const val BASE_SCROLL_ZOOM_DELTA = 0.1f

private fun Offset.toPoint() = Point(x, y)

internal fun Modifier.pointerInput(
  scrollState: VicoScrollState,
  onInteraction: ((Interaction) -> Unit)?,
  onZoom: ((Float, Offset) -> Unit)?,
  consumeMoveEvents: Boolean,
  longPressEnabled: Boolean,
) =
  scrollable(
      state = scrollState.scrollableState,
      orientation = Orientation.Horizontal,
      enabled = scrollState.scrollEnabled,
      reverseDirection = true,
    )
    .pointerInput(onZoom, onInteraction) {
      awaitPointerEventScope {
        var isHoverActive = false
        while (true) {
          val event = awaitPointerEvent()
          val position = event.changes.first().position
          val pointerPosition = position.toPoint()
          when {
            event.type == PointerEventType.Scroll && scrollState.scrollEnabled && onZoom != null ->
              onZoom(
                1 - event.changes.first().scrollDelta.y * BASE_SCROLL_ZOOM_DELTA,
                event.changes.first().position,
              )
            onInteraction == null -> continue
            event.type == PointerEventType.Press && event.changes.size == 1 ->
              onInteraction(Interaction.Press(pointerPosition))
            event.type == PointerEventType.Release || event.type == PointerEventType.Press ->
              onInteraction(Interaction.Release(pointerPosition))
            event.type == PointerEventType.Move && !scrollState.scrollEnabled -> {
              val changes = event.changes.first()
              if (consumeMoveEvents) changes.consume()
              onInteraction(Interaction.Move(pointerPosition))
            }
            event.type == PointerEventType.Enter -> {
              isHoverActive = true
              onInteraction(Interaction.Enter(pointerPosition))
            }
            event.type == PointerEventType.Move && scrollState.scrollEnabled && isHoverActive ->
              onInteraction(Interaction.Move(pointerPosition))
            event.type == PointerEventType.Exit -> {
              val isInsideChartBounds = position.fits(size)
              isHoverActive = isInsideChartBounds
              onInteraction(Interaction.Exit(pointerPosition, isInsideChartBounds))
            }
          }
        }
      }
    }
    .then(
      if (onInteraction != null) {
        Modifier.pointerInput(onInteraction, longPressEnabled) {
          detectTapGestures(
            onLongPress =
              if (longPressEnabled) {
                { onInteraction(Interaction.LongPress(it.toPoint())) }
              } else {
                null
              },
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

private fun Offset.fits(size: IntSize) = x >= 0f && x <= size.width && y >= 0f && y <= size.height
