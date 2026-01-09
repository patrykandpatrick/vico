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

class RectExtensionsTest {
  @Test
  fun `When square is rotated 45 degrees, width increases to diagonal`() {
    val rect = Rect(0f, 0f, 10f, 10f)
    val squareDiagonalWidth = rect.width * sqrt(2f)
    val originalCenterX = rect.center.x
    val originalCenterY = rect.center.y
    val rotatedRect = rect.rotate(45f)
    assertEquals(squareDiagonalWidth, rotatedRect.width)
    assertEquals(originalCenterX, rotatedRect.center.x)
    assertEquals(originalCenterY, rotatedRect.center.y)
  }

  @Test
  fun `When Rect is rotated 180 degrees, dimensions are preserved`() {
    val left = 0f
    val top = 0f
    val right = 10f
    val bottom = 10f
    val rect = Rect(left, top, right, bottom)
    val rotatedRect = rect.rotate(180f)
    assertEquals(left, rotatedRect.left)
    assertEquals(top, rotatedRect.top)
    assertEquals(right, rotatedRect.right)
    assertEquals(bottom, rotatedRect.bottom)
  }

  @Test
  fun `When Rect is rotated 90 degrees, width and height are swapped`() {
    val width = 5f
    val height = 10f
    val rect = Rect(0f, 0f, width, height)
    val rotatedRect = rect.rotate(90f)
    assertEquals(height, rotatedRect.width)
    assertEquals(width, rotatedRect.height)
  }
}
