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

package com.patrykandpatrick.vico.core.cartesian.layer

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.common.DefaultAlpha
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.copyColor
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.getEnd
import com.patrykandpatrick.vico.core.common.getStart
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.core.common.shader.getShader

internal abstract class BaseAreaFill(open val splitY: (ExtraStore) -> Number) :
  LineCartesianLayer.AreaFill {
  private val areaBounds = RectF()
  private val areaPath = Path()

  abstract fun CartesianDrawingContext.drawAreas(areaPath: Path, canvasSplitY: Float)

  override fun draw(
    context: CartesianDrawingContext,
    linePath: Path,
    halfLineThickness: Float,
    verticalAxisPosition: Axis.Position.Vertical?,
  ) {
    @Suppress("DEPRECATION") linePath.computeBounds(areaBounds, false)
    with(context) {
      val canvasSplitY =
        getCanvasSplitY(splitY, halfLineThickness, verticalAxisPosition)
          .coerceIn(layerBounds.top, layerBounds.bottom)
      with(areaPath) {
        set(linePath)
        lineTo(areaBounds.getEnd(isLtr), canvasSplitY)
        lineTo(areaBounds.getStart(isLtr), canvasSplitY)
        close()
      }
      drawAreas(areaPath, canvasSplitY)
    }
  }

  protected fun CartesianDrawingContext.fillArea(areaPath: Path, paint: Paint, fillBounds: RectF) {
    if (fillBounds.width() <= 0f || fillBounds.height() <= 0f) return
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
    if (canvasSplitY > layerBounds.top) {
      bounds.set(layerBounds.left, layerBounds.top, layerBounds.right, canvasSplitY)
      paint.color = topFill.color
      paint.shader = topFill.shaderProvider?.getShader(this, bounds)
      fillArea(areaPath, paint, bounds)
    }
    if (canvasSplitY < layerBounds.bottom) {
      bounds.set(layerBounds.left, canvasSplitY, layerBounds.right, layerBounds.bottom)
      paint.color = bottomFill.color
      paint.shader = bottomFill.shaderProvider?.getShader(this, bounds)
      fillArea(areaPath, paint, bounds)
    }
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
