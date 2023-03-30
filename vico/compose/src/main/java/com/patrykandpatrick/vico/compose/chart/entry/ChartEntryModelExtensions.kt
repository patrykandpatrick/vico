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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.patrykandpatrick.vico.compose.state.MutableSharedState
import com.patrykandpatrick.vico.compose.state.mutableSharedStateOf
import com.patrykandpatrick.vico.core.Animation
import com.patrykandpatrick.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The default [AnimationSpec] for difference animations.
 *
 * @see collect
 */
public val defaultDiffAnimationSpec: AnimationSpec<Float> = tween(
    durationMillis = Animation.DIFF_DURATION,
)

/**
 * Observes the data provided by this [ChartModelProducer] and launches an animation for each [ChartEntryModel] update.
 *
 * @see ChartModelProducer
 */
@Composable
public fun <Model : ChartEntryModel> ChartModelProducer<Model>.collect(
    chartKey: Any,
    producerKey: Any,
    animationSpec: AnimationSpec<Float>? = defaultDiffAnimationSpec,
    runInitialAnimation: Boolean = true,
): Model? = collectAsState(
    chartKey = chartKey,
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
    chartKey: Any,
    producerKey: Any,
    animationSpec: AnimationSpec<Float>? = defaultDiffAnimationSpec,
    runInitialAnimation: Boolean = true,
): MutableSharedState<Model?, Model?> {
    val model: MutableSharedState<Model?, Model?> = remember(key1 = chartKey, key2 = producerKey) {
        mutableSharedStateOf(null)
    }

    val scope = rememberCoroutineScope()
    DisposableEffect(key1 = chartKey, key2 = producerKey) {
        var animationJob: Job? = null
        val listener = {
            if (animationSpec != null && (model.value != null || runInitialAnimation)) {
                animationJob?.cancel()
                animationJob = scope.launch {
                    animate(
                        initialValue = Animation.range.start,
                        targetValue = Animation.range.endInclusive,
                        animationSpec = animationSpec,
                    ) { value, _ ->
                        if (animationJob?.isActive == true) {
                            progressModel(chartKey, value)
                        }
                    }
                }
            } else {
                progressModel(chartKey, Animation.range.endInclusive)
            }
        }
        registerForUpdates(
            key = chartKey,
            updateListener = listener,
            getOldModel = { model.value },
        ) { updatedModel ->
            model.value = updatedModel
        }
        onDispose { unregisterFromUpdates(chartKey) }
    }
    return model
}

/**
 * Combines two [ChartEntryModel] implementations—the receiver and [other]—into a [ComposedChartEntryModel].
 */
@Deprecated("Use `com.patrykandpatrick.vico.core.entry.composed.plus` instead.")
public operator fun <Model : ChartEntryModel> Model.plus(other: Model): ComposedChartEntryModel<Model> =
    ComposedChartEntryModelProducer.composedChartEntryModelOf(listOf(this, other))
