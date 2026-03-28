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

package com.patrykandpatrick.vico.compose.cartesian.marker

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DefaultCartesianMarkerValueFormatterTest {

  @Test
  fun valueFormatterEqualityConsidersDecimalCount() {
    val formatter1 = DefaultCartesianMarker.ValueFormatter.default(decimalCount = 2)
    val formatter2 = DefaultCartesianMarker.ValueFormatter.default(decimalCount = 2)
    val formatter3 = DefaultCartesianMarker.ValueFormatter.default(decimalCount = 3)

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }

  @Test
  fun valueFormatterEqualityConsidersDecimalSeparator() {
    val formatter1 = DefaultCartesianMarker.ValueFormatter.default(decimalSeparator = ".")
    val formatter2 = DefaultCartesianMarker.ValueFormatter.default(decimalSeparator = ".")
    val formatter3 = DefaultCartesianMarker.ValueFormatter.default(decimalSeparator = ",")

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }

  @Test
  fun valueFormatterEqualityConsidersThousandsSeparator() {
    val formatter1 = DefaultCartesianMarker.ValueFormatter.default(thousandsSeparator = "")
    val formatter2 = DefaultCartesianMarker.ValueFormatter.default(thousandsSeparator = "")
    val formatter3 = DefaultCartesianMarker.ValueFormatter.default(thousandsSeparator = ",")

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }

  @Test
  fun valueFormatterEqualityConsidersPrefix() {
    val formatter1 = DefaultCartesianMarker.ValueFormatter.default(prefix = "")
    val formatter2 = DefaultCartesianMarker.ValueFormatter.default(prefix = "")
    val formatter3 = DefaultCartesianMarker.ValueFormatter.default(prefix = "$")

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }

  @Test
  fun valueFormatterEqualityConsidersSuffix() {
    val formatter1 = DefaultCartesianMarker.ValueFormatter.default(suffix = "")
    val formatter2 = DefaultCartesianMarker.ValueFormatter.default(suffix = "")
    val formatter3 = DefaultCartesianMarker.ValueFormatter.default(suffix = "%")

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }

  @Test
  fun valueFormatterEqualityConsidersColorCode() {
    val formatter1 = DefaultCartesianMarker.ValueFormatter.default(colorCode = true)
    val formatter2 = DefaultCartesianMarker.ValueFormatter.default(colorCode = true)
    val formatter3 = DefaultCartesianMarker.ValueFormatter.default(colorCode = false)

    assertEquals(formatter1, formatter2)
    assertNotEquals(formatter1, formatter3)
  }

  @Test
  fun valueFormatterHashCodeConsidersAllProperties() {
    val formatter1 = DefaultCartesianMarker.ValueFormatter.default(decimalCount = 2, suffix = "%")
    val formatter2 = DefaultCartesianMarker.ValueFormatter.default(decimalCount = 2, suffix = "%")
    val formatter3 = DefaultCartesianMarker.ValueFormatter.default(decimalCount = 2, suffix = "kg")

    assertEquals(formatter1.hashCode(), formatter2.hashCode())
    assertNotEquals(formatter1.hashCode(), formatter3.hashCode())
  }
}
