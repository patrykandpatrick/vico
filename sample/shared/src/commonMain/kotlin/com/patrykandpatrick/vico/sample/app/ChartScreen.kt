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

import androidx.compose.animation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ChartScreen(navController: NavController, initialChartID: Int) {
  var chartID by rememberSaveable { mutableIntStateOf(initialChartID) }
  val charts = Charts.all
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
          IconButton(onClick = navigateBack) {
            Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
          }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
      )
    },
    bottomBar = {
      Box(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp),
        contentAlignment = Alignment.Center,
      ) {
        ButtonGroup(
          overflowIndicator = ButtonGroupDefaults::OverflowIndicator,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          chartNavigationButton(
            label = "Previous",
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            enabled = chartID > 0,
            onClick = { chartID = (chartID - 1).coerceAtLeast(0) },
          )
          chartNavigationButton(
            label = "Next",
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            enabled = chartID < charts.lastIndex,
            onClick = { chartID = (chartID + 1).coerceAtMost(charts.lastIndex) },
          )
        }
      }
    },
    containerColor = Color.Transparent,
  ) { paddingValues ->
    Box(Modifier.fillMaxSize().padding(paddingValues)) {
      AnimatedContent(
        targetState = chartID,
        transitionSpec = {
          val direction = targetState - initialState
          slideInHorizontally { direction * it / 3 } + fadeIn() togetherWith
            slideOutHorizontally { -direction * it / 3 } + fadeOut()
        },
      ) { chartID ->
        Box(Modifier.fillMaxSize().padding(horizontal = 16.dp), Alignment.Center) {
          charts[chartID]()
        }
      }
      charts[chartID].details.citation?.let { citation ->
        Text(
          text = citation,
          modifier =
            Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
              .align(Alignment.BottomStart),
          fontSize = 12.sp,
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ButtonGroupScope.chartNavigationButton(
  label: String,
  imageVector: ImageVector,
  enabled: Boolean,
  onClick: () -> Unit,
) {
  customItem(
    buttonGroupContent = {
      val interactionSource = remember { MutableInteractionSource() }
      FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.animateWidth(interactionSource),
        enabled = enabled,
        colors =
          IconButtonDefaults.filledTonalIconButtonColors(
            disabledContainerColor = Color.Transparent
          ),
        interactionSource = interactionSource,
      ) {
        Icon(imageVector = imageVector, contentDescription = label)
      }
    },
    menuContent = { menuState ->
      DropdownMenuItem(
        text = { Text(label) },
        onClick = {
          onClick()
          menuState.dismiss()
        },
        enabled = enabled,
        leadingIcon = { Icon(imageVector = imageVector, contentDescription = null) },
      )
    },
  )
}
