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

package com.patrykandpatrick.vico.compose.cartesian.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CartesianValueFormatterTest {

  @Test
  fun decimalFormatterEqualityConsidersDecimalCount() {
    val formatter1 = CartesianValueFormatter.decimal(decimalCount = 2)
    val formatter2 = CartesianValueFormatter.decimal(decimalCount = 2)
    val formatter3 = CartesianValueFormatter.decimal(decimalCount = 3)

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }

  @Test
  fun decimalFormatterEqualityConsidersDecimalSeparator() {
    val formatter1 = CartesianValueFormatter.decimal(decimalSeparator = ".")
    val formatter2 = CartesianValueFormatter.decimal(decimalSeparator = ".")
    val formatter3 = CartesianValueFormatter.decimal(decimalSeparator = ",")

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }

  @Test
  fun decimalFormatterEqualityConsidersThousandsSeparator() {
    val formatter1 = CartesianValueFormatter.decimal(thousandsSeparator = "")
    val formatter2 = CartesianValueFormatter.decimal(thousandsSeparator = "")
    val formatter3 = CartesianValueFormatter.decimal(thousandsSeparator = ",")

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }

  @Test
  fun decimalFormatterEqualityConsidersPrefix() {
    val formatter1 = CartesianValueFormatter.decimal(prefix = "")
    val formatter2 = CartesianValueFormatter.decimal(prefix = "")
    val formatter3 = CartesianValueFormatter.decimal(prefix = "$")

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }

  @Test
  fun decimalFormatterEqualityConsidersSuffix() {
    val formatter1 = CartesianValueFormatter.decimal(suffix = "")
    val formatter2 = CartesianValueFormatter.decimal(suffix = "")
    val formatter3 = CartesianValueFormatter.decimal(suffix = "%")

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }

  @Test
  fun decimalFormatterHashCodeConsidersAllProperties() {
    val formatter1 = CartesianValueFormatter.decimal(decimalCount = 2, suffix = "%")
    val formatter2 = CartesianValueFormatter.decimal(decimalCount = 2, suffix = "%")
    val formatter3 = CartesianValueFormatter.decimal(decimalCount = 2, suffix = "kg")

    assertEquals(formatter1.hashCode(), formatter2.hashCode())
    assertNotEquals(formatter1.hashCode(), formatter3.hashCode())
  }

  @Test
  fun yPercentFormatterEqualityConsidersDecimalCount() {
    val formatter1 = CartesianValueFormatter.yPercent(decimalCount = 2)
    val formatter2 = CartesianValueFormatter.yPercent(decimalCount = 2)
    val formatter3 = CartesianValueFormatter.yPercent(decimalCount = 3)

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }
}
