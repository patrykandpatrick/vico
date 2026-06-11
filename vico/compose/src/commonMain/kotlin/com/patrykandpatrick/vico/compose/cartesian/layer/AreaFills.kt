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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.withSave
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.ColorScale
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.getEnd
import com.patrykandpatrick.vico.compose.common.getStart

internal abstract class BaseAreaFill(open val splitY: (ExtraStore) -> Number) :
  LineCartesianLayer.AreaFill {
  private val areaPath = Path()
  private val translatedPath = Path()

  /**
   * Draws the area(s). [areaPath] is the region between the line and the split line; [canvasSplitY]
   * is the split line’s canvas _y_-coordinate. Implementations fill the relevant band(s) via
   * [fillArea].
   */
  abstract fun CartesianDrawingContext.drawAreas(areaPath: Path, canvasSplitY: Float)

  override fun draw(
    context: CartesianDrawingContext,
    linePath: Path,
    halfLineThickness: Float,
    verticalAxisPosition: Axis.Position.Vertical?,
  ) {
    val areaBounds = linePath.getBounds()
    with(context) {
      val canvasSplitY = getCanvasSplitY(splitY, halfLineThickness, verticalAxisPosition)
      // The area fill is the region between the line and the split line. Closing the line path to
      // the split line yields this region on both sides of the split. The fill is positioned and
      // separated via canvas clipping (see `fillArea`) rather than a boolean path operation:
      // `Path.op` can invert a self-intersecting subject, flipping the fill to the wrong side of
      // the line. See https://github.com/patrykandpatrick/vico/issues/1517.
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

  /** Fills [areaPath] with [paint], clipped to and aligned with [fillBounds]. */
  protected fun CartesianDrawingContext.fillArea(areaPath: Path, paint: Paint, fillBounds: Rect) {
    if (fillBounds.height <= 0f) return
    val (left, top) = fillBounds
    // The brush is anchored at the origin and sized to `fillBounds`, so the canvas is translated to
    // `fillBounds`’ top-left and a translated copy of the path is drawn (rather than mutating the
    // shared `areaPath`, which `DoubleAreaFill` reuses across two draws).
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
    // `canvasSplitY` can fall slightly outside `layerBounds`, so the bands are clamped to it.
    val splitY = canvasSplitY.coerceIn(layerBounds.top, layerBounds.bottom)
    if (splitY > layerBounds.top) {
      val bounds = Rect(layerBounds.left, layerBounds.top, layerBounds.right, splitY)
      paint.color = topFill.color
      topFill.brush?.applyTo(size = bounds.size, p = paint, alpha = 1f)
      fillArea(areaPath, paint, bounds)
    }
    if (splitY < layerBounds.bottom) {
      val bounds = Rect(layerBounds.left, splitY, layerBounds.right, layerBounds.bottom)
      paint.color = bottomFill.color
      bottomFill.brush?.applyTo(size = bounds.size, p = paint, alpha = 1f)
      fillArea(areaPath, paint, bounds)
    }
  }
}

internal data class ColorScaleAreaFill(private val colorScale: ColorScale) : BaseAreaFill({ 0 }) {
  private val paint = Paint()

  override fun CartesianDrawingContext.drawAreas(areaPath: Path, canvasSplitY: Float) {
    paint.shader = colorScale.getColorScaleShader(this, layerBounds.top)
    fillArea(areaPath, paint, layerBounds)
  }
}
