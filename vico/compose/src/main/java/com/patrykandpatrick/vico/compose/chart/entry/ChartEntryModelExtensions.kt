/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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
import com.patrykandpatrick.vico.compose.state.ChartEntryModelState
import com.patrykandpatrick.vico.core.Animation
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.entry.diff.MutableDrawingModelStore
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
): State<Pair<Model?, Model?>> {
    val chartEntryModelState = remember(chart, producerKey) { ChartEntryModelState<Model>() }

    val modelTransformerProvider = remember(chart) { chart.modelTransformerProvider }
    val drawingModelStore = remember(chart) { MutableDrawingModelStore() }

    val scope = rememberCoroutineScope()
    val isInPreview = LocalInspectionMode.current
    var animationJob: Job? = null
    var isAnimationRunning: Boolean?
    DisposableEffect(chart, producerKey, runInitialAnimation, isInPreview) {
        val afterUpdate = { progressModel: (chartKey: Any, progress: Float) -> Unit ->
            if (animationSpec != null && !isInPreview &&
                (chartEntryModelState.value.first != null || runInitialAnimation)
            ) {
                isAnimationRunning = false
                animationJob = scope.launch {
                    animate(
                        initialValue = Animation.range.start,
                        targetValue = Animation.range.endInclusive,
                        animationSpec = animationSpec,
                    ) { value, _ ->
                        if (isAnimationRunning == false) {
                            progressModel(chart, value)
                        }
                    }
                }
            } else {
                progressModel(chart, Animation.range.endInclusive)
            }
        }
        registerForUpdates(
            key = chart,
            cancelAnimation = {
                runBlocking { animationJob?.cancelAndJoin() }
                isAnimationRunning = true
            },
            startAnimation = afterUpdate,
            getOldModel = { chartEntryModelState.value.first },
            modelTransformerProvider = modelTransformerProvider,
            drawingModelStore = drawingModelStore,
            updateChartValues = { model ->
                chartValuesManager.resetChartValues()
                chart.updateChartValues(chartValuesManager, model, getXStep?.invoke(model))
                chartValuesManager
            },
        ) { updatedModel ->
            chartEntryModelState.set(updatedModel)
        }
        onDispose {
            unregisterFromUpdates(chart)
            animationJob = null
            isAnimationRunning = null
        }
    }
    return chartEntryModelState
}
