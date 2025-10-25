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

package com.patrykandpatrick.vico.multiplatform.cartesian.layer

import androidx.compose.ui.graphics.drawscope.clipRect
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.common.inClip

/** A base [CartesianLayer] implementation. */
public abstract class BaseCartesianLayer<T : CartesianLayerModel> : CartesianLayer<T> {
  private val margins: CartesianLayerMargins = CartesianLayerMargins()

  protected abstract fun drawInternal(context: CartesianDrawingContext, model: T)

  override fun draw(context: CartesianDrawingContext, model: T) {
    with(context) {
      margins.clear()
      updateLayerMargins(this, margins, layerDimensions, model)
      val left = layerBounds.left - margins.getLeft(isLtr)
      val top = layerBounds.top - margins.top
      val right = layerBounds.right + margins.getRight(isLtr)
      val bottom = layerBounds.bottom + margins.bottom
      mutableDrawScope.clipRect(left, top, right, bottom) {
        canvas.inClip(left, top, right, bottom) { drawInternal(context, model) }
      }
    }
  }
}
