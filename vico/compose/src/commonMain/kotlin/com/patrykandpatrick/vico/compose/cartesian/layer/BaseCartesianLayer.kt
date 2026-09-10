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

/** A base [CartesianLayer] implementation. */
public abstract class BaseCartesianLayer<T : CartesianLayerModel> : CartesianLayer<T> {
  private val margins: CartesianLayerMargins = CartesianLayerMargins()

  protected abstract fun drawInternal(
    context: CartesianDrawingContext,
    model: T,
    phases: Set<CartesianLayer.DrawingPhase>,
  )

  /**
   * Whether this [CartesianLayer] has area fills that it can draw separately from the rest of its
   * content for [model]. This is the hook to override—[canSeparateAreaFills] is `final` and
   * combines this with [opacity], so an implementation needn’t account for fade-ins itself.
   */
  protected open fun separatesAreaFills(context: CartesianDrawingContext, model: T): Boolean = false

  /**
   * The opacity this [CartesianLayer] draws itself with for [model]. Override this alongside
   * [separatesAreaFills] whenever this [CartesianLayer] composites itself at anything other than
   * full opacity—typically while it fades in, as it does when its model first appears—so that
   * [canSeparateAreaFills] can keep an interruption out of the resulting opacity group.
   */
  protected open fun opacity(context: CartesianDrawingContext, model: T): Float = 1f

  final override fun canSeparateAreaFills(context: CartesianDrawingContext, model: T): Boolean =
    // An interruption must not split the layer's opacity group, which is what separating the
    // phases would do while `opacity` is below 1. See `CartesianChart.DrawingOrder`.
    separatesAreaFills(context, model) && opacity(context, model) == 1f

  final override fun draw(
    context: CartesianDrawingContext,
    model: T,
    phases: Set<CartesianLayer.DrawingPhase>,
  ) {
    with(context) {
      margins.clear()
      updateLayerMargins(this, margins, layerDimensions, model)
      val left = layerBounds.left - margins.getLeft(isLtr)
      val top = layerBounds.top - margins.top
      val right = layerBounds.right + margins.getRight(isLtr)
      val bottom = layerBounds.bottom + margins.bottom
      mutableDrawScope.clipRect(left, top, right, bottom) {
        canvas.inClip(left, top, right, bottom) { drawInternal(context, model, phases) }
      }
    }
  }
}
