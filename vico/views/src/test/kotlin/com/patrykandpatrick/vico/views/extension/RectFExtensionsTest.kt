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

package com.patrykandpatrick.vico.views.extension

import android.graphics.RectF
import com.patrykandpatrick.vico.views.common.rotate
import com.patrykandpatrick.vico.views.common.set
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlin.math.sqrt
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RectFExtensionsTest {
  @MockK private lateinit var rect: RectF

  @BeforeTest
  fun setUp() {
    MockKAnnotations.init(this)
    every { rect.width() } answers { rect.right - rect.left }
    every { rect.height() } answers { rect.bottom - rect.top }
    every { rect.centerX() } answers { (rect.right + rect.left) / 2 }
    every { rect.centerY() } answers { (rect.bottom + rect.top) / 2 }
    every { rect.set(any(), any(), any(), any()) } answers
      {
        rect.left = args[0] as Float
        rect.top = args[1] as Float
        rect.right = args[2] as Float
        rect.bottom = args[3] as Float
      }
  }

  @Test
  fun `When square is rotated 45 degrees, width increases to diagonal`() {
    rect.set(0f, 0f, 10f, 10f)
    val squareDiagonalWidth = rect.width() * sqrt(2f)
    val originalCenterX = rect.centerX()
    val originalCenterY = rect.centerY()
    rect.rotate(45f)
    assertEquals(squareDiagonalWidth, rect.width())
    assertEquals(originalCenterX, rect.centerX())
    assertEquals(originalCenterY, rect.centerY())
  }

  @Test
  fun `When RectF is rotated 180 degrees, dimensions are preserved`() {
    val left = 0f
    val top = 0f
    val right = 10f
    val bottom = 10f
    rect.set(left, top, right, bottom)
    rect.rotate(180f)
    assertEquals(left, rect.left)
    assertEquals(top, rect.top)
    assertEquals(right, rect.right)
    assertEquals(bottom, rect.bottom)
  }

  @Test
  fun `When RectF is rotated 90 degrees, width and height are swapped`() {
    val width = 5f
    val height = 10f
    rect.set(0, 0, width, height)
    rect.rotate(90f)
    assertEquals(rect.width(), height)
    assertEquals(rect.height(), width)
  }
}
