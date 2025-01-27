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

package com.patrykandpatrick.vico.multiplatform.cartesian.layer

/**
 * Stores the sizes of horizontal [CartesianLayer]-area margins.
 *
 * @see CartesianLayerMargins
 * @see CartesianLayerMarginUpdater
 */
public interface HorizontalCartesianLayerMargins {
  /** The start margin’s size. */
  public val start: Float

  /** The end margin’s size. */
  public val end: Float

  /** The sum of [start] and [end]. */
  public val horizontal: Float
    get() = start + end

  /** Returns the left margin’s size. */
  public fun getLeft(isLtr: Boolean): Float = if (isLtr) start else end

  /** Returns the right margin’s size. */
  public fun getRight(isLtr: Boolean): Float = if (isLtr) end else start

  /** Ensures that the stored values are no smaller than those provided. */
  public fun ensureValuesAtLeast(start: Float = this.start, end: Float = this.end)
}
