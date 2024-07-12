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

package com.patrykandpatrick.vico.core.cartesian

import androidx.compose.runtime.Immutable
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer

/**
 * Defines how a [CartesianChart]’s content is positioned horizontally. This affects the
 * [CartesianLayer]s and the [HorizontalAxis] instances.
 */
@Immutable
public sealed interface HorizontalLayout {
  /**
   * When this is applied, the [CartesianChart] centers each major entry in a designated segment.
   * Some empty space is visible at the start and end of the [CartesianChart]. [HorizontalAxis]
   * instances display ticks and guidelines at the edges of the segments.
   */
  public data object Segmented : HorizontalLayout

  /**
   * When this is applied, the [CartesianChart]’s content takes up the [CartesianChart]’s entire
   * width (unless padding is added). [HorizontalAxis] instances display a tick and a guideline for
   * each label, with the tick, guideline, and label vertically centered relative to one another.
   * [scalableStartPaddingDp], [scalableEndPaddingDp], [unscalableStartPaddingDp], and
   * [unscalableEndPaddingDp] control the amount of empty space at the start and end of the
   * [CartesianChart]. Scalable padding values are multiplied by the zoom factor, unlike unscalable
   * ones.
   */
  public class FullWidth(
    public val scalableStartPaddingDp: Float = 0f,
    public val scalableEndPaddingDp: Float = 0f,
    public val unscalableStartPaddingDp: Float = 0f,
    public val unscalableEndPaddingDp: Float = 0f,
  ) : HorizontalLayout {
    override fun equals(other: Any?): Boolean =
      this === other ||
        other is FullWidth &&
          scalableStartPaddingDp == other.scalableStartPaddingDp &&
          scalableEndPaddingDp == other.scalableEndPaddingDp &&
          unscalableStartPaddingDp == other.unscalableStartPaddingDp &&
          unscalableEndPaddingDp == other.unscalableEndPaddingDp

    override fun hashCode(): Int {
      var result = scalableStartPaddingDp.hashCode()
      result = 31 * result + scalableEndPaddingDp.hashCode()
      result = 31 * result + unscalableStartPaddingDp.hashCode()
      result = 31 * result + unscalableEndPaddingDp.hashCode()
      return result
    }
  }

  public companion object
}
