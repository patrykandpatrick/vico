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

package com.patrykandpatrick.vico.sample

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.patrykandpatrick.vico.sample.showcase.ChartListScreen
import com.patrykandpatrick.vico.sample.showcase.ChartScreen
import com.patrykandpatrick.vico.sample.showcase.ChartViewModel

@Composable
internal fun VicoApp() {
    val navController = rememberNavController()
    val chartViewModel = viewModel<ChartViewModel>()
    VicoTheme {
        NavHost(navController = navController, startDestination = "chartList") {
            composable("chartList") { ChartListScreen(navController) }
            composable(
                "chart/{initialChartID}/{uiSystemID}",
                listOf(
                    navArgument("initialChartID") { type = NavType.IntType },
                    navArgument("uiSystemID") { type = NavType.IntType },
                ),
            ) { backStackEntry ->
                val arguments = requireNotNull(backStackEntry.arguments)
                ChartScreen(
                    chartViewModel,
                    navController,
                    arguments.getInt("initialChartID"),
                    arguments.getInt("uiSystemID"),
                )
            }
        }
    }
}
