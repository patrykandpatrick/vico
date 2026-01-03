/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModel

/** Controls [CartesianMarker] visibility. */
public fun interface CartesianMarkerController {
  /** Whether this [CartesianMarkerController] wants to handle long presses. */
  public val acceptsLongPress: Boolean
    get() = true

  /**
   * Specifies whether the marker retains its _x_-value or its on-screen position when the _x_-value
   * corresponding to its position changes for non-gesture reasons (for example, an automatic scroll
   * or a [CartesianChartModel] update).
   */
  public val lock: Lock
    get() = Lock.X

  /** Whether to consume move touch events when scroll is disabled. */
  public val consumeMoveEvents: Boolean
    get() = false

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

  /**
   * Specifies whether a marker retains its _x_-value or its on-screen position when the _x_-value
   * corresponding to its position changes for non-gesture reasons (for example, an automatic scroll
   * or a [CartesianChartModel] update).
   */
  public enum class Lock {
    /** The marker retains its _x_-value, and its on-screen position moves accordingly. */
    X,
    /** The marker retains its on-screen position, and the _x_-value updates accordingly. */
    Position,
  }

  /** Houses [CartesianMarkerController] singletons and factory functions. */
  public companion object {
    internal fun showOnPress(): CartesianMarkerController =
      ShowOnPressMarkerController(consumeMoveEvents = false)

    /**
     * Creates and remembers a [CartesianMarkerController] that shows the marker on press.
     * [consumeMoveEvents] defines whether to consume move touch events when scroll is disabled.
     */
    @Composable
    public fun rememberShowOnPress(consumeMoveEvents: Boolean = false): CartesianMarkerController =
      remember(consumeMoveEvents) { ShowOnPressMarkerController(consumeMoveEvents) }

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

private class ShowOnPressMarkerController(override val consumeMoveEvents: Boolean) :
  CartesianMarkerController {
  private var isPressed = false

  override val acceptsLongPress = false

  override val lock = CartesianMarkerController.Lock.Position

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

  override fun hashCode() = 31 * isPressed.hashCode() + consumeMoveEvents.hashCode()

  override fun equals(other: Any?) =
    other === this ||
      other is ShowOnPressMarkerController &&
        isPressed == other.isPressed &&
        consumeMoveEvents == other.consumeMoveEvents
}

private class ShowOnHoverMarkerController : CartesianMarkerController {
  private var isHovering = false

  override val lock = CartesianMarkerController.Lock.Position

  override fun shouldAcceptInteraction(
    interaction: Interaction,
    targets: List<CartesianMarker.Target>,
  ) =
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
