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

package com.patrykandpatryk.vico.compose.chart.scroll

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.scroll.AutoScrollCondition
import com.patrykandpatryk.vico.core.scroll.InitialScroll

@Stable
public class ChartScrollSpec<in Model : ChartEntryModel>(
    public val isScrollEnabled: Boolean,
    public val initialScroll: InitialScroll,
    public val autoScrollCondition: AutoScrollCondition<Model>,
    public val autoScrollAnimationSpec: AnimationSpec<Float>,
) {

    public suspend fun performAutoScroll(
        model: Model,
        oldModel: Model?,
        currentScroll: Float,
        maxScrollDistance: Float,
        scrollableState: ScrollableState,
    ) {
        if (autoScrollCondition.shouldPerformAutoScroll(model, oldModel)) {
            scrollableState.animateScrollBy(
                value = when (initialScroll) {
                    InitialScroll.Start -> 0f
                    InitialScroll.End -> currentScroll - maxScrollDistance
                },
                animationSpec = autoScrollAnimationSpec,
            )
        }
    }
}

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
