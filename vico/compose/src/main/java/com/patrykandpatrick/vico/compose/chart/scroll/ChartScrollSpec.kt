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

package com.patrykandpatrick.vico.compose.chart.scroll

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.scroll.AutoScrollCondition
import com.patrykandpatrick.vico.core.scroll.InitialScroll

/**
 * Houses scrolling-related settings for charts.
 *
 * @property isScrollEnabled whether horizontal scrolling is enabled.
 * @property initialScroll represents the chart’s initial scroll position.
 * @property autoScrollCondition defines when an automatic scroll should be performed.
 * @property autoScrollAnimationSpec the [AnimationSpec] to use for automatic scrolling.
 */
@Stable
public class ChartScrollSpec<in Model : ChartEntryModel>(
    public val isScrollEnabled: Boolean,
    public val initialScroll: InitialScroll,
    public val autoScrollCondition: AutoScrollCondition<Model>,
    public val autoScrollAnimationSpec: AnimationSpec<Float>,
) {

    /**
     * Performs an automatic scroll.
     */
    public suspend fun performAutoScroll(
        model: Model,
        oldModel: Model?,
        chartScrollState: ChartScrollState,
    ) {
        if (autoScrollCondition.shouldPerformAutoScroll(model, oldModel)) {
            if (chartScrollState.isScrollInProgress) {
                chartScrollState.stopScroll(MutatePriority.PreventUserInput)
            }

            chartScrollState.animateScrollBy(
                value = when (initialScroll) {
                    InitialScroll.Start -> -chartScrollState.value
                    InitialScroll.End -> -chartScrollState.value + chartScrollState.maxValue
                },
                animationSpec = autoScrollAnimationSpec,
            )
        }
    }
}

/**
 * Creates and remembers an instance of [ChartScrollSpec].
 */
@Composable
public fun <Model : ChartEntryModel> rememberChartScrollSpec(
    isScrollEnabled: Boolean = true,
    initialScroll: InitialScroll = InitialScroll.Start,
    autoScrollCondition: AutoScrollCondition<Model> = AutoScrollCondition.Never,
    autoScrollAnimationSpec: AnimationSpec<Float> = spring(),
): ChartScrollSpec<Model> = remember(
    isScrollEnabled,
    initialScroll,
    autoScrollCondition,
    autoScrollAnimationSpec,
) {
    ChartScrollSpec(
        isScrollEnabled = isScrollEnabled,
        initialScroll = initialScroll,
        autoScrollCondition = autoScrollCondition,
        autoScrollAnimationSpec = autoScrollAnimationSpec,
    )
}
