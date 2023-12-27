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

package com.patrykandpatrick.vico.sample.showcase

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.sample.showcase.charts.Chart1
import com.patrykandpatrick.vico.sample.showcase.charts.Chart10
import com.patrykandpatrick.vico.sample.showcase.charts.Chart2
import com.patrykandpatrick.vico.sample.showcase.charts.Chart3
import com.patrykandpatrick.vico.sample.showcase.charts.Chart4
import com.patrykandpatrick.vico.sample.showcase.charts.Chart5
import com.patrykandpatrick.vico.sample.showcase.charts.Chart6
import com.patrykandpatrick.vico.sample.showcase.charts.Chart7
import com.patrykandpatrick.vico.sample.showcase.charts.Chart8
import com.patrykandpatrick.vico.sample.showcase.charts.Chart9
import com.patrykandpatrick.vico.sample.showcase.charts.PieChart1
import com.patrykandpatrick.vico.sample.utils.plus

@Composable
internal fun ShowcaseScreen(viewModel: ShowcaseViewModel = viewModel()) {
    val composeShowcaseState = rememberLazyListState()
    val viewShowcaseState = rememberLazyListState()
    Scaffold(
        bottomBar = {
            NavigationBar {
                UISystem.entries.forEach { uiSystem ->
                    NavigationBarItem(
                        selected = viewModel.uiSystem == uiSystem,
                        onClick = { viewModel.setUISystem(uiSystem) },
                        icon = {
                            Icon(
                                painterResource(uiSystem.iconResourceID),
                                stringResource(uiSystem.labelResourceID),
                            )
                        },
                        label = { Text(stringResource(uiSystem.labelResourceID)) },
                    )
                }
            }
        },
    ) { paddingValues ->
        Crossfade(viewModel.uiSystem) { uiSystem ->
            LazyColumn(
                state =
                    when (uiSystem) {
                        UISystem.Compose -> composeShowcaseState
                        UISystem.Views -> viewShowcaseState
                    },
                contentPadding = paddingValues + PaddingValues(padding),
                verticalArrangement = Arrangement.spacedBy(padding),
            ) {
                chartItems(uiSystem, viewModel)
            }
        }
    }
}

private fun LazyListScope.chartItems(
    uiSystem: UISystem,
    viewModel: ShowcaseViewModel,
) {
    cardItem { Chart1(uiSystem, viewModel.modelProducer1) }
    cardItem { Chart2(uiSystem, viewModel.modelProducer2) }
    cardItem { Chart3(uiSystem, viewModel.modelProducer1) }
    cardItem { Chart4(uiSystem, viewModel.modelProducer3) }
    cardItem { Chart5(uiSystem, viewModel.modelProducer4) }
    cardItem { Chart6(uiSystem, viewModel.modelProducer4) }
    cardItem { Chart7(uiSystem, viewModel.modelProducer5) }
    cardItem { Chart8(uiSystem, viewModel.modelProducer3) }
    cardItem { Chart9(uiSystem, viewModel.modelProducer6) }
    cardItem { Chart10(uiSystem, viewModel.modelProducer10) }
    cardItem { PieChart1(uiSystem, viewModel.pieModelProducer1) }
}

private fun LazyListScope.cardItem(content: @Composable () -> Unit) {
    item {
        Card(shape = MaterialTheme.shapes.large, colors = CardDefaults.elevatedCardColors()) {
            Box(Modifier.padding(padding)) {
                content()
            }
        }
    }
}

private val padding = 16.dp
