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

package com.patrykandpatrick.vico.compose.pie

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** Houses information on a [PieChart]’s rotation. */
public class VicoPieRotationState internal constructor(angle: Float) {
  private val animatable = Animatable(angle)

  internal var rotationEnabled: Boolean by mutableStateOf(false)

  /** The current rotation (in degrees). */
  public val angle: Float
    get() = animatable.value

  internal suspend fun stop() {
    animatable.stop()
  }

  internal suspend fun rotateBy(delta: Float) {
    animatable.snapTo(animatable.value + delta)
  }

  internal suspend fun fling(velocity: Float) {
    animatable.animateDecay(velocity, exponentialDecay())
    settle()
  }

  /** Normalizes the angle to [0, 360). To be called only when the state is idle. */
  internal suspend fun settle() {
    animatable.snapTo(normalizeAngle(animatable.value))
  }

  internal companion object {
    val Saver: Saver<VicoPieRotationState, Float> =
      Saver(save = { normalizeAngle(it.angle) }, restore = { VicoPieRotationState(it) })
  }
}

/**
 * Creates and remembers a [VicoPieRotationState] instance.
 *
 * @param rotationEnabled whether drag-to-rotate is enabled.
 * @param initialRotation the initial rotation (in degrees).
 */
@Composable
public fun rememberVicoPieRotationState(
  rotationEnabled: Boolean = false,
  initialRotation: Float = -90f,
): VicoPieRotationState {
  val state =
    rememberSaveable(saver = VicoPieRotationState.Saver) { VicoPieRotationState(initialRotation) }
  SideEffect { state.rotationEnabled = rotationEnabled }
  return state
}
