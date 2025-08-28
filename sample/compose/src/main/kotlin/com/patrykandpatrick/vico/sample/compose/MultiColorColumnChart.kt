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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
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
private fun MultiColorColumnChart(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  // Track column colors - each x-coordinate maps to a color index
  var columnColors by remember { mutableStateOf(mapOf<Double, Int>()) }
  var clickInfo by remember { mutableStateOf<String?>(null) }
  
  // Define a palette of colors
  val colorPalette = listOf(
    Color(0xFF2196F3), // Blue (default)
    Color(0xFFFF5722), // Deep Orange
    Color(0xFF4CAF50), // Green
    Color(0xFF9C27B0), // Purple
    Color(0xFFFFC107), // Amber
    Color(0xFFF44336), // Red
    Color(0xFF00BCD4), // Cyan
    Color(0xFF795548), // Brown
  )
  
  // Create line components for each color
  val lineComponents = remember(colorPalette) {
    colorPalette.map { color ->
      LineComponent(
        fill(color), 
        Defaults.COLUMN_WIDTH,
        margins = com.patrykandpatrick.vico.core.common.Insets.Zero
      )
    }
  }
  
  // Create dynamic column provider that cycles through colors
  val dynamicColumnProvider = remember(columnColors, lineComponents) {
    object : ColumnCartesianLayer.ColumnProvider {
      override fun getColumn(
        entry: ColumnCartesianLayerModel.Entry,
        seriesIndex: Int,
        extraStore: ExtraStore,
      ): LineComponent {
        val colorIndex = columnColors[entry.x] ?: 0
        return lineComponents[colorIndex]
      }
      
      override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore): LineComponent {
        return lineComponents[0]
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
            val currentColorIndex = columnColors[x] ?: 0
            val nextColorIndex = (currentColorIndex + 1) % colorPalette.size
            
            columnColors = columnColors.toMutableMap().apply {
              put(x, nextColorIndex)
            }
            
            val colorName = when (nextColorIndex) {
              0 -> "Blue"
              1 -> "Orange"
              2 -> "Green"
              3 -> "Purple"
              4 -> "Amber"
              5 -> "Red"
              6 -> "Cyan"
              7 -> "Brown"
              else -> "Unknown"
            }
            
            clickInfo = "Column at X=$x (Value=${entry.y}) changed to $colorName"
          }
        ),
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis = HorizontalAxis.rememberBottom(),
      ),
      modelProducer = modelProducer,
      modifier = Modifier.fillMaxWidth(),
    )
    
    Text(
      text = "🎨 Multi-Color Column Chart Demo",
      modifier = Modifier.padding(16.dp),
      fontWeight = FontWeight.Bold,
    )
    
    Text(
      text = "Tap any column to cycle through colors!\nEach click changes the column to the next color in the palette.",
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    
    // Show color legend
    Text(
      text = "Color Palette: Blue → Orange → Green → Purple → Amber → Red → Cyan → Brown → (repeat)",
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      color = Color.Gray,
    )
    
    clickInfo?.let { info ->
      Text(
        text = "Last Action: $info",
        modifier = Modifier.padding(16.dp),
        color = Color(0xFF4CAF50),
        fontWeight = FontWeight.Medium,
      )
    }
    
    if (columnColors.isNotEmpty()) {
      val coloredColumns = columnColors.entries.sortedBy { it.key }
      val colorText = coloredColumns.joinToString(", ") { (x, colorIndex) ->
        val colorName = when (colorIndex) {
          0 -> "Blue"
          1 -> "Orange"
          2 -> "Green"
          3 -> "Purple"
          4 -> "Amber"
          5 -> "Red"
          6 -> "Cyan"
          7 -> "Brown"
          else -> "Unknown"
        }
        "X=$x: $colorName"
      }
      
      Text(
        text = "Customized Columns: $colorText",
        modifier = Modifier.padding(16.dp),
        color = Color(0xFF666666),
      )
    }
  }
}

@Composable
fun JetpackComposeMultiColorColumnChart(modifier: Modifier = Modifier) {
  MultiColorColumnChart(modifier)
}

@Composable
fun MultiColorColumnChart(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      columnSeries { series(8, 12, 6, 15, 9, 18, 5, 11, 14, 7, 16, 4, 13, 10, 12, 8) }
    }
  }
  MultiColorColumnChart(modelProducer, modifier)
}

@Composable
@Preview
private fun Preview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  runBlocking {
    modelProducer.runTransaction {
      columnSeries { series(8, 12, 6, 15, 9, 18, 5, 11, 14, 7, 16, 4, 13, 10, 12, 8) }
    }
  }
  PreviewBox { MultiColorColumnChart(modelProducer) }
}