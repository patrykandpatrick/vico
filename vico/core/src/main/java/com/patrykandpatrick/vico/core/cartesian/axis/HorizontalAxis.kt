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

import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ChartValues
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.doubled
import com.patrykandpatrick.vico.core.common.getStart
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.isBoundOf
import com.patrykandpatrick.vico.core.common.orZero
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Draws horizontal axes. See the [BaseAxis] documentation for descriptions of the inherited
 * properties.
 *
 * @property itemPlacer determines for what _x_ values the [HorizontalAxis] displays labels, ticks,
 *   and guidelines.
 */
public open class HorizontalAxis<P : Axis.Position.Horizontal>
protected constructor(
  override val position: P,
  line: LineComponent?,
  label: TextComponent?,
  labelRotationDegrees: Float,
  valueFormatter: CartesianValueFormatter,
  tick: LineComponent?,
  tickLengthDp: Float,
  guideline: LineComponent?,
  public var itemPlacer: ItemPlacer,
  sizeConstraint: SizeConstraint,
  titleComponent: TextComponent?,
  title: CharSequence?,
) :
  BaseAxis<P>(
    line,
    label,
    labelRotationDegrees,
    valueFormatter,
    tick,
    tickLengthDp,
    guideline,
    sizeConstraint,
    titleComponent,
    title,
  ) {
  protected val Axis.Position.Horizontal.textVerticalPosition: VerticalPosition
    get() =
      when (this) {
        Axis.Position.Horizontal.Top -> VerticalPosition.Top
        Axis.Position.Horizontal.Bottom -> VerticalPosition.Bottom
      }

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public constructor(
    position: P,
    itemPlacer: ItemPlacer,
  ) : this(
    position = position,
    line = null,
    label = null,
    labelRotationDegrees = 0f,
    valueFormatter = CartesianValueFormatter.decimal(),
    tick = null,
    tickLengthDp = 0f,
    guideline = null,
    itemPlacer = itemPlacer,
    sizeConstraint = SizeConstraint.Auto(),
    titleComponent = null,
    title = null,
  )

  override fun drawUnderLayers(context: CartesianDrawContext) {
    with(context) {
      val clipRestoreCount = canvas.save()
      val tickTop =
        if (position == Axis.Position.Horizontal.Top) {
          bounds.bottom - lineThickness - tickLength
        } else {
          bounds.top
        }
      val tickBottom = tickTop + lineThickness + tickLength
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
        min(bounds.top, layerBounds.top),
        bounds.right +
          itemPlacer.getEndHorizontalAxisInset(
            this,
            horizontalDimensions,
            tickThickness,
            maxLabelWidth,
          ),
        max(bounds.bottom, layerBounds.bottom),
      )

      val textY = if (position == Axis.Position.Horizontal.Top) tickTop else tickBottom
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
            ((x - chartValues.minX) / chartValues.xStep).toFloat() *
              horizontalDimensions.xSpacing *
              layoutDirectionMultiplier
        val previousX = labelValues.getOrNull(index - 1) ?: (fullXRange.start.doubled - x)
        val nextX = labelValues.getOrNull(index + 1) ?: (fullXRange.endInclusive.doubled - x)
        val maxWidth =
          ceil(min(x - previousX, nextX - x) / chartValues.xStep * horizontalDimensions.xSpacing)
            .toInt()

        label?.draw(
          context = context,
          text =
            valueFormatter.format(
              value = x,
              chartValues = chartValues,
              verticalAxisPosition = null,
            ),
          x = canvasX,
          y = textY,
          verticalPosition = position.textVerticalPosition,
          maxWidth = maxWidth,
          maxHeight = (bounds.height() - tickLength - lineThickness.half).toInt(),
          rotationDegrees = labelRotationDegrees,
        )

        if (lineValues == null) {
          tick?.drawVertical(
            context = this,
            top = tickTop,
            bottom = tickBottom,
            centerX = canvasX + getLinesCorrectionX(x, fullXRange),
          )
        }
      }

      lineValues?.forEach { x ->
        tick?.drawVertical(
          context = this,
          top = tickTop,
          bottom = tickBottom,
          centerX =
            baseCanvasX +
              ((x - chartValues.minX) / chartValues.xStep).toFloat() *
                horizontalDimensions.xSpacing *
                layoutDirectionMultiplier +
              getLinesCorrectionX(x, fullXRange),
        )
      }

      val lineExtensionLength =
        if (itemPlacer.getShiftExtremeLines(context)) {
          tickThickness
        } else {
          tickThickness.half
        }

      line?.drawHorizontal(
        context = context,
        left = layerBounds.left - lineExtensionLength,
        right = layerBounds.right + lineExtensionLength,
        centerY =
          if (position == Axis.Position.Horizontal.Top) {
            bounds.bottom - lineThickness.half
          } else {
            bounds.top + lineThickness.half
          },
      )

      title?.let { title ->
        titleComponent?.draw(
          context = context,
          x = bounds.centerX(),
          y = if (position == Axis.Position.Horizontal.Top) bounds.top else bounds.bottom,
          verticalPosition =
            if (position == Axis.Position.Horizontal.Top) {
              VerticalPosition.Bottom
            } else {
              VerticalPosition.Top
            },
          maxWidth = bounds.width().toInt(),
          text = title,
        )
      }

      if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)

      drawGuidelines(context, baseCanvasX, fullXRange, labelValues, lineValues)
    }
  }

  protected open fun drawGuidelines(
    context: CartesianDrawContext,
    baseCanvasX: Float,
    fullXRange: ClosedFloatingPointRange<Double>,
    labelValues: List<Double>,
    lineValues: List<Double>?,
  ): Unit =
    with(context) {
      val guideline = guideline ?: return
      val clipRestoreCount = canvas.save()
      canvas.clipRect(layerBounds)

      if (lineValues == null) {
        labelValues.forEach { x ->
          val canvasX =
            baseCanvasX +
              ((x - chartValues.minX) / chartValues.xStep).toFloat() *
                horizontalDimensions.xSpacing *
                layoutDirectionMultiplier

          guideline
            .takeUnless { x.isBoundOf(fullXRange) }
            ?.drawVertical(this, layerBounds.top, layerBounds.bottom, canvasX)
        }
      } else {
        lineValues.forEach { x ->
          val canvasX =
            baseCanvasX +
              ((x - chartValues.minX) / chartValues.xStep).toFloat() *
                horizontalDimensions.xSpacing *
                layoutDirectionMultiplier +
              getLinesCorrectionX(x, fullXRange)

          guideline
            .takeUnless { x.isBoundOf(fullXRange) }
            ?.drawVertical(this, layerBounds.top, layerBounds.bottom, canvasX)
        }
      }

      if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)
    }

  protected fun CartesianDrawContext.getLinesCorrectionX(
    entryX: Double,
    fullXRange: ClosedFloatingPointRange<Double>,
  ): Float =
    when {
      itemPlacer.getShiftExtremeLines(this).not() -> 0f
      entryX == fullXRange.start -> -tickThickness.half
      entryX == fullXRange.endInclusive -> tickThickness.half
      else -> 0f
    } * layoutDirectionMultiplier

  override fun drawOverLayers(context: CartesianDrawContext) {}

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
          (firstLabelValue - chartValues.minX).toFloat() * horizontalDimensions.xSpacing
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
        unscalableEndPadding -=
          ((chartValues.maxX - lastLabelValue) * horizontalDimensions.xSpacing).toFloat()
      }
      horizontalDimensions.ensureValuesAtLeast(unscalableEndPadding = unscalableEndPadding)
    }
  }

  override fun updateInsets(
    context: CartesianMeasureContext,
    horizontalDimensions: HorizontalDimensions,
    model: CartesianChartModel,
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
    when (position) {
      Axis.Position.Horizontal.Top -> insets.ensureValuesAtLeast(top = height)
      Axis.Position.Horizontal.Bottom -> insets.ensureValuesAtLeast(bottom = height)
    }
  }

  protected fun CartesianMeasureContext.getFullXRange(
    horizontalDimensions: HorizontalDimensions
  ): ClosedFloatingPointRange<Double> =
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
                  maxWidth = bounds.width().toInt(),
                  text = title,
                )
              }
              .orZero
          (labelHeight +
              titleComponentHeight +
              (if (position == Axis.Position.Horizontal.Bottom) lineThickness else 0f) +
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
    fullXRange: ClosedFloatingPointRange<Double>,
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
    fullXRange: ClosedFloatingPointRange<Double>,
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

  /** Determines for what _x_ values a [HorizontalAxis] displays labels, ticks, and guidelines. */
  public interface ItemPlacer {
    /**
     * Returns a boolean indicating whether to shift the lines whose _x_ values are equal to
     * [ChartValues.minX] or [ChartValues.maxX], if such lines are present, outward. The
     * [ChartValues.minX] lines are then positioned immediately before the start edge of the
     * [CartesianLayer] bounds, and the [ChartValues.maxX] lines are positioned immediately after
     * these bounds’ end edge. On either side, if a [VerticalAxis] is present, the shifted tick is
     * aligned with it, and the shifted guideline is hidden.
     */
    public fun getShiftExtremeLines(context: CartesianDrawContext): Boolean = true

    /**
     * If the [HorizontalAxis] is to reserve room for the first label, returns the first label’s _x_
     * value. Otherwise, returns `null`.
     */
    public fun getFirstLabelValue(context: CartesianMeasureContext, maxLabelWidth: Float): Double? =
      null

    /**
     * If the [HorizontalAxis] is to reserve room for the last label, returns the last label’s _x_
     * value. Otherwise, returns `null`.
     */
    public fun getLastLabelValue(context: CartesianMeasureContext, maxLabelWidth: Float): Double? =
      null

    /**
     * Returns, as a list, the _x_ values for which labels are to be displayed, restricted to
     * [visibleXRange] and with two extra values on either side (if applicable).
     */
    public fun getLabelValues(
      context: CartesianDrawContext,
      visibleXRange: ClosedFloatingPointRange<Double>,
      fullXRange: ClosedFloatingPointRange<Double>,
      maxLabelWidth: Float,
    ): List<Double>

    /**
     * Returns, as a list, the _x_ values for which the [HorizontalAxis] is to create labels and
     * measure their widths during the measuring phase. The width of the widest label is passed to
     * other functions.
     */
    public fun getWidthMeasurementLabelValues(
      context: CartesianMeasureContext,
      horizontalDimensions: HorizontalDimensions,
      fullXRange: ClosedFloatingPointRange<Double>,
    ): List<Double>

    /**
     * Returns, as a list, the _x_ values for which the [HorizontalAxis] is to create labels and
     * measure their heights during the measuring phase. This affects how much vertical space the
     * [HorizontalAxis] requests.
     */
    public fun getHeightMeasurementLabelValues(
      context: CartesianMeasureContext,
      horizontalDimensions: HorizontalDimensions,
      fullXRange: ClosedFloatingPointRange<Double>,
      maxLabelWidth: Float,
    ): List<Double>

    /**
     * Returns, as a list, the _x_ values for which ticks and guidelines are to be displayed,
     * restricted to [visibleXRange] and with an extra value on either side (if applicable). If
     * `null` is returned, the values returned by [getLabelValues] are used.
     */
    public fun getLineValues(
      context: CartesianDrawContext,
      visibleXRange: ClosedFloatingPointRange<Double>,
      fullXRange: ClosedFloatingPointRange<Double>,
      maxLabelWidth: Float,
    ): List<Double>? = null

    /** Returns the start inset required by the [HorizontalAxis]. */
    public fun getStartHorizontalAxisInset(
      context: CartesianMeasureContext,
      horizontalDimensions: HorizontalDimensions,
      tickThickness: Float,
      maxLabelWidth: Float,
    ): Float

    /** Returns the end inset required by the [HorizontalAxis]. */
    public fun getEndHorizontalAxisInset(
      context: CartesianMeasureContext,
      horizontalDimensions: HorizontalDimensions,
      tickThickness: Float,
      maxLabelWidth: Float,
    ): Float

    /** Houses an [ItemPlacer] factory function. */
    public companion object {
      /**
       * Creates a base [ItemPlacer] implementation. [spacing] defines how often items should be
       * drawn (relative to [ChartValues.xStep]). [offset] is the number of labels (and, for
       * [HorizontalLayout.FullWidth], their corresponding ticks and guidelines) to skip from the
       * start. [shiftExtremeTicks] defines whether ticks whose _x_ values are bounds of the
       * _x_-axis value range should be shifted to the edges of the axis bounds, to be aligned with
       * the vertical axes. [addExtremeLabelPadding] specifies whether, for
       * [HorizontalLayout.FullWidth], padding should be added for the first and last labels,
       * ensuring their visibility.
       */
      public fun default(
        spacing: Int = 1,
        offset: Int = 0,
        shiftExtremeTicks: Boolean = true,
        addExtremeLabelPadding: Boolean = false,
      ): ItemPlacer =
        DefaultHorizontalAxisItemPlacer(spacing, offset, shiftExtremeTicks, addExtremeLabelPadding)
    }
  }

  /** Houses [HorizontalAxis] factory functions. */
  public companion object {
    private const val MAX_HEIGHT_DIVISOR = 3f

    /** Creates a top [HorizontalAxis]. */
    public fun top(
      line: LineComponent? = null,
      label: TextComponent? = null,
      labelRotationDegrees: Float = 0f,
      valueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
      tick: LineComponent? = null,
      tickLengthDp: Float = 0f,
      guideline: LineComponent? = null,
      itemPlacer: ItemPlacer = ItemPlacer.default(),
      sizeConstraint: SizeConstraint = SizeConstraint.Auto(),
      titleComponent: TextComponent? = null,
      title: CharSequence? = null,
    ): HorizontalAxis<Axis.Position.Horizontal.Top> =
      HorizontalAxis(
        Axis.Position.Horizontal.Top,
        line,
        label,
        labelRotationDegrees,
        valueFormatter,
        tick,
        tickLengthDp,
        guideline,
        itemPlacer,
        sizeConstraint,
        titleComponent,
        title,
      )

    /** Creates a bottom [HorizontalAxis]. */
    public fun bottom(
      line: LineComponent? = null,
      label: TextComponent? = null,
      labelRotationDegrees: Float = 0f,
      valueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
      tick: LineComponent? = null,
      tickLengthDp: Float = 0f,
      guideline: LineComponent? = null,
      itemPlacer: ItemPlacer = ItemPlacer.default(),
      sizeConstraint: SizeConstraint = SizeConstraint.Auto(),
      titleComponent: TextComponent? = null,
      title: CharSequence? = null,
    ): HorizontalAxis<Axis.Position.Horizontal.Bottom> =
      HorizontalAxis(
        Axis.Position.Horizontal.Bottom,
        line,
        label,
        labelRotationDegrees,
        valueFormatter,
        tick,
        tickLengthDp,
        guideline,
        itemPlacer,
        sizeConstraint,
        titleComponent,
        title,
      )
  }
}
