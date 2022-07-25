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

package com.patrykandpatryk.vico.sample.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.sample.util.SampleChart
import com.patrykandpatryk.vico.sample.util.Tab

@Composable
@OptIn(ExperimentalMaterialApi::class)
internal fun ChartPager(
    state: SwipeableState<Int>,
    itemCount: Int,
    sampleCharts: List<SampleChart>,
    tab: Tab,
) {
    HorizontalPager(
        state = state,
        itemCount = itemCount,
        above = {
            Crossfade(targetState = state.currentValue) { targetState ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                ) {
                    Text(
                        text = stringResource(id = sampleCharts[targetState].labelResourceId),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(id = sampleCharts[targetState].descriptionResourceId),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        below = {
            SwipeHint(
                currentPage = state.currentValue + 1,
                pageCount = itemCount,
            )
        },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(size = 20.dp),
                )
                .padding(16.dp)
                .height(220.dp)
                .fillMaxWidth(),
        ) {
            with(sampleCharts[it]) {
                when (tab) {
                    Tab.Compose -> composeBased()
                    Tab.Views -> viewBased()
                }
            }
        }
    }
}
