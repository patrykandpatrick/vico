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

package com.patrykandpatrick.vico.compose.common

import androidx.compose.foundation.gestures.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.math.abs

internal suspend fun PointerInputScope.detectZoomGestures(
  onGesture: (centroid: Offset, zoom: Float) -> Unit
) {
  awaitEachGesture {
    var zoom = 1f
    var pastTouchSlop = false
    val touchSlop = viewConfiguration.touchSlop
    awaitFirstDown(requireUnconsumed = false)
    do {
      val event = awaitPointerEvent()
      val canceled = event.changes.any { it.isConsumed }
      if (!canceled) {
        val zoomChange = event.calculateZoom()
        if (!pastTouchSlop) {
          zoom *= zoomChange
          val centroidSize = event.calculateCentroidSize(useCurrent = false)
          val zoomMotion = abs(1 - zoom) * centroidSize
          if (zoomMotion > touchSlop) pastTouchSlop = true
        }
        if (pastTouchSlop) {
          val centroid = event.calculateCentroid(useCurrent = false)
          if (zoomChange != 1f) onGesture(centroid, zoomChange)
          // Every change, not only the moved ones. While a pinch is in progress, `scrollable`’s
          // drag node parks in a gesture-pickup state, which re-enters touch-slop detection on the
          // first event whose changes are *all* unconsumed, seeding the detector with
          // `changes.first().position - initialDown.position`. `changes.first()` isn’t necessarily
          // the pointer `initialDown` belongs to, so when one finger lifts, that seed becomes the
          // lifted finger’s entire travel: the remaining finger clears the slop threshold at once
          // and a drag starts, jerking the content sideways just as the pinch ends. Consuming
          // unconditionally keeps the pickup from ever seeing such an event.
          event.changes.forEach { it.consume() }
        }
      }
    } while (!canceled && event.changes.any { it.pressed })
  }
}
