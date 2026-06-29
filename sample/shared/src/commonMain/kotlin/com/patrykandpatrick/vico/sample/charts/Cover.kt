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

package com.patrykandpatrick.vico.sample.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview(widthDp = 1800, heightDp = 1800)
internal fun Cover() {
  CompositionLocalProvider(LocalMarkersEnabled provides false) {
    Box(contentAlignment = Alignment.Center) {
      Row(Modifier.background(Color(0xfff2f2f3)).padding(48.dp), Arrangement.spacedBy(24.dp)) {
        Column {
          Card { ComposeBasicColumnChartPreview() }
          Card { ComposeElectricCarSalesPreview() }
          Card { ComposeBasicPieChartPreview() }
        }
        Column {
          Card { ComposeAITestScoresPreview() }
          Card { ComposeRockMetalRatiosPreview() }
        }
        Column {
          Card { ComposeBasicLineChartPreview() }
          Card { ComposeBasicComboChartPreview() }
          Card { ComposeTemperatureAnomaliesPreview() }
        }
        Column {
          Card { ComposeDailyDigitalMediaUsePreview() }
          Card { ComposeGoldPricesPreview() }
        }
      }
    }
  }
}

@Composable
private fun RowScope.Column(content: @Composable ColumnScope.() -> Unit) {
  Column(
    modifier = Modifier.weight(1f),
    verticalArrangement = Arrangement.spacedBy(24.dp),
    content = content,
  )
}

@Composable
private fun Card(content: @Composable BoxScope.() -> Unit) {
  Box(
    modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color.White).padding(8.dp),
    content = content,
  )
}
