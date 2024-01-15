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

package com.patrykandpatrick.vico.compose.chart.entry

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalInspectionMode
import com.patrykandpatrick.vico.compose.state.ChartEntryModelWrapper
import com.patrykandpatrick.vico.compose.state.ChartEntryModelWrapperState
import com.patrykandpatrick.vico.core.Animation
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.chart.values.toChartValuesProvider
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.entry.diff.MutableExtraStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * The default [AnimationSpec] for difference animations.
 *
 * @see collectAsState
 */
public val defaultDiffAnimationSpec: AnimationSpec<Float> = tween(durationMillis = Animation.DIFF_DURATION)

/**
 * Observes the data provided by this [ChartModelProducer] and launches an animation for each [ChartEntryModel] update.
 *
 * @see ChartModelProducer
 */
@Composable
public fun <Model : ChartEntryModel> ChartModelProducer<Model>.collectAsState(
    chart: Chart<Model>,
    producerKey: Any,
    animationSpec: AnimationSpec<Float>? = defaultDiffAnimationSpec,
    runInitialAnimation: Boolean = true,
    chartValuesManager: ChartValuesManager,
    getXStep: ((Model) -> Float)?,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): State<ChartEntryModelWrapper<Model>> {
    val chartEntryModelWrapperState = remember(chart, producerKey) { ChartEntryModelWrapperState<Model>() }
    val modelTransformerProvider = remember(chart) { chart.modelTransformerProvider }
    val extraStore = remember(chart) { MutableExtraStore() }
    val scope = rememberCoroutineScope()
    val isInPreview = LocalInspectionMode.current
    DisposableEffect(chart, producerKey, runInitialAnimation, isInPreview) {
        var mainAnimationJob: Job? = null
        var animationFrameJob: Job? = null
        var finalAnimationFrameJob: Job? = null
        var isAnimationRunning: Boolean
        var isAnimationFrameGenerationRunning = false
        val startAnimation: (transformModel: suspend (key: Any, fraction: Float) -> Unit) -> Unit = { transformModel ->
            if (animationSpec != null && !isInPreview &&
                (chartEntryModelWrapperState.value.chartEntryModel != null || runInitialAnimation)
            ) {
                isAnimationRunning = true
                mainAnimationJob = scope.launch(dispatcher) {
                    animate(
                        initialValue = Animation.range.start,
                        targetValue = Animation.range.endInclusive,
                        animationSpec = animationSpec,
                    ) { fraction, _ ->
                        when {
                            !isAnimationRunning -> return@animate
                            !isAnimationFrameGenerationRunning -> {
                                isAnimationFrameGenerationRunning = true
                                animationFrameJob = scope.launch(dispatcher) {
                                    transformModel(chart, fraction)
                                    isAnimationFrameGenerationRunning = false
                                }
                            }
                            fraction == 1f -> {
                                finalAnimationFrameJob = scope.launch(dispatcher) {
                                    animationFrameJob?.cancelAndJoin()
                                    transformModel(chart, fraction)
                                    isAnimationFrameGenerationRunning = false
                                }
                            }
                        }
                    }
                }
            } else {
                finalAnimationFrameJob = scope.launch(dispatcher) {
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
                getOldModel = { chartEntryModelWrapperState.value.chartEntryModel },
                modelTransformerProvider = modelTransformerProvider,
                extraStore = extraStore,
                updateChartValues = { model ->
                    chartValuesManager.resetChartValues()
                    if (model != null) {
                        chart.updateChartValues(chartValuesManager, model, getXStep?.invoke(model))
                        chartValuesManager.toChartValuesProvider()
                    } else {
                        ChartValuesProvider.Empty
                    }
                },
            ) { chartEntryModel, chartValuesProvider ->
                chartEntryModelWrapperState.set(chartEntryModel, chartValuesProvider)
            }
        }
        onDispose {
            mainAnimationJob?.cancel()
            animationFrameJob?.cancel()
            finalAnimationFrameJob?.cancel()
            unregisterFromUpdates(chart)
        }
    }
    return chartEntryModelWrapperState
}
