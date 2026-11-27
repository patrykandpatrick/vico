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

package com.patrykandpatrick.vico.compose.pie.data

import com.patrykandpatrick.vico.compose.common.format
import com.patrykandpatrick.vico.compose.pie.PieChartMeasuringContext

/**
 * Formats pie-chart values for display.
 *
 * Implementations must be pure functions of their arguments and of the values they capture; to
 * change the formatting, pass a new [PieValueFormatter]. It’s called on the main thread, while
 * measuring and drawing. In a composable function, a lambda converted to [PieValueFormatter] is
 * memoized while the values it captures are unchanged (compared via `equals` for stable types and
 * by identity otherwise), so it needs no explicit `remember`.
 */
public fun interface PieValueFormatter {
  /** Formats the value at [index]. */
  public fun format(context: PieChartMeasuringContext, value: Float, index: Int): CharSequence

  public companion object {
    /**
     * Formats values to include up to two decimal digits, rounding half away from zero. Trailing
     * zeros are skipped.
     */
    public val Value: PieValueFormatter = PieValueFormatter { _, value, _ ->
      value.toDouble().format()
    }
  }
}
