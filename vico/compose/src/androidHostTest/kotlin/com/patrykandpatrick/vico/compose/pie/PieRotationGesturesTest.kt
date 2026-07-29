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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PieRotationGesturesTest {
  private val geometry =
    PieGestureGeometry(center = Offset.Zero, outerRadius = 100f, holeRadius = 30f)

  @Test
  fun `When the drag at slop is tangential, then the gesture is captured`() {
    assertTrue(isTangentialDrag(dragVector = Offset(0f, 20f), radialOffset = Offset(100f, 0f)))
  }

  @Test
  fun `When the drag at slop is radial, then the gesture is handed off to the parent`() {
    assertFalse(isTangentialDrag(dragVector = Offset(20f, 0f), radialOffset = Offset(100f, 0f)))
  }

  @Test
  fun `When the tangential-radial split is a toss-up, then the parent wins`() {
    assertFalse(isTangentialDrag(dragVector = Offset(20f, 20f), radialOffset = Offset(100f, 0f)))
    assertFalse(isTangentialDrag(dragVector = Offset(20f, 24f), radialOffset = Offset(100f, 0f)))
  }

  @Test
  fun `When the tangential component clearly dominates, then the gesture is captured`() {
    assertTrue(isTangentialDrag(dragVector = Offset(20f, 30f), radialOffset = Offset(100f, 0f)))
  }

  @Test
  fun `When the pointer is in the ring, then it is inside the pie`() {
    assertTrue(isInsidePie(Offset(0f, 50f), geometry, deadZoneRadius = 16f))
  }

  @Test
  fun `When the pointer is in the hole or beyond the outer radius, then it is outside the pie`() {
    assertFalse(isInsidePie(Offset(0f, 10f), geometry, deadZoneRadius = 16f))
    assertFalse(isInsidePie(Offset(0f, 150f), geometry, deadZoneRadius = 16f))
  }

  @Test
  fun `When the pie is holeless, then the dead zone excludes the center`() {
    val holeless = geometry.copy(holeRadius = 0f)
    assertFalse(isInsidePie(Offset(0f, 10f), holeless, deadZoneRadius = 16f))
    assertTrue(isInsidePie(Offset(0f, 20f), holeless, deadZoneRadius = 16f))
  }

  @Test
  fun `When no geometry is available, then nothing is inside the pie`() {
    assertFalse(isInsidePie(Offset.Zero, PieGestureGeometry.Zero, deadZoneRadius = 16f))
  }

  @Test
  fun `When the pointer sweeps a clockwise quarter turn, then angleDelta is 90 degrees`() {
    assertEquals(90f, angleDelta(Offset(1f, 0f), Offset(0f, 1f)), absoluteTolerance = 0.001f)
  }

  @Test
  fun `When the pointer sweeps counterclockwise, then angleDelta is negative`() {
    assertEquals(-90f, angleDelta(Offset(0f, 1f), Offset(1f, 0f)), absoluteTolerance = 0.001f)
  }

  @Test
  fun `When the pointer crosses the 180-degree seam, then angleDelta stays small`() {
    val delta = angleDelta(Offset(-1f, -0.01f), Offset(-1f, 0.01f))
    assertTrue(delta in -5f..5f, "Expected a small delta but got $delta.")
  }

  @Test
  fun `When the release velocity is tangential, then the angular velocity matches the radius`() {
    val angularVelocity = angularFlingVelocity(Velocity(0f, 100f), radialOffset = Offset(100f, 0f))
    assertEquals(57.29578f, angularVelocity, absoluteTolerance = 0.001f)
  }

  @Test
  fun `When the release velocity is counterclockwise, then the angular velocity is negative`() {
    val angularVelocity = angularFlingVelocity(Velocity(0f, -100f), radialOffset = Offset(100f, 0f))
    assertEquals(-57.29578f, angularVelocity, absoluteTolerance = 0.001f)
  }

  @Test
  fun `When the release is near the center, then the angular velocity is capped`() {
    val angularVelocity =
      angularFlingVelocity(Velocity(0f, 1_000_000f), radialOffset = Offset(10f, 0f))
    assertEquals(PIE_MAX_ROTATION_VELOCITY, angularVelocity)
  }

  @Test
  fun `When the release is at the center, then the angular velocity is zero`() {
    assertEquals(0f, angularFlingVelocity(Velocity(0f, 1_000_000f), radialOffset = Offset.Zero))
  }

  @Test
  fun `When normalizeAngle is applied, then the result is in 0 until 360`() {
    assertEquals(5f, normalizeAngle(725f), absoluteTolerance = 0.001f)
    assertEquals(270f, normalizeAngle(-90f), absoluteTolerance = 0.001f)
    assertEquals(0f, normalizeAngle(360f), absoluteTolerance = 0.001f)
  }
}
