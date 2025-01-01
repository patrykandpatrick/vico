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
  private val clipPath = Path()
  private val fillBounds = RectF()

  open fun reset() {}

  abstract fun onTopAreasCreated(context: CartesianDrawingContext, path: Path, fillBounds: RectF)

  abstract fun onBottomAreasCreated(context: CartesianDrawingContext, path: Path, fillBounds: RectF)

  open fun onAreasCreated(context: CartesianDrawingContext, fillBounds: RectF) {}

  override fun draw(
    context: CartesianDrawingContext,
    linePath: Path,
    halfLineThickness: Float,
    verticalAxisPosition: Axis.Position.Vertical?,
  ) {
    reset()
    @Suppress("DEPRECATION") linePath.computeBounds(areaBounds, false)
    with(context) {
      val canvasSplitY = getCanvasSplitY(splitY, halfLineThickness, verticalAxisPosition)
      if (canvasSplitY > layerBounds.top) {
        clipPath.rewind()
        fillBounds.set(layerBounds.left, layerBounds.top, layerBounds.right, canvasSplitY)
        clipPath.addRect(fillBounds, Path.Direction.CW)
        with(areaPath) {
          set(linePath)
          lineTo(areaBounds.getEnd(isLtr), layerBounds.bottom)
          lineTo(areaBounds.getStart(isLtr), layerBounds.bottom)
          close()
          op(clipPath, Path.Op.INTERSECT)
        }
        onTopAreasCreated(this, areaPath, fillBounds)
      }
      if (canvasSplitY < layerBounds.bottom) {
        clipPath.rewind()
        fillBounds.set(layerBounds.left, canvasSplitY, layerBounds.right, layerBounds.bottom)
        clipPath.addRect(fillBounds, Path.Direction.CW)
        with(areaPath) {
          set(linePath)
          lineTo(areaBounds.getEnd(isLtr), layerBounds.top)
          lineTo(areaBounds.getStart(isLtr), layerBounds.top)
          close()
          op(clipPath, Path.Op.INTERSECT)
        }
        onBottomAreasCreated(this, areaPath, fillBounds)
      }
      fillBounds.set(layerBounds)
      onAreasCreated(this, fillBounds)
    }
  }
}

internal data class SingleAreaFill(
  private val fill: Fill,
  override val splitY: (ExtraStore) -> Number,
) : BaseAreaFill(splitY) {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val areaPath = Path()

  override fun reset() {
    areaPath.rewind()
  }

  override fun onTopAreasCreated(context: CartesianDrawingContext, path: Path, fillBounds: RectF) {
    areaPath.addPath(path)
  }

  override fun onBottomAreasCreated(
    context: CartesianDrawingContext,
    path: Path,
    fillBounds: RectF,
  ) {
    areaPath.addPath(path)
  }

  override fun onAreasCreated(context: CartesianDrawingContext, fillBounds: RectF) {
    with(context) {
      paint.color = fill.color
      paint.shader = fill.shaderProvider?.getShader(this, fillBounds)
      canvas.drawPath(areaPath, paint)
    }
  }
}

internal data class DoubleAreaFill(
  private val topFill: Fill,
  private val bottomFill: Fill,
  override val splitY: (ExtraStore) -> Number,
) : BaseAreaFill(splitY) {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

  override fun onTopAreasCreated(context: CartesianDrawingContext, path: Path, fillBounds: RectF) {
    with(context) {
      paint.color = topFill.color
      paint.shader = topFill.shaderProvider?.getShader(this, fillBounds)
      canvas.drawPath(path, paint)
    }
  }

  override fun onBottomAreasCreated(
    context: CartesianDrawingContext,
    path: Path,
    fillBounds: RectF,
  ) {
    with(context) {
      paint.color = bottomFill.color
      paint.shader = bottomFill.shaderProvider?.getShader(this, fillBounds)
      canvas.drawPath(path, paint)
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
