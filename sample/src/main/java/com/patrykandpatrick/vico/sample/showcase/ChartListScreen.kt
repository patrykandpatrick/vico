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

package com.patrykandpatrick.vico.sample.showcase

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.patrykandpatrick.vico.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChartListScreen(navController: NavController) {
  var uiFramework by rememberSaveable { mutableStateOf(UIFramework.Compose) }
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    {
      TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        scrollBehavior = scrollBehavior,
      )
    },
  ) { paddingValues ->
    LazyColumn(contentPadding = paddingValues) {
      item {
        SingleChoiceSegmentedButtonRow(
          Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
          UIFramework.entries.forEachIndexed { index, segmentUIFramework ->
            SegmentedButton(
              selected = uiFramework == segmentUIFramework,
              onClick = { uiFramework = segmentUIFramework },
              shape = SegmentedButtonDefaults.itemShape(index, UIFramework.entries.size),
            ) {
              Text(stringResource(segmentUIFramework.labelResourceID))
            }
          }
        }
      }
      items(charts.size) { chartID ->
        ListItem(
          { Text(charts[chartID].title) },
          Modifier.clickable { navController.navigate("chart/$chartID/${uiFramework.ordinal}") },
        )
      }
    }
  }
}
