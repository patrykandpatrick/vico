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

package com.patrykandpatrick.vico.multiplatform.cartesian.axis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis.HorizontalLabelPosition.Inside
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis.HorizontalLabelPosition.Outside
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis.ItemPlacer.Companion.count
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis.ItemPlacer.Companion.step
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.formatForAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.HorizontalCartesianLayerMargins
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.multiplatform.common.Defaults
import com.patrykandpatrick.vico.multiplatform.common.Position
import com.patrykandpatrick.vico.multiplatform.common.component.LineComponent
import com.patrykandpatrick.vico.multiplatform.common.component.TextComponent
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.half
import com.patrykandpatrick.vico.multiplatform.common.orZero
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
  public val verticalLabelPosition: Position.Vertical,
  valueFormatter: CartesianValueFormatter,
  tick: LineComponent?,
  tickLength: Dp,
  guideline: LineComponent?,
  public val itemPlacer: ItemPlacer,
  size: Size,
  titleComponent: TextComponent?,
  title: CharSequence?,
) :
  BaseAxis<P>(
    line,
    label,
    labelRotationDegrees,
    valueFormatter,
    tick,
    tickLength,
    guideline,
    size,
    titleComponent,
    title,
  ) {
  protected val areLabelsOutsideAtStartOrInsideAtEnd: Boolean
    get() =
      position == Axis.Position.Vertical.Start && horizontalLabelPosition == Outside ||
        position == Axis.Position.Vertical.End && horizontalLabelPosition == Inside

  protected val textHorizontalPosition: Position.Horizontal
    get() =
      if (areLabelsOutsideAtStartOrInsideAtEnd) {
        Position.Horizontal.Start
      } else {
        Position.Horizontal.End
      }

  protected var maxLabelWidth: Float? = null

  internal constructor(
    position: P,
    line: LineComponent?,
    label: TextComponent?,
    labelRotationDegrees: Float,
    horizontalLabelPosition: HorizontalLabelPosition,
    verticalLabelPosition: Position.Vertical,
    tick: LineComponent?,
    tickLength: Dp,
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
    tickLength,
    guideline,
    itemPlacer,
    Size.Auto(),
    titleComponent,
    title,
  )

  override fun drawUnderLayers(context: CartesianDrawingContext) {
    with(context) {
      var centerY: Float
      val yRange = ranges.getYRange(position)
      val maxLabelHeight = getMaxLabelHeight()
      val lineValues =
        itemPlacer.getLineValues(this, bounds.height, maxLabelHeight, position)
          ?: itemPlacer.getLabelValues(this, bounds.height, maxLabelHeight, position)

      lineValues.forEach { lineValue ->
        centerY =
          bounds.bottom - bounds.height * ((lineValue - yRange.minY) / yRange.length).toFloat() +
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
            y = centerY,
          )
      }
      val lineExtensionLength = if (itemPlacer.getShiftTopLines(this)) tickThickness else 0f
      line?.drawVertical(
        context = context,
        x =
          if (position.isLeft(this)) {
            bounds.right - lineThickness.half
          } else {
            bounds.left + lineThickness.half
          },
        top = bounds.top - lineExtensionLength,
        bottom = bounds.bottom + lineExtensionLength,
      )
    }
  }

  override fun drawOverLayers(context: CartesianDrawingContext) {
    with(context) {
      val label = label
      val labelValues =
        itemPlacer.getLabelValues(this, bounds.height, getMaxLabelHeight(), position)
      val tickLeftX = getTickLeftX()
      val tickRightX = tickLeftX + lineThickness + this.tickLength
      val labelX = if (areLabelsOutsideAtStartOrInsideAtEnd == isLtr) tickLeftX else tickRightX
      var tickCenterY: Float
      val yRange = ranges.getYRange(position)

      labelValues.forEach { labelValue ->
        tickCenterY =
          bounds.bottom - bounds.height * ((labelValue - yRange.minY) / yRange.length).toFloat() +
            getLineCanvasYCorrection(tickThickness, labelValue)

        tick?.drawHorizontal(
          context = context,
          left = tickLeftX,
          right = tickRightX,
          y = tickCenterY,
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
          y = bounds.center.y,
          horizontalPosition =
            if (position == Axis.Position.Vertical.Start) {
              Position.Horizontal.End
            } else {
              Position.Horizontal.Start
            },
          verticalPosition = Position.Vertical.Center,
          rotationDegrees =
            if (position == Axis.Position.Vertical.Start) {
              -TITLE_ABS_ROTATION_DEGREES
            } else {
              TITLE_ABS_ROTATION_DEGREES
            },
          maxHeight = bounds.height.toInt(),
        )
      }
    }
  }

  override fun updateLayerDimensions(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
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
          .apply { translate(labelX, tickCenterY - center.y) }

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
          verticalPosition = verticalLabelPosition,
          rotationDegrees = labelRotationDegrees,
          maxWidth = (maxLabelWidth ?: (layerBounds.width.half - this.tickLength)).toInt(),
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

  override fun updateHorizontalLayerMargins(
    context: CartesianMeasuringContext,
    horizontalLayerMargins: HorizontalCartesianLayerMargins,
    layerHeight: Float,
    model: CartesianChartModel,
  ) {
    val width = getWidth(context, layerHeight)
    when (position) {
      Axis.Position.Vertical.Start -> horizontalLayerMargins.ensureValuesAtLeast(start = width)
      Axis.Position.Vertical.End -> horizontalLayerMargins.ensureValuesAtLeast(end = width)
    }
  }

  override fun updateLayerMargins(
    context: CartesianMeasuringContext,
    layerMargins: CartesianLayerMargins,
    layerDimensions: CartesianLayerDimensions,
    model: CartesianChartModel,
  ): Unit =
    with(context) {
      val maxLabelHeight = getMaxLabelHeight()
      val maxLineThickness = max(lineThickness, tickThickness)
      layerMargins.ensureValuesAtLeast(
        top =
          itemPlacer.getTopLayerMargin(
            context,
            verticalLabelPosition,
            maxLabelHeight,
            maxLineThickness,
          ),
        bottom =
          itemPlacer.getBottomLayerMargin(
            context,
            verticalLabelPosition,
            maxLabelHeight,
            maxLineThickness,
          ),
      )
    }

  protected open fun getWidth(context: CartesianMeasuringContext, freeHeight: Float): Float =
    with(context) {
      when (size) {
        is Size.Auto -> {
          val titleComponentWidth =
            title
              ?.let { title ->
                titleComponent?.getWidth(
                  context = this,
                  text = title,
                  rotationDegrees = TITLE_ABS_ROTATION_DEGREES,
                  maxHeight = bounds.height.toInt(),
                )
              }
              .orZero
          val labelSpace =
            when (horizontalLabelPosition) {
              Outside ->
                ceil(getMaxLabelWidth(freeHeight)).also { maxLabelWidth = it } + this.tickLength
              Inside -> 0f
            }
          (labelSpace + titleComponentWidth + lineThickness).coerceIn(
            size.min.pixels,
            size.max.pixels,
          )
        }
        is Size.Fixed -> size.value.pixels
        is Size.Fraction -> canvasSize.width * size.fraction
        is Size.Text ->
          titleComponent
            ?.getWidth(context = this, text = size.text, rotationDegrees = labelRotationDegrees)
            .orZero + this.tickLength + lineThickness.half
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
    verticalLabelPosition: Position.Vertical = this.verticalLabelPosition,
    valueFormatter: CartesianValueFormatter = this.valueFormatter,
    tick: LineComponent? = this.tick,
    tickLength: Dp = this.tickLength,
    guideline: LineComponent? = this.guideline,
    itemPlacer: ItemPlacer = this.itemPlacer,
    size: Size = this.size,
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
      tickLength,
      guideline,
      itemPlacer,
      size,
      titleComponent,
      title,
    )

  override fun equals(other: Any?): Boolean =
    super.equals(other) &&
      other is VerticalAxis<*> &&
      horizontalLabelPosition == other.horizontalLabelPosition &&
      verticalLabelPosition == other.verticalLabelPosition &&
      itemPlacer == other.itemPlacer

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + horizontalLabelPosition.hashCode()
    result = 31 * result + verticalLabelPosition.hashCode()
    result = 31 * result + itemPlacer.hashCode()
    return result
  }

  /**
   * Defines the horizontal position of each of a vertical axis’s labels relative to the axis line.
   */
  public enum class HorizontalLabelPosition {
    Outside,
    Inside,
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

    /** Returns the top [CartesianLayer]-area margin required by the [VerticalAxis]. */
    public fun getTopLayerMargin(
      context: CartesianMeasuringContext,
      verticalLabelPosition: Position.Vertical,
      maxLabelHeight: Float,
      maxLineThickness: Float,
    ): Float

    /** Returns the bottom [CartesianLayer]-area margin required by the [VerticalAxis]. */
    public fun getBottomLayerMargin(
      context: CartesianMeasuringContext,
      verticalLabelPosition: Position.Vertical,
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
    /** Creates and remembers a start [VerticalAxis]. */
    @Composable
    public fun rememberStart(
      line: LineComponent? = rememberAxisLineComponent(),
      label: TextComponent? = rememberAxisLabelComponent(),
      labelRotationDegrees: Float = Defaults.AXIS_LABEL_ROTATION_DEGREES,
      horizontalLabelPosition: HorizontalLabelPosition = Outside,
      verticalLabelPosition: Position.Vertical = Position.Vertical.Center,
      valueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
      tick: LineComponent? = rememberAxisTickComponent(),
      tickLength: Dp = Defaults.AXIS_TICK_LENGTH.dp,
      guideline: LineComponent? = rememberAxisGuidelineComponent(),
      itemPlacer: ItemPlacer = remember { step() },
      size: Size = Size.Auto(),
      titleComponent: TextComponent? = null,
      title: CharSequence? = null,
    ): VerticalAxis<Axis.Position.Vertical.Start> =
      remember(
        line,
        label,
        labelRotationDegrees,
        horizontalLabelPosition,
        verticalLabelPosition,
        valueFormatter,
        tick,
        tickLength,
        guideline,
        itemPlacer,
        size,
        titleComponent,
        title,
      ) {
        VerticalAxis(
          Axis.Position.Vertical.Start,
          line,
          label,
          labelRotationDegrees,
          horizontalLabelPosition,
          verticalLabelPosition,
          valueFormatter,
          tick,
          tickLength,
          guideline,
          itemPlacer,
          size,
          titleComponent,
          title,
        )
      }

    /** Creates and remembers an end [VerticalAxis]. */
    @Composable
    public fun rememberEnd(
      line: LineComponent? = rememberAxisLineComponent(),
      label: TextComponent? = rememberAxisLabelComponent(),
      labelRotationDegrees: Float = Defaults.AXIS_LABEL_ROTATION_DEGREES,
      horizontalLabelPosition: HorizontalLabelPosition = Outside,
      verticalLabelPosition: Position.Vertical = Position.Vertical.Center,
      valueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
      tick: LineComponent? = rememberAxisTickComponent(),
      tickLength: Dp = Defaults.AXIS_TICK_LENGTH.dp,
      guideline: LineComponent? = rememberAxisGuidelineComponent(),
      itemPlacer: ItemPlacer = remember { step() },
      size: Size = Size.Auto(),
      titleComponent: TextComponent? = null,
      title: CharSequence? = null,
    ): VerticalAxis<Axis.Position.Vertical.End> =
      remember(
        line,
        label,
        labelRotationDegrees,
        horizontalLabelPosition,
        verticalLabelPosition,
        valueFormatter,
        tick,
        tickLength,
        guideline,
        itemPlacer,
        size,
        titleComponent,
        title,
      ) {
        VerticalAxis(
          Axis.Position.Vertical.End,
          line,
          label,
          labelRotationDegrees,
          horizontalLabelPosition,
          verticalLabelPosition,
          valueFormatter,
          tick,
          tickLength,
          guideline,
          itemPlacer,
          size,
          titleComponent,
          title,
        )
      }
  }
}
