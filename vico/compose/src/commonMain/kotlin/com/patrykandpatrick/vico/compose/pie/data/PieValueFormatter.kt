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

/** Formats pie-chart values for display. */
public fun interface PieValueFormatter {
  /** Formats the value at [index]. */
  public fun format(context: PieChartMeasuringContext, value: Float, index: Int): CharSequence

  public companion object {
    /** Returns the value as a string. */
    public val Value: PieValueFormatter = PieValueFormatter { _, value, _ ->
      value.toDouble().format()
    }
  }
}
