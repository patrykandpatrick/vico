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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.patrykandpatrick.vico.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChartScreen(navController: NavController, initialChartID: Int, uiFrameworkID: Int) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  val nestedNavController = rememberNavController()
  val chartID =
    nestedNavController.currentBackStackEntryAsState().value?.arguments?.getInt("chartID")
      ?: initialChartID
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  val navigateBack = {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
  }
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.chart_x, chartID + 1)) },
        navigationIcon = {
          IconButton(navigateBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
          }
        },
        scrollBehavior = scrollBehavior,
      )
    },
    bottomBar = {
      Row(
        Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp),
        Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = { nestedNavController.navigate("${chartID - 1}") },
          enabled = chartID > 0,
        ) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
        IconButton(
          onClick = { nestedNavController.navigate("${chartID + 1}") },
          enabled = chartID < charts.lastIndex,
        ) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
      }
    },
  ) { paddingValues ->
    NavHost(
      navController = nestedNavController,
      startDestination = "{chartID}",
      modifier = Modifier.padding(paddingValues),
    ) {
      composable(
        "{chartID}",
        listOf(
          navArgument("chartID") {
            type = NavType.IntType
            defaultValue = initialChartID
          }
        ),
      ) { backStackEntry ->
        BackHandler(onBack = navigateBack)
        val arguments = requireNotNull(backStackEntry.arguments)
        charts[arguments.getInt("chartID")](
          UIFramework.entries[uiFrameworkID],
          Modifier.padding(horizontal = 16.dp),
        )
      }
    }
  }
}
