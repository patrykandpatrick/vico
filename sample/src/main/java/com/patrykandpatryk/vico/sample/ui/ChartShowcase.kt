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

package com.patrykandpatryk.vico.sample.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.patrykandpatryk.vico.sample.util.SampleChart
import com.patrykandpatryk.vico.sample.util.Tab

@Composable
@OptIn(ExperimentalMaterialApi::class)
internal fun ChartShowcase(
    sampleCharts: List<SampleChart>,
    state: SwipeableState<Int>,
    tab: Tab,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        ChartPager(
            state = state,
            itemCount = sampleCharts.size,
            sampleCharts = sampleCharts,
            tab = tab,
        )
    }
}
