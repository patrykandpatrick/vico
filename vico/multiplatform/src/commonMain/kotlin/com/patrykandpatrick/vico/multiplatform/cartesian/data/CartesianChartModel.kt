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

@file:OptIn(ExperimentalUuidApi::class)

package com.patrykandpatrick.vico.multiplatform.cartesian.data

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalInspectionMode
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChart
import com.patrykandpatrick.vico.multiplatform.common.Animation
import com.patrykandpatrick.vico.multiplatform.common.NEW_PRODUCER_ERROR_MESSAGE
import com.patrykandpatrick.vico.multiplatform.common.ValueWrapper
import com.patrykandpatrick.vico.multiplatform.common.data.CartesianLayerDrawingModel
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.data.MutableExtraStore
import com.patrykandpatrick.vico.multiplatform.common.gcdWith
import com.patrykandpatrick.vico.multiplatform.common.getValue
import com.patrykandpatrick.vico.multiplatform.common.rememberWrappedValue
import com.patrykandpatrick.vico.multiplatform.common.setValue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** Stores a [CartesianChart]’s data. */
public class CartesianChartModel {
  /** The [CartesianLayerModel]s. */
  public val models: List<CartesianLayerModel>

  /** Identifies this [CartesianChartModel] in terms of the [CartesianLayerModel.id]s. */
  public val id: Int

  /**
   * Expresses the size of this [CartesianChartModel] in terms of the range of the _x_ values
   * covered.
   */
  public val width: Double

  /** Stores auxiliary data, including [CartesianLayerDrawingModel]s. */
  public val extraStore: ExtraStore

  /** Creates a [CartesianChartModel] consisting of the given [CartesianLayerModel]s. */
  public constructor(models: List<CartesianLayerModel>) : this(models, ExtraStore.Empty)

  /** Creates a [CartesianChartModel] consisting of the given [CartesianLayerModel]s. */
  public constructor(vararg models: CartesianLayerModel) : this(models.toList())

  internal constructor(
    models: List<CartesianLayerModel>,
    extraStore: ExtraStore,
  ) : this(
    models = models,
    id = models.map { it.id }.hashCode(),
    width = models.maxOf { it.maxX } - models.minOf { it.minX },
    extraStore = extraStore,
  )

  internal constructor(
    models: List<CartesianLayerModel>,
    id: Int,
    width: Double,
    extraStore: ExtraStore,
  ) {
    this.models = models
    this.id = id
    this.width = width
    this.extraStore = extraStore
  }

  /** Returns the greatest common divisor of the _x_ values’ differences. */
  public fun getXDeltaGcd(): Double =
    models.fold<CartesianLayerModel, Double?>(null) { gcd, layerModel ->
      val layerModelGcd = layerModel.getXDeltaGcd()
      gcd?.gcdWith(layerModelGcd) ?: layerModelGcd
    } ?: 1.0

  /**
   * Creates a copy of this [CartesianChartModel] with the given [ExtraStore], which is also applied
   * to the [CartesianLayerModel]s.
   */
  public fun copy(extraStore: ExtraStore): CartesianChartModel =
    CartesianChartModel(models.map { it.copy(extraStore) }, id, width, extraStore)

  /** Creates an immutable copy of this [CartesianChartModel]. */
  public fun toImmutable(): CartesianChartModel = this

  internal companion object {
    val Empty: CartesianChartModel =
      CartesianChartModel(models = emptyList(), id = 0, width = 0.0, extraStore = ExtraStore.Empty)
  }
}

internal val defaultCartesianDiffAnimationSpec: AnimationSpec<Float> =
  tween(durationMillis = Animation.DIFF_DURATION)

@Composable
internal fun CartesianChartModelProducer.collectAsState(
  chart: CartesianChart,
  animationSpec: AnimationSpec<Float>?,
  animateIn: Boolean,
  ranges: MutableCartesianChartRanges,
): State<CartesianChartModelWrapper> {
  var previousHashCode by remember { ValueWrapper<Int?>(null) }
  val hashCode = hashCode()
  check(previousHashCode == null || hashCode == previousHashCode) { NEW_PRODUCER_ERROR_MESSAGE }
  previousHashCode = hashCode
  val modelWrapperState = remember(chart.id) { CartesianChartModelWrapperState() }
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
        if (
          animationSpec != null &&
            !isInPreview &&
            (modelWrapperState.value.model != null || animateIn)
        ) {
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
      ) { model, ranges ->
        modelWrapperState.set(model, ranges)
      }
    }
    return@LaunchRegistration {
      mainAnimationJob?.cancel()
      animationFrameJob?.cancel()
      finalAnimationFrameJob?.cancel()
      unregisterFromUpdates(chartState.value.id)
    }
  }
  return modelWrapperState
}

@Composable
private fun LaunchRegistration(
  chartID: Uuid,
  animateIn: Boolean,
  isInPreview: Boolean,
  block: () -> () -> Unit,
) {
  if (isInPreview) {
    runBlocking(getCoroutineContext(isPreview = true)) { block() }
  } else {
    DisposableEffect(chartID, animateIn) {
      val disposable = block()
      onDispose { disposable() }
    }
  }
}

private fun getCoroutineContext(isPreview: Boolean): CoroutineContext =
  if (isPreview) Dispatchers.Main + PreviewContext else EmptyCoroutineContext
