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

package com.patrykandpatrick.vico.compose.pie

import androidx.annotation.RestrictTo
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.common.MutableDrawScope
import com.patrykandpatrick.vico.compose.common.data.CacheStore
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel

/**
 * Renders this [PieChart] with the given [model] to an [ImageBitmap] of the specified dimensions.
 * This is intended for off-screen rendering (e.g., for use in app widgets).
 *
 * @param model the [PieChartModel] providing the chart data.
 * @param width the width of the output [ImageBitmap] in pixels.
 * @param height the height of the output [ImageBitmap] in pixels.
 * @param fontFamilyResolver the [FontFamily.Resolver] for text rendering.
 * @param density the [Density] for dp-to-pixel conversions.
 * @param layoutDirection the [LayoutDirection].
 * @suppress
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun PieChart.renderToImageBitmap(
  model: PieChartModel,
  width: Int,
  height: Int,
  fontFamilyResolver: FontFamily.Resolver,
  density: Density,
  layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): ImageBitmap {
  val imageBitmap = ImageBitmap(width, height)
  val canvas = Canvas(imageBitmap)
  val size = Size(width.toFloat(), height.toFloat())
  val cacheStore = CacheStore()
  val measuringContext =
    MutablePieChartMeasuringContext(
      canvasSize = size,
      fontFamilyResolver = fontFamilyResolver,
      density = density,
      extraStore = model.extraStore,
      layoutDirection = layoutDirection,
      model = model,
      cacheStore = cacheStore,
    )

  CanvasDrawScope().draw(density, layoutDirection, canvas, size) {
    val mutableDrawScope = MutableDrawScope(this)
    val legendHeight = getLegendHeight(measuringContext)
    val chartBounds = Rect(0f, 0f, size.width, size.height - legendHeight)
    if (!chartBounds.isEmpty) {
      bounds = chartBounds
      legend?.setBounds(0f, chartBounds.bottom, size.width, chartBounds.bottom + legendHeight)
      val drawingContext =
        PieChartDrawingContext(measuringContext, canvas, chartBounds, mutableDrawScope)
      draw(drawingContext, model.toDrawingModel())
    }
  }

  cacheStore.purge()
  return imageBitmap
}
