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

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.withSave
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.Axis
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore

internal data class SingleLineFill(val fill: Fill) : LineCartesianLayer.LineFill {
  private val paint = Paint().apply { color = fill.color }

  override fun draw(
    context: CartesianDrawingContext,
    halfLineThickness: Float,
    verticalAxisPosition: Axis.Position.Vertical?,
  ) {
    with(context) {
      fill.brush?.applyTo(size = layerBounds.size, p = paint, alpha = 1f)
      canvas.withSave {
        canvas.translate(layerBounds.left, layerBounds.top)
        canvas.drawRect(0f, 0f, layerBounds.width, layerBounds.height, paint)
      }
    }
  }
}

internal data class DoubleLineFill(
  val topFill: Fill,
  val bottomFill: Fill,
  val splitY: (ExtraStore) -> Number,
) : LineCartesianLayer.LineFill {
  private val paint = Paint()

  override fun draw(
    context: CartesianDrawingContext,
    halfLineThickness: Float,
    verticalAxisPosition: Axis.Position.Vertical?,
  ) {
    with(context) {
      val canvasSplitY = getCanvasSplitY(splitY, halfLineThickness, verticalAxisPosition)
      paint.color = topFill.color
      topFill.brush?.applyTo(
        Size(layerBounds.width, canvasSplitY - layerBounds.top - halfLineThickness),
        paint,
        1f,
      )
      canvas.withSave {
        canvas.translate(layerBounds.left, layerBounds.top)
        canvas.drawRect(
          0f,
          -halfLineThickness,
          layerBounds.width,
          canvasSplitY - layerBounds.top,
          paint,
        )
      }

      paint.color = bottomFill.color
      bottomFill.brush?.applyTo(
        Size(layerBounds.width, layerBounds.bottom - canvasSplitY + halfLineThickness),
        paint,
        1f,
      )
      canvas.withSave {
        canvas.translate(layerBounds.left, canvasSplitY)
        canvas.drawRect(
          0f,
          0f,
          layerBounds.width,
          layerBounds.bottom - canvasSplitY + halfLineThickness,
          paint,
        )
      }
    }
  }
}
