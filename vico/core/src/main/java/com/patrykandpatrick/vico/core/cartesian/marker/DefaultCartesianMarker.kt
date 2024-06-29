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

package com.patrykandpatrick.vico.core.cartesian.marker

import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.averageOf
import com.patrykandpatrick.vico.core.common.ceil
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.doubled
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.orZero
import com.patrykandpatrick.vico.core.common.shape.MarkerCorneredShape

/**
 * The default [CartesianMarker] implementation.
 *
 * @param label the [TextComponent] for the label.
 * @param valueFormatter formats the values.
 * @param labelPosition specifies the position of the label.
 * @param indicator drawn at the marked points.
 * @param indicatorSizeDp the indicator size (in dp).
 * @param setIndicatorColor sets the indicator color for each marked point.
 * @param guideline drawn vertically through the marked points.
 */
public open class DefaultCartesianMarker(
  protected val label: TextComponent,
  protected val valueFormatter: CartesianMarkerValueFormatter =
    DefaultCartesianMarkerValueFormatter(),
  protected val labelPosition: LabelPosition = LabelPosition.Top,
  protected val indicator: Component? = null,
  protected val indicatorSizeDp: Float = Defaults.MARKER_INDICATOR_SIZE,
  protected val setIndicatorColor: ((Int) -> Unit)? = null,
  protected val guideline: LineComponent? = null,
) : CartesianMarker {
  protected val tempBounds: RectF = RectF()

  protected val TextComponent.tickSizeDp: Float
    get() = ((background as? ShapeComponent)?.shape as? MarkerCorneredShape)?.tickSizeDp.orZero

  override fun draw(context: CartesianDrawContext, targets: List<CartesianMarker.Target>): Unit =
    with(context) {
      drawGuideline(targets)
      val halfIndicatorSize = indicatorSizeDp.half.pixels

      targets.forEach { target ->
        when (target) {
          is CandlestickCartesianLayerMarkerTarget -> {
            drawIndicator(
              target.canvasX,
              target.openingCanvasY,
              target.openingColor,
              halfIndicatorSize,
            )
            drawIndicator(
              target.canvasX,
              target.closingCanvasY,
              target.closingColor,
              halfIndicatorSize,
            )
            drawIndicator(target.canvasX, target.lowCanvasY, target.lowColor, halfIndicatorSize)
            drawIndicator(target.canvasX, target.highCanvasY, target.highColor, halfIndicatorSize)
          }
          is ColumnCartesianLayerMarkerTarget -> {
            target.columns.forEach { column ->
              drawIndicator(target.canvasX, column.canvasY, column.color, halfIndicatorSize)
            }
          }
          is LineCartesianLayerMarkerTarget -> {
            target.points.forEach { point ->
              drawIndicator(target.canvasX, point.canvasY, point.color, halfIndicatorSize)
            }
          }
        }
      }
      drawLabel(context, targets)
    }

  protected open fun CartesianDrawContext.drawIndicator(
    x: Float,
    y: Float,
    color: Int,
    halfIndicatorSize: Float,
  ) {
    if (indicator == null) return
    setIndicatorColor?.invoke(color)
    indicator.draw(
      this,
      x - halfIndicatorSize,
      y - halfIndicatorSize,
      x + halfIndicatorSize,
      y + halfIndicatorSize,
    )
  }

  protected fun drawLabel(
    context: CartesianDrawContext,
    targets: List<CartesianMarker.Target>,
  ): Unit =
    with(context) {
      val text = valueFormatter.format(context, targets)
      val targetX = targets.averageOf { it.canvasX }
      val labelBounds =
        label.getTextBounds(
          context = context,
          text = text,
          width = chartBounds.width().toInt(),
          outRect = tempBounds,
        )
      val halfOfTextWidth = labelBounds.width().half
      val x = overrideXPositionToFit(targetX, chartBounds, halfOfTextWidth)
      extraStore[MarkerCorneredShape.tickXKey] = targetX
      val tickPosition: MarkerCorneredShape.TickPosition
      val y: Float
      val verticalPosition: VerticalPosition
      when (labelPosition) {
        LabelPosition.Top -> {
          tickPosition = MarkerCorneredShape.TickPosition.Bottom
          y = context.chartBounds.top - label.tickSizeDp.pixels
          verticalPosition = VerticalPosition.Top
        }
        LabelPosition.Bottom -> {
          tickPosition = MarkerCorneredShape.TickPosition.Top
          y = context.chartBounds.bottom + label.tickSizeDp.pixels
          verticalPosition = VerticalPosition.Bottom
        }
        LabelPosition.AroundPoint,
        LabelPosition.AbovePoint -> {
          val topPointY =
            targets.minOf { target ->
              when (target) {
                is CandlestickCartesianLayerMarkerTarget -> target.highCanvasY
                is ColumnCartesianLayerMarkerTarget ->
                  target.columns.minOf(ColumnCartesianLayerMarkerTarget.Column::canvasY)
                is LineCartesianLayerMarkerTarget ->
                  target.points.minOf(LineCartesianLayerMarkerTarget.Point::canvasY)
                else -> error("Unexpected `CartesianMarker.Target` implementation.")
              }
            }
          val flip =
            labelPosition == LabelPosition.AroundPoint &&
              topPointY - labelBounds.height() - label.tickSizeDp.pixels < context.chartBounds.top
          tickPosition =
            if (flip) MarkerCorneredShape.TickPosition.Top
            else MarkerCorneredShape.TickPosition.Bottom
          y = topPointY + (if (flip) 1 else -1) * label.tickSizeDp.pixels
          verticalPosition = if (flip) VerticalPosition.Bottom else VerticalPosition.Top
        }
      }
      extraStore[MarkerCorneredShape.tickPositionKey] = tickPosition

      label.drawText(
        context = context,
        text = text,
        textX = x,
        textY = y,
        verticalPosition = verticalPosition,
        maxTextWidth = minOf(chartBounds.right - x, x - chartBounds.left).doubled.ceil.toInt(),
      )
    }

  protected fun overrideXPositionToFit(
    xPosition: Float,
    bounds: RectF,
    halfOfTextWidth: Float,
  ): Float =
    when {
      xPosition - halfOfTextWidth < bounds.left -> bounds.left + halfOfTextWidth
      xPosition + halfOfTextWidth > bounds.right -> bounds.right - halfOfTextWidth
      else -> xPosition
    }

  protected fun CartesianDrawContext.drawGuideline(targets: List<CartesianMarker.Target>) {
    targets
      .map { it.canvasX }
      .toSet()
      .forEach { x -> guideline?.drawVertical(this, chartBounds.top, chartBounds.bottom, x) }
  }

  override fun updateInsets(
    context: CartesianMeasureContext,
    horizontalDimensions: HorizontalDimensions,
    insets: Insets,
  ) {
    with(context) {
      when (labelPosition) {
        LabelPosition.Top,
        LabelPosition.AbovePoint ->
          insets.ensureValuesAtLeast(top = label.getHeight(context) + label.tickSizeDp.pixels)
        LabelPosition.Bottom ->
          insets.ensureValuesAtLeast(bottom = label.getHeight(context) + label.tickSizeDp.pixels)
        LabelPosition.AroundPoint -> Unit // Will be inside the chart
      }
    }
  }

  /** Specifies the position of a [DefaultCartesianMarker]’s label. */
  public enum class LabelPosition {
    /** Positions the label at the top of the [CartesianChart]. Sufficient room is made. */
    Top,

    /** Positions the label at the bottom of the [CartesianChart]. Sufficient room is made. */
    Bottom,

    /**
     * Positions the label above the topmost marked point or, if there isn’t enough room, below it.
     */
    AroundPoint,

    /**
     * Positions the label above the topmost marked point. Sufficient room is made at the top of the
     * [CartesianChart].
     */
    AbovePoint,
  }
}
