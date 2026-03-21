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

package com.patrykandpatrick.vico.views.pie

import android.graphics.Canvas
import android.graphics.RectF
import com.patrykandpatrick.vico.views.common.DrawingContext
import com.patrykandpatrick.vico.views.common.MeasuringContext
import com.patrykandpatrick.vico.views.pie.data.PieChartModel

/** A [MeasuringContext] extension with pie-chart data. */
public interface PieChartMeasuringContext : MeasuringContext {
  /** The pie-chart model. */
  public val model: PieChartModel
}

/** A [DrawingContext] extension with pie-chart data. */
public interface PieChartDrawingContext : DrawingContext, PieChartMeasuringContext {
  /** The chart bounds. */
  public val chartBounds: RectF
}

internal fun PieChartDrawingContext(
  measuringContext: PieChartMeasuringContext,
  canvas: Canvas,
  chartBounds: RectF,
): PieChartDrawingContext =
  object : PieChartDrawingContext, PieChartMeasuringContext by measuringContext {
    override var canvas: Canvas = canvas

    override val chartBounds: RectF = chartBounds

    override fun withCanvas(canvas: Canvas, block: () -> Unit) {
      val originalCanvas = this.canvas
      this.canvas = canvas
      block()
      this.canvas = originalCanvas
    }
  }
