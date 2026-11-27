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

package com.patrykandpatrick.vico.compose.cartesian.layer

import androidx.compose.ui.graphics.drawscope.clipRect
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.compose.common.inClip

/**
 * A base [CartesianLayer] implementation. It clips the drawing area to the [CartesianLayer] area
 * and its margins (as defined by [updateLayerMargins]) and delegates the drawing to [drawInternal].
 */
public abstract class BaseCartesianLayer<M : CartesianLayerModel> : CartesianLayer<M> {
  private val margins: CartesianLayerMargins = CartesianLayerMargins()

  /** Draws the [CartesianLayer]’s content. */
  protected abstract fun drawInternal(context: CartesianDrawingContext)

  override fun draw(context: CartesianDrawingContext) {
    with(context) {
      margins.clear()
      updateLayerMargins(this, margins, layerDimensions)
      val left = layerBounds.left - margins.getLeft(isLtr)
      val top = layerBounds.top - margins.top
      val right = layerBounds.right + margins.getRight(isLtr)
      val bottom = layerBounds.bottom + margins.bottom
      mutableDrawScope.clipRect(left, top, right, bottom) {
        canvas.inClip(left, top, right, bottom) { drawInternal(context) }
      }
    }
  }
}
