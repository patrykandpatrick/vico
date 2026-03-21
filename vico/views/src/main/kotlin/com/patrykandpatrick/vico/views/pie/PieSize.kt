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

package com.patrykandpatrick.vico.views.pie

import com.patrykandpatrick.vico.views.common.MeasuringContext
import com.patrykandpatrick.vico.views.common.half

/** Defines the size of a pie-chart component. */
public interface PieSize {
  /** Returns the radius for the available width and height. */
  public fun getRadius(
    context: MeasuringContext,
    availableWidth: Float,
    availableHeight: Float,
  ): Float

  /** Defines the size of a slice’s outer boundary. */
  public interface Outer : PieSize {
    public companion object {
      /** Fills the available space. */
      public val Fill: Outer = FillImpl

      /** Creates a fixed outer size. */
      public fun fixed(maxDiameterDp: Float): Outer = Fixed(maxDiameterDp)
    }
  }

  /** Defines the size of the donut hole. */
  public interface Inner : PieSize {
    public companion object {
      /** Creates an empty inner size. */
      public val Zero: Inner = Fixed(0f)

      /** Creates a fixed inner size. */
      public fun fixed(maxDiameterDp: Float): Inner = Fixed(maxDiameterDp)
    }
  }
}

private object FillImpl : PieSize.Outer {
  override fun getRadius(
    context: MeasuringContext,
    availableWidth: Float,
    availableHeight: Float,
  ): Float = minOf(availableWidth, availableHeight).half
}

private class Fixed(private val maxDiameterDp: Float) : PieSize.Outer, PieSize.Inner {
  init {
    require(maxDiameterDp >= 0f) { "The max diameter cannot be negative, but was $maxDiameterDp." }
  }

  override fun getRadius(
    context: MeasuringContext,
    availableWidth: Float,
    availableHeight: Float,
  ): Float = with(context) { minOf(availableWidth, availableHeight, maxDiameterDp.pixels).half }
}
