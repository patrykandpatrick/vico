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
 * Stores inset sizes for the sides of a rectangle. Used for margins and padding.
 *
 * @param startDp the start inset’s size (in dp).
 * @param topDp the top inset’s size (in dp).
 * @param endDp the end inset’s size (in dp).
 * @param bottomDp the bottom inset’s size (in dp).
 */
@Immutable
public class Insets(
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

  /** Creates an [Insets] instance with [startDp] = [endDp] and [topDp] = [bottomDp]. */
  public constructor(
    horizontalDp: Float = 0f,
    verticalDp: Float = 0f,
  ) : this(horizontalDp, verticalDp, horizontalDp, verticalDp)

  /** Creates an [Insets] instance with a common size for all four insets. */
  public constructor(allDp: Float = 0f) : this(allDp, allDp, allDp, allDp)

  /** Returns the left inset’s size. */
  public fun getLeft(context: MeasuringContext): Float =
    with(context) { (if (isLtr) startDp else endDp).pixels }

  /** Returns the right inset’s size. */
  public fun getRight(context: MeasuringContext): Float =
    with(context) { (if (isLtr) endDp else startDp).pixels }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is Insets &&
        startDp == other.startDp &&
        topDp == other.topDp &&
        endDp == other.endDp &&
        bottomDp == other.bottomDp

  override fun hashCode(): Int {
    var result = startDp.hashCode()
    result = 31 * result + topDp.hashCode()
    result = 31 * result + endDp.hashCode()
    result = 31 * result + bottomDp.hashCode()
    return result
  }

  /** Houses an [Insets] singleton. */
  public companion object {
    /** An [Insets] instance with a size of zero for all four insets. */
    public val Zero: Insets = Insets(0f, 0f, 0f, 0f)
  }
}
