/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.model

import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.getXDeltaGcd
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

public class XDeltaGcdTest {
  private fun getEntries(vararg x: Number) =
    x.map { value ->
      mockk<CartesianLayerModel.Entry> { every { this@mockk.x } returns value.toDouble() }
    }

  @Test
  public fun `Ensure 1 is returned for empty collection`() {
    assertEquals(1.0, getEntries().getXDeltaGcd())
  }

  @Test
  public fun `Ensure 1 is returned for single number`() {
    assertEquals(1.0, getEntries(4).getXDeltaGcd())
  }

  @Test
  public fun `Ensure 2 is returned for multiples of 2`() {
    assertEquals(2.0, getEntries(0, 2, 4, 6).getXDeltaGcd())
  }

  @Test
  public fun `Ensure 1 is returned for primes`() {
    assertEquals(1.0, getEntries(2, 3, 5, 7).getXDeltaGcd())
  }

  @Test
  public fun `Ensure one half is returned for multiples of one half`() {
    assertEquals(0.5, getEntries(0, 0.5, 1, 1.5).getXDeltaGcd())
  }

  @Test
  public fun `Ensure 3 is returned for shuffled multiples of 3`() {
    assertEquals(3.0, getEntries(12, 3, 6, 21).getXDeltaGcd())
  }
}
