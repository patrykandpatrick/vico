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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChartScreen(navController: NavController, initialChartID: Int, uiFrameworkID: Int) {
  var chartID by remember { mutableIntStateOf(initialChartID) }
  val charts = Charts.all.getValue(UIFramework.entries[uiFrameworkID])
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  val navigateBack = {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
  }
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        title = { Text(charts[chartID].details.title) },
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
        IconButton(onClick = { chartID-- }, enabled = chartID > 0) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
        IconButton(onClick = { chartID++ }, enabled = chartID < charts.lastIndex) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
      }
    },
  ) { paddingValues ->
    Crossfade(chartID, Modifier.padding(paddingValues)) { chartID ->
      val chart = charts[chartID]
      Box(Modifier.fillMaxSize()) {
        chart.content(Modifier.padding(horizontal = 16.dp).align(Alignment.Center))
        if (chart.details.citation != null) {
          Text(
            text = chart.details.citation,
            modifier =
              Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .align(Alignment.BottomStart),
            fontSize = 12.sp,
          )
        }
      }
    }
  }
}
