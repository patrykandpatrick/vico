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

package com.patrykandpatrick.vico.multiplatform.cartesian.marker

import com.patrykandpatrick.vico.multiplatform.common.Point

/** TODO */
public sealed class PointerEvent {
  public abstract val point: Point

  public open val isPressedOrMoved: Boolean = false

  /** TODO */
  public data class Press(override val point: Point) : PointerEvent() {
    override val isPressedOrMoved: Boolean = true
  }

  /** TODO */
  public data class Tap(override val point: Point) : PointerEvent() {
    override val isPressedOrMoved: Boolean = true
  }

  /** TODO */
  public data class LongPress(override val point: Point) : PointerEvent() {
    override val isPressedOrMoved: Boolean = true
  }

  /** TODO */
  public data class Move(override val point: Point) : PointerEvent() {
    override val isPressedOrMoved: Boolean = true
  }

  /** TODO */
  public data class Release(override val point: Point) : PointerEvent()

  /** TODO */
  public data class Zoom(override val point: Point) : PointerEvent()
}
