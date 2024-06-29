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

/**
 * Used to apply horizontal insets to [CartesianChart]s.
 *
 * @see ChartInsetter
 * @see Insets
 */
public interface HorizontalInsets {
  /** The start inset’s size (in pixels). */
  public val start: Float

  /** The end inset’s size (in pixels). */
  public val end: Float

  /** The sum of [start] and [end]. */
  public val horizontal: Float
    get() = start + end

  /** Returns the left inset’s size (in pixels). */
  public fun getLeft(isLtr: Boolean): Float = if (isLtr) start else end

  /** Returns the right inset’s size (in pixels). */
  public fun getRight(isLtr: Boolean): Float = if (isLtr) end else start

  /** Ensures that the stored values are no smaller than the provided ones. */
  public fun ensureValuesAtLeast(start: Float = this.start, end: Float = this.end)

  /** Ensures that the stored values are no smaller than the provided ones. */
  @Suppress("DeprecatedCallableAddReplaceWith")
  @Deprecated("Use `ensureValuesAtLeast`.")
  public fun setValuesIfGreater(start: Float, end: Float) {
    ensureValuesAtLeast(start, end)
  }
}
