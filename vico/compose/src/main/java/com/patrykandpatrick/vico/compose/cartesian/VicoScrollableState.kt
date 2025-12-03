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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.stopScroll

internal class VicoScrollableState(
  private val consumeScrollDelta: (Float, VicoScrollState.ScrollTrigger) -> Float
) {
  private var currentTrigger: VicoScrollState.ScrollTrigger? = null

  /**
   * [currentTrigger] has a fallback to [VicoScrollState.ScrollTrigger.User] due to the delta being
   * used by a draggable [androidx.compose.ui.Modifier] which uses [ScrollScope].
   */
  internal val scrollableState = ScrollableState { delta ->
    consumeScrollDelta(delta, currentTrigger ?: VicoScrollState.ScrollTrigger.User)
  }

  val isScrollInProgress: Boolean
    get() = scrollableState.isScrollInProgress

  suspend fun scroll(
    scrollPriority: MutatePriority = MutatePriority.Default,
    trigger: VicoScrollState.ScrollTrigger,
    block: suspend ScrollScope.() -> Unit,
  ) {
    currentTrigger = trigger
    scrollableState.scroll(scrollPriority, block)
    currentTrigger = null
  }

  suspend fun stopScroll(mutatePriority: MutatePriority = MutatePriority.Default) {
    scrollableState.stopScroll(mutatePriority)
    currentTrigger = null
  }

  suspend fun scrollBy(delta: Float, trigger: VicoScrollState.ScrollTrigger): Float {
    currentTrigger = trigger
    val consumedScroll = scrollableState.scrollBy(delta)
    currentTrigger = null
    return consumedScroll
  }

  suspend fun animateScrollBy(
    delta: Float,
    animationSpec: AnimationSpec<Float> = spring(),
    trigger: VicoScrollState.ScrollTrigger,
  ): Float {
    currentTrigger = trigger
    val consumedScroll = scrollableState.animateScrollBy(delta, animationSpec)
    currentTrigger = null
    return consumedScroll
  }
}
