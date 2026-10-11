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
  fun roundsExcessDecimals() {
    assertEquals("1.23", 1.234.format())
    assertEquals("1.24", 1.239.format())
    assertEquals("0.3", 0.296.format())
    assertEquals("13", 12.5.format(decimalCount = 0))
  }

  @Test
  fun roundsHalfUpDespiteRepresentationErrors() {
    assertEquals("0.13", 0.125.format())
    assertEquals("1.01", 1.005.format())
    assertEquals("2.68", 2.675.format())
  }

  @Test
  fun carriesRoundingIntoIntegerPart() {
    assertEquals("1", 0.999.format())
    assertEquals("10", 9.999.format())
    assertEquals("1,000,000", 999999.999.format(thousandsSeparator = ","))
  }

  @Test
  fun roundsNegativeValuesAwayFromZero() {
    assertEquals("−1.24", (-1.239).format())
    assertEquals("−0.13", (-0.125).format())
    assertEquals("−0.01", (-0.006).format())
  }

  @Test
  fun keepsDecimalsThatAreNotExactlyRepresentable() {
    assertEquals("0.29", 0.29.format())
    assertEquals("1.15", 1.15.format())
    assertEquals("4.35", 4.35.format())
    assertEquals("1234567.15", 1234567.15.format())
  }

  @Test
  fun formatsLargeNumbersWithoutScientificNotation() {
    assertEquals("10000000", 1e7.format())
    assertEquals("25000000", 2.5e7.format())
    assertEquals("10000000000", 1e10.format())
    assertEquals("10000000000000000", 1e16.format())
    assertEquals("1000000000000000000", 1e18.format())
    assertEquals("123456789012346000000", 1.23456789012345678e20.format())
  }

  @Test
  fun keepsWholeNumbersWholeAtLargeMagnitudes() {
    assertEquals("20000000000000", 2e13.format())
    assertEquals("30000000000000", 3e13.format())
    assertEquals("2000000000", 2e9.format(decimalCount = 6))
    assertEquals("2000000000000000", 2e15.format(decimalCount = 0))
  }

  @Test
  fun roundsToFifteenSignificantDigits() {
    assertEquals("20000000000000.3", (2e13 + 0.25).format())
    assertEquals("123456789012346000", 1.2345678901234568e17.format())
    assertEquals("1234567890123460000", 1.2345678901234568e18.format())
    assertEquals("10", 9.999999999999998.format())
  }

  @Test
  fun formatsExtremeMagnitudes() {
    assertEquals("179769313486232" + "0".repeat(294), Double.MAX_VALUE.format())
    assertEquals("0", Double.MIN_VALUE.format())
  }

  @Test
  fun omitsNoiseDigitsAtHighDecimalCounts() {
    assertEquals("0.00001", 1e-5.format(decimalCount = 25))
    assertEquals("0.3", 0.3.format(decimalCount = 20))
    assertEquals("0", 0.0.format(decimalCount = 400))
  }

  @Test
  fun formatsSmallNumbersWithoutScientificNotation() {
    assertEquals("0.0001", 1e-4.format(decimalCount = 4))
    assertEquals("0", 1e-4.format())
  }

  @Test
  fun formatsNegativeWholeNumbers() {
    assertEquals("−20", (-20.0).format())
  }

  @Test
  fun omitsSignWhenNegativeValueRoundsToZero() {
    assertEquals("0", (-0.004).format())
    assertEquals("0", (-0.0).format())
  }

  @Test
  fun appliesSeparatorsToWholeNumbers() {
    assertEquals("1,200", 1200.0.format(thousandsSeparator = ","))
    assertEquals("$30%", 30.0.format(prefix = "$", suffix = "%"))
  }

  @Test
  fun appliesThousandsSeparatorsToIntegerPartOnly() {
    assertEquals("1.200,5", 1200.5.format(decimalSeparator = ",", thousandsSeparator = "."))
    assertEquals("1,234,567.1234", 1234567.1234.format(decimalCount = 4, thousandsSeparator = ","))
  }

  @Test
  fun appliesThousandsSeparatorsToNegativeNumbers() {
    assertEquals("−200", (-200.0).format(thousandsSeparator = ","))
    assertEquals("−1,234", (-1234.0).format(thousandsSeparator = ","))
    assertEquals("−123,456", (-123456.0).format(thousandsSeparator = ","))
  }

  @Test
  fun keepsMultiCharacterThousandsSeparatorsIntact() {
    assertEquals("1, 234, 567", 1234567.0.format(thousandsSeparator = ", "))
  }

  @Test
  fun placesSignAfterPrefix() {
    assertEquals("$−20", (-20.0).format(prefix = "$"))
  }

  @Test
  fun formatsNonFiniteValues() {
    assertEquals("NaN", Double.NaN.format())
    assertEquals("Infinity", Double.POSITIVE_INFINITY.format())
    assertEquals("−Infinity", Double.NEGATIVE_INFINITY.format())
  }
}
