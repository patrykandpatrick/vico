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

package pl.patrykgoworowski.vico.core.chart.entry.collection.diff

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator

public class DefaultDiffAnimator(
    animationDuration: Long = DEFAULT_ANIM_DURATION,
    animationInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
) : DiffAnimator {

    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = animationDuration
        interpolator = animationInterpolator

        addUpdateListener {
            onProgress?.invoke(it.animatedFraction)
        }
        addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationCancel(animation: Animator?) {
                onProgress = null
            }

            override fun onAnimationEnd(animation: Animator?) {
                onProgress = null
            }
        })
    }

    private var onProgress: ((progress: Float) -> Unit)? = null

    override val currentProgress: Float
        get() = valueAnimator.animatedFraction

    override var animationInterpolator: TimeInterpolator by valueAnimator::interpolator

    override var animationDuration: Long by valueAnimator::duration

    override fun start(onProgress: (progress: Float) -> Unit) {
        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
        this.onProgress = onProgress
        valueAnimator.start()
    }

    override fun cancel() {
        valueAnimator.cancel()
        onProgress = null
    }

    private companion object {
        const val DEFAULT_ANIM_DURATION = 250L
    }
}
