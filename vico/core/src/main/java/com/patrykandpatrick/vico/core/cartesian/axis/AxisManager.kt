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

package com.patrykandpatrick.vico.core.cartesian.axis

import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMargins
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
    canvasBounds: RectF,
    layerBounds: RectF,
    layerMargins: CartesianLayerMargins,
  ) {
    startAxis?.setStartAxisBounds(context, canvasBounds, layerBounds, layerMargins)
    topAxis?.setTopAxisBounds(context, canvasBounds, layerMargins)
    endAxis?.setEndAxisBounds(context, canvasBounds, layerBounds, layerMargins)
    bottomAxis?.setBottomAxisBounds(context, canvasBounds, layerBounds, layerMargins)
    setRestrictedBounds()
  }

  private fun Axis<Axis.Position.Vertical.Start>.setStartAxisBounds(
    context: CartesianMeasuringContext,
    canvasBounds: RectF,
    layerBounds: RectF,
    layerMargins: CartesianLayerMargins,
  ) {
    with(context) {
      setBounds(
        left = if (isLtr) canvasBounds.left else canvasBounds.right - layerMargins.start,
        top = layerBounds.top,
        right = if (isLtr) canvasBounds.left + layerMargins.start else canvasBounds.right,
        bottom = layerBounds.bottom,
      )
    }
  }

  private fun Axis<Axis.Position.Horizontal.Top>.setTopAxisBounds(
    context: CartesianMeasuringContext,
    canvasBounds: RectF,
    layerMargins: CartesianLayerMargins,
  ) {
    with(context) {
      setBounds(
        left = canvasBounds.left + if (isLtr) layerMargins.start else layerMargins.end,
        top = canvasBounds.top,
        right = canvasBounds.right - if (isLtr) layerMargins.end else layerMargins.start,
        bottom = canvasBounds.top + layerMargins.top,
      )
    }
  }

  private fun Axis<Axis.Position.Vertical.End>.setEndAxisBounds(
    context: CartesianMeasuringContext,
    canvasBounds: RectF,
    layerBounds: RectF,
    layerMargins: CartesianLayerMargins,
  ) {
    with(context) {
      setBounds(
        left = if (isLtr) canvasBounds.right - layerMargins.end else canvasBounds.left,
        top = layerBounds.top,
        right = if (isLtr) canvasBounds.right else canvasBounds.left + layerMargins.end,
        bottom = layerBounds.bottom,
      )
    }
  }

  private fun Axis<Axis.Position.Horizontal.Bottom>.setBottomAxisBounds(
    context: CartesianMeasuringContext,
    canvasBounds: RectF,
    layerBounds: RectF,
    layerMargins: CartesianLayerMargins,
  ) {
    with(context) {
      setBounds(
        left = canvasBounds.left + if (isLtr) layerMargins.start else layerMargins.end,
        top = layerBounds.bottom,
        right = canvasBounds.right - if (isLtr) layerMargins.end else layerMargins.start,
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
