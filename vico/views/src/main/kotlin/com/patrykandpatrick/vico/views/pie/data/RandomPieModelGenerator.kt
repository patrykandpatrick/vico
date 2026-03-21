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

package com.patrykandpatrick.vico.views.pie.data

import kotlin.random.Random

internal object RandomPieModelGenerator {
  public val valueRange: IntRange = 1..8

  public val sliceRange: IntRange = 2..8

  public fun getRandomPartial(
    sliceRange: IntRange = RandomPieModelGenerator.sliceRange,
    valueRange: IntRange = RandomPieModelGenerator.valueRange,
  ): PieChartModel.Partial =
    PieChartModel.Builder()
      .apply {
        series(
          List(Random.nextInt(sliceRange.first, sliceRange.last + 1)) {
            Random.nextInt(valueRange.first, valueRange.last + 1)
          }
        )
      }
      .build()

  public fun getRandomModel(
    sliceRange: IntRange = RandomPieModelGenerator.sliceRange,
    valueRange: IntRange = RandomPieModelGenerator.valueRange,
  ): PieChartModel =
    getRandomPartial(sliceRange, valueRange)
      .complete(com.patrykandpatrick.vico.views.common.data.ExtraStore.Empty)
}
