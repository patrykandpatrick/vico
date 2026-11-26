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

import androidx.annotation.RestrictTo
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.compose.common.MutableDrawScope
import com.patrykandpatrick.vico.compose.common.data.CacheStore

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun CartesianChart.renderToImageBitmap(
  width: Int,
  height: Int,
  fontFamilyResolver: FontFamily.Resolver,
  density: Density,
  layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): ImageBitmap {
  val imageBitmap = ImageBitmap(width, height)
  if (layers.isEmpty()) return imageBitmap
  val canvas = Canvas(imageBitmap)
  val size = Size(width.toFloat(), height.toFloat())
  val ranges = CartesianChartPipeline.getRanges(this)
  val cacheStore = CacheStore()
  val measuringContext =
    MutableCartesianMeasuringContext(
      canvasWidth = size.width,
      fontFamilyResolver = fontFamilyResolver,
      density = density,
      layoutDirection = layoutDirection,
      chartModel = model,
      ranges = ranges,
      scrollEnabled = false,
      zoomEnabled = false,
      layerPadding = layerPadding,
      markerX = null,
      markerSeriesIndex = null,
      cacheStore = cacheStore,
      drawingModels = CartesianChartPipeline.getFinalDrawingModels(this, ranges),
    )

  val layerDimensions = MutableCartesianLayerDimensions()
  prepare(measuringContext, layerDimensions, size)

  if (!layerBounds.isEmpty) {
    CanvasDrawScope().draw(density, layoutDirection, canvas, size) {
      val drawingContext =
        CartesianDrawingContext(
          measuringContext = measuringContext,
          canvasSize = size,
          canvas = canvas,
          layerDimensions = layerDimensions,
          layerBounds = layerBounds,
          scroll = 0f,
          zoom = 1f,
          mutableDrawScope = MutableDrawScope(this),
        )
      draw(drawingContext)
    }
  }

  cacheStore.purge()
  return imageBitmap
}
