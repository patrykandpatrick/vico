/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.sample.app

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ChartListScreen(navController: NavController) {
  val uiFrameworks = Charts.all.keys.toList()
  var uiFramework by rememberSaveable { mutableStateOf(uiFrameworks[0]) }
  val charts = Charts.all.getValue(uiFramework)
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        title = { Text("Vico") },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
      )
    },
    containerColor = Color.Transparent,
  ) { paddingValues ->
    LazyColumn(
      contentPadding = paddingValues,
      verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
      if (uiFrameworks.size > 1) {
        item {
          ButtonGroup(
            overflowIndicator = ButtonGroupDefaults::OverflowIndicator,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
          ) {
            uiFrameworks.forEachIndexed { index, segmentUIFramework ->
              customItem(
                buttonGroupContent = {
                  val interactionSource = remember { MutableInteractionSource() }
                  ToggleButton(
                    checked = uiFramework == segmentUIFramework,
                    onCheckedChange = { uiFramework = segmentUIFramework },
                    modifier = Modifier.weight(1f).animateWidth(interactionSource),
                    shapes = connectedButtonShapes(index, uiFrameworks.size),
                    interactionSource = interactionSource,
                  ) {
                    Text(segmentUIFramework.label)
                  }
                },
                menuContent = { menuState ->
                  DropdownMenuItem(
                    text = { Text(segmentUIFramework.label) },
                    onClick = {
                      uiFramework = segmentUIFramework
                      menuState.dismiss()
                    },
                  )
                },
              )
            }
          }
        }
      }
      items(charts.size) { chartID ->
        SegmentedListItem(
          onClick = { navController.navigate(Destination.Chart(uiFramework.ordinal, chartID)) },
          shapes = ListItemDefaults.segmentedShapes(chartID, charts.size),
          colors =
            ListItemDefaults.segmentedColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
          Text(charts[chartID].details.title)
        }
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun connectedButtonShapes(index: Int, count: Int): ToggleButtonShapes =
  when {
    count == 1 -> ToggleButtonDefaults.shapes()
    index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
    index == count - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
  }
