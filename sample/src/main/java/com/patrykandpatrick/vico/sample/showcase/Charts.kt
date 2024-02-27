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

package com.patrykandpatrick.vico.sample.showcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.sample.showcase.charts.Chart1
import com.patrykandpatrick.vico.sample.showcase.charts.Chart2
import com.patrykandpatrick.vico.sample.showcase.charts.Chart3
import com.patrykandpatrick.vico.sample.showcase.charts.Chart4
import com.patrykandpatrick.vico.sample.showcase.charts.Chart5
import com.patrykandpatrick.vico.sample.showcase.charts.Chart6
import com.patrykandpatrick.vico.sample.showcase.charts.Chart7
import com.patrykandpatrick.vico.sample.showcase.charts.Chart8
import com.patrykandpatrick.vico.sample.showcase.charts.Chart9

internal val charts =
    listOf<@Composable (ChartViewModel, UISystem, Modifier) -> Unit>(
        { _, uiSystem, modifier -> Chart1(uiSystem, modifier) },
        { _, uiSystem, modifier -> Chart2(uiSystem, modifier) },
        { viewModel, uiSystem, modifier -> Chart3(viewModel.modelProducer1, uiSystem, modifier) },
        { viewModel, uiSystem, modifier -> Chart4(viewModel.modelProducer3, uiSystem, modifier) },
        { viewModel, uiSystem, modifier -> Chart5(viewModel.modelProducer4, uiSystem, modifier) },
        { viewModel, uiSystem, modifier -> Chart6(viewModel.modelProducer4, uiSystem, modifier) },
        { viewModel, uiSystem, modifier -> Chart7(viewModel.modelProducer5, uiSystem, modifier) },
        { viewModel, uiSystem, modifier -> Chart8(viewModel.modelProducer3, uiSystem, modifier) },
        { viewModel, uiSystem, modifier -> Chart9(viewModel.modelProducer6, uiSystem, modifier) },
    )
