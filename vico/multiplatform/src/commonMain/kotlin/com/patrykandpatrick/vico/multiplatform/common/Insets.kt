/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.multiplatform.common

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Stores inset sizes for the sides of a rectangle. Used for margins and padding.
 *
 * @param start the start inset’s size.
 * @param top the top inset’s size.
 * @param end the end inset’s size.
 * @param bottom the bottom inset’s size.
 */
@Immutable
public class Insets(
  public val start: Dp = 0f.dp,
  public val top: Dp = 0f.dp,
  public val end: Dp = 0f.dp,
  public val bottom: Dp = 0f.dp,
) {
  /** The sum of [start] and [end]. */
  public val horizontal: Dp
    get() = start + end

  /** The sum of [top] and [bottom]. */
  public val vertical: Dp
    get() = top + bottom

  /** Creates an [Insets] instance with [start] = [end] and [top] = [bottom]. */
  public constructor(
    horizontal: Dp = 0f.dp,
    vertical: Dp = 0f.dp,
  ) : this(horizontal, vertical, horizontal, vertical)

  /** Creates an [Insets] instance with a common size for all four insets. */
  public constructor(all: Dp = 0f.dp) : this(all, all, all, all)

  /** Returns the left inset’s size. */
  public fun getLeft(context: MeasuringContext): Float =
    with(context) { (if (isLtr) start else end).pixels }

  /** Returns the right inset’s size. */
  public fun getRight(context: MeasuringContext): Float =
    with(context) { (if (isLtr) end else start).pixels }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is Insets &&
        start == other.start &&
        top == other.top &&
        end == other.end &&
        bottom == other.bottom

  override fun hashCode(): Int {
    var result = start.hashCode()
    result = 31 * result + top.hashCode()
    result = 31 * result + end.hashCode()
    result = 31 * result + bottom.hashCode()
    return result
  }

  /** Houses an [Insets] singleton. */
  public companion object {
    /** An [Insets] instance with a size of zero for all four insets. */
    public val Zero: Insets = Insets(all = 0f.dp)
  }
}
