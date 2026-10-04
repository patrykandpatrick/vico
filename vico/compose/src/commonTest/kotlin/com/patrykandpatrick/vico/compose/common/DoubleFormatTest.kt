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

import kotlin.test.Test
import kotlin.test.assertEquals

class DoubleFormatTest {

  @Test
  fun keepsTrailingZerosOfWholeNumbers() {
    assertEquals("10", 10.0.format())
    assertEquals("100", 100.0.format())
    assertEquals("0", 0.0.format())
  }

  @Test
  fun trimsTrailingZerosOfFractions() {
    assertEquals("1.5", 1.5.format())
    assertEquals("2.25", 2.25.format(decimalCount = 3))
  }

  @Test
  fun formatsNegativeWholeNumbers() {
    assertEquals("−20", (-20.0).format())
  }

  @Test
  fun appliesSeparatorsToWholeNumbers() {
    assertEquals("1,200", 1200.0.format(thousandsSeparator = ","))
    assertEquals("$30%", 30.0.format(prefix = "$", suffix = "%"))
  }
}
