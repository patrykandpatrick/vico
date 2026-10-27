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

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toPixelMap
import kotlin.test.Test
import kotlin.test.assertEquals

class PieChartSlicePathTest {

  @Test
  fun `A full-circle donut keeps its hole when copied to the drawing path`() {
    val slice = PieChart.Slice()

    slice.buildPath(Rect(0f, 0f, 250f, 250f), 110f, -90f, 360f, destination = slice.path)

    assertPixels(slice, center = Color.Transparent, ring = Color.Red)
  }

  @Test
  fun `A full-circle pie stays filled without a hole`() {
    val slice = PieChart.Slice()

    slice.buildPath(Rect(0f, 0f, 250f, 250f), 0f, -90f, 360f, destination = slice.path)

    assertPixels(slice, center = Color.Red, ring = Color.Red)
  }

  @Test
  fun `A reused drawing path keeps the hole as a donut grows to a full circle`() {
    val slice = PieChart.Slice()

    for (sweep in listOf(180f, 360f, 180f, 360f)) {
      slice.buildPath(Rect(0f, 0f, 250f, 250f), 110f, -90f, sweep, destination = slice.path)
      assertPixels(slice, center = Color.Transparent, ring = Color.Red)
    }
  }

  private fun assertPixels(slice: PieChart.Slice, center: Color, ring: Color) {
    val bitmap = ImageBitmap(250, 250)
    Canvas(bitmap).drawPath(slice.path, Paint().apply { color = Color.Red })
    val pixels = bitmap.toPixelMap()
    assertEquals(center, pixels[125, 125])
    assertEquals(ring, pixels[242, 125])
    assertEquals(Color.Transparent, pixels[0, 0])
  }
}
