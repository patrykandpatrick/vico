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
 * Stores the sizes of [CartesianLayer]-area margins.
 *
 * @see CartesianLayerMarginUpdater
 */
public class CartesianLayerMargins : HorizontalCartesianLayerMargins {
  /** The start margin’s size. */
  public override var start: Float = 0f
    private set

  /** The top margin’s size. */
  public var top: Float = 0f
    private set

  /** The end margin’s size. */
  public override var end: Float = 0f
    private set

  /** The bottom margin’s size. */
  public var bottom: Float = 0f
    private set

  /** The sum of [top] and [bottom]. */
  public val vertical: Float
    get() = top + bottom

  /** The largest of [start], [top], [end], and [bottom]. */
  public val max: Float
    get() = maxOf(start, top, end, bottom)

  override fun ensureValuesAtLeast(start: Float, end: Float) {
    this.start = this.start.coerceAtLeast(start)
    this.end = this.end.coerceAtLeast(end)
  }

  /** Ensures that the stored values are no smaller than those provided. */
  public fun ensureValuesAtLeast(
    start: Float = this.start,
    top: Float = this.top,
    end: Float = this.end,
    bottom: Float = this.bottom,
  ) {
    this.start = this.start.coerceAtLeast(start)
    this.top = this.top.coerceAtLeast(top)
    this.end = this.end.coerceAtLeast(end)
    this.bottom = this.bottom.coerceAtLeast(bottom)
  }

  /** Clears the stored values. */
  public fun clear() {
    start = 0f
    top = 0f
    end = 0f
    bottom = 0f
  }
}
