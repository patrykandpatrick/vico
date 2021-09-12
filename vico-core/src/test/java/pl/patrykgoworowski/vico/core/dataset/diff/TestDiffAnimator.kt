/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.dataset.diff

import android.animation.TimeInterpolator
import pl.patrykgoworowski.vico.core.dataset.entry.collection.diff.DiffAnimator

public class TestDiffAnimator : DiffAnimator {

    private var onProgress: ((progress: Float) -> Unit)? = null
    override var currentProgress: Float = 0f
    override lateinit var animationInterpolator: TimeInterpolator

    override var animationDuration: Long = 0

    override fun start(onProgress: (progress: Float) -> Unit) {
        this.onProgress = onProgress
        onProgress(currentProgress)
    }

    fun updateProgress(progress: Float) {
        onProgress?.invoke(progress)
    }

    override fun cancel() = Unit
}
