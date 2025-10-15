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

/** Controls [CartesianMarker] visibility. */
public fun interface CartesianMarkerController {
  /**
   * Whether this [CartesianMarkerController] wants to respond to [interactionEvent]. If it returns
   * `true`, [isMarkerVisible] is called. Otherwise the marker visibility remains unchanged.
   */
  public fun acceptEvent(
    interactionEvent: InteractionEvent,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean = true

  /**
   * Whether the marker should be visible, given the current [interactionEvent] and the currently
   * [markedEntries].
   */
  public fun isMarkerVisible(
    interactionEvent: InteractionEvent,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean

  public companion object {
    /** Shows [CartesianMarker] on press interaction. */
    public val showOnPress: CartesianMarkerController
      get() = ShowOnPressMarkerController()

    /** Toggles the visibility of [CartesianMarker] upon tap interaction. */
    public val toggleOnTap: CartesianMarkerController
      get() = ToggleOnTapMarkerController()
  }
}

private class ShowOnPressMarkerController : CartesianMarkerController {
  override fun acceptEvent(
    interactionEvent: InteractionEvent,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean =
    interactionEvent is InteractionEvent.Press ||
      interactionEvent is InteractionEvent.Release ||
      interactionEvent is InteractionEvent.Move

  override fun isMarkerVisible(
    interactionEvent: InteractionEvent,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean = interactionEvent !is InteractionEvent.Release

  override fun hashCode(): Int = 31

  override fun equals(other: Any?): Boolean = other === this || other is ShowOnPressMarkerController
}

private class ToggleOnTapMarkerController : CartesianMarkerController {
  private var lastMarkedEntries: List<CartesianMarker.Target>? = null

  override fun acceptEvent(
    interactionEvent: InteractionEvent,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean = interactionEvent is InteractionEvent.Tap

  override fun isMarkerVisible(
    interactionEvent: InteractionEvent,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean {
    val show = markedEntries != lastMarkedEntries
    lastMarkedEntries = if (show) markedEntries else null
    return show
  }

  override fun hashCode(): Int = 31

  override fun equals(other: Any?): Boolean = other === this || other is ToggleOnTapMarkerController
}
