/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
internal fun SampleApp() {
  val navController = rememberNavController()
  Theme {
    NavHost(
      navController = navController,
      startDestination = Destination.ChartList,
      modifier = Modifier.background(MaterialTheme.colorScheme.background),
      enterTransition = { slideInHorizontally { it / 3 } + fadeIn() },
      exitTransition = { slideOutHorizontally { -it / 3 } + fadeOut() },
      popEnterTransition = { slideInHorizontally { -it / 3 } + fadeIn() },
      popExitTransition = { slideOutHorizontally { it / 3 } + fadeOut() },
    ) {
      composable<Destination.ChartList> { ChartListScreen(navController) }
      composable<Destination.Chart> { backStackEntry ->
        val destination = backStackEntry.toRoute<Destination.Chart>()
        ChartScreen(navController, destination.uiFrameworkID, destination.initialChartID)
      }
    }
  }
}
