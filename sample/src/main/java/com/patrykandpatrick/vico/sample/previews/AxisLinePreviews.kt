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

package com.patrykandpatrick.vico.sample.previews

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEndAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.shape.Corner
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.CutCornerTreatment
import com.patrykandpatrick.vico.core.common.shape.RoundedCornerTreatment
import com.patrykandpatrick.vico.core.common.shape.Shape

private val model = CartesianChartModel(ColumnCartesianLayerModel.build { series(1, 2, 3, 4) })

@Composable
@Preview(showBackground = true, widthDp = 250)
fun HorizontalAxisTextInside() {
  val label =
    rememberAxisLabelComponent(
      background =
        rememberShapeComponent(
          shape =
            CorneredShape(
              topLeft = Corner.Relative(percentage = 50, cornerTreatment = CutCornerTreatment),
              bottomRight =
                Corner.Relative(percentage = 50, cornerTreatment = RoundedCornerTreatment),
            ),
          color = Color.LightGray,
          strokeColor = Color.Gray,
          strokeWidth = 1.dp,
        ),
      padding = Dimensions.of(horizontal = 2.dp, vertical = 8.dp),
      margins = Dimensions.of(horizontal = 4.dp, vertical = 4.dp),
    )
  PaddingValues()
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(),
        startAxis =
          rememberStartAxis(
            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
            label = label,
          ),
        endAxis =
          rememberEndAxis(
            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
            guideline = null,
            label = label,
          ),
      ),
    model = model,
  )
}

@Composable
@Preview(showBackground = true, widthDp = 250)
fun HorizontalAxisTextInsideAndBottomAxis() {
  val label =
    rememberAxisLabelComponent(
      background = rememberShapeComponent(shape = Shape.Pill, color = Color.LightGray),
      padding = Dimensions.of(horizontal = 2.dp, vertical = 8.dp),
      margins = Dimensions.of(horizontal = 4.dp, vertical = 4.dp),
    )
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(),
        startAxis =
          rememberStartAxis(
            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
            label = label,
          ),
        endAxis =
          rememberEndAxis(
            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
            guideline = null,
            label = label,
          ),
        bottomAxis = rememberBottomAxis(),
      ),
    model = model,
  )
}

@Composable
@Preview(showBackground = true, widthDp = 250)
fun HorizontalAxisTextOutside() {
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(),
        startAxis =
          rememberStartAxis(horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside),
        endAxis =
          rememberEndAxis(
            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside,
            guideline = null,
          ),
      ),
    model = model,
  )
}

@Composable
@Preview(showBackground = true, widthDp = 250)
fun HorizontalAxisGuidelineDoesNotOverlayBottomAxisLine() {
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(),
        startAxis =
          rememberStartAxis(horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside),
        bottomAxis = rememberBottomAxis(),
      ),
    model = model,
  )
}
