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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.patrykandpatrick.vico.compose.cartesian.marker.Interaction
import com.patrykandpatrick.vico.compose.common.Point
import com.patrykandpatrick.vico.compose.common.detectZoomGestures
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

private const val BASE_SCROLL_ZOOM_DELTA = 0.1f

private fun Offset.toPoint() = Point(x, y)

@Composable internal expect fun Modifier.extraPointerInput(scrollState: VicoScrollState): Modifier

@Composable
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
            event.type == PointerEventType.Move -> {
              if (consumeMoveEvents && !scrollState.scrollEnabled) event.changes.first().consume()
              onInteraction(Interaction.Move(pointerPosition))
            }
            event.type == PointerEventType.Enter ->
              onInteraction(Interaction.Enter(pointerPosition))
            event.type == PointerEventType.Exit -> {
              val isInsideChartBounds = position.fits(size)
              onInteraction(Interaction.Exit(pointerPosition, isInsideChartBounds))
            }
          }
        }
      }
    }
    .then(
      if (onInteraction != null) {
        Modifier.pointerInput(onInteraction, longPressEnabled) {
          detectTapGesturesWithoutConsume(
            onTap = { onInteraction(Interaction.Tap(it.toPoint())) },
            onLongPress =
              if (longPressEnabled) {
                { onInteraction(Interaction.LongPress(it.toPoint())) }
              } else {
                null
              },
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

private suspend fun PointerInputScope.detectTapGesturesWithoutConsume(
  onTap: (Offset) -> Unit,
  onLongPress: ((Offset) -> Unit)?,
) {
  awaitEachGesture {
    val down = awaitFirstDown()
    if (onLongPress != null) {
      val longPress = awaitLongPressOrCancellation(down.id)
      if (longPress != null) {
        onLongPress(longPress.position)
        return@awaitEachGesture
      }
    } else {
      waitForUpOrCancellation()
    }
    val inputChange = currentEvent.changes.firstOrNull()
    if (inputChange.isTap(down)) {
      onTap(inputChange.position)
    }
  }
}

@OptIn(ExperimentalContracts::class)
context(pointerEventScope: AwaitPointerEventScope)
private fun PointerInputChange?.isTap(firstDown: PointerInputChange): Boolean {
  contract { returns(true).implies(this@isTap != null) }
  this ?: return false
  val longPressTimeoutMillis = pointerEventScope.viewConfiguration.longPressTimeoutMillis
  val touchSlop = pointerEventScope.viewConfiguration.touchSlop
  val isNotLongPress = previousUptimeMillis - uptimeMillis < longPressTimeoutMillis
  val isNotMove = (firstDown.position - position).getDistance() < touchSlop
  return !pressed && previousPressed && isNotLongPress && isNotMove
}

private fun Offset.fits(size: IntSize) = x >= 0f && x <= size.width && y >= 0f && y <= size.height
