/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.common

import android.graphics.RectF

/** Defines the relative position of an object. */
public object Position {
  /** Defines the relative horizontal position of an object. */
  public enum class Horizontal {
    Start,
    Center,
    End,
  }

  /** Defines the relative vertical position of an object. */
  public enum class Vertical {
    Top,
    Center,
    Bottom,
  }
}

internal operator fun Position.Horizontal.unaryMinus() =
  when (this) {
    Position.Horizontal.Start -> Position.Horizontal.End
    Position.Horizontal.Center -> Position.Horizontal.Center
    Position.Horizontal.End -> Position.Horizontal.Start
  }

internal operator fun Position.Vertical.unaryMinus() =
  when (this) {
    Position.Vertical.Top -> Position.Vertical.Bottom
    Position.Vertical.Center -> Position.Vertical.Center
    Position.Vertical.Bottom -> Position.Vertical.Top
  }

internal fun Position.Vertical.inBounds(
  bounds: RectF,
  componentHeight: Float,
  referenceY: Float,
  referenceDistance: Float = 0f,
): Position.Vertical {
  val topFits = referenceY - referenceDistance - componentHeight >= bounds.top
  val centerFits =
    referenceY - componentHeight.half >= bounds.top &&
      referenceY + componentHeight.half <= bounds.bottom
  val bottomFits = referenceY + referenceDistance + componentHeight <= bounds.bottom
  return when (this) {
    Position.Vertical.Top -> if (topFits) this else Position.Vertical.Bottom
    Position.Vertical.Bottom -> if (bottomFits) this else Position.Vertical.Top
    Position.Vertical.Center ->
      when {
        centerFits -> this
        topFits -> Position.Vertical.Top
        else -> Position.Vertical.Bottom
      }
  }
}
