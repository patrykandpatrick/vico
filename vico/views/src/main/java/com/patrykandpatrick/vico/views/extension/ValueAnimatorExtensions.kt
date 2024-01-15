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

package com.patrykandpatrick.vico.views.extension

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator

internal fun ValueAnimator.start(block: (Float) -> Unit) {
    val updateListener = ValueAnimator.AnimatorUpdateListener { block(it.animatedFraction) }
    addUpdateListener(updateListener)
    addListener(
        object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                removeUpdateListener(updateListener)
                removeListener(this)
            }

            override fun onAnimationEnd(animation: Animator) {
                onAnimationCancel(animation)
            }
        },
    )
    start()
}
