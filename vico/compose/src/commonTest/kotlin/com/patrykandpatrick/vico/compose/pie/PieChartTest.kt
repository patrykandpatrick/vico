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

import com.patrykandpatrick.vico.compose.common.toRadians
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals

class PieChartTest {

  @Test
  fun `Donut spacing yields the same gap width at the inner and outer radii`() {
    val halfSpacing = 4f
    val outerRadius = 100f
    val innerRadius = 40f

    // A trimmed edge’s perpendicular offset from the radial line is radius * sin(trim).
    val outerOffset = outerRadius * sin(asinDegrees(halfSpacing / outerRadius).toRadians()).toFloat()
    val innerOffset = innerRadius * sin(asinDegrees(halfSpacing / innerRadius).toRadians()).toFloat()

    assertEquals(halfSpacing, outerOffset, absoluteTolerance = 0.001f)
    assertEquals(halfSpacing, innerOffset, absoluteTolerance = 0.001f)
  }

  @Test
  fun `asinDegrees clamps out-of-range input to a right angle`() {
    assertEquals(90f, asinDegrees(Float.POSITIVE_INFINITY), absoluteTolerance = 0.001f)
  }

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
