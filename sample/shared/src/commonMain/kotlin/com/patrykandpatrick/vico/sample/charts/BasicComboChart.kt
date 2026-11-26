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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent

@Composable
fun ComposeBasicComboChart(modifier: Modifier = Modifier) {
  val columnModel = remember {
    // Learn more: https://patrykandpatrick.com/3aqy4o.
    ColumnCartesianLayerModel.build {
      series(4, 15, 5, 8, 10, 15, 9, 10, 7, 9, 10, 12, 2, 9, 5, 14)
    }
  }
  val lineModel = remember {
    // Learn more: https://patrykandpatrick.com/z5ah6v.
    LineCartesianLayerModel.build { series(1, 5, 4, 7, 3, 14, 5, 9, 9, 14, 7, 13, 14, 4, 10, 12) }
  }
  CartesianChartHost(
    rememberCartesianChart(
      rememberColumnCartesianLayer(
        columnModel,
        ColumnCartesianLayer.ColumnProvider.series(
          rememberLineComponent(fill = Fill(Color(0xffffc002)), thickness = 16.dp),
        ),
      ),
      rememberLineCartesianLayer(
        lineModel,
        LineCartesianLayer.LineProvider.series(
          LineCartesianLayer.rememberLine(
            LineCartesianLayer.LineFill.single(Fill(Color(0xffee2b2b))),
          ),
        ),
      ),
      startAxis = VerticalAxis.rememberStart(),
      bottomAxis = HorizontalAxis.rememberBottom(),
    ),
    modifier,
  )
}

@Composable
@Preview
fun ComposeBasicComboChartPreview() {
  PreviewBox { ComposeBasicComboChart() }
}
