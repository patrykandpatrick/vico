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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** Controls [CartesianMarker] visibility. */
public fun interface CartesianMarkerController {
  /** Whether this [CartesianMarkerController] wants to handle long presses. */
  public val acceptsLongPress: Boolean
    get() = true

  /**
   * Called when the viewport changes due to scrolling, auto-scrolling, zooming, or data updates.
   * Allows updating the last accepted [Interaction] accordingly.
   *
   * @param lastAcceptedInteraction The last accepted interaction.
   * @param reason The reason for the viewport change.
   * @return An updated [Interaction] or `null` to keep the last accepted interaction unchanged.
   */
  public fun onViewportChange(
    lastAcceptedInteraction: Interaction,
    reason: ViewportChangeReason,
  ): Interaction? = null

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

  public sealed class ViewportChangeReason {
    public open val scrollDelta: Float = 0f

    public data class Scroll(public override val scrollDelta: Float) : ViewportChangeReason()

    public data class AutoScroll(public override val scrollDelta: Float) : ViewportChangeReason()

    public data class Zoom(public override val scrollDelta: Float) : ViewportChangeReason()

    public data object DataUpdate : ViewportChangeReason()
  }

  /** Houses [CartesianMarkerController] singletons and factory functions. */
  public companion object {
    internal fun showOnPress(): CartesianMarkerController = ShowOnPressMarkerController()

    /** Creates and remembers a [CartesianMarkerController] that shows the marker on press. */
    @Composable
    public fun rememberShowOnPress(): CartesianMarkerController = remember {
      ShowOnPressMarkerController()
    }

    /** Creates and remembers a [CartesianMarkerController] that shows the marker on hover. */
    @Composable
    public fun rememberShowOnHover(): CartesianMarkerController = remember {
      ShowOnHoverMarkerController()
    }

    /**
     * Creates and remembers a [CartesianMarkerController] that toggles the marker visibility on
     * tap.
     */
    @Composable
    public fun rememberToggleOnTap(): CartesianMarkerController = remember {
      ToggleOnTapMarkerController()
    }
  }
}

private class ShowOnPressMarkerController : CartesianMarkerController {
  private var isPressed = false

  override val acceptsLongPress = false

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

  override fun onViewportChange(
    lastAcceptedInteraction: Interaction,
    reason: CartesianMarkerController.ViewportChangeReason,
  ): Interaction? =
    when (reason) {
      is CartesianMarkerController.ViewportChangeReason.Scroll ->
        Interaction.Move(
          lastAcceptedInteraction.point.copy(
            x = lastAcceptedInteraction.point.x + reason.scrollDelta
          )
        )
      else -> lastAcceptedInteraction
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
  ) =
    interaction is Interaction.Enter ||
      interaction is Interaction.Exit ||
      interaction is Interaction.Press ||
      interaction is Interaction.Move

  override fun onViewportChange(
    lastAcceptedInteraction: Interaction,
    reason: CartesianMarkerController.ViewportChangeReason,
  ): Interaction = lastAcceptedInteraction

  override fun shouldShowMarker(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ): Boolean {
    when (interaction) {
      is Interaction.Enter -> isHovering = targets.isNotEmpty()
      is Interaction.Exit -> isHovering = interaction.isInBounds
      else -> {}
    }
    return isHovering
  }

  override fun hashCode() = isHovering.hashCode()

  override fun equals(other: Any?) =
    other === this || other is ShowOnHoverMarkerController && isHovering == other.isHovering
}

private class ToggleOnTapMarkerController : CartesianMarkerController {
  private var lastTargets: List<CartesianMarker.Target>? = null

  override val acceptsLongPress = false

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
