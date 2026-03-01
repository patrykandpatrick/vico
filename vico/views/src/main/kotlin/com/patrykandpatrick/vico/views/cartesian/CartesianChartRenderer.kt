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

package com.patrykandpatrick.vico.views.cartesian

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.views.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.views.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.views.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.views.common.MutableSize
import com.patrykandpatrick.vico.views.common.data.CacheStore

/**
 * Renders this [CartesianChart] with the given [model] to a [Bitmap] of the specified dimensions.
 * This is intended for off-screen rendering (e.g., for use in app widgets).
 *
 * @param context the [Context] for density and font metrics.
 * @param model the [CartesianChartModel] providing the chart data.
 * @param width the width of the output [Bitmap] in pixels.
 * @param height the height of the output [Bitmap] in pixels.
 * @suppress
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun CartesianChart.renderToBitmap(
  context: Context,
  model: CartesianChartModel,
  width: Int,
  height: Int,
): Bitmap {
  val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bitmap)
  val resources = context.resources

  val ranges = MutableCartesianChartRanges()
  ranges.reset()
  updateRanges(ranges, model)

  val cacheStore = CacheStore()
  val isLtr =
    resources.configuration.layoutDirection != View.LAYOUT_DIRECTION_RTL
  val density = resources.displayMetrics.density
  val fontScale = resources.configuration.fontScale
  val measuringContext =
    MutableCartesianMeasuringContext(
      canvasSize = MutableSize(width.toFloat(), height.toFloat()),
      density = density,
      extraStore = model.extraStore,
      isLtr = isLtr,
      spToPx = { it * density * fontScale },
      model = model,
      ranges = ranges,
      scrollEnabled = false,
      zoomEnabled = false,
      layerPadding = layerPadding(model.extraStore),
      markerX = null,
      cacheStore = cacheStore,
    )

  val layerDimensions = MutableCartesianLayerDimensions()
  layerDimensions.clear()
  prepare(measuringContext, layerDimensions)

  if (!layerBounds.isEmpty) {
    val drawingContext =
      CartesianDrawingContext(
        measuringContext = measuringContext,
        canvas = canvas,
        layerDimensions = layerDimensions,
        layerBounds = layerBounds,
        scroll = 0f,
        zoom = 1f,
      )
    draw(drawingContext)
  }

  cacheStore.purge()
  return bitmap
}
