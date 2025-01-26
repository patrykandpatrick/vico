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
  private val clipPath = Path()

  open fun reset() {}

  abstract fun onTopAreasCreated(context: CartesianDrawingContext, path: Path, fillBounds: Rect)

  abstract fun onBottomAreasCreated(context: CartesianDrawingContext, path: Path, fillBounds: Rect)

  open fun onAreasCreated(context: CartesianDrawingContext, fillBounds: Rect) {}

  override fun draw(
    context: CartesianDrawingContext,
    linePath: Path,
    halfLineThickness: Float,
    verticalAxisPosition: Axis.Position.Vertical?,
  ) {
    reset()
    val areaBounds = linePath.getBounds()
    with(context) {
      val canvasSplitY = getCanvasSplitY(splitY, halfLineThickness, verticalAxisPosition)
      if (canvasSplitY > layerBounds.top) {
        clipPath.rewind()
        val fillBounds = Rect(layerBounds.left, layerBounds.top, layerBounds.right, canvasSplitY)
        clipPath.addRect(fillBounds, Path.Direction.Clockwise)
        with(areaPath) {
          rewind()
          addPath(linePath)
          lineTo(areaBounds.getEnd(isLtr), layerBounds.bottom)
          lineTo(areaBounds.getStart(isLtr), layerBounds.bottom)
          close()
        }
        onTopAreasCreated(this, areaPath.and(clipPath), fillBounds)
      }
      if (canvasSplitY < layerBounds.bottom) {
        clipPath.rewind()
        val fillBounds = Rect(layerBounds.left, canvasSplitY, layerBounds.right, layerBounds.bottom)
        clipPath.addRect(fillBounds, Path.Direction.CounterClockwise)
        with(areaPath) {
          rewind()
          addPath(linePath)
          lineTo(areaBounds.getEnd(isLtr), layerBounds.top)
          lineTo(areaBounds.getStart(isLtr), layerBounds.top)
          close()
        }
        onBottomAreasCreated(this, areaPath.and(clipPath), fillBounds)
      }
      onAreasCreated(this, layerBounds)
    }
  }
}

internal data class SingleAreaFill(
  private val fill: Fill,
  override val splitY: (ExtraStore) -> Number,
) : BaseAreaFill(splitY) {
  private val paint = Paint()
  private val areaPath = Path()

  override fun reset() {
    areaPath.rewind()
  }

  override fun onTopAreasCreated(context: CartesianDrawingContext, path: Path, fillBounds: Rect) {
    areaPath.addPath(path)
  }

  override fun onBottomAreasCreated(
    context: CartesianDrawingContext,
    path: Path,
    fillBounds: Rect,
  ) {
    areaPath.addPath(path)
  }

  override fun onAreasCreated(context: CartesianDrawingContext, fillBounds: Rect) {
    with(context) {
      paint.color = fill.color
      fill.brush?.applyTo(size = fillBounds.size, p = paint, alpha = 1f)
      val (left, top) = areaPath.getBounds()
      canvas.withSave {
        canvas.translate(left, top)
        areaPath.translate(Offset(-left, -top))
        canvas.drawPath(areaPath, paint)
      }
    }
  }
}

internal data class DoubleAreaFill(
  private val topFill: Fill,
  private val bottomFill: Fill,
  override val splitY: (ExtraStore) -> Number,
) : BaseAreaFill(splitY) {
  private val paint = Paint()

  override fun onTopAreasCreated(context: CartesianDrawingContext, path: Path, fillBounds: Rect) {
    with(context) {
      paint.color = topFill.color
      topFill.brush?.applyTo(size = fillBounds.size, p = paint, alpha = 1f)
      val (left, top) = path.getBounds()
      canvas.withSave {
        canvas.translate(left, top)
        path.translate(Offset(-left, -top))
        canvas.drawPath(path, paint)
      }
    }
  }

  override fun onBottomAreasCreated(
    context: CartesianDrawingContext,
    path: Path,
    fillBounds: Rect,
  ) {
    with(context) {
      paint.color = bottomFill.color
      bottomFill.brush?.applyTo(size = fillBounds.size, p = paint, alpha = 1f)
      val (left, top) = path.getBounds()
      canvas.withSave {
        canvas.translate(left, top)
        path.translate(Offset(-left, -top))
        canvas.drawPath(path, paint)
      }
    }
  }
}
