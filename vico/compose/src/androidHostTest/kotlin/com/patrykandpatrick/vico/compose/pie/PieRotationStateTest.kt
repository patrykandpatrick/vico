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

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Timeout

@Timeout(1)
class PieRotationStateTest {
  @Test
  fun `When rotate is called, then the angle reaches the target`() = runBlocking {
    val sut = PieRotationState(0f)

    sut.rotate(90f)

    assertEquals(90f, sut.angle)
  }

  @Test
  fun `When the pointer sweeps a quarter turn, then angleDelta is 90 degrees`() {
    val delta = angleDelta(Offset.Zero, Offset(1f, 0f), Offset(0f, 1f))

    assertEquals(90f, delta, absoluteTolerance = 0.001f)
  }

  @Test
  fun `When the pointer crosses the 180-degree seam, then angleDelta stays small`() {
    val delta = angleDelta(Offset.Zero, Offset(-1f, -0.01f), Offset(-1f, 0.01f))

    assertTrue(delta in -5f..5f, "Expected a small delta but got $delta")
  }

  @Test
  fun `When state is saved and restored, then the angle is preserved`() {
    val sut = PieRotationState(45f)
    val saver = PieRotationState.Saver

    val restored = saver.restore(with(saver) { SaverScope { true }.save(sut)!! })

    assertEquals(45f, restored?.angle)
  }
}
