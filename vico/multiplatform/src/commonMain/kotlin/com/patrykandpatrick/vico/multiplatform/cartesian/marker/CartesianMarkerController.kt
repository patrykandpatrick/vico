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

/** TODO */
public fun interface CartesianMarkerController {
  /** TODO */
  public fun isMarkerVisible(
    pointerEvent: PointerEvent,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean

  public companion object {
    /** TODO */
    public val showOnPress: CartesianMarkerController
      get() = ShowOnPressMarkerController()

    /** TODO */
    public val toggleOnPress: CartesianMarkerController
      get() = ToggleOnPressMarkerController()
  }
}

private class ShowOnPressMarkerController : CartesianMarkerController {

  override fun isMarkerVisible(
    pointerEvent: PointerEvent,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean = pointerEvent.isPressedOrMoved

  override fun hashCode(): Int = 31

  override fun equals(other: Any?): Boolean = other === this || other is ShowOnPressMarkerController
}

private class ToggleOnPressMarkerController : CartesianMarkerController {
  private var lastMarkedEntries: List<CartesianMarker.Target>? = null
  private var wasShown = false

  override fun isMarkerVisible(
    pointerEvent: PointerEvent,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean {
    val show =
      when (pointerEvent) {
        is PointerEvent.Tap,
        is PointerEvent.LongPress,
        is PointerEvent.Press -> markedEntries != lastMarkedEntries
        is PointerEvent.Move,
        is PointerEvent.Release -> wasShown

        is PointerEvent.Zoom -> false
      }
    wasShown = show
    lastMarkedEntries = if (show) markedEntries else null
    return show
  }

  override fun hashCode(): Int = 31

  override fun equals(other: Any?): Boolean =
    other === this || other is ToggleOnPressMarkerController
}
