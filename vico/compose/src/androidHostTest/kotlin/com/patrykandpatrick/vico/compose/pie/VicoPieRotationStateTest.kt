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

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.saveable.SaverScope
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.Timeout

@Timeout(1)
class VicoPieRotationStateTest {
  @Test
  fun `When created, then the angle equals the initial rotation`() {
    assertEquals(45f, VicoPieRotationState(45f).angle)
  }

  @Test
  fun `When rotateBy accumulates past 360 degrees, then the angle is not normalized mid-gesture`() =
    runBlocking {
      val sut = VicoPieRotationState(0f)

      sut.rotateBy(400f)

      assertEquals(400f, sut.angle)
    }

  @Test
  fun `When settle is called, then the angle is normalized`() = runBlocking {
    val sut = VicoPieRotationState(0f)

    sut.rotateBy(725f)
    sut.settle()

    assertEquals(5f, sut.angle, absoluteTolerance = 0.001f)
  }

  @Test
  fun `When a fling completes, then the angle is normalized`() =
    runBlocking(AutoAdvancingFrameClock()) {
      val sut = VicoPieRotationState(350f)

      sut.fling(720f)

      assertTrue(sut.angle in 0f..<360f, "Expected a normalized angle but got ${sut.angle}.")
    }

  @Test
  fun `When stop is called during a fling, then the fling is superseded without settling`() =
    runBlocking(SuspendingFrameClock()) {
      val sut = VicoPieRotationState(350f)

      val fling = launch(start = CoroutineStart.UNDISPATCHED) { sut.fling(720f) }
      assertFalse(fling.isCompleted)

      sut.stop()
      fling.join()

      assertEquals(350f, sut.angle)
    }

  @Test
  fun `When the state is saved and restored, then the normalized angle is preserved`() {
    val saver = VicoPieRotationState.Saver

    val restored =
      saver.restore(with(saver) { SaverScope { true }.save(VicoPieRotationState(405f))!! })

    assertEquals(45f, restored?.angle)
  }

  private class AutoAdvancingFrameClock : MonotonicFrameClock {
    private var time = 0L

    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
      time += 16_000_000L
      return onFrame(time)
    }
  }

  private class SuspendingFrameClock : MonotonicFrameClock {
    override val key: CoroutineContext.Key<*>
      get() = MonotonicFrameClock.Key

    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R =
      suspendCancellableCoroutine {}
  }
}
