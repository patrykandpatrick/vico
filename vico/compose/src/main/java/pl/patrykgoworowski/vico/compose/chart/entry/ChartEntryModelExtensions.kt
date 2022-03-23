/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package pl.patrykgoworowski.vico.compose.chart.entry

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.patrykgoworowski.vico.core.Animation
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.entry.ChartModelProducer

/**
 * The default [AnimationSpec] used the animation run on each [ChartEntryModel] update.
 *
 * @see collect
 */
public val defaultDiffAnimationSpec: AnimationSpec<Float> = tween(
    durationMillis = Animation.DIFF_DURATION,
)

/**
 * Observes data provided by [ChartModelProducer] and launches an animation for each [ChartEntryModel] update.
 *
 * @see ChartModelProducer
 */
@Composable
public fun <Model : ChartEntryModel> ChartModelProducer<Model>.collect(
    key: Any,
    animationSpec: AnimationSpec<Float>? = defaultDiffAnimationSpec,
): Model? {
    var model: Model? by remember(key) { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    DisposableEffect(key1 = key) {
        var animationJob: Job? = null
        val listener = {
            if (animationSpec != null) {
                animationJob?.cancel()
                animationJob = scope.launch {
                    animate(
                        initialValue = Animation.range.start,
                        targetValue = Animation.range.endInclusive,
                        animationSpec = animationSpec,
                    ) { value, _ ->
                        if (animationJob?.isActive == true) {
                            progressModel(key, value)
                        }
                    }
                }
            } else {
                progressModel(key, Animation.range.endInclusive)
            }
            model
        }
        registerForUpdates(key, listener) { updatedModel -> model = updatedModel }
        onDispose { unregisterFromUpdates(key) }
    }
    return model
}
