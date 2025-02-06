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
public sealed class PointerState {
  public abstract val point: Point

  public open val isPressedOrMoved: Boolean = false

  /** TODO */
  public data class Pressed(override val point: Point) : PointerState() {
    override val isPressedOrMoved: Boolean = true
  }

  /** TODO */
  public data class Moved(override val point: Point) : PointerState() {
    override val isPressedOrMoved: Boolean = true
  }

  /** TODO */
  public data class Released(override val point: Point) : PointerState()

  /** TODO */
  public data class Zoomed(override val point: Point) : PointerState()
}
