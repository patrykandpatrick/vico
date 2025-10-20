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

package com.patrykandpatrick.vico.core.cartesian.marker

import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.common.Point
import java.io.Serializable

/** Represents a pointer interaction (such as a press, move, or release). */
public sealed class Interaction : Serializable {
  public abstract val point: Point

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public fun moveXBy(deltaX: Float): Interaction =
    when (this) {
      is Press -> copy(point.copy(x = point.x + deltaX))
      is Tap -> copy(point.copy(x = point.x + deltaX))
      is LongPress -> copy(point.copy(x = point.x + deltaX))
      is Move -> copy(point.copy(x = point.x + deltaX))
      is Release -> copy(point.copy(x = point.x + deltaX))
      is Zoom -> copy(point.copy(x = point.x + deltaX))
    }

  /** A press interaction. */
  public data class Press(override val point: Point) : Interaction()

  /** A tap interaction. */
  public data class Tap(override val point: Point) : Interaction()

  /** A long-press interaction. */
  public data class LongPress(override val point: Point) : Interaction()

  /** A move interaction. */
  public data class Move(override val point: Point) : Interaction()

  /** A release interaction. */
  public data class Release(override val point: Point) : Interaction()

  /** A zoom interaction. */
  public data class Zoom(override val point: Point) : Interaction()
}
