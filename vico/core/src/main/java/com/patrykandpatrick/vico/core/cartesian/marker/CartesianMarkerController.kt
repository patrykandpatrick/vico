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
   * Whether this [CartesianMarkerController] wants to respond to [interaction]. If it returns
   * `true`, [isMarkerVisible] is called. Otherwise the marker visibility remains unchanged.
   */
  public fun acceptEvent(
    interaction: Interaction,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean = true

  /**
   * Whether the marker should be visible, given the current [interaction] and the currently
   * [markedEntries].
   */
  public fun isMarkerVisible(
    interaction: Interaction,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean

  public companion object {
    /** Shows [CartesianMarker] on press interaction. */
    public val ShowOnPress: CartesianMarkerController = ShowOnPressMarkerController

    /** Toggles the visibility of [CartesianMarker] upon tap interaction. */
    public fun toggleOnTap(): CartesianMarkerController = ToggleOnTapMarkerController()
  }
}

private object ShowOnPressMarkerController : CartesianMarkerController {
  override fun acceptEvent(
    interaction: Interaction,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean =
    interaction is Interaction.Press ||
      interaction is Interaction.Release ||
      interaction is Interaction.Move

  override fun isMarkerVisible(
    interaction: Interaction,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean = interaction !is Interaction.Release
}

private class ToggleOnTapMarkerController : CartesianMarkerController {
  private var lastMarkedEntries: List<CartesianMarker.Target>? = null

  override fun acceptEvent(
    interaction: Interaction,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean = interaction is Interaction.Tap

  override fun isMarkerVisible(
    interaction: Interaction,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean {
    val show = markedEntries != lastMarkedEntries
    lastMarkedEntries = if (show) markedEntries else null
    return show
  }

  override fun hashCode(): Int = 31

  override fun equals(other: Any?): Boolean = other === this || other is ToggleOnTapMarkerController
}
