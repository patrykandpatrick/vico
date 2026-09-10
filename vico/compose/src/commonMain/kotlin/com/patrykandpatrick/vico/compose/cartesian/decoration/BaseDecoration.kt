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

package com.patrykandpatrick.vico.compose.cartesian.decoration

import androidx.compose.runtime.Immutable
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer

/**
 * A base [Decoration] implementation. Draws itself at the position given by [drawingOrder]. This is
 * extended by [HorizontalLine] and [HorizontalBox].
 *
 * @property drawingOrder where to draw the [Decoration] relative to the [CartesianLayer]s.
 */
@Immutable
public abstract class BaseDecoration(
  public val drawingOrder: CartesianChart.DrawingOrder = CartesianChart.DrawingOrder.OverLayers
) : Decoration {
  final override val hasContentOverAreaFills: Boolean
    get() = drawingOrder == CartesianChart.DrawingOrder.OverAreaFills

  /** Draws the [Decoration]. */
  protected abstract fun draw(context: CartesianDrawingContext)

  final override fun drawUnderLayers(context: CartesianDrawingContext) {
    if (drawingOrder == CartesianChart.DrawingOrder.UnderLayers) draw(context)
  }

  final override fun drawOverAreaFills(context: CartesianDrawingContext) {
    if (drawingOrder == CartesianChart.DrawingOrder.OverAreaFills) draw(context)
  }

  final override fun drawOverLayers(context: CartesianDrawingContext) {
    if (drawingOrder == CartesianChart.DrawingOrder.OverLayers) draw(context)
  }
}
