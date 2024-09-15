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
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis.HorizontalLabelPosition.Inside
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis.HorizontalLabelPosition.Outside
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis.VerticalLabelPosition.Center
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.formatForAxis
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.common.HorizontalInsets
import com.patrykandpatrick.vico.core.common.HorizontalPosition
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.orZero
import com.patrykandpatrick.vico.core.common.translate
import kotlin.math.ceil
import kotlin.math.max

private const val TITLE_ABS_ROTATION_DEGREES = 90f

/**
 * Draws vertical axes. See the [BaseAxis] documentation for descriptions of the inherited
 * properties.
 *
 * @property itemPlacer determines for what _y_ values the [VerticalAxis] displays labels, ticks,
 *   and guidelines.
 * @property horizontalLabelPosition defines the horizontal position of the labels relative to the
 *   axis line.
 * @property verticalLabelPosition defines the vertical positions of the labels relative to their
 *   ticks.
 */
public open class VerticalAxis<P : Axis.Position.Vertical>
protected constructor(
  override val position: P,
  line: LineComponent?,
  label: TextComponent?,
  labelRotationDegrees: Float,
  public val horizontalLabelPosition: HorizontalLabelPosition,
  public val verticalLabelPosition: VerticalLabelPosition,
  valueFormatter: CartesianValueFormatter,
  tick: LineComponent?,
  tickLengthDp: Float,
  guideline: LineComponent?,
  public val itemPlacer: ItemPlacer,
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
  protected val areLabelsOutsideAtStartOrInsideAtEnd: Boolean
    get() =
      position == Axis.Position.Vertical.Start && horizontalLabelPosition == Outside ||
        position == Axis.Position.Vertical.End && horizontalLabelPosition == Inside

  protected val textHorizontalPosition: HorizontalPosition
    get() =
      if (areLabelsOutsideAtStartOrInsideAtEnd) HorizontalPosition.Start else HorizontalPosition.End

  protected var maxLabelWidth: Float? = null

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public constructor(
    position: P,
    line: LineComponent?,
    label: TextComponent?,
    labelRotationDegrees: Float,
    horizontalLabelPosition: HorizontalLabelPosition,
    verticalLabelPosition: VerticalLabelPosition,
    tick: LineComponent?,
    tickLengthDp: Float,
    guideline: LineComponent?,
    itemPlacer: ItemPlacer,
    titleComponent: TextComponent?,
    title: CharSequence?,
  ) : this(
    position,
    line,
    label,
    labelRotationDegrees,
    horizontalLabelPosition,
    verticalLabelPosition,
    CartesianValueFormatter.decimal(),
    tick,
    tickLengthDp,
    guideline,
    itemPlacer,
    SizeConstraint.Auto(),
    titleComponent,
    title,
  )

  override fun drawUnderLayers(context: CartesianDrawingContext) {
    with(context) {
      var centerY: Float
      val yRange = ranges.getYRange(position)
      val maxLabelHeight = getMaxLabelHeight()
      val lineValues =
        itemPlacer.getLineValues(this, bounds.height(), maxLabelHeight, position)
          ?: itemPlacer.getLabelValues(this, bounds.height(), maxLabelHeight, position)

      lineValues.forEach { lineValue ->
        centerY =
          bounds.bottom - bounds.height() * ((lineValue - yRange.minY) / yRange.length).toFloat() +
            getLineCanvasYCorrection(guidelineThickness, lineValue)

        guideline
          ?.takeIf {
            isNotInRestrictedBounds(
              left = layerBounds.left,
              top = centerY - guidelineThickness.half,
              right = layerBounds.right,
              bottom = centerY + guidelineThickness.half,
            )
          }
          ?.drawHorizontal(
            context = context,
            left = layerBounds.left,
            right = layerBounds.right,
            centerY = centerY,
          )
      }
      val lineExtensionLength = if (itemPlacer.getShiftTopLines(this)) tickThickness else 0f
      line?.drawVertical(
        context = context,
        top = bounds.top - lineExtensionLength,
        bottom = bounds.bottom + lineExtensionLength,
        centerX =
          if (position.isLeft(this)) {
            bounds.right - lineThickness.half
          } else {
            bounds.left + lineThickness.half
          },
      )
    }
  }

  override fun drawOverLayers(context: CartesianDrawingContext) {
    with(context) {
      val label = label
      val labelValues =
        itemPlacer.getLabelValues(this, bounds.height(), getMaxLabelHeight(), position)
      val tickLeftX = getTickLeftX()
      val tickRightX = tickLeftX + lineThickness + tickLength
      val labelX = if (areLabelsOutsideAtStartOrInsideAtEnd == isLtr) tickLeftX else tickRightX
      var tickCenterY: Float
      val yRange = ranges.getYRange(position)

      labelValues.forEach { labelValue ->
        tickCenterY =
          bounds.bottom - bounds.height() * ((labelValue - yRange.minY) / yRange.length).toFloat() +
            getLineCanvasYCorrection(tickThickness, labelValue)

        tick?.drawHorizontal(
          context = context,
          left = tickLeftX,
          right = tickRightX,
          centerY = tickCenterY,
        )

        label ?: return@forEach
        drawLabel(
          context = this,
          labelComponent = label,
          label = valueFormatter.formatForAxis(this, labelValue, position),
          labelX = labelX,
          tickCenterY = tickCenterY,
        )
      }

      title?.let { title ->
        titleComponent?.draw(
          context = this,
          text = title,
          x = if (position.isLeft(this)) bounds.left else bounds.right,
          y = bounds.centerY(),
          horizontalPosition =
            if (position == Axis.Position.Vertical.Start) {
              HorizontalPosition.End
            } else {
              HorizontalPosition.Start
            },
          verticalPosition = VerticalPosition.Center,
          rotationDegrees =
            if (position == Axis.Position.Vertical.Start) {
              -TITLE_ABS_ROTATION_DEGREES
            } else {
              TITLE_ABS_ROTATION_DEGREES
            },
          maxHeight = bounds.height().toInt(),
        )
      }
    }
  }

  override fun updateHorizontalDimensions(
    context: CartesianMeasuringContext,
    horizontalDimensions: MutableHorizontalDimensions,
  ): Unit = Unit

  protected open fun drawLabel(
    context: CartesianDrawingContext,
    labelComponent: TextComponent,
    label: CharSequence,
    labelX: Float,
    tickCenterY: Float,
  ): Unit =
    with(context) {
      val textBounds =
        labelComponent
          .getBounds(context = this, text = label, rotationDegrees = labelRotationDegrees)
          .apply { translate(labelX, tickCenterY - centerY()) }

      if (
        horizontalLabelPosition == Outside ||
          isNotInRestrictedBounds(
            left = textBounds.left,
            top = textBounds.top,
            right = textBounds.right,
            bottom = textBounds.bottom,
          )
      ) {
        labelComponent.draw(
          context = this,
          text = label,
          x = labelX,
          y = tickCenterY,
          horizontalPosition = textHorizontalPosition,
          verticalPosition = verticalLabelPosition.textPosition,
          rotationDegrees = labelRotationDegrees,
          maxWidth = (maxLabelWidth ?: (layerBounds.width().half - tickLength)).toInt(),
        )
      }
    }

  protected fun CartesianMeasuringContext.getTickLeftX(): Float {
    val onLeft = position.isLeft(this)
    val base = if (onLeft) bounds.right else bounds.left
    return when {
      onLeft && horizontalLabelPosition == Outside -> base - lineThickness - tickLength
      onLeft && horizontalLabelPosition == Inside -> base - lineThickness
      horizontalLabelPosition == Outside -> base
      horizontalLabelPosition == Inside -> base - tickLength
      else -> error("Unexpected combination of axis position and label position")
    }
  }

  override fun updateHorizontalInsets(
    context: CartesianMeasuringContext,
    freeHeight: Float,
    model: CartesianChartModel,
    insets: HorizontalInsets,
  ) {
    val width = getWidth(context, freeHeight)
    when (position) {
      Axis.Position.Vertical.Start -> insets.ensureValuesAtLeast(start = width)
      Axis.Position.Vertical.End -> insets.ensureValuesAtLeast(end = width)
    }
  }

  override fun updateInsets(
    context: CartesianMeasuringContext,
    horizontalDimensions: HorizontalDimensions,
    model: CartesianChartModel,
    insets: Insets,
  ): Unit =
    with(context) {
      val maxLabelHeight = getMaxLabelHeight()
      val maxLineThickness = max(lineThickness, tickThickness)
      insets.ensureValuesAtLeast(
        top =
          itemPlacer.getTopVerticalAxisInset(
            context,
            verticalLabelPosition,
            maxLabelHeight,
            maxLineThickness,
          ),
        bottom =
          itemPlacer.getBottomVerticalAxisInset(
            context,
            verticalLabelPosition,
            maxLabelHeight,
            maxLineThickness,
          ),
      )
    }

  protected open fun getWidth(context: CartesianMeasuringContext, freeHeight: Float): Float =
    with(context) {
      when (val constraint = sizeConstraint) {
        is SizeConstraint.Auto -> {
          val titleComponentWidth =
            title
              ?.let { title ->
                titleComponent?.getWidth(
                  context = this,
                  text = title,
                  rotationDegrees = TITLE_ABS_ROTATION_DEGREES,
                  maxHeight = bounds.height().toInt(),
                )
              }
              .orZero
          val labelSpace =
            when (horizontalLabelPosition) {
              Outside -> ceil(getMaxLabelWidth(freeHeight)).also { maxLabelWidth = it } + tickLength
              Inside -> 0f
            }
          (labelSpace + titleComponentWidth + lineThickness).coerceIn(
            minimumValue = constraint.minSizeDp.pixels,
            maximumValue = constraint.maxSizeDp.pixels,
          )
        }
        is SizeConstraint.Exact -> constraint.sizeDp.pixels
        is SizeConstraint.Fraction -> canvasBounds.width() * constraint.fraction
        is SizeConstraint.TextWidth ->
          label
            ?.getWidth(
              context = this,
              text = constraint.text,
              rotationDegrees = labelRotationDegrees,
            )
            .orZero + tickLength + lineThickness.half
      }
    }

  protected fun CartesianMeasuringContext.getMaxLabelHeight(): Float =
    label
      ?.let { label ->
        itemPlacer.getHeightMeasurementLabelValues(this, position).maxOfOrNull { value ->
          label.getHeight(
            context = this,
            text = valueFormatter.formatForAxis(this, value, position),
            rotationDegrees = labelRotationDegrees,
          )
        }
      }
      .orZero

  protected fun CartesianMeasuringContext.getMaxLabelWidth(axisHeight: Float): Float =
    label
      ?.let { label ->
        itemPlacer
          .getWidthMeasurementLabelValues(this, axisHeight, getMaxLabelHeight(), position)
          .maxOfOrNull { value ->
            label.getWidth(
              context = this,
              text = valueFormatter.formatForAxis(this, value, position),
              rotationDegrees = labelRotationDegrees,
            )
          }
      }
      .orZero

  protected fun CartesianDrawingContext.getLineCanvasYCorrection(
    thickness: Float,
    y: Double,
  ): Float =
    if (y == ranges.getYRange(position).maxY && itemPlacer.getShiftTopLines(this)) {
      -thickness.half
    } else {
      thickness.half
    }

  /** Creates a new [VerticalAxis] based on this one. */
  public fun copy(
    line: LineComponent? = this.line,
    label: TextComponent? = this.label,
    labelRotationDegrees: Float = this.labelRotationDegrees,
    horizontalLabelPosition: HorizontalLabelPosition = this.horizontalLabelPosition,
    verticalLabelPosition: VerticalLabelPosition = this.verticalLabelPosition,
    valueFormatter: CartesianValueFormatter = this.valueFormatter,
    tick: LineComponent? = this.tick,
    tickLengthDp: Float = this.tickLengthDp,
    guideline: LineComponent? = this.guideline,
    itemPlacer: ItemPlacer = this.itemPlacer,
    sizeConstraint: SizeConstraint = this.sizeConstraint,
    titleComponent: TextComponent? = this.titleComponent,
    title: CharSequence? = this.title,
  ): VerticalAxis<P> =
    VerticalAxis(
      position,
      line,
      label,
      labelRotationDegrees,
      horizontalLabelPosition,
      verticalLabelPosition,
      valueFormatter,
      tick,
      tickLengthDp,
      guideline,
      itemPlacer,
      sizeConstraint,
      titleComponent,
      title,
    )

  /**
   * Defines the horizontal position of each of a vertical axis’s labels relative to the axis line.
   */
  public enum class HorizontalLabelPosition {
    Outside,
    Inside,
  }

  /**
   * Defines the vertical position of each of a horizontal axis’s labels relative to the label’s
   * corresponding tick.
   *
   * @param textPosition the label position.
   * @see VerticalPosition
   */
  public enum class VerticalLabelPosition(public val textPosition: VerticalPosition) {
    Center(VerticalPosition.Center),
    Top(VerticalPosition.Top),
    Bottom(VerticalPosition.Bottom),
  }

  /** Determines for what _y_ values a [VerticalAxis] displays labels, ticks, and guidelines. */
  public interface ItemPlacer {
    /**
     * Returns a boolean indicating whether to shift the lines whose _y_ values are equal to
     * [CartesianChartRanges.YRange.maxY], if such lines are present, such that they’re immediately
     * above the [CartesianLayer] bounds. If a top [HorizontalAxis] is present, the shifted tick
     * will then be aligned with the axis line, and the shifted guideline will be hidden.
     */
    public fun getShiftTopLines(context: CartesianDrawingContext): Boolean = true

    /** Returns, as a list, the _y_ values for which labels are to be displayed. */
    public fun getLabelValues(
      context: CartesianDrawingContext,
      axisHeight: Float,
      maxLabelHeight: Float,
      position: Axis.Position.Vertical,
    ): List<Double>

    /**
     * Returns, as a list, the _y_ values for which the [VerticalAxis] is to create labels and
     * measure their widths during the measuring phase. This affects how much horizontal space the
     * [VerticalAxis] requests.
     */
    public fun getWidthMeasurementLabelValues(
      context: CartesianMeasuringContext,
      axisHeight: Float,
      maxLabelHeight: Float,
      position: Axis.Position.Vertical,
    ): List<Double>

    /**
     * Returns, as a list, the _y_ values for which the [VerticalAxis] is to create labels and
     * measure their heights during the measuring phase. The height of the tallest label is passed
     * to other functions.
     */
    public fun getHeightMeasurementLabelValues(
      context: CartesianMeasuringContext,
      position: Axis.Position.Vertical,
    ): List<Double>

    /** Returns, as a list, the _y_ values for which ticks and guidelines are to be displayed. */
    public fun getLineValues(
      context: CartesianDrawingContext,
      axisHeight: Float,
      maxLabelHeight: Float,
      position: Axis.Position.Vertical,
    ): List<Double>? = null

    /** Returns the top inset required by the [VerticalAxis]. */
    public fun getTopVerticalAxisInset(
      context: CartesianMeasuringContext,
      verticalLabelPosition: VerticalLabelPosition,
      maxLabelHeight: Float,
      maxLineThickness: Float,
    ): Float

    /** Returns the bottom inset required by the [VerticalAxis]. */
    public fun getBottomVerticalAxisInset(
      context: CartesianMeasuringContext,
      verticalLabelPosition: VerticalLabelPosition,
      maxLabelHeight: Float,
      maxLineThickness: Float,
    ): Float

    /** Houses [ItemPlacer] factory functions. */
    public companion object {
      /**
       * Creates a step-based [ItemPlacer] implementation. [step] returns the difference between the
       * _y_ values of neighboring labels (and their corresponding line pairs). A multiple of this
       * may be used for overlap prevention. If `null` is returned, the step will be determined
       * automatically. [shiftTopLines] is used as the return value of
       * [ItemPlacer.getShiftTopLines].
       */
      public fun step(
        step: (ExtraStore) -> Double? = { null },
        shiftTopLines: Boolean = true,
      ): ItemPlacer =
        DefaultVerticalAxisItemPlacer(DefaultVerticalAxisItemPlacer.Mode.Step(step), shiftTopLines)

      /**
       * Creates a count-based [ItemPlacer] implementation. [count] returns the number of labels
       * (and their corresponding line pairs) to be displayed. This may be reduced for overlap
       * prevention. If `null` is returned, the [VerticalAxis] will display as many items as
       * possible. [shiftTopLines] is used as the return value of [ItemPlacer.getShiftTopLines].
       */
      public fun count(
        count: (ExtraStore) -> Int? = { null },
        shiftTopLines: Boolean = true,
      ): ItemPlacer =
        DefaultVerticalAxisItemPlacer(
          DefaultVerticalAxisItemPlacer.Mode.Count(count),
          shiftTopLines,
        )
    }
  }

  /** Houses [VerticalAxis] factory functions. */
  public companion object {
    /** Creates a start [VerticalAxis]. */
    public fun start(
      line: LineComponent? = null,
      label: TextComponent? = null,
      labelRotationDegrees: Float = 0f,
      horizontalLabelPosition: HorizontalLabelPosition = Outside,
      verticalLabelPosition: VerticalLabelPosition = Center,
      valueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
      tick: LineComponent? = null,
      tickLengthDp: Float = 0f,
      guideline: LineComponent? = null,
      itemPlacer: ItemPlacer = ItemPlacer.step(),
      sizeConstraint: SizeConstraint = SizeConstraint.Auto(),
      titleComponent: TextComponent? = null,
      title: CharSequence? = null,
    ): VerticalAxis<Axis.Position.Vertical.Start> =
      VerticalAxis(
        Axis.Position.Vertical.Start,
        line,
        label,
        labelRotationDegrees,
        horizontalLabelPosition,
        verticalLabelPosition,
        valueFormatter,
        tick,
        tickLengthDp,
        guideline,
        itemPlacer,
        sizeConstraint,
        titleComponent,
        title,
      )

    /** Creates an end [VerticalAxis]. */
    public fun end(
      line: LineComponent? = null,
      label: TextComponent? = null,
      labelRotationDegrees: Float = 0f,
      horizontalLabelPosition: HorizontalLabelPosition = Outside,
      verticalLabelPosition: VerticalLabelPosition = Center,
      valueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
      tick: LineComponent? = null,
      tickLengthDp: Float = 0f,
      guideline: LineComponent? = null,
      itemPlacer: ItemPlacer = ItemPlacer.step(),
      sizeConstraint: SizeConstraint = SizeConstraint.Auto(),
      titleComponent: TextComponent? = null,
      title: CharSequence? = null,
    ): VerticalAxis<Axis.Position.Vertical.End> =
      VerticalAxis(
        Axis.Position.Vertical.End,
        line,
        label,
        labelRotationDegrees,
        horizontalLabelPosition,
        verticalLabelPosition,
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
