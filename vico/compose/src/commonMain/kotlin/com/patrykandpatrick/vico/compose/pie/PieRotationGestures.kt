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

package com.patrykandpatrick.vico.compose.pie

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal const val PIE_ROTATION_PARENT_BIAS = 1.25f
internal const val PIE_MAX_ROTATION_VELOCITY = 1080f
internal val PIE_ROTATION_DEAD_ZONE_RADIUS = 16.dp

private const val RADIANS_TO_DEGREES = (180.0 / PI).toFloat()

internal data class PieGestureGeometry(
  val center: Offset,
  val outerRadius: Float,
  val holeRadius: Float,
) {
  internal companion object {
    val Zero: PieGestureGeometry = PieGestureGeometry(Offset.Zero, 0f, 0f)
  }
}

private fun cross(a: Offset, b: Offset): Float = a.x * b.y - a.y * b.x

private fun dot(a: Offset, b: Offset): Float = a.x * b.x + a.y * b.y

/** Returns the signed angle (in degrees, in (−180, 180]) from [previous] to [current]. */
internal fun angleDelta(previous: Offset, current: Offset): Float =
  atan2(cross(previous, current), dot(previous, current)) * RADIANS_TO_DEGREES

/**
 * Returns whether a drag of [dragVector] starting at [radialOffset] from the pie’s center is
 * predominantly tangential. Near ties are radial so that ambiguous gestures go to the parent.
 */
internal fun isTangentialDrag(dragVector: Offset, radialOffset: Offset): Boolean =
  abs(cross(dragVector, radialOffset)) >
    PIE_ROTATION_PARENT_BIAS * abs(dot(dragVector, radialOffset))

/**
 * Returns whether [position] is inside the pie’s interactive ring—outside of the hole and the
 * central dead zone, and inside of the outer radius.
 */
internal fun isInsidePie(
  position: Offset,
  geometry: PieGestureGeometry,
  deadZoneRadius: Float,
): Boolean {
  val distance = (position - geometry.center).getDistance()
  return distance >= max(geometry.holeRadius, deadZoneRadius) && distance <= geometry.outerRadius
}

/**
 * Converts [velocity] at [radialOffset] from the pie’s center to an angular velocity (in degrees
 * per second), capped at ±[PIE_MAX_ROTATION_VELOCITY].
 */
internal fun angularFlingVelocity(velocity: Velocity, radialOffset: Offset): Float {
  val radiusSquared = radialOffset.getDistanceSquared()
  if (radiusSquared < 1f) return 0f
  val degreesPerSecond =
    cross(radialOffset, Offset(velocity.x, velocity.y)) / radiusSquared * RADIANS_TO_DEGREES
  return degreesPerSecond.coerceIn(-PIE_MAX_ROTATION_VELOCITY, PIE_MAX_ROTATION_VELOCITY)
}

/** Normalizes [angle] to [0, 360). */
internal fun normalizeAngle(angle: Float): Float = (angle % 360f + 360f) % 360f

/**
 * Detects drag-to-rotate gestures for a [PieChart]. The tangential-or-radial decision is made once,
 * at touch slop: tangential drags are captured and consumed, and radial and ambiguous drags are
 * left entirely to the parent.
 */
internal suspend fun PointerInputScope.detectPieRotationGestures(
  state: VicoPieRotationState,
  geometry: () -> PieGestureGeometry,
): Unit = coroutineScope {
  val deadZoneRadius = PIE_ROTATION_DEAD_ZONE_RADIUS.toPx()
  awaitEachGesture {
    val down = awaitFirstDown(requireUnconsumed = false)
    if (!state.rotationEnabled) return@awaitEachGesture
    val geom = geometry()
    if (!isInsidePie(down.position, geom, deadZoneRadius)) return@awaitEachGesture
    launch { state.stop() }

    var decided = false
    var captured = false
    val slopChange =
      awaitTouchSlopOrCancellation(down.id) { change, _ ->
        if (!decided) {
          decided = true
          captured = isTangentialDrag(change.position - down.position, down.position - geom.center)
        }
        if (captured) change.consume()
      }

    if (slopChange == null || !captured) {
      launch { state.settle() }
      return@awaitEachGesture
    }

    val velocityTracker = VelocityTracker()
    velocityTracker.addPointerInputChange(slopChange)
    var previousPosition = slopChange.position
    launch {
      state.rotateBy(angleDelta(down.position - geom.center, previousPosition - geom.center))
    }
    val completed =
      drag(slopChange.id) { change ->
        velocityTracker.addPointerInputChange(change)
        val delta = angleDelta(previousPosition - geom.center, change.position - geom.center)
        previousPosition = change.position
        launch { state.rotateBy(delta) }
        change.consume()
      }
    if (completed) {
      val velocity =
        angularFlingVelocity(velocityTracker.calculateVelocity(), previousPosition - geom.center)
      launch { state.fling(velocity) }
    } else {
      launch { state.settle() }
    }
  }
}
