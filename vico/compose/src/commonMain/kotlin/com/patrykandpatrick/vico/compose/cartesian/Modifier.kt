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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.patrykandpatrick.vico.compose.cartesian.marker.Interaction
import com.patrykandpatrick.vico.compose.common.Point
import com.patrykandpatrick.vico.compose.common.detectZoomGestures

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
): Modifier {
  val defaultFlingBehavior = ScrollableDefaults.flingBehavior()
  val flingBehavior =
    remember(scrollState, defaultFlingBehavior) {
      if (scrollState.snapScrollX != null) {
        SnapFlingBehavior(scrollState, defaultFlingBehavior)
      } else {
        defaultFlingBehavior
      }
    }
  return scrollable(
      state = scrollState.scrollableState,
      orientation = Orientation.Horizontal,
      enabled = scrollState.scrollEnabled,
      reverseDirection = true,
      flingBehavior = flingBehavior,
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
    .extraPointerInput(scrollState)
}

private fun Offset.fits(size: IntSize) = x >= 0f && x <= size.width && y >= 0f && y <= size.height

internal class SnapFlingBehavior(
  private val scrollState: VicoScrollState,
  private val delegate: FlingBehavior,
  private val snapAnimationSpec: AnimationSpec<Float> = spring(),
) : FlingBehavior {
  override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
    val remaining = with(delegate) { performFling(initialVelocity) }
    val snapDelta = scrollState.getSnapDelta() ?: return remaining
    var previousValue = 0f
    animate(0f, snapDelta, animationSpec = snapAnimationSpec) { value, _ ->
      scrollBy(value - previousValue)
      previousValue = value
    }
    return 0f
  }
}
