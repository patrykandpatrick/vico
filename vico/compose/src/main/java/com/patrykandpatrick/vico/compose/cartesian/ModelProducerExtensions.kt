/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalInspectionMode
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.ChartValues
import com.patrykandpatrick.vico.core.cartesian.MutableChartValues
import com.patrykandpatrick.vico.core.cartesian.model.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.toImmutable
import com.patrykandpatrick.vico.core.common.Animation
import com.patrykandpatrick.vico.core.common.MutableExtraStore
import com.patrykandpatrick.vico.core.common.NEW_PRODUCER_ERROR_MESSAGE
import com.patrykandpatrick.vico.core.common.ValueWrapper
import com.patrykandpatrick.vico.core.common.getValue
import com.patrykandpatrick.vico.core.common.setValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal val defaultCartesianDiffAnimationSpec: AnimationSpec<Float> = tween(durationMillis = Animation.DIFF_DURATION)

@Composable
internal fun CartesianChartModelProducer.collectAsState(
    chart: CartesianChart,
    animationSpec: AnimationSpec<Float>? = defaultCartesianDiffAnimationSpec,
    runInitialAnimation: Boolean = true,
    mutableChartValues: MutableChartValues,
    getXStep: ((CartesianChartModel) -> Float)?,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): State<CartesianChartModelWrapper> {
    var previousHashCode by remember { ValueWrapper<Int?>(null) }
    val hashCode = hashCode()
    check(previousHashCode == null || hashCode == previousHashCode) { NEW_PRODUCER_ERROR_MESSAGE }
    previousHashCode = hashCode
    val modelWrapperState = remember(chart) { CartesianChartModelWrapperState() }
    val extraStore = remember(chart) { MutableExtraStore() }
    val scope = rememberCoroutineScope()
    val isInPreview = LocalInspectionMode.current
    DisposableEffect(chart, runInitialAnimation, isInPreview) {
        var mainAnimationJob: Job? = null
        var animationFrameJob: Job? = null
        var finalAnimationFrameJob: Job? = null
        var isAnimationRunning: Boolean
        var isAnimationFrameGenerationRunning = false
        val startAnimation: (transformModel: suspend (key: Any, fraction: Float) -> Unit) -> Unit = { transformModel ->
            if (animationSpec != null && !isInPreview &&
                (modelWrapperState.value.model != null || runInitialAnimation)
            ) {
                isAnimationRunning = true
                mainAnimationJob =
                    scope.launch(dispatcher) {
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
                                        scope.launch(dispatcher) {
                                            transformModel(chart, fraction)
                                            isAnimationFrameGenerationRunning = false
                                        }
                                }

                                fraction == 1f -> {
                                    finalAnimationFrameJob =
                                        scope.launch(dispatcher) {
                                            animationFrameJob?.cancelAndJoin()
                                            transformModel(chart, fraction)
                                            isAnimationFrameGenerationRunning = false
                                        }
                                }
                            }
                        }
                    }
            } else {
                finalAnimationFrameJob =
                    scope.launch(dispatcher) {
                        transformModel(chart, Animation.range.endInclusive)
                    }
            }
        }
        scope.launch(dispatcher) {
            registerForUpdates(
                key = chart,
                cancelAnimation = {
                    runBlocking {
                        mainAnimationJob?.cancelAndJoin()
                        animationFrameJob?.cancelAndJoin()
                        finalAnimationFrameJob?.cancelAndJoin()
                    }
                    isAnimationRunning = false
                    isAnimationFrameGenerationRunning = false
                },
                startAnimation = startAnimation,
                prepareForTransformation = chart::prepareForTransformation,
                transform = chart::transform,
                extraStore = extraStore,
                updateChartValues = { model ->
                    mutableChartValues.reset()
                    if (model != null) {
                        chart.updateChartValues(mutableChartValues, model, getXStep?.invoke(model))
                        mutableChartValues.toImmutable()
                    } else {
                        ChartValues.Empty
                    }
                },
            ) { model, chartValues ->
                modelWrapperState.set(model, chartValues)
            }
        }
        onDispose {
            mainAnimationJob?.cancel()
            animationFrameJob?.cancel()
            finalAnimationFrameJob?.cancel()
            unregisterFromUpdates(chart)
        }
    }
    return modelWrapperState
}
