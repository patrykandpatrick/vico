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

package com.patrykandpatrick.vico.core.cartesian.axis

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.ceil
import com.patrykandpatrick.vico.core.common.doubled
import com.patrykandpatrick.vico.core.common.getStart
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.isBoundOf
import com.patrykandpatrick.vico.core.common.orZero
import kotlin.math.min

/**
 * An implementation of [Axis] used for horizontal axes. This class extends [BaseAxis].
 *
 * @see Axis
 * @see BaseAxis
 */
public open class HorizontalAxis<Position : AxisPosition.Horizontal>(
  override val position: Position
) : BaseAxis<Position>() {
  protected val AxisPosition.Horizontal.textVerticalPosition: VerticalPosition
    get() = if (isBottom) VerticalPosition.Bottom else VerticalPosition.Top

  /**
   * Determines for what _x_ values this [HorizontalAxis] is to display labels, ticks, and
   * guidelines.
   */
  public var itemPlacer: AxisItemPlacer.Horizontal = AxisItemPlacer.Horizontal.default()

  override fun drawBehindChart(context: CartesianDrawContext): Unit =
    with(context) {
      val clipRestoreCount = canvas.save()
      val tickMarkTop =
        if (position.isBottom) bounds.top else bounds.bottom - axisThickness - tickLength
      val tickMarkBottom = tickMarkTop + axisThickness + tickLength
      val fullXRange = getFullXRange(horizontalDimensions)
      val maxLabelWidth = getMaxLabelWidth(horizontalDimensions, fullXRange)

      canvas.clipRect(
        bounds.left -
          itemPlacer.getStartHorizontalAxisInset(
            this,
            horizontalDimensions,
            tickThickness,
            maxLabelWidth,
          ),
        minOf(bounds.top, chartBounds.top),
        bounds.right +
          itemPlacer.getEndHorizontalAxisInset(
            this,
            horizontalDimensions,
            tickThickness,
            maxLabelWidth,
          ),
        maxOf(bounds.bottom, chartBounds.bottom),
      )

      val textY = if (position.isBottom) tickMarkBottom else tickMarkTop
      val baseCanvasX =
        bounds.getStart(isLtr) - scroll +
          horizontalDimensions.startPadding * layoutDirectionMultiplier
      val firstVisibleX =
        fullXRange.start +
          scroll / horizontalDimensions.xSpacing * chartValues.xStep * layoutDirectionMultiplier
      val lastVisibleX =
        firstVisibleX + bounds.width() / horizontalDimensions.xSpacing * chartValues.xStep
      val visibleXRange = firstVisibleX..lastVisibleX
      val labelValues = itemPlacer.getLabelValues(this, visibleXRange, fullXRange, maxLabelWidth)
      val lineValues = itemPlacer.getLineValues(this, visibleXRange, fullXRange, maxLabelWidth)

      labelValues.forEachIndexed { index, x ->
        val canvasX =
          baseCanvasX +
            (x - chartValues.minX) / chartValues.xStep *
              horizontalDimensions.xSpacing *
              layoutDirectionMultiplier
        val previousX = labelValues.getOrNull(index - 1) ?: (fullXRange.start.doubled - x)
        val nextX = labelValues.getOrNull(index + 1) ?: (fullXRange.endInclusive.doubled - x)
        val maxWidth =
          (min(x - previousX, nextX - x) / chartValues.xStep * horizontalDimensions.xSpacing)
            .ceil
            .toInt()

        label?.drawText(
          context = context,
          text =
            valueFormatter.format(
              value = x,
              chartValues = chartValues,
              verticalAxisPosition = null,
            ),
          textX = canvasX,
          textY = textY,
          verticalPosition = position.textVerticalPosition,
          maxTextWidth = maxWidth,
          maxTextHeight = (bounds.height() - tickLength - axisThickness.half).toInt(),
          rotationDegrees = labelRotationDegrees,
        )

        if (lineValues == null) {
          tick?.drawVertical(
            context = this,
            top = tickMarkTop,
            bottom = tickMarkBottom,
            centerX = canvasX + getLinesCorrectionX(x, fullXRange),
          )
        }
      }

      lineValues?.forEach { x ->
        tick?.drawVertical(
          context = this,
          top = tickMarkTop,
          bottom = tickMarkBottom,
          centerX =
            baseCanvasX +
              (x - chartValues.minX) / chartValues.xStep *
                horizontalDimensions.xSpacing *
                layoutDirectionMultiplier +
              getLinesCorrectionX(x, fullXRange),
        )
      }

      val axisLineExtend =
        if (itemPlacer.getShiftExtremeTicks(context)) {
          tickThickness
        } else {
          tickThickness.half
        }

      axisLine?.drawHorizontal(
        context = context,
        left = chartBounds.left - axisLineExtend,
        right = chartBounds.right + axisLineExtend,
        centerY =
          if (position.isBottom) bounds.top + axisThickness.half
          else bounds.bottom - axisThickness.half,
      )

      title?.let { title ->
        titleComponent?.drawText(
          context = context,
          textX = bounds.centerX(),
          textY = if (position.isTop) bounds.top else bounds.bottom,
          verticalPosition = if (position.isTop) VerticalPosition.Bottom else VerticalPosition.Top,
          maxTextWidth = bounds.width().toInt(),
          text = title,
        )
      }

      if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)

      drawGuidelines(context, baseCanvasX, fullXRange, labelValues, lineValues)
    }

  protected open fun drawGuidelines(
    context: CartesianDrawContext,
    baseCanvasX: Float,
    fullXRange: ClosedFloatingPointRange<Float>,
    labelValues: List<Float>,
    lineValues: List<Float>?,
  ): Unit =
    with(context) {
      val guideline = guideline ?: return
      val clipRestoreCount = canvas.save()
      canvas.clipRect(chartBounds)

      if (lineValues == null) {
        labelValues.forEach { x ->
          val canvasX =
            baseCanvasX +
              (x - chartValues.minX) / chartValues.xStep *
                horizontalDimensions.xSpacing *
                layoutDirectionMultiplier

          guideline
            .takeUnless { x.isBoundOf(fullXRange) }
            ?.drawVertical(this, chartBounds.top, chartBounds.bottom, canvasX)
        }
      } else {
        lineValues.forEach { x ->
          val canvasX =
            baseCanvasX +
              (x - chartValues.minX) / chartValues.xStep *
                horizontalDimensions.xSpacing *
                layoutDirectionMultiplier +
              getLinesCorrectionX(x, fullXRange)

          guideline
            .takeUnless { x.isBoundOf(fullXRange) }
            ?.drawVertical(this, chartBounds.top, chartBounds.bottom, canvasX)
        }
      }

      if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)
    }

  protected fun CartesianDrawContext.getLinesCorrectionX(
    entryX: Float,
    fullXRange: ClosedFloatingPointRange<Float>,
  ): Float =
    when {
      itemPlacer.getShiftExtremeTicks(this).not() -> 0f
      entryX == fullXRange.start -> -tickThickness.half
      entryX == fullXRange.endInclusive -> tickThickness.half
      else -> 0f
    } * layoutDirectionMultiplier

  override fun drawAboveChart(context: CartesianDrawContext): Unit = Unit

  override fun updateHorizontalDimensions(
    context: CartesianMeasureContext,
    horizontalDimensions: MutableHorizontalDimensions,
  ) {
    val label = label ?: return
    val chartValues = context.chartValues
    val maxLabelWidth =
      context.getMaxLabelWidth(horizontalDimensions, context.getFullXRange(horizontalDimensions))
    val firstLabelValue = itemPlacer.getFirstLabelValue(context, maxLabelWidth)
    val lastLabelValue = itemPlacer.getLastLabelValue(context, maxLabelWidth)
    if (firstLabelValue != null) {
      val text =
        valueFormatter.format(
          value = firstLabelValue,
          chartValues = chartValues,
          verticalAxisPosition = null,
        )
      var unscalableStartPadding =
        label
          .getWidth(
            context = context,
            text = text,
            rotationDegrees = labelRotationDegrees,
            pad = true,
          )
          .half
      if (!context.zoomEnabled) {
        unscalableStartPadding -=
          (firstLabelValue - chartValues.minX) * horizontalDimensions.xSpacing
      }
      horizontalDimensions.ensureValuesAtLeast(unscalableStartPadding = unscalableStartPadding)
    }
    if (lastLabelValue != null) {
      val text =
        valueFormatter.format(
          value = lastLabelValue,
          chartValues = chartValues,
          verticalAxisPosition = null,
        )
      var unscalableEndPadding =
        label
          .getWidth(
            context = context,
            text = text,
            rotationDegrees = labelRotationDegrees,
            pad = true,
          )
          .half
      if (!context.zoomEnabled) {
        unscalableEndPadding -= (chartValues.maxX - lastLabelValue) * horizontalDimensions.xSpacing
      }
      horizontalDimensions.ensureValuesAtLeast(unscalableEndPadding = unscalableEndPadding)
    }
  }

  override fun updateInsets(
    context: CartesianMeasureContext,
    horizontalDimensions: HorizontalDimensions,
    insets: Insets,
  ) {
    val maxLabelWidth =
      context.getMaxLabelWidth(horizontalDimensions, context.getFullXRange(horizontalDimensions))
    val height = getHeight(context, horizontalDimensions, maxLabelWidth)
    insets.ensureValuesAtLeast(
      itemPlacer.getStartHorizontalAxisInset(
        context,
        horizontalDimensions,
        context.tickThickness,
        maxLabelWidth,
      ),
      itemPlacer.getEndHorizontalAxisInset(
        context,
        horizontalDimensions,
        context.tickThickness,
        maxLabelWidth,
      ),
    )
    when {
      position.isTop -> insets.ensureValuesAtLeast(top = height)
      position.isBottom -> insets.ensureValuesAtLeast(bottom = height)
    }
  }

  protected fun CartesianMeasureContext.getFullXRange(
    horizontalDimensions: HorizontalDimensions
  ): ClosedFloatingPointRange<Float> =
    with(horizontalDimensions) {
      val start = chartValues.minX - startPadding / xSpacing * chartValues.xStep
      val end = chartValues.maxX + endPadding / xSpacing * chartValues.xStep
      start..end
    }

  protected open fun getHeight(
    context: CartesianMeasureContext,
    horizontalDimensions: HorizontalDimensions,
    maxLabelWidth: Float,
  ): Float =
    with(context) {
      val fullXRange = getFullXRange(horizontalDimensions)

      when (val constraint = sizeConstraint) {
        is SizeConstraint.Auto -> {
          val labelHeight = getMaxLabelHeight(horizontalDimensions, fullXRange, maxLabelWidth)
          val titleComponentHeight =
            title
              ?.let { title ->
                titleComponent?.getHeight(
                  context = context,
                  width = bounds.width().toInt(),
                  text = title,
                )
              }
              .orZero
          (labelHeight +
              titleComponentHeight +
              (if (position.isBottom) axisThickness else 0f) +
              tickLength)
            .coerceAtMost(maximumValue = canvasBounds.height() / MAX_HEIGHT_DIVISOR)
            .coerceIn(
              minimumValue = constraint.minSizeDp.pixels,
              maximumValue = constraint.maxSizeDp.pixels,
            )
        }
        is SizeConstraint.Exact -> constraint.sizeDp.pixels
        is SizeConstraint.Fraction -> canvasBounds.height() * constraint.fraction
        is SizeConstraint.TextWidth ->
          label
            ?.getHeight(
              context = this,
              text = constraint.text,
              rotationDegrees = labelRotationDegrees,
            )
            .orZero
      }
    }

  protected fun CartesianMeasureContext.getMaxLabelWidth(
    horizontalDimensions: HorizontalDimensions,
    fullXRange: ClosedFloatingPointRange<Float>,
  ): Float {
    val label = label ?: return 0f
    return itemPlacer
      .getWidthMeasurementLabelValues(this, horizontalDimensions, fullXRange)
      .maxOfOrNull { value ->
        val text =
          valueFormatter.format(
            value = value,
            chartValues = chartValues,
            verticalAxisPosition = null,
          )
        label.getWidth(
          context = this,
          text = text,
          rotationDegrees = labelRotationDegrees,
          pad = true,
        )
      }
      .orZero
  }

  protected fun CartesianMeasureContext.getMaxLabelHeight(
    horizontalDimensions: HorizontalDimensions,
    fullXRange: ClosedFloatingPointRange<Float>,
    maxLabelWidth: Float,
  ): Float {
    val label = label ?: return 0f
    return itemPlacer
      .getHeightMeasurementLabelValues(this, horizontalDimensions, fullXRange, maxLabelWidth)
      .maxOf { value ->
        val text =
          valueFormatter.format(
            value = value,
            chartValues = chartValues,
            verticalAxisPosition = null,
          )
        label.getHeight(
          context = this,
          text = text,
          rotationDegrees = labelRotationDegrees,
          pad = true,
        )
      }
  }

  /**
   * Creates [HorizontalAxis] instances. It’s recommended to use this via [HorizontalAxis.build].
   */
  public class Builder<Position : AxisPosition.Horizontal>(
    builder: BaseAxis.Builder<Position>? = null
  ) : BaseAxis.Builder<Position>(builder) {
    /**
     * Determines for what _x_ values the [HorizontalAxis] is to display labels, ticks, and
     * guidelines.
     */
    public var itemPlacer: AxisItemPlacer.Horizontal = AxisItemPlacer.Horizontal.default()

    /** Creates a [HorizontalAxis] instance with the properties from this [Builder]. */
    @Suppress("UNCHECKED_CAST")
    public inline fun <reified T : Position> build(): HorizontalAxis<T> {
      val position =
        when (T::class.java) {
          AxisPosition.Horizontal.Top::class.java -> AxisPosition.Horizontal.Top
          AxisPosition.Horizontal.Bottom::class.java -> AxisPosition.Horizontal.Bottom
          else ->
            throw IllegalStateException("Got unknown AxisPosition class ${T::class.java.name}")
        }
          as Position
      return setTo(HorizontalAxis(position = position)).also { it.itemPlacer = itemPlacer }
        as HorizontalAxis<T>
    }
  }

  /** Houses a [HorizontalAxis] factory function. */
  public companion object {
    private const val MAX_HEIGHT_DIVISOR = 3f

    /** Creates a [HorizontalAxis] via [Builder]. */
    public inline fun <reified P : AxisPosition.Horizontal> build(
      block: Builder<P>.() -> Unit = {}
    ): HorizontalAxis<P> = Builder<P>().apply(block).build()
  }
}
