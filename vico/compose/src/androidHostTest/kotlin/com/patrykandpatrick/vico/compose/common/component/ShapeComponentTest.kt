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

package com.patrykandpatrick.vico.compose.common.component

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class ShapeComponentTest {
  private val density = Density(2f)

  @Test
  fun `When the shape is a rectangle, then the fast-path corner radius is zero`() {
    assertEquals(0f, RectangleShape.uniformCornerRadiusOrNegative(100f, 50f, density))
  }

  @Test
  fun `When the shape has four equal dp corners, then the fast path is taken with that radius`() {
    // 4 dp at density 2 = 8 px.
    assertEquals(8f, RoundedCornerShape(4.dp).uniformCornerRadiusOrNegative(100f, 50f, density))
  }

  @Test
  fun `When the shape has four equal percent corners, then the radius is resolved against the size`() {
    // 25% of the smaller dimension (50) = 12.5 px.
    assertEquals(
      12.5f,
      RoundedCornerShape(percent = 25).uniformCornerRadiusOrNegative(100f, 50f, density),
    )
  }

  @Test
  fun `When the shape has unequal corners, then the fast path is skipped`() {
    assertEquals(
      -1f,
      RoundedCornerShape(topStart = 8.dp, topEnd = 4.dp)
        .uniformCornerRadiusOrNegative(100f, 50f, density),
    )
  }

  @Test
  fun `When the shape is not a rectangle or a RoundedCornerShape, then the fast path is skipped`() {
    assertEquals(-1f, CutCornerShape(4.dp).uniformCornerRadiusOrNegative(100f, 50f, density))
  }
}
