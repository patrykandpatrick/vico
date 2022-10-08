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

package com.patrykandpatryk.vico.core.scroll

import android.animation.TimeInterpolator
import android.view.animation.AccelerateDecelerateInterpolator
import com.patrykandpatryk.vico.core.entry.ChartEntryModel

public class ChartScrollSpec<in Model : ChartEntryModel>(
    public val isScrollEnabled: Boolean = true,
    public val initialScroll: InitialScroll = InitialScroll.Start,
    public val autoScrollCondition: AutoScrollCondition<Model> = AutoScrollCondition.Never,
    public val autoScrollInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
) {
    public fun performAutoScroll() { /* TODO */ }
}

public fun <Model : ChartEntryModel> ChartScrollSpec<Model>.copy(
    isScrollEnabled: Boolean = this.isScrollEnabled,
    initialScroll: InitialScroll = this.initialScroll,
    autoScrollCondition: AutoScrollCondition<Model> = this.autoScrollCondition,
    autoScrollInterpolator: TimeInterpolator = this.autoScrollInterpolator,
): ChartScrollSpec<Model> = ChartScrollSpec(
    isScrollEnabled = isScrollEnabled,
    initialScroll = initialScroll,
    autoScrollCondition = autoScrollCondition,
    autoScrollInterpolator = autoScrollInterpolator,
)
