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

package com.patrykandpatrick.vico.compose.cartesian.data

import kotlin.test.Test
import kotlin.test.assertEquals

class ColumnCartesianLayerModelTest {
  @Test
  fun `getAggregateYRange aggregates positive and negative values by x and group`() {
    val entries =
      listOf(
        ColumnCartesianLayerModel.Entry(x = 0.0, y = 7.0, seriesKey = "income"),
        ColumnCartesianLayerModel.Entry(x = 0.0, y = -2.0, seriesKey = "income"),
        ColumnCartesianLayerModel.Entry(x = 0.0, y = 3.0, seriesKey = "home"),
        ColumnCartesianLayerModel.Entry(x = 0.0, y = 2.0, seriesKey = "car"),
      )

    val range = entries.getAggregateYRange { seriesKey ->
      when (seriesKey) {
        "income" -> "income"
        "home",
        "car" -> "spending"
        else -> error("Unexpected series key: $seriesKey")
      }
    }

    assertEquals(-2.0, range.start)
    assertEquals(7.0, range.endInclusive)
  }
}
