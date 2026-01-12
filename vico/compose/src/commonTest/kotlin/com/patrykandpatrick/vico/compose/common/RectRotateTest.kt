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

import androidx.compose.ui.geometry.Rect
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class RectRotateTest {

  @Test
  fun `When square is rotated 45 degrees, width increases to diagonal`() {
    val rect = Rect(0f, 0f, 10f, 10f)
    val squareDiagonalWidth = rect.width * sqrt(2f)

    val rotated = rect.rotate(45f)

    assertEquals(squareDiagonalWidth, rotated.width, 1e-3f)
    assertEquals(rect.center.x, rotated.center.x, 1e-3f)
    assertEquals(rect.center.y, rotated.center.y, 1e-3f)
  }

  @Test
  fun `When Rect is rotated 180 degrees, dimensions are preserved`() {
    val rect = Rect(0f, 0f, 10f, 10f)

    val rotated = rect.rotate(180f)

    assertEquals(rect.left, rotated.left)
    assertEquals(rect.top, rotated.top)
    assertEquals(rect.right, rotated.right)
    assertEquals(rect.bottom, rotated.bottom)
  }

  @Test
  fun `When Rect is rotated 90 degrees, width and height are swapped`() {
    val width = 5f
    val height = 10f
    val rect = Rect(0f, 0f, width, height)

    val rotated = rect.rotate(90f)

    assertEquals(height, rotated.width, 1e-3f)
    assertEquals(width, rotated.height, 1e-3f)
  }
}
