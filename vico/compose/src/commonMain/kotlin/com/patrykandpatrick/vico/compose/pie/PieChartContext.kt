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

package com.patrykandpatrick.vico.compose.pie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.common.DrawingContext
import com.patrykandpatrick.vico.compose.common.MeasuringContext
import com.patrykandpatrick.vico.compose.common.MutableDrawScope
import com.patrykandpatrick.vico.compose.common.data.CacheStore
import com.patrykandpatrick.vico.compose.common.data.ExtraStore

/** A [MeasuringContext] extension with pie-chart data. */
public interface PieChartMeasuringContext : MeasuringContext {
  /** The pie-chart model. */
  public val model: PieChartModel
}

/** A [DrawingContext] extension with pie-chart data. */
public interface PieChartDrawingContext : DrawingContext, PieChartMeasuringContext {
  /** The chart bounds. */
  public val chartBounds: Rect
}

internal class MutablePieChartMeasuringContext(
  override var canvasSize: Size,
  override val fontFamilyResolver: FontFamily.Resolver,
  override var density: Density,
  override var extraStore: ExtraStore,
  override val layoutDirection: LayoutDirection,
  override var model: PieChartModel,
  override val cacheStore: CacheStore,
) : PieChartMeasuringContext

@Composable
internal fun rememberPieChartMeasuringContext(
  model: PieChartModel,
  extraStore: ExtraStore,
): State<MutablePieChartMeasuringContext> {
  val fontFamilyResolver = LocalFontFamilyResolver.current
  val density = LocalDensity.current
  val layoutDirection = LocalLayoutDirection.current
  val cacheStore = remember { CacheStore() }
  return rememberUpdatedState(
    remember(fontFamilyResolver, density, extraStore, layoutDirection, model, cacheStore) {
      MutablePieChartMeasuringContext(
        canvasSize = Size.Zero,
        fontFamilyResolver = fontFamilyResolver,
        density = density,
        extraStore = extraStore,
        layoutDirection = layoutDirection,
        model = model,
        cacheStore = cacheStore,
      )
    }
  )
}

internal fun PieChartDrawingContext(
  measuringContext: PieChartMeasuringContext,
  canvas: Canvas,
  chartBounds: Rect,
  mutableDrawScope: MutableDrawScope,
): PieChartDrawingContext =
  object : PieChartDrawingContext, PieChartMeasuringContext by measuringContext {
    override var canvas: Canvas = canvas

    override val chartBounds: Rect = chartBounds

    override val mutableDrawScope: MutableDrawScope = mutableDrawScope

    override fun withCanvas(canvas: Canvas, block: () -> Unit) {
      val originalCanvas = this.canvas
      this.canvas = canvas
      block()
      this.canvas = originalCanvas
    }
  }
