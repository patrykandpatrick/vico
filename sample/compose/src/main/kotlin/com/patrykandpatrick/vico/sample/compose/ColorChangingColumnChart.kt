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

package com.patrykandpatrick.vico.sample.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlinx.coroutines.runBlocking

@Composable
private fun ColorChangingColumnChart(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  // Track which columns have been clicked (using x-coordinate as key)
  var clickedColumns by remember { mutableStateOf(setOf<Double>()) }
  var clickedInfo by remember { mutableStateOf<String?>(null) }
  
  // Define colors
  val defaultColor = Color(0xFF2196F3) // Blue
  val clickedColor = Color(0xFFFF5722) // Orange/Red
  
  // Create dynamic column provider that changes colors based on clicks
  val dynamicColumnProvider = remember(clickedColumns, defaultColor, clickedColor) {
    object : ColumnCartesianLayer.ColumnProvider {
      private val defaultComponent = LineComponent(
        fill(defaultColor), 
        Defaults.COLUMN_WIDTH, 
        margins = com.patrykandpatrick.vico.core.common.Insets.Zero
      )
      private val clickedComponent = LineComponent(
        fill(clickedColor), 
        Defaults.COLUMN_WIDTH,
        margins = com.patrykandpatrick.vico.core.common.Insets.Zero
      )
      
      override fun getColumn(
        entry: ColumnCartesianLayerModel.Entry,
        seriesIndex: Int,
        extraStore: ExtraStore,
      ): LineComponent {
        return if (clickedColumns.contains(entry.x)) {
          clickedComponent
        } else {
          defaultComponent
        }
      }
      
      override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore): LineComponent {
        return defaultComponent
      }
    }
  }
  
  Column(modifier = modifier) {
    CartesianChartHost(
      chart = rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnProvider = dynamicColumnProvider,
          onColumnClick = { entry, seriesIndex ->
            val x = entry.x
            clickedColumns = if (clickedColumns.contains(x)) {
              clickedColumns - x
            } else {
              clickedColumns + x
            }
            clickedInfo = "Clicked column at X=$x, Value=${entry.y}. ${
              if (clickedColumns.contains(x)) "Added to" else "Removed from"
            } selection."
          }
        ),
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis = HorizontalAxis.rememberBottom(),
      ),
      modelProducer = modelProducer,
      modifier = Modifier.fillMaxWidth(),
    )
    
    Text(
      text = "Tap columns to change their color!\nBlue = Default, Orange = Selected",
      modifier = Modifier.padding(16.dp),
    )
    
    if (clickedColumns.isNotEmpty()) {
      Text(
        text = "Selected columns: ${clickedColumns.sorted().joinToString(", ")}",
        modifier = Modifier.padding(horizontal = 16.dp),
        color = clickedColor,
      )
    }
    
    clickedInfo?.let { info ->
      Text(
        text = info,
        modifier = Modifier.padding(16.dp),
        color = Color.Gray,
      )
    }
  }
}

@Composable
fun JetpackComposeColorChangingColumnChart(modifier: Modifier = Modifier) {
  ColorChangingColumnChart(modifier)
}

@Composable
fun ColorChangingColumnChart(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      columnSeries { series(5, 8, 3, 12, 7, 15, 4, 9, 11, 6, 14, 2, 13, 10, 8, 5) }
    }
  }
  ColorChangingColumnChart(modelProducer, modifier)
}

@Composable
@Preview
private fun Preview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  runBlocking {
    modelProducer.runTransaction {
      columnSeries { series(5, 8, 3, 12, 7, 15, 4, 9, 11, 6, 14, 2, 13, 10, 8, 5) }
    }
  }
  PreviewBox { ColorChangingColumnChart(modelProducer) }
}