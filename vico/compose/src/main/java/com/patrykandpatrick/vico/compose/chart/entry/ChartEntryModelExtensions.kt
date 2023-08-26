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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.patrykandpatrick.vico.compose.state.MutableSharedState
import com.patrykandpatrick.vico.compose.state.mutableSharedStateOf
import com.patrykandpatrick.vico.core.Animation
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.diff.MutableDrawingModelStore
import com.patrykandpatrick.vico.core.extension.doubled
import com.patrykandpatrick.vico.core.extension.half
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * The default [AnimationSpec] for difference animations.
 *
 * @see collect
 */
public val defaultDiffAnimationSpec: AnimationSpec<Float> = tween(
    durationMillis = Animation.DIFF_DURATION * 5,
    easing = { fraction ->
        if (fraction >= .5f) {
            .5f + FastOutSlowInEasing.transform((fraction - .5f).doubled).half
        } else {
            FastOutSlowInEasing.transform(fraction.doubled).half
        }
    },
)

/**
 * Observes the data provided by this [ChartModelProducer] and launches an animation for each [ChartEntryModel] update.
 *
 * @see ChartModelProducer
 */
@Composable
public fun <Model : ChartEntryModel> ChartModelProducer<Model>.collect(
    chart: Chart<Model>,
    producerKey: Any,
    animationSpec: AnimationSpec<Float>? = defaultDiffAnimationSpec,
    runInitialAnimation: Boolean = true,
): Model? = collectAsState(
    chart = chart,
    producerKey = producerKey,
    animationSpec = animationSpec,
    runInitialAnimation = runInitialAnimation,
).value

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
): MutableSharedState<Model?, Model?> {
    val model: MutableSharedState<Model?, Model?> = remember(key1 = chart, key2 = producerKey) {
        mutableSharedStateOf(null)
    }

    val modelTransformerProvider = remember(chart) { chart.modelTransformerProvider }
    val drawingModelStore = remember(chart) { MutableDrawingModelStore() }

    val scope = rememberCoroutineScope()
    val animationJobs = remember { mutableMapOf<Any, Job>() }
    val animationRunningStates = remember { mutableMapOf<Any, Boolean>() }
    DisposableEffect(key1 = chart, key2 = producerKey) {
        val afterUpdate = { producerKey: Any, progressModel: (chartKey: Any, progress: Float) -> Unit ->
            if (animationSpec != null && (model.value != null || runInitialAnimation)) {
                animationRunningStates[producerKey] = false
                animationJobs[producerKey] = scope.launch {
                    animate(
                        initialValue = Animation.range.start,
                        targetValue = Animation.range.endInclusive,
                        animationSpec = animationSpec,
                    ) { value, _ ->
                        if (animationRunningStates[producerKey] == false) {
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
            cancelProgressAnimation = { producerKey ->
                runBlocking { animationJobs[producerKey]?.cancelAndJoin() }
                animationRunningStates[producerKey] = true
            },
            startProgressAnimation = afterUpdate,
            getOldModel = { model.value },
            modelTransformerProvider = modelTransformerProvider,
            drawingModelStore = drawingModelStore,
        ) { updatedModel ->
            model.value = updatedModel
        }
        onDispose {
            unregisterFromUpdates(chart)
            animationJobs.clear()
            animationRunningStates.clear()
        }
    }
    return model
}

/**
 * Combines two [ChartEntryModel] implementations—the receiver and [other]—into a [ComposedChartEntryModel].
 */
@Deprecated("Use `com.patrykandpatrick.vico.core.entry.composed.plus` instead.")
public operator fun <Model : ChartEntryModel> Model.plus(other: Model): ComposedChartEntryModel<Model> =
    ComposedChartEntryModelProducer.composedChartEntryModelOf(listOf(this, other))
