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
import com.patrykandpatrick.vico.core.cartesian.HorizontalInsets
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis.HorizontalLabelPosition.Inside
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis.HorizontalLabelPosition.Outside
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis.VerticalLabelPosition.Center
import com.patrykandpatrick.vico.core.common.HorizontalPosition
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.ceil
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.getEnd
import com.patrykandpatrick.vico.core.common.getStart
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.orZero
import com.patrykandpatrick.vico.core.common.translate

private const val TITLE_ABS_ROTATION_DEGREES = 90f

/**
 * An implementation of [Axis] used for vertical axes. This class extends [BaseAxis].
 *
 * @see Axis
 * @see BaseAxis
 */
public open class VerticalAxis<Position : AxisPosition.Vertical>(override val position: Position) :
  BaseAxis<Position>() {
  protected val areLabelsOutsideAtStartOrInsideAtEnd: Boolean
    get() =
      horizontalLabelPosition == Outside && position is AxisPosition.Vertical.Start ||
        horizontalLabelPosition == Inside && position is AxisPosition.Vertical.End

  protected val textHorizontalPosition: HorizontalPosition
    get() =
      if (areLabelsOutsideAtStartOrInsideAtEnd) HorizontalPosition.Start else HorizontalPosition.End

  protected val maxLabelWidthKey: ExtraStore.Key<Float> = ExtraStore.Key()

  /**
   * Determines for what _y_ values this [VerticalAxis] is to display labels, ticks, and guidelines.
   */
  public var itemPlacer: AxisItemPlacer.Vertical = AxisItemPlacer.Vertical.step()

  /** Defines the horizontal position of each axis label relative to the axis line. */
  public var horizontalLabelPosition: HorizontalLabelPosition = Outside

  /** Defines the vertical position of each axis label relative to its corresponding tick. */
  public var verticalLabelPosition: VerticalLabelPosition = Center

  override fun drawBehindChart(context: CartesianDrawContext): Unit =
    with(context) {
      var centerY: Float
      val yRange = chartValues.getYRange(position)
      val maxLabelHeight = getMaxLabelHeight()
      val lineValues =
        itemPlacer.getLineValues(this, bounds.height(), maxLabelHeight, position)
          ?: itemPlacer.getLabelValues(this, bounds.height(), maxLabelHeight, position)

      lineValues.forEach { lineValue ->
        centerY =
          bounds.bottom - bounds.height() * (lineValue - yRange.minY) / yRange.length +
            getLineCanvasYCorrection(guidelineThickness, lineValue)

        guideline
          ?.takeIf {
            isNotInRestrictedBounds(
              left = chartBounds.left,
              top = centerY - guidelineThickness.half,
              right = chartBounds.right,
              bottom = centerY + guidelineThickness.half,
            )
          }
          ?.drawHorizontal(
            context = context,
            left = chartBounds.left,
            right = chartBounds.right,
            centerY = centerY,
          )
      }
      val axisLineExtensionLength = if (itemPlacer.getShiftTopLines(this)) tickThickness else 0f
      axisLine?.drawVertical(
        context = context,
        top = bounds.top - axisLineExtensionLength,
        bottom = bounds.bottom + axisLineExtensionLength,
        centerX =
          if (position.isLeft(isLtr = isLtr)) {
            bounds.right - axisThickness.half
          } else {
            bounds.left + axisThickness.half
          },
      )
    }

  override fun drawAboveChart(context: CartesianDrawContext): Unit =
    with(context) {
      val label = label
      val labelValues =
        itemPlacer.getLabelValues(this, bounds.height(), getMaxLabelHeight(), position)
      val tickLeftX = getTickLeftX()
      val tickRightX = tickLeftX + axisThickness + tickLength
      val labelX = if (areLabelsOutsideAtStartOrInsideAtEnd == isLtr) tickLeftX else tickRightX
      var tickCenterY: Float
      val yRange = chartValues.getYRange(position)

      labelValues.forEach { labelValue ->
        tickCenterY =
          bounds.bottom - bounds.height() * (labelValue - yRange.minY) / yRange.length +
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
          label = label,
          labelText = valueFormatter.format(labelValue, chartValues, position),
          labelX = labelX,
          tickCenterY = tickCenterY,
        )
      }

      title?.let { title ->
        titleComponent?.drawText(
          context = this,
          text = title,
          textX =
            if (position.isStart) bounds.getStart(isLtr = isLtr) else bounds.getEnd(isLtr = isLtr),
          textY = bounds.centerY(),
          horizontalPosition =
            if (position.isStart) HorizontalPosition.End else HorizontalPosition.Start,
          verticalPosition = VerticalPosition.Center,
          rotationDegrees = TITLE_ABS_ROTATION_DEGREES * if (position.isStart) -1f else 1f,
          maxTextHeight = bounds.height().toInt(),
        )
      }
    }

  override fun updateHorizontalDimensions(
    context: CartesianMeasureContext,
    horizontalDimensions: MutableHorizontalDimensions,
  ): Unit = Unit

  protected open fun drawLabel(
    context: CartesianDrawContext,
    label: TextComponent,
    labelText: CharSequence,
    labelX: Float,
    tickCenterY: Float,
  ): Unit =
    with(context) {
      val textBounds =
        label.getTextBounds(this, labelText, rotationDegrees = labelRotationDegrees).apply {
          translate(x = labelX, y = tickCenterY - centerY())
        }

      if (
        horizontalLabelPosition == Outside ||
          isNotInRestrictedBounds(
            left = textBounds.left,
            top = textBounds.top,
            right = textBounds.right,
            bottom = textBounds.bottom,
          )
      ) {
        label.drawText(
          context = this,
          text = labelText,
          textX = labelX,
          textY = tickCenterY,
          horizontalPosition = textHorizontalPosition,
          verticalPosition = verticalLabelPosition.textPosition,
          rotationDegrees = labelRotationDegrees,
          maxTextWidth =
            (extraStore.getOrNull(maxLabelWidthKey) ?: (chartBounds.width().half - tickLength))
              .toInt(),
        )
      }
    }

  protected fun CartesianMeasureContext.getTickLeftX(): Float {
    val onLeft = position.isLeft(isLtr = isLtr)
    val base = if (onLeft) bounds.right else bounds.left
    return when {
      onLeft && horizontalLabelPosition == Outside -> base - axisThickness - tickLength
      onLeft && horizontalLabelPosition == Inside -> base - axisThickness
      horizontalLabelPosition == Outside -> base
      horizontalLabelPosition == Inside -> base - tickLength
      else -> error("Unexpected combination of axis position and label position")
    }
  }

  override fun updateHorizontalInsets(
    context: CartesianMeasureContext,
    freeHeight: Float,
    insets: HorizontalInsets,
  ) {
    val width = getWidth(context, freeHeight)
    when {
      position.isStart -> insets.ensureValuesAtLeast(start = width)
      position.isEnd -> insets.ensureValuesAtLeast(end = width)
    }
  }

  override fun updateInsets(
    context: CartesianMeasureContext,
    horizontalDimensions: HorizontalDimensions,
    insets: Insets,
  ): Unit =
    with(context) {
      val maxLabelHeight = getMaxLabelHeight()
      val maxLineThickness = maxOf(axisThickness, tickThickness)
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

  protected open fun getWidth(context: CartesianMeasureContext, freeHeight: Float): Float =
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
                  height = bounds.height().toInt(),
                )
              }
              .orZero
          val labelSpace =
            when (horizontalLabelPosition) {
              Outside -> {
                val maxLabelWidth = getMaxLabelWidth(freeHeight).ceil
                extraStore[maxLabelWidthKey] = maxLabelWidth
                maxLabelWidth + tickLength
              }
              Inside -> 0f
            }
          (labelSpace + titleComponentWidth + axisThickness).coerceIn(
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
            .orZero + tickLength + axisThickness.half
      }
    }

  protected fun CartesianMeasureContext.getMaxLabelHeight(): Float =
    label
      ?.let { label ->
        itemPlacer.getHeightMeasurementLabelValues(this, position).maxOfOrNull { value ->
          label.getHeight(
            context = this,
            text = valueFormatter.format(value, chartValues, position),
            rotationDegrees = labelRotationDegrees,
          )
        }
      }
      .orZero

  protected fun CartesianMeasureContext.getMaxLabelWidth(axisHeight: Float): Float =
    label
      ?.let { label ->
        itemPlacer
          .getWidthMeasurementLabelValues(this, axisHeight, getMaxLabelHeight(), position)
          .maxOfOrNull { value ->
            label.getWidth(
              context = this,
              text = valueFormatter.format(value, chartValues, position),
              rotationDegrees = labelRotationDegrees,
            )
          }
      }
      .orZero

  protected fun CartesianDrawContext.getLineCanvasYCorrection(thickness: Float, y: Float): Float =
    if (y == chartValues.getYRange(position).maxY && itemPlacer.getShiftTopLines(this)) {
      -thickness.half
    } else {
      thickness.half
    }

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

  /** Creates [VerticalAxis] instances. It’s recommended to use this via [VerticalAxis.build]. */
  public class Builder<Position : AxisPosition.Vertical>(
    builder: BaseAxis.Builder<Position>? = null
  ) : BaseAxis.Builder<Position>(builder) {
    /**
     * Determines for what _y_ values this [VerticalAxis] is to display labels, ticks, and
     * guidelines.
     */
    public var itemPlacer: AxisItemPlacer.Vertical = AxisItemPlacer.Vertical.step()

    /** Defines the horizontal position of each axis label relative to the axis line. */
    public var horizontalLabelPosition: HorizontalLabelPosition = Outside

    /** Defines the vertical position of each axis label relative to its corresponding tick. */
    public var verticalLabelPosition: VerticalLabelPosition = Center

    /** Creates a [VerticalAxis] instance with the properties from this [Builder]. */
    @Suppress("UNCHECKED_CAST")
    public inline fun <reified T : Position> build(): VerticalAxis<T> {
      val position =
        when (T::class.java) {
          AxisPosition.Vertical.Start::class.java -> AxisPosition.Vertical.Start
          AxisPosition.Vertical.End::class.java -> AxisPosition.Vertical.End
          else ->
            throw IllegalStateException("Got unknown AxisPosition class ${T::class.java.name}")
        }
          as Position
      return setTo(VerticalAxis(position)).also { axis ->
        axis.itemPlacer = itemPlacer
        axis.horizontalLabelPosition = horizontalLabelPosition
        axis.verticalLabelPosition = verticalLabelPosition
      } as VerticalAxis<T>
    }
  }

  /** Houses a [VerticalAxis] factory function. */
  public companion object {
    /** Creates a [VerticalAxis] via [Builder]. */
    public inline fun <reified P : AxisPosition.Vertical> build(
      block: Builder<P>.() -> Unit = {}
    ): VerticalAxis<P> = Builder<P>().apply(block).build()
  }
}
