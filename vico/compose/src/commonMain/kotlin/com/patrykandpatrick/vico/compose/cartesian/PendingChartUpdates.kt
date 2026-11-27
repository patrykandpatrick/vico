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

package com.patrykandpatrick.vico.compose.cartesian

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

/**
 * Tracks a host’s asynchronous chart updates so that programmatic scroll and zoom commands can wait
 * for the update that was pending when they were called. An update is handled once the host has
 * drawn a frame based on it (or on a newer chart), as only then are the scroll and zoom states
 * updated with its ranges and dimensions. All functions must be called on the main thread.
 */
internal class PendingChartUpdates {
  private var latestAsyncGeneration = 0L
  private val handledGeneration = MutableStateFlow(0L)

  /** Records that the chart with [generation] is being derived asynchronously. */
  fun onAsyncSubmitted(generation: Long) {
    latestAsyncGeneration = generation
  }

  /** Records that the host has drawn the chart with [generation]. */
  fun onHandled(generation: Long) {
    if (generation > handledGeneration.value) handledGeneration.value = generation
  }

  /** Releases all waiters, now and in the future (e.g., because the host left composition). */
  fun dispose() {
    handledGeneration.value = Long.MAX_VALUE
  }

  /**
   * Suspends until the chart update that’s pending at call time (if any) has been handled. Newer
   * updates aren’t waited for.
   */
  suspend fun await() {
    val target = latestAsyncGeneration
    if (handledGeneration.value >= target) return
    handledGeneration.first { it >= target }
  }
}
