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

package com.patrykandpatrick.vico.views.cartesian.layer

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.views.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.views.cartesian.ColorScale
import com.patrykandpatrick.vico.views.cartesian.axis.Axis
import com.patrykandpatrick.vico.views.common.*
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import com.patrykandpatrick.vico.views.common.shader.ShaderProvider
import com.patrykandpatrick.vico.views.common.shader.getShader

internal abstract class BaseAreaFill(open val splitY: (ExtraStore) -> Number) :
  LineCartesianLayer.AreaFill {
  private val areaBounds = RectF()
  private val areaPath = Path()

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
    @Suppress("DEPRECATION") linePath.computeBounds(areaBounds, false)
    with(context) {
      val canvasSplitY = getCanvasSplitY(splitY, halfLineThickness, verticalAxisPosition)
      // The area fill is the region between the line and the split line. Closing the line path to
      // the split line yields this region on both sides of the split. The fill is positioned and
      // separated via canvas clipping (see `fillArea`) rather than a boolean path operation:
      // `Path.op` can invert a self-intersecting subject, flipping the fill to the wrong side of
      // the line. See https://github.com/patrykandpatrick/vico/issues/1517.
      with(areaPath) {
        set(linePath)
        lineTo(areaBounds.getEnd(isLtr), canvasSplitY)
        lineTo(areaBounds.getStart(isLtr), canvasSplitY)
        close()
      }
      drawAreas(areaPath, canvasSplitY)
    }
  }

  /** Fills [areaPath] with [paint], clipped to [fillBounds]. */
  protected fun CartesianDrawingContext.fillArea(areaPath: Path, paint: Paint, fillBounds: RectF) {
    if (fillBounds.height() <= 0f) return
    val checkpoint = canvas.save()
    canvas.clipRect(fillBounds)
    canvas.drawPath(areaPath, paint)
    canvas.restoreToCount(checkpoint)
  }
}

internal data class SingleAreaFill(
  private val fill: Fill,
  override val splitY: (ExtraStore) -> Number,
) : BaseAreaFill(splitY) {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

  override fun CartesianDrawingContext.drawAreas(areaPath: Path, canvasSplitY: Float) {
    paint.color = fill.color
    paint.shader = fill.shaderProvider?.getShader(this, layerBounds)
    fillArea(areaPath, paint, layerBounds)
  }
}

internal data class DoubleAreaFill(
  private val topFill: Fill,
  private val bottomFill: Fill,
  override val splitY: (ExtraStore) -> Number,
) : BaseAreaFill(splitY) {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val bounds = RectF()

  override fun CartesianDrawingContext.drawAreas(areaPath: Path, canvasSplitY: Float) {
    // `canvasSplitY` can fall slightly outside `layerBounds`, so the bands are clamped to it.
    val splitY = canvasSplitY.coerceIn(layerBounds.top, layerBounds.bottom)
    if (splitY > layerBounds.top) {
      bounds.set(layerBounds.left, layerBounds.top, layerBounds.right, splitY)
      paint.color = topFill.color
      paint.shader = topFill.shaderProvider?.getShader(this, bounds)
      fillArea(areaPath, paint, bounds)
    }
    if (splitY < layerBounds.bottom) {
      bounds.set(layerBounds.left, splitY, layerBounds.right, layerBounds.bottom)
      paint.color = bottomFill.color
      paint.shader = bottomFill.shaderProvider?.getShader(this, bounds)
      fillArea(areaPath, paint, bounds)
    }
  }
}

internal data class ColorScaleAreaFill(private val colorScale: ColorScale) : BaseAreaFill({ 0 }) {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

  override fun CartesianDrawingContext.drawAreas(areaPath: Path, canvasSplitY: Float) {
    paint.shader = colorScale.getColorScaleShader(this)
    fillArea(areaPath, paint, layerBounds)
  }
}

private fun LineCartesianLayer.AreaFill.Companion.default(
  topColor: Int,
  bottomColor: Int,
  splitY: (ExtraStore) -> Number = { 0 },
) =
  double(
    topFill =
      Fill(
        ShaderProvider.verticalGradient(
          topColor.copyColor(DefaultAlpha.LINE_BACKGROUND_SHADER_START),
          topColor.copyColor(DefaultAlpha.LINE_BACKGROUND_SHADER_END),
        )
      ),
    bottomFill =
      Fill(
        ShaderProvider.verticalGradient(
          bottomColor.copyColor(DefaultAlpha.LINE_BACKGROUND_SHADER_END),
          bottomColor.copyColor(DefaultAlpha.LINE_BACKGROUND_SHADER_START),
        )
      ),
    splitY = splitY,
  )
