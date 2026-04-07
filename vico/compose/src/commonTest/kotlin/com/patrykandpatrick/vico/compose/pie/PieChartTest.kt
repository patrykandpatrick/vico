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

import kotlin.test.Test
import kotlin.test.assertEquals

class PieChartTest {

  @Test
  fun `Inside label max width does not collapse for a full sweep`() {
    assertEquals(
      50,
      getInsideLabelMaxWidth(textRadius = 50f, ringThickness = 50f, sweepAngle = 360f),
    )
  }

  @Test
  fun `Inside label max width does not shrink below ring thickness past a half sweep`() {
    assertEquals(
      40,
      getInsideLabelMaxWidth(textRadius = 40f, ringThickness = 40f, sweepAngle = 270f),
    )
  }
}
