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

package com.patrykandpatrick.vico.multiplatform.cartesian.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.multiplatform.common.Defaults
import com.patrykandpatrick.vico.multiplatform.common.Position
import com.patrykandpatrick.vico.multiplatform.common.averageOf
import com.patrykandpatrick.vico.multiplatform.common.component.Component
import com.patrykandpatrick.vico.multiplatform.common.component.LineComponent
import com.patrykandpatrick.vico.multiplatform.common.component.ShapeComponent
import com.patrykandpatrick.vico.multiplatform.common.component.TextComponent
import com.patrykandpatrick.vico.multiplatform.common.data.CacheStore
import com.patrykandpatrick.vico.multiplatform.common.doubled
import com.patrykandpatrick.vico.multiplatform.common.format
import com.patrykandpatrick.vico.multiplatform.common.half
import com.patrykandpatrick.vico.multiplatform.common.orZero
import com.patrykandpatrick.vico.multiplatform.common.shape.MarkerCorneredShape
import kotlin.math.ceil
import kotlin.math.min

/**
 * The default [CartesianMarker] implementation.
 *
 * @property label the [TextComponent] for the label.
 * @property valueFormatter formats the values.
 * @property labelPosition specifies the position of the label.
 * @property indicator returns a [Component] to be drawn at points with the given color.
 * @property indicatorSize the indicator size.
 * @property guideline drawn vertically through the marked points.
 */
public open class DefaultCartesianMarker(
  protected val label: TextComponent,
  protected val valueFormatter: ValueFormatter = ValueFormatter.default(),
  protected val labelPosition: LabelPosition = LabelPosition.Top,
  protected val indicator: ((Color) -> Component)? = null,
  protected val indicatorSize: Dp = Defaults.MARKER_INDICATOR_SIZE.dp,
  protected val guideline: LineComponent? = null,
) : CartesianMarker {

  protected val markerCorneredShape: MarkerCorneredShape? =
    (label.background as? ShapeComponent)?.shape as? MarkerCorneredShape

  protected val tickSize: Dp = markerCorneredShape?.tickSize.orZero

  override fun drawOverLayers(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>,
  ) {
    with(context) {
      drawGuideline(targets)
      val halfIndicatorSize = indicatorSize.pixels.half

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
  }

  protected open fun CartesianDrawingContext.drawIndicator(
    x: Float,
    y: Float,
    color: Color,
    halfIndicatorSize: Float,
  ) {
    val indicator = indicator ?: return
    cacheStore
      .getOrSet(keyNamespace, indicator, color) { indicator.invoke(color) }
      .draw(
        this,
        x - halfIndicatorSize,
        y - halfIndicatorSize,
        x + halfIndicatorSize,
        y + halfIndicatorSize,
      )
  }

  protected fun drawLabel(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>,
  ): Unit =
    with(context) {
      val text = valueFormatter.format(context, targets)
      val targetX = targets.averageOf { it.canvasX }
      val labelBounds = label.getBounds(context, text, layerBounds.width.toInt())
      val halfOfTextWidth = labelBounds.width.half
      val x = overrideXPositionToFit(targetX, layerBounds, halfOfTextWidth)
      markerCorneredShape?.tickX = targetX - x
      val tickPosition: MarkerCorneredShape.TickPosition
      val y: Float
      val verticalPosition: Position.Vertical
      when (labelPosition) {
        LabelPosition.Top -> {
          tickPosition = MarkerCorneredShape.TickPosition.Bottom
          y = context.layerBounds.top - tickSize.pixels
          verticalPosition = Position.Vertical.Top
        }

        LabelPosition.Bottom -> {
          tickPosition = MarkerCorneredShape.TickPosition.Top
          y = context.layerBounds.bottom + tickSize.pixels
          verticalPosition = Position.Vertical.Bottom
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
              topPointY - labelBounds.height - tickSize.pixels < context.layerBounds.top
          tickPosition =
            if (flip) MarkerCorneredShape.TickPosition.Top
            else MarkerCorneredShape.TickPosition.Bottom
          y = topPointY + (if (flip) 1 else -1) * tickSize.pixels
          verticalPosition = if (flip) Position.Vertical.Bottom else Position.Vertical.Top
        }
        LabelPosition.BelowPoint -> {
          val bottomPointY =
            targets.maxOf { target ->
              when (target) {
                is CandlestickCartesianLayerMarkerTarget -> target.lowCanvasY
                is ColumnCartesianLayerMarkerTarget ->
                  target.columns.maxOf(ColumnCartesianLayerMarkerTarget.Column::canvasY)
                is LineCartesianLayerMarkerTarget ->
                  target.points.maxOf(LineCartesianLayerMarkerTarget.Point::canvasY)
                else -> error("Unexpected `CartesianMarker.Target` implementation.")
              }
            }
          tickPosition = MarkerCorneredShape.TickPosition.Top
          y = bottomPointY + tickSize.pixels
          verticalPosition = Position.Vertical.Bottom
        }
      }
      markerCorneredShape?.tickPosition = tickPosition

      label.draw(
        context = context,
        text = text,
        x = x,
        y = y,
        verticalPosition = verticalPosition,
        maxWidth = ceil(min(layerBounds.right - x, x - layerBounds.left).doubled).toInt(),
      )
    }

  protected fun overrideXPositionToFit(
    xPosition: Float,
    bounds: Rect,
    halfOfTextWidth: Float,
  ): Float =
    when {
      xPosition - halfOfTextWidth < bounds.left -> bounds.left + halfOfTextWidth
      xPosition + halfOfTextWidth > bounds.right -> bounds.right - halfOfTextWidth
      else -> xPosition
    }

  protected fun CartesianDrawingContext.drawGuideline(targets: List<CartesianMarker.Target>) {
    targets
      .map { it.canvasX }
      .toSet()
      .forEach { x -> guideline?.drawVertical(this, x, layerBounds.top, layerBounds.bottom) }
  }

  override fun updateLayerMargins(
    context: CartesianMeasuringContext,
    layerMargins: CartesianLayerMargins,
    layerDimensions: CartesianLayerDimensions,
    model: CartesianChartModel,
  ) {
    with(context) {
      when (labelPosition) {
        LabelPosition.Top,
        LabelPosition.AbovePoint ->
          layerMargins.ensureValuesAtLeast(top = label.getHeight(context) + tickSize.pixels)

        LabelPosition.Bottom,
        LabelPosition.BelowPoint ->
          layerMargins.ensureValuesAtLeast(bottom = label.getHeight(context) + tickSize.pixels)

        LabelPosition.AroundPoint -> Unit // Will be inside the chart
      }
    }
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is DefaultCartesianMarker &&
        label == other.label &&
        valueFormatter == other.valueFormatter &&
        labelPosition == other.labelPosition &&
        indicator == other.indicator &&
        indicatorSize == other.indicatorSize &&
        guideline == other.guideline

  override fun hashCode(): Int {
    var result = label.hashCode()
    result = 31 * result + valueFormatter.hashCode()
    result = 31 * result + labelPosition.hashCode()
    result = 31 * result + indicator.hashCode()
    result = 31 * result + indicatorSize.hashCode()
    result = 31 * result + guideline.hashCode()
    return result
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

    /**
     * Positions the label below the bottommost marked point. Sufficient room is made at the bottom
     * of the [CartesianChart].
     */
    BelowPoint,
  }

  /** Formats [CartesianMarker] values for display. */
  public fun interface ValueFormatter {
    /** Returns a label for the given [CartesianMarker.Target]s. */
    public fun format(
      context: CartesianDrawingContext,
      targets: List<CartesianMarker.Target>,
    ): CharSequence

    /** Houses a [ValueFormatter] factory function. */
    public companion object {
      /**
       * Creates an instance of the default [ValueFormatter] implementation. The labels produced
       * include the [CartesianLayerModel.Entry] _y_ values, which are formatted to include up to
       * [decimalCount] decimal digits and, if [colorCode] is true, color-coded. Trailing zeros are
       * skipped.
       */
      public fun default(
        decimalCount: Int = 2,
        decimalSeparator: String = ".",
        thousandsSeparator: String = ",",
        prefix: String = "",
        suffix: String = "",
        colorCode: Boolean = true,
      ): ValueFormatter =
        DefaultValueFormatter(
          decimalCount,
          decimalSeparator,
          thousandsSeparator,
          prefix,
          suffix,
          colorCode,
        )
    }
  }

  protected companion object {
    public val keyNamespace: CacheStore.KeyNamespace = CacheStore.KeyNamespace()
  }
}

internal class DefaultValueFormatter(
  private val decimalCount: Int,
  private val decimalSeparator: String,
  private val thousandsSeparator: String,
  private val prefix: String,
  private val suffix: String,
  private val colorCode: Boolean,
) : DefaultCartesianMarker.ValueFormatter {
  private fun AnnotatedString.Builder.append(y: Double, color: Color? = null) {
    if (colorCode && color != null) {
      withStyle(SpanStyle(color = color)) {
        append(y.format(decimalCount, decimalSeparator, thousandsSeparator, prefix, suffix))
      }
    } else {
      append(y.format(decimalCount, decimalSeparator, thousandsSeparator, prefix, suffix))
    }
  }

  private fun AnnotatedString.Builder.append(target: CartesianMarker.Target, shorten: Boolean) {
    when (target) {
      is CandlestickCartesianLayerMarkerTarget -> {
        if (shorten) {
          append(target.entry.closing, target.closingColor)
        } else {
          append("O ")
          append(target.entry.opening, target.openingColor)
          append(", C ")
          append(target.entry.closing, target.closingColor)
          append(", L ")
          append(target.entry.low, target.lowColor)
          append(", H ")
          append(target.entry.high, target.highColor)
        }
      }

      is ColumnCartesianLayerMarkerTarget -> {
        val includeSum = target.columns.size > 1
        if (includeSum) {
          append(target.columns.sumOf { it.entry.y })
          append(" (")
        }
        target.columns.forEachIndexed { index, column ->
          append(column.entry.y, column.color)
          if (index != target.columns.lastIndex) append(", ")
        }
        if (includeSum) append(")")
      }

      is LineCartesianLayerMarkerTarget -> {
        target.points.forEachIndexed { index, point ->
          append(point.entry.y, point.color)
          if (index != target.points.lastIndex) append(", ")
        }
      }

      else -> throw IllegalArgumentException("Unexpected `CartesianMarker.Target` implementation.")
    }
  }

  override fun format(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>,
  ): CharSequence = buildAnnotatedString {
    targets.forEachIndexed { index, target ->
      append(target = target, shorten = targets.size > 1)
      if (index != targets.lastIndex) append(", ")
    }
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is DefaultValueFormatter &&
        decimalCount == other.decimalCount &&
        colorCode == other.colorCode

  override fun hashCode(): Int = 31 * decimalCount.hashCode() + colorCode.hashCode()
}

/** Creates and remembers a [DefaultCartesianMarker]. */
@Composable
public fun rememberDefaultCartesianMarker(
  label: TextComponent,
  valueFormatter: DefaultCartesianMarker.ValueFormatter = remember {
    DefaultCartesianMarker.ValueFormatter.default()
  },
  labelPosition: DefaultCartesianMarker.LabelPosition = DefaultCartesianMarker.LabelPosition.Top,
  indicator: ((Color) -> Component)? = null,
  indicatorSize: Dp = Defaults.MARKER_INDICATOR_SIZE.dp,
  guideline: LineComponent? = null,
): DefaultCartesianMarker =
  remember(label, valueFormatter, labelPosition, indicator, indicatorSize, guideline) {
    DefaultCartesianMarker(
      label = label,
      valueFormatter = valueFormatter,
      labelPosition = labelPosition,
      indicator = indicator,
      indicatorSize = indicatorSize,
      guideline = guideline,
    )
  }
