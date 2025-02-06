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

/** TODO */
public fun interface CartesianMarkerController {
  /** TODO */
  public fun onPointerState(
    pointerState: PointerState,
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

  override fun onPointerState(
    pointerState: PointerState,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean = pointerState.isPressedOrMoved

  override fun hashCode(): Int = 31

  override fun equals(other: Any?): Boolean = other === this || other is ShowOnPressMarkerController
}

private class ToggleOnPressMarkerController : CartesianMarkerController {
  private var lastMarkedEntries: List<CartesianMarker.Target>? = null
  private var wasShown = false

  override fun onPointerState(
    pointerState: PointerState,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean {
    val show =
      when (pointerState) {
        is PointerState.Pressed -> markedEntries != lastMarkedEntries
        is PointerState.Moved,
        is PointerState.Released -> wasShown

        is PointerState.Zoomed -> false
      }
    wasShown = show
    lastMarkedEntries = if (show) markedEntries else null
    return show
  }

  override fun hashCode(): Int = 31

  override fun equals(other: Any?): Boolean =
    other === this || other is ToggleOnPressMarkerController
}
