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
  /** Whether this [CartesianMarkerController] wants to handle long presses. */
  public val isLongPressSupported: Boolean
    get() = true

  /** The lock to use for the marker. */
  public val lock: Lock
    get() = Lock.ScrollPosition

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

  /** Defines what the marker is locked to. */
  public enum class Lock {
    /** The marker is locked to an x-position. */
    XPosition,
    /** The marker is locked to the scroll position. */
    ScrollPosition,
  }

  /** Houses [CartesianMarkerController] singletons and factory functions. */
  public companion object {
    /** Shows the [CartesianMarker] on press. */
    public fun showOnPress(): CartesianMarkerController = ShowOnPressMarkerController()

    /** Shows the [CartesianMarker] on hover. */
    public fun showOnHover(): CartesianMarkerController = ShowOnHoverMarkerController()

    /** Toggles the visibility of the [CartesianMarker] on tap. */
    public fun toggleOnTap(): CartesianMarkerController = ToggleOnTapMarkerController()
  }
}

private class ShowOnPressMarkerController : CartesianMarkerController {
  private var isPressed = false

  override val isLongPressSupported: Boolean = false

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

  override fun hashCode() = isPressed.hashCode()

  override fun equals(other: Any?) =
    other === this || other is ShowOnPressMarkerController && isPressed == other.isPressed
}

private class ShowOnHoverMarkerController : CartesianMarkerController {
  private var isHovering = false

  override fun shouldAcceptInteraction(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ): Boolean =
    interaction is Interaction.Enter ||
      interaction is Interaction.Exit ||
      interaction is Interaction.Press ||
      interaction is Interaction.Move

  override fun shouldShowMarker(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ): Boolean {
    when (interaction) {
      is Interaction.Enter -> isHovering = targets.isNotEmpty()
      is Interaction.Exit -> isHovering = interaction.isInsideChartBounds
      else -> Unit
    }
    return isHovering
  }

  override fun hashCode() = isHovering.hashCode()

  override fun equals(other: Any?) =
    other === this || other is ShowOnHoverMarkerController && isHovering == other.isHovering
}

private class ToggleOnTapMarkerController : CartesianMarkerController {
  private var lastTargets: List<CartesianMarker.Target>? = null

  override val isLongPressSupported: Boolean = false

  override val lock: CartesianMarkerController.Lock = CartesianMarkerController.Lock.XPosition

  override fun shouldAcceptInteraction(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ) = interaction is Interaction.Tap && targets.isNotEmpty()

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
