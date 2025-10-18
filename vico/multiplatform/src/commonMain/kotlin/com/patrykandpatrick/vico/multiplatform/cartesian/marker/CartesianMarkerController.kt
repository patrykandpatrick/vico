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

/** Controls [CartesianMarker] visibility. */
public fun interface CartesianMarkerController {
  /**
   * Indicates whether this [CartesianMarkerController] wants to respond to [interaction]. If `true`
   * is returned, [isMarkerVisible] is called; otherwise, the marker visibility remains unchanged.
   */
  public fun acceptEvent(
    interaction: Interaction,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean = true

  /** Whether the marker should be visible. */
  public fun isMarkerVisible(
    interaction: Interaction,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean

  /** Houses [CartesianMarkerController] singletons and factory functions. */
  public companion object {
    /** Shows the [CartesianMarker] on press. */
    public val ShowOnPress: CartesianMarkerController = ShowOnPressMarkerController

    /** Toggles the visibility of the [CartesianMarker] on tap. */
    public fun toggleOnTap(): CartesianMarkerController = ToggleOnTapMarkerController()
  }
}

private object ShowOnPressMarkerController : CartesianMarkerController {
  override fun acceptEvent(interaction: Interaction, markedEntries: List<CartesianMarker.Target>) =
    interaction is Interaction.Press ||
      interaction is Interaction.Release ||
      interaction is Interaction.Move

  override fun isMarkerVisible(
    interaction: Interaction,
    markedEntries: List<CartesianMarker.Target>,
  ) = interaction !is Interaction.Release
}

private class ToggleOnTapMarkerController : CartesianMarkerController {
  private var lastMarkedEntries: List<CartesianMarker.Target>? = null

  override fun acceptEvent(interaction: Interaction, markedEntries: List<CartesianMarker.Target>) =
    interaction is Interaction.Tap

  override fun isMarkerVisible(
    interaction: Interaction,
    markedEntries: List<CartesianMarker.Target>,
  ): Boolean {
    val show = markedEntries != lastMarkedEntries
    lastMarkedEntries = if (show) markedEntries else null
    return show
  }

  override fun hashCode() = 31

  override fun equals(other: Any?) = other === this || other is ToggleOnTapMarkerController
}
