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

package com.patrykandpatrick.vico.core.cartesian.layer

import android.graphics.Paint
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.data.ExtraStore

internal data class SingleLineFill(val fill: Fill) : LineCartesianLayer.LineFill {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill.color }

  override fun draw(
    context: CartesianDrawingContext,
    halfLineThickness: Float,
    verticalAxisPosition: Axis.Position.Vertical?,
  ) {
    with(context) {
      paint.shader =
        fill.shader?.provideShader(
          this,
          layerBounds.left,
          layerBounds.top,
          layerBounds.right,
          layerBounds.bottom,
        )
      canvas.drawPaint(paint)
    }
  }
}

internal data class DoubleLineFill(
  val topFill: Fill,
  val bottomFill: Fill,
  val splitY: (ExtraStore) -> Number,
) : LineCartesianLayer.LineFill {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

  override fun draw(
    context: CartesianDrawingContext,
    halfLineThickness: Float,
    verticalAxisPosition: Axis.Position.Vertical?,
  ) {
    with(context) {
      val canvasSplitY = getCanvasSplitY(splitY, halfLineThickness, verticalAxisPosition)
      paint.color = topFill.color
      paint.shader =
        topFill.shader?.provideShader(
          this,
          layerBounds.left,
          layerBounds.top - halfLineThickness,
          layerBounds.right,
          canvasSplitY,
        )
      canvas.drawRect(
        layerBounds.left,
        layerBounds.top - halfLineThickness,
        layerBounds.right,
        canvasSplitY,
        paint,
      )
      paint.color = bottomFill.color
      paint.shader =
        bottomFill.shader?.provideShader(
          this,
          layerBounds.left,
          canvasSplitY,
          layerBounds.right,
          layerBounds.bottom + halfLineThickness,
        )
      canvas.drawRect(
        layerBounds.left,
        canvasSplitY,
        layerBounds.right,
        layerBounds.bottom + halfLineThickness,
        paint,
      )
    }
  }
}
