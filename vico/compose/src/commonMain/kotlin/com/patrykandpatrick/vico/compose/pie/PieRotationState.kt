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
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlin.math.PI
import kotlin.math.atan2
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Houses a [PieChart]’s rotation. Allows for rotation customization and programmatic rotation. */
public class PieRotationState internal constructor(angle: Float) {
  internal val animatable = Animatable(angle)

  /** The current rotation (in degrees). */
  public val angle: Float
    get() = animatable.value

  /** Rotates to [angle] (in degrees). */
  public suspend fun rotate(angle: Float) {
    animatable.snapTo(angle)
  }

  /** Triggers an animated rotation to [angle] (in degrees). */
  public suspend fun animateRotate(angle: Float, animationSpec: AnimationSpec<Float> = spring()) {
    animatable.animateTo(angle, animationSpec)
  }

  internal companion object {
    val Saver: Saver<PieRotationState, Float> =
      Saver(save = { it.angle }, restore = { PieRotationState(it) })
  }
}

/**
 * Creates and remembers a [PieRotationState] instance.
 *
 * @param initialAngle the initial rotation (in degrees).
 */
@Composable
public fun rememberPieRotationState(initialAngle: Float = 0f): PieRotationState =
  rememberSaveable(saver = PieRotationState.Saver) { PieRotationState(initialAngle) }

/** Enables the rotation of a [PieChart] via drag, with fling. */
@Composable
public fun Modifier.pieRotation(state: PieRotationState): Modifier {
  val decay = rememberSplineBasedDecay<Float>()
  return pointerInput(state) {
    coroutineScope {
      val tracker = VelocityTracker()
      var angle = 0f
      detectDragGestures(
        onDragStart = {
          launch { state.animatable.stop() }
          tracker.resetTracking()
          angle = state.angle
        },
        onDragEnd = { launch { state.animatable.animateDecay(tracker.calculateVelocity().x, decay) } },
      ) { change, _ ->
        val center = Offset(size.width / 2f, size.height / 2f)
        angle += angleDelta(center, change.previousPosition, change.position)
        tracker.addPosition(change.uptimeMillis, Offset(angle, 0f))
        launch { state.animatable.snapTo(angle) }
        change.consume()
      }
    }
  }
}

/** Returns the signed angle (in degrees) from [from] to [to] around [center], normalized to ±180°. */
internal fun angleDelta(center: Offset, from: Offset, to: Offset): Float {
  val a = from - center
  val b = to - center
  var delta = (atan2(b.y, b.x) - atan2(a.y, a.x)) * 180f / PI.toFloat()
  if (delta > 180f) delta -= 360f else if (delta < -180f) delta += 360f
  return delta
}
