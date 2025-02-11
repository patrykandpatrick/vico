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

package com.patrykandpatrick.vico.multiplatform.cartesian

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.multiplatform.common.getValue
import com.patrykandpatrick.vico.multiplatform.common.rememberWrappedValue
import com.patrykandpatrick.vico.multiplatform.common.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
internal actual fun Modifier.extraPointerInput(scrollState: VicoScrollState): Modifier {
  var scrollJob by rememberWrappedValue<Job?>(null)
  val coroutineScope = rememberCoroutineScope()
  val animationSpec = rememberSplineBasedDecay<Float>()
  return draggable(
    state =
      rememberDraggableState { delta ->
        scrollJob?.cancel()
        scrollJob = coroutineScope.launch { scrollState.scroll(Scroll.Relative.pixels(delta)) }
      },
    orientation = Orientation.Horizontal,
    onDragStopped = { velocity ->
      scrollJob?.cancel()
      scrollJob =
        coroutineScope.launch {
          AnimationState(scrollState.value, velocity).animateDecay(animationSpec) {
            launch { scrollState.scroll(Scroll.Absolute.pixels(value)) }
          }
        }
    },
    reverseDirection = true,
  )
}
