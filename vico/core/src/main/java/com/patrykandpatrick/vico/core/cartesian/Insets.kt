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
 * Used to store the insets requested by [ChartInsetter]s.
 *
 * @see ChartInsetter
 */
public class Insets : HorizontalInsets {
  /** The start inset’s size (in pixels). */
  public override var start: Float = 0f
    private set

  /** The top inset’s size (in pixels). */
  public var top: Float = 0f
    private set

  /** The end inset’s size (in pixels). */
  public override var end: Float = 0f
    private set

  /** The bottom inset’s size (in pixels). */
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

  /** Ensures that the stored values are no smaller than the provided ones. */
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

  /** Ensures that the stored values are no smaller than the provided ones. */
  @Deprecated(
    "Use `ensureValuesAtLeast`.",
    ReplaceWith("ensureValuesAtLeast(start, top, end, bottom)"),
  )
  public fun setAllIfGreater(
    start: Float = this.start,
    top: Float = this.top,
    end: Float = this.end,
    bottom: Float = this.bottom,
  ) {
    ensureValuesAtLeast(start, top, end, bottom)
  }

  /** Ensures that the stored values are no smaller than those in [other]. */
  @Deprecated(
    "Use `ensureValuesAtLeast`.",
    ReplaceWith("ensureValuesAtLeast(other.start, other.top, other.end, other.bottom)"),
  )
  public fun setValuesIfGreater(other: Insets) {
    ensureValuesAtLeast(other.start, other.top, other.end, other.bottom)
  }

  /** Clears the stored values. */
  public fun clear() {
    start = 0f
    top = 0f
    end = 0f
    bottom = 0f
  }
}
