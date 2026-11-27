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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarkerTargetLifecycleTest {
  private fun chart() =
    testCartesianChart(
      ColumnCartesianLayer(
        ColumnCartesianLayerModel.build { series(1, 2, 3) },
        ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill(Color.Red))),
        key = "columns",
      ),
      marker = object : CartesianMarker {},
    )

  @Test
  fun `Measuring doesn’t clear the marker targets`() {
    val chart = chart()
    assertFalse(chart.isDrawn)
    val density = Density(1f)
    val fontFamilyResolver = createFontFamilyResolver()
    chart.renderToImageBitmap(300, 200, fontFamilyResolver, density)
    assertTrue(chart.isDrawn)
    assertTrue(chart.markerTargets.isNotEmpty())
    val ranges = CartesianChartPipeline.getRanges(chart)
    val context =
      MutableCartesianMeasuringContext(
        canvasWidth = 300f,
        fontFamilyResolver = fontFamilyResolver,
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        chartModel = chart.model,
        ranges = ranges,
        scrollEnabled = false,
        zoomEnabled = false,
        layerPadding = chart.layerPadding,
        markerX = null,
        markerSeriesIndex = null,
        drawingModels = CartesianChartPipeline.getFinalDrawingModels(chart, ranges),
      )
    // A layout-phase measurement.
    chart.getVerticalExtras(context, MutableCartesianLayerDimensions())
    assertTrue(chart.markerTargets.isNotEmpty(), "Measuring cleared the marker targets.")
  }
}
