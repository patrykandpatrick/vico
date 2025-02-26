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
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalInspectionMode
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartData
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartDataState
import com.patrykandpatrick.vico.compose.common.rememberWrappedValue
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.data.PreviewContext
import com.patrykandpatrick.vico.core.cartesian.data.toImmutable
import com.patrykandpatrick.vico.core.common.Animation
import com.patrykandpatrick.vico.core.common.NEW_PRODUCER_ERROR_MESSAGE
import com.patrykandpatrick.vico.core.common.ValueWrapper
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore
import com.patrykandpatrick.vico.core.common.getValue
import com.patrykandpatrick.vico.core.common.setValue
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal val defaultCartesianDiffAnimationSpec: AnimationSpec<Float> =
  tween(durationMillis = Animation.DIFF_DURATION)

@Composable
internal fun CartesianChartModelProducer.collectAsState(
  chart: CartesianChart,
  animationSpec: AnimationSpec<Float>?,
  animateIn: Boolean,
  ranges: MutableCartesianChartRanges,
): State<CartesianChartData> {
  var previousHashCode by remember { ValueWrapper<Int?>(null) }
  val hashCode = hashCode()
  check(previousHashCode == null || hashCode == previousHashCode) { NEW_PRODUCER_ERROR_MESSAGE }
  previousHashCode = hashCode
  val dataState = remember(chart.id) { CartesianChartDataState() }
  val extraStore = remember(chart.id) { MutableExtraStore() }
  val isInPreview = LocalInspectionMode.current
  val scope = rememberCoroutineScope { getCoroutineContext(isInPreview) }
  val chartState = rememberWrappedValue(chart)
  LaunchRegistration(chart.id, animateIn, isInPreview) {
    var mainAnimationJob: Job? = null
    var animationFrameJob: Job? = null
    var finalAnimationFrameJob: Job? = null
    var isAnimationRunning: Boolean
    var isAnimationFrameGenerationRunning = false
    val startAnimation: (transformModel: suspend (key: Any, fraction: Float) -> Unit) -> Unit =
      { transformModel ->
        if (animationSpec != null && !isInPreview && (dataState.value.model != null || animateIn)) {
          isAnimationRunning = true
          mainAnimationJob =
            scope.launch {
              animate(
                initialValue = Animation.range.start,
                targetValue = Animation.range.endInclusive,
                animationSpec = animationSpec,
              ) { fraction, _ ->
                when {
                  !isAnimationRunning -> return@animate
                  !isAnimationFrameGenerationRunning -> {
                    isAnimationFrameGenerationRunning = true
                    animationFrameJob =
                      scope.launch {
                        transformModel(chartState.value.id, fraction)
                        isAnimationFrameGenerationRunning = false
                      }
                  }
                  fraction == 1f -> {
                    finalAnimationFrameJob =
                      scope.launch(Dispatchers.Default) {
                        animationFrameJob?.cancelAndJoin()
                        transformModel(chartState.value.id, fraction)
                        isAnimationFrameGenerationRunning = false
                      }
                  }
                }
              }
            }
        } else {
          finalAnimationFrameJob =
            scope.launch { transformModel(chartState.value.id, Animation.range.endInclusive) }
        }
      }
    scope.launch {
      registerForUpdates(
        key = chartState.value.id,
        cancelAnimation = {
          mainAnimationJob?.cancelAndJoin()
          animationFrameJob?.cancelAndJoin()
          finalAnimationFrameJob?.cancelAndJoin()
          isAnimationRunning = false
          isAnimationFrameGenerationRunning = false
        },
        startAnimation = startAnimation,
        prepareForTransformation = { model, extraStore, ranges ->
          chartState.value.prepareForTransformation(model, extraStore, ranges)
        },
        transform = { extraStore, fraction -> chartState.value.transform(extraStore, fraction) },
        hostExtraStore = extraStore,
        updateRanges = { model ->
          ranges.reset()
          if (model != null) {
            chartState.value.updateRanges(ranges, model)
            ranges.toImmutable()
          } else {
            CartesianChartRanges.Empty
          }
        },
      ) { model, ranges, extraStore ->
        dataState.set(model, ranges, extraStore)
      }
    }
    return@LaunchRegistration {
      mainAnimationJob?.cancel()
      animationFrameJob?.cancel()
      finalAnimationFrameJob?.cancel()
      unregisterFromUpdates(chartState.value.id)
    }
  }
  return dataState
}

@Composable
private fun LaunchRegistration(
  chartID: UUID,
  animateIn: Boolean,
  isInPreview: Boolean,
  block: () -> () -> Unit,
) {
  if (isInPreview) {
    runBlocking(getCoroutineContext(isPreview = true)) { block() }
  } else {
    LaunchedEffect(chartID, animateIn) {
      withContext(getCoroutineContext(isPreview = false)) {
        val disposable = block()
        currentCoroutineContext().job.invokeOnCompletion { disposable() }
      }
    }
  }
}

private fun getCoroutineContext(isPreview: Boolean): CoroutineContext =
  if (isPreview) Dispatchers.Unconfined + PreviewContext else EmptyCoroutineContext
