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

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.patrykandpatrick.vico.multiplatform.common.Point

/** Represents a pointer interaction (such as a press, move, or release). */
public sealed class Interaction {
  public abstract val point: Point

  internal fun moveXBy(deltaX: Float): Interaction =
    when (this) {
      is Press -> copy(point = point.copy(x = point.x + deltaX))
      is Tap -> copy(point = point.copy(x = point.x + deltaX))
      is LongPress -> copy(point = point.copy(x = point.x + deltaX))
      is Move -> copy(point = point.copy(x = point.x + deltaX))
      is Release -> copy(point = point.copy(x = point.x + deltaX))
      is Zoom -> copy(point = point.copy(x = point.x + deltaX))
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

  internal companion object {
    internal val Saver: Saver<MutableState<Interaction?>, Any> =
      listSaver(
        save = { eventState ->
          val event = eventState.value
          when (event) {
            is Press -> listOf("Press", event.point.x, event.point.y)
            is Tap -> listOf("Tap", event.point.x, event.point.y)
            is LongPress -> listOf("LongPress", event.point.x, event.point.y)
            is Move -> listOf("Move", event.point.x, event.point.y)
            is Release -> listOf("Release", event.point.x, event.point.y)
            is Zoom -> listOf("Zoom", event.point.x, event.point.y)
            else -> emptyList()
          }
        },
        restore = { list ->
          if (list.isEmpty()) return@listSaver mutableStateOf(null)
          val type = list[0] as String
          val point = Point(list[1] as Float, list[2] as Float)
          when (type) {
            "Press" -> Press(point)
            "Tap" -> Tap(point)
            "LongPress" -> LongPress(point)
            "Move" -> Move(point)
            "Release" -> Release(point)
            "Zoom" -> Zoom(point)
            else -> error("Unknown Interaction type: $type")
          }.let(::mutableStateOf)
        },
      )
  }
}
