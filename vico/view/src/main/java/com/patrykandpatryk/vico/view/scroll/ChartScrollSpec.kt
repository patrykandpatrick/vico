/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatryk.vico.view.scroll

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.patrykandpatryk.vico.core.Animation
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.scroll.AutoScrollCondition
import com.patrykandpatryk.vico.core.scroll.InitialScroll
import com.patrykandpatryk.vico.core.scroll.ScrollHandler

/**
 * Houses scrolling-related settings for charts.
 *
 * @property isScrollEnabled whether horizontal scrolling is enabled.
 * @property initialScroll represents the chart’s initial scroll position.
 * @property autoScrollCondition defines when an automatic scroll should be performed.
 * @property autoScrollInterpolator the [TimeInterpolator] to use for automatic scrolling.
 * @property autoScrollDuration the animation duration for automatic scrolling.
 */
public class ChartScrollSpec<in Model : ChartEntryModel>(
    public val isScrollEnabled: Boolean = true,
    public val initialScroll: InitialScroll = InitialScroll.Start,
    public val autoScrollCondition: AutoScrollCondition<Model> = AutoScrollCondition.Never,
    public val autoScrollInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    public val autoScrollDuration: Long = Animation.DIFF_DURATION.toLong(),
) {
    private val animator: ValueAnimator = ValueAnimator.ofFloat(
        Animation.range.start,
        Animation.range.endInclusive,
    ).apply {
        duration = autoScrollDuration
        interpolator = autoScrollInterpolator
    }

    /**
     * Performs an automatic scroll.
     */
    public fun performAutoScroll(
        model: Model,
        oldModel: Model?,
        scrollHandler: ScrollHandler,
    ) {
        if (!autoScrollCondition.shouldPerformAutoScroll(model, oldModel)) return
        with(receiver = animator) {
            cancel()
            removeAllUpdateListeners()
            addUpdateListener {
                scrollHandler.handleScroll(
                    targetScroll = when (initialScroll) {
                        InitialScroll.Start -> (1 - it.animatedFraction) * scrollHandler.currentScroll
                        InitialScroll.End ->
                            scrollHandler.currentScroll + it.animatedFraction *
                                (scrollHandler.maxScrollDistance - scrollHandler.currentScroll)
                    },
                )
            }
            start()
        }
    }
}

/**
 * Copies this [ChartScrollSpec], changing select values.
 */
public fun <Model : ChartEntryModel> ChartScrollSpec<Model>.copy(
    isScrollEnabled: Boolean = this.isScrollEnabled,
    initialScroll: InitialScroll = this.initialScroll,
    autoScrollCondition: AutoScrollCondition<Model> = this.autoScrollCondition,
    autoScrollInterpolator: TimeInterpolator = this.autoScrollInterpolator,
    autoScrollDuration: Long = this.autoScrollDuration,
): ChartScrollSpec<Model> = ChartScrollSpec(
    isScrollEnabled = isScrollEnabled,
    initialScroll = initialScroll,
    autoScrollCondition = autoScrollCondition,
    autoScrollInterpolator = autoScrollInterpolator,
    autoScrollDuration = autoScrollDuration,
)
