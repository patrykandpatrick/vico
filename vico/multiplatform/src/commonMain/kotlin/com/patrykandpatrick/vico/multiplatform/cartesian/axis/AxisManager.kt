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

package com.patrykandpatrick.vico.multiplatform.cartesian.axis

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerMargins
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class AxisManager {
  internal val axisCache = ArrayList<Axis<*>>(MAX_AXIS_COUNT)

  var startAxis: Axis<Axis.Position.Vertical.Start>? by cacheInList()
  var topAxis: Axis<Axis.Position.Horizontal.Top>? by cacheInList()
  var endAxis: Axis<Axis.Position.Vertical.End>? by cacheInList()
  var bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? by cacheInList()

  fun setAxesBounds(
    context: CartesianMeasuringContext,
    canvasSize: Size,
    layerBounds: Rect,
    layerMargins: CartesianLayerMargins,
  ) {
    startAxis?.setStartAxisBounds(context, canvasSize, layerBounds, layerMargins)
    topAxis?.setTopAxisBounds(context, canvasSize, layerMargins)
    endAxis?.setEndAxisBounds(context, canvasSize, layerBounds, layerMargins)
    bottomAxis?.setBottomAxisBounds(context, canvasSize, layerBounds, layerMargins)
    setRestrictedBounds()
  }

  private fun Axis<Axis.Position.Vertical.Start>.setStartAxisBounds(
    context: CartesianMeasuringContext,
    canvasSize: Size,
    layerBounds: Rect,
    layerMargins: CartesianLayerMargins,
  ) {
    with(context) {
      setBounds(
        left = if (isLtr) 0f else canvasSize.width - layerMargins.start,
        top = layerBounds.top,
        right = if (isLtr) 0f + layerMargins.start else canvasSize.width,
        bottom = layerBounds.bottom,
      )
    }
  }

  private fun Axis<Axis.Position.Horizontal.Top>.setTopAxisBounds(
    context: CartesianMeasuringContext,
    canvasSize: Size,
    layerMargins: CartesianLayerMargins,
  ) {
    with(context) {
      setBounds(
        left = if (isLtr) layerMargins.start else layerMargins.end,
        top = 0f,
        right = canvasSize.width - if (isLtr) layerMargins.end else layerMargins.start,
        bottom = layerMargins.top,
      )
    }
  }

  private fun Axis<Axis.Position.Vertical.End>.setEndAxisBounds(
    context: CartesianMeasuringContext,
    canvasSize: Size,
    layerBounds: Rect,
    layerMargins: CartesianLayerMargins,
  ) {
    with(context) {
      setBounds(
        left = if (isLtr) canvasSize.width - layerMargins.end else 0f,
        top = layerBounds.top,
        right = if (isLtr) canvasSize.width else 0f + layerMargins.end,
        bottom = layerBounds.bottom,
      )
    }
  }

  private fun Axis<Axis.Position.Horizontal.Bottom>.setBottomAxisBounds(
    context: CartesianMeasuringContext,
    canvasSize: Size,
    layerBounds: Rect,
    layerMargins: CartesianLayerMargins,
  ) {
    with(context) {
      setBounds(
        left = if (isLtr) layerMargins.start else layerMargins.end,
        top = layerBounds.bottom,
        right = canvasSize.width - if (isLtr) layerMargins.end else layerMargins.start,
        bottom = layerBounds.bottom + layerMargins.bottom,
      )
    }
  }

  private fun setRestrictedBounds() {
    startAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
    topAxis?.setRestrictedBounds(startAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
    endAxis?.setRestrictedBounds(topAxis?.bounds, startAxis?.bounds, bottomAxis?.bounds)
    bottomAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, startAxis?.bounds)
  }

  fun drawUnderLayers(context: CartesianDrawingContext) {
    axisCache.forEach { axis -> axis.drawUnderLayers(context) }
  }

  fun drawOverLayers(context: CartesianDrawingContext) {
    axisCache.forEach { axis -> axis.drawOverLayers(context) }
  }

  private fun <S, T : Axis<S>?> cacheInList(): ReadWriteProperty<AxisManager, T?> =
    object : ReadWriteProperty<AxisManager, T?> {
      var field: T? = null

      override fun getValue(thisRef: AxisManager, property: KProperty<*>): T? = field

      override fun setValue(thisRef: AxisManager, property: KProperty<*>, value: T?) {
        if (field == value) return
        field?.let(thisRef.axisCache::remove)
        field = value
        value?.let(thisRef.axisCache::add)
      }
    }

  private companion object {
    const val MAX_AXIS_COUNT = 4
  }
}
