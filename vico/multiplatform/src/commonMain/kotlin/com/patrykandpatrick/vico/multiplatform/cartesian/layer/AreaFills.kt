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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.withSave
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.Axis
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.getEnd
import com.patrykandpatrick.vico.multiplatform.common.getStart

internal abstract class BaseAreaFill(open val splitY: (ExtraStore) -> Number) :
  LineCartesianLayer.AreaFill {
  private val areaPath = Path()
  private val translatedPath = Path()

  abstract fun CartesianDrawingContext.drawAreas(areaPath: Path, canvasSplitY: Float)

  override fun draw(
    context: CartesianDrawingContext,
    linePath: Path,
    halfLineThickness: Float,
    verticalAxisPosition: Axis.Position.Vertical?,
  ) {
    val areaBounds = linePath.getBounds()
    with(context) {
      val canvasSplitY =
        getCanvasSplitY(splitY, halfLineThickness, verticalAxisPosition)
          .coerceIn(layerBounds.top, layerBounds.bottom)
      with(areaPath) {
        rewind()
        addPath(linePath)
        lineTo(areaBounds.getEnd(isLtr), canvasSplitY)
        lineTo(areaBounds.getStart(isLtr), canvasSplitY)
        close()
      }
      drawAreas(areaPath, canvasSplitY)
    }
  }

  protected fun CartesianDrawingContext.fillArea(areaPath: Path, paint: Paint, fillBounds: Rect) {
    if (fillBounds.width <= 0f || fillBounds.height <= 0f) return
    val (left, top) = fillBounds
    translatedPath.rewind()
    translatedPath.addPath(areaPath, Offset(-left, -top))
    canvas.withSave {
      canvas.clipRect(fillBounds)
      canvas.translate(left, top)
      canvas.drawPath(translatedPath, paint)
    }
  }
}

internal data class SingleAreaFill(
  private val fill: Fill,
  override val splitY: (ExtraStore) -> Number,
) : BaseAreaFill(splitY) {
  private val paint = Paint()

  override fun CartesianDrawingContext.drawAreas(areaPath: Path, canvasSplitY: Float) {
    paint.color = fill.color
    fill.brush?.applyTo(size = layerBounds.size, p = paint, alpha = 1f)
    fillArea(areaPath, paint, layerBounds)
  }
}

internal data class DoubleAreaFill(
  private val topFill: Fill,
  private val bottomFill: Fill,
  override val splitY: (ExtraStore) -> Number,
) : BaseAreaFill(splitY) {
  private val paint = Paint()

  override fun CartesianDrawingContext.drawAreas(areaPath: Path, canvasSplitY: Float) {
    if (canvasSplitY > layerBounds.top) {
      val bounds = Rect(layerBounds.left, layerBounds.top, layerBounds.right, canvasSplitY)
      paint.color = topFill.color
      topFill.brush?.applyTo(size = bounds.size, p = paint, alpha = 1f)
      fillArea(areaPath, paint, bounds)
    }
    if (canvasSplitY < layerBounds.bottom) {
      val bounds = Rect(layerBounds.left, canvasSplitY, layerBounds.right, layerBounds.bottom)
      paint.color = bottomFill.color
      bottomFill.brush?.applyTo(size = bounds.size, p = paint, alpha = 1f)
      fillArea(areaPath, paint, bounds)
    }
  }
}
