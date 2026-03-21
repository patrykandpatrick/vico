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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.MeasuringContext
import com.patrykandpatrick.vico.compose.common.half

/** Defines the size of a pie-chart component. */
public interface PieSize {
  /** Returns the radius for the available space. */
  public fun getRadius(
    context: MeasuringContext,
    availableWidth: Float,
    availableHeight: Float,
  ): Float

  /** Defines the size of the pie’s outer portion. */
  public interface Outer : PieSize {
    public companion object {
      /** Uses all available space. */
      public val Fill: Outer = FillImpl

      /** Uses a fixed maximum diameter in dp. */
      public fun fixed(maxDiameter: Dp): Outer = Fixed(maxDiameter)
    }
  }

  /** Defines the size of the inner donut hole. */
  public interface Inner : PieSize {
    public companion object {
      /** Produces no donut hole. */
      public val Zero: Inner = Fixed(0.dp)

      /** Uses a fixed maximum diameter in dp. */
      public fun fixed(maxDiameter: Dp): Inner = Fixed(maxDiameter)
    }
  }

  @Immutable
  private data object FillImpl : Outer {
    override fun getRadius(
      context: MeasuringContext,
      availableWidth: Float,
      availableHeight: Float,
    ): Float = minOf(availableWidth, availableHeight).half
  }

  @Immutable
  private data class Fixed(private val maxDiameter: Dp) : Outer, Inner {
    init {
      require(maxDiameter >= 0.dp) { "The max diameter must be nonnegative." }
    }

    override fun getRadius(
      context: MeasuringContext,
      availableWidth: Float,
      availableHeight: Float,
    ): Float = with(context) { minOf(availableWidth, availableHeight, maxDiameter.pixels).half }
  }
}
