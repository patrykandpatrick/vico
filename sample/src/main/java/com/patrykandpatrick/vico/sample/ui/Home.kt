/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.sample.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.sample.util.Tab
import com.patrykandpatrick.vico.sample.util.rememberSampleCharts
import com.patrykandpatrick.vico.sample.viewmodel.ShowcaseViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
internal fun Home() {
    var tab by remember { mutableStateOf(value = Tab.Compose) }
    val composeShowcaseState = rememberSwipeableState(initialValue = 0)
    val viewShowcaseState = rememberSwipeableState(initialValue = 0)
    val showcaseViewModel = viewModel<ShowcaseViewModel>()
    val sampleCharts = rememberSampleCharts(
        chartEntryModelProducer = showcaseViewModel.chartEntryModelProducer,
        customStepChartEntryModelProducer = showcaseViewModel.customStepChartEntryModelProducer,
        composedChartEntryModelProducer = showcaseViewModel.composedChartEntryModelProducer,
        multiDataSetChartEntryModelProducer = showcaseViewModel.multiDataSetChartEntryModelProducer,
    )
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                Tab.values().forEach {
                    NavigationBarItem(
                        selected = tab == it,
                        onClick = { tab = it },
                        label = { Text(text = stringResource(id = it.labelResourceId)) },
                        icon = {
                            Icon(
                                painter = painterResource(id = it.iconResourceId),
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        Column {
            Crossfade(targetState = tab) {
                ChartShowcase(
                    tab = it,
                    sampleCharts = sampleCharts,
                    state = when (it) {
                        Tab.Compose -> composeShowcaseState
                        Tab.Views -> viewShowcaseState
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues)
                        .statusBarsPadding(),
                )
            }
        }
    }
}
