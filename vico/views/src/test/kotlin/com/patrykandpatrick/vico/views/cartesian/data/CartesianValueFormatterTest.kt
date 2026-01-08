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

package com.patrykandpatrick.vico.views.cartesian.data

import com.patrykandpatrick.vico.views.cartesian.CartesianMeasuringContext
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CartesianValueFormatterTest {
  @MockK private lateinit var context: CartesianMeasuringContext

  @BeforeEach
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `formatForAxis throws exception when formatter returns empty string`() {
    val formatter = CartesianValueFormatter { _, _, _ -> "" }

    val exception =
      Assertions.assertThrows(IllegalStateException::class.java) {
        formatter.formatForAxis(context, 0.0, null)
      }

    Assertions.assertTrue(exception.message!!.contains("returned a blank string"))
  }

  @Test
  fun `formatForAxis throws exception when formatter returns blank string with spaces`() {
    val formatter = CartesianValueFormatter { _, _, _ -> "   " }

    val exception =
      Assertions.assertThrows(IllegalStateException::class.java) {
        formatter.formatForAxis(context, 0.0, null)
      }

    Assertions.assertTrue(exception.message!!.contains("returned a blank string"))
  }

  @Test
  fun `formatForAxis throws exception when formatter returns blank string with tabs`() {
    val formatter = CartesianValueFormatter { _, _, _ -> "\t\t" }

    val exception =
      Assertions.assertThrows(IllegalStateException::class.java) {
        formatter.formatForAxis(context, 0.0, null)
      }

    Assertions.assertTrue(exception.message!!.contains("returned a blank string"))
  }

  @Test
  fun `formatForAxis throws exception when formatter returns blank string with newlines`() {
    val formatter = CartesianValueFormatter { _, _, _ -> "\n\n" }

    val exception =
      Assertions.assertThrows(IllegalStateException::class.java) {
        formatter.formatForAxis(context, 0.0, null)
      }

    Assertions.assertTrue(exception.message!!.contains("returned a blank string"))
  }

  @Test
  fun `formatForAxis succeeds when formatter returns valid string`() {
    val formatter = CartesianValueFormatter { _, _, _ -> "123" }

    val result = formatter.formatForAxis(context, 0.0, null)

    Assertions.assertEquals("123", result)
  }

  @Test
  fun `formatForAxis succeeds when formatter returns string with leading or trailing spaces`() {
    val formatter = CartesianValueFormatter { _, _, _ -> "  123  " }

    val result = formatter.formatForAxis(context, 0.0, null)

    Assertions.assertEquals("  123  ", result)
  }
}
