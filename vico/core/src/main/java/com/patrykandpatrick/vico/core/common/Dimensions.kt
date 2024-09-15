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

import androidx.compose.runtime.Immutable

/**
 * Defines the size of each edge of a rectangle. Used to store measurements such as padding or
 * margin values.
 *
 * @param startDp the value for the start edge in the dp unit.
 * @param topDp the value for the top edge in the dp unit.
 * @param endDp the value for the end edge in the dp unit.
 * @param bottomDp the value for the bottom edge in the dp unit.
 */
@Immutable
public data class Dimensions(
  public val startDp: Float = 0f,
  public val topDp: Float = 0f,
  public val endDp: Float = 0f,
  public val bottomDp: Float = 0f,
) {
  /** The sum of [startDp] and [endDp]. */
  public val horizontalDp: Float
    get() = startDp + endDp

  /** The sum of [topDp] and [bottomDp]. */
  public val verticalDp: Float
    get() = topDp + bottomDp

  /** Creates a [Dimensions] instance using the provided measurements. */
  public constructor(
    horizontalDp: Float = 0f,
    verticalDp: Float = 0f,
  ) : this(horizontalDp, verticalDp, horizontalDp, verticalDp)

  /** Creates a [Dimensions] instance using the provided measurements. */
  public constructor(allDp: Float = 0f) : this(allDp, allDp, allDp, allDp)

  /**
   * Returns the dimension of the left edge depending on the layout orientation.
   *
   * @param isLtr whether the device layout is left-to-right.
   */
  public fun getLeftDp(isLtr: Boolean): Float = if (isLtr) startDp else endDp

  /**
   * Returns the dimension of the right edge depending on the layout orientation.
   *
   * @param isLtr whether the device layout is left-to-right.
   */
  public fun getRightDp(isLtr: Boolean): Float = if (isLtr) endDp else startDp

  public companion object {
    /** A [Dimensions] instance with all coordinates set to 0. */
    public val Empty: Dimensions = Dimensions(0f, 0f, 0f, 0f)
  }
}
