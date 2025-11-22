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
   * is returned, [shouldShowMarker] is called; otherwise, the marker visibility remains unchanged.
   */
  public fun shouldAcceptInteraction(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ): Boolean = true

  /** Whether the marker should be visible. */
  public fun shouldShowMarker(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ): Boolean

  /** Houses [CartesianMarkerController] singletons and factory functions. */
  public companion object {
    /** Shows the [CartesianMarker] on press. */
    @Deprecated(
      "Use `showOnPress()` instead. This property creates a new instance on each access.",
      ReplaceWith("showOnPress()"),
    )
    public val ShowOnPress: CartesianMarkerController
      get() = showOnPress()

    /** Shows the [CartesianMarker] on press. */
    public fun showOnPress(): CartesianMarkerController = ShowOnPressMarkerController()

    /** Shows the [CartesianMarker] on hover. */
    public val ShowOnHover: CartesianMarkerController = ShowOnHoverMarkerController

    /** Toggles the visibility of the [CartesianMarker] on tap. */
    public fun toggleOnTap(): CartesianMarkerController = ToggleOnTapMarkerController()
  }
}

private class ShowOnPressMarkerController : CartesianMarkerController {
  private var isPressed = false

  override fun shouldAcceptInteraction(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ) =
    when (interaction) {
      is Interaction.Press -> {
        isPressed = true
        true
      }
      is Interaction.Move -> isPressed
      is Interaction.Release -> {
        isPressed = false
        true
      }
      else -> false
    }

  override fun shouldShowMarker(interaction: Interaction, targets: List<CartesianMarker.Target>) =
    interaction !is Interaction.Release

  override fun hashCode() = 31

  override fun equals(other: Any?) = other === this || other is ShowOnPressMarkerController
}

private object ShowOnHoverMarkerController : CartesianMarkerController {
  override fun shouldAcceptInteraction(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ) = interaction is Interaction.Enter || interaction is Interaction.Exit

  override fun shouldShowMarker(interaction: Interaction, targets: List<CartesianMarker.Target>) =
    interaction is Interaction.Enter
}

private class ToggleOnTapMarkerController : CartesianMarkerController {
  private var lastTargets: List<CartesianMarker.Target>? = null

  override fun shouldAcceptInteraction(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ) = interaction is Interaction.Tap

  override fun shouldShowMarker(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ): Boolean {
    val show = targets != lastTargets
    lastTargets = if (show) targets else null
    return show
  }

  override fun hashCode() = 31

  override fun equals(other: Any?) = other === this || other is ToggleOnTapMarkerController
}
