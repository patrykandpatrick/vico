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

/**
 * Stores the coordinates of a rectangle’s sides.
 *
 * @param left the _x_-coordinate for the left edge.
 * @param top the _y_-coordinate for the top edge.
 * @param right the _x_-coordinate for the right edge.
 * @param bottom the _y_-coordinate for the bottom edge.
 */
public class MutableRect(
  public var left: Float = 0f,
  public var top: Float = 0f,
  public var right: Float = 0f,
  public var bottom: Float = 0f,
) {
  public val isEmpty: Boolean
    get() = left >= right || top >= bottom

  /** Sets new coordinates for the rectangle. */
  public fun set(left: Float, top: Float, right: Float, bottom: Float) {
    this.left = left
    this.top = top
    this.right = right
    this.bottom = bottom
  }

  /** Returns the start coordinate, depending on the layout direction. */
  public fun getStart(isLtr: Boolean): Float = if (isLtr) left else right

  /** Returns the end coordinate, depending on the layout direction. */
  public fun getEnd(isLtr: Boolean): Float = if (isLtr) right else left
}
