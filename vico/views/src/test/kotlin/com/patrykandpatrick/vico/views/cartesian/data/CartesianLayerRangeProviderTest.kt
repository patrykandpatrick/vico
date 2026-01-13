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

package com.patrykandpatrick.vico.views.cartesian.data

import com.patrykandpatrick.vico.views.common.data.MutableExtraStore
import kotlin.test.Test
import kotlin.test.assertEquals

class CartesianLayerRangeProviderTest {

  private val extraStore = MutableExtraStore()

  @Test
  fun `intrinsic range provider keeps positive min and max y`() {
    val minY = 5.0
    val maxY = 10.0

    val provider = CartesianLayerRangeProvider.Intrinsic

    assertEquals(minY, provider.getMinY(minY, maxY, extraStore))
    assertEquals(maxY, provider.getMaxY(minY, maxY, extraStore))
  }
}
