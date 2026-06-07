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

package com.patrykandpatrick.vico.views.cartesian

import kotlin.test.Test
import kotlin.test.assertEquals

class ScrollHandlerTest {
  @Test
  fun `When xSnapStep is set, then snap delta accounts for layer start padding`() =
    assertEquals(
      3f,
      getSnapDelta(
        value = 27f,
        maxValue = 100f,
        xSnapStep = 2.0,
        xStep = 1.0,
        xSpacing = 10f,
        startPadding = 10f,
      ),
    )

  @Test
  fun `When xSnapStep is set and scroll is at start, then snap delta is null`() {
    assertEquals(
      null,
      getSnapDelta(
        value = 0f,
        maxValue = 100f,
        xSnapStep = 2.0,
        xStep = 1.0,
        xSpacing = 10f,
        startPadding = 10f,
      ),
    )
  }

  @Test
  fun `When xSnapStep is set and target is projected, then snap delta uses projected target`() {
    assertEquals(
      38f,
      getSnapDelta(
        value = 12f,
        maxValue = 100f,
        xSnapStep = 2.0,
        xStep = 1.0,
        xSpacing = 10f,
        startPadding = 10f,
        targetValue = 49f,
      ),
    )
  }
}
