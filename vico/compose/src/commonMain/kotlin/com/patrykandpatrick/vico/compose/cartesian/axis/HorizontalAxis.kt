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

package com.patrykandpatrick.vico.compose.cartesian.axis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.formatForAxis
import com.patrykandpatrick.vico.compose.cartesian.getFullXRange as internalGetFullXRange
import com.patrykandpatrick.vico.compose.cartesian.getVisibleXRange
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
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
  protected val Axis.Position.Horizontal.textVerticalPosition: Position.Vertical
    get() =
      when (this) {
        Axis.Position.Horizontal.Top -> Position.Vertical.Top
        Axis.Position.Horizontal.Bottom -> Position.Vertical.Bottom
      }

  private val clipPath = Path()

  internal constructor(
    position: P,
    line: LineComponent?,
    label: TextComponent?,
    labelRotationDegrees: Float,
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
    CartesianValueFormatter.decimal(),
    tick,
    tickLength,
    guideline,
    itemPlacer,
    Size.Auto(),
    titleComponent,
    title,
  )

  override fun updateAxisDimensions(
    context: CartesianDrawingContext,
    axisDimensions: MutableAxisDimensions,
  ) {
    with(context) {
      val lineExtensionLength =
        if (itemPlacer.getShiftExtremeLines(context)) {
          tickThickness
        } else {
          tickThickness.half
        }

      axisDimensions.lineBounds.set(
        layerBounds.left - lineExtensionLength,
        if (position == Axis.Position.Horizontal.Top) bounds.bottom - lineThickness else bounds.top,
        layerBounds.right + lineExtensionLength,
        if (position == Axis.Position.Horizontal.Top) bounds.bottom else bounds.top + lineThickness,
      )
    }
  }

  override fun drawUnderLayers(
    context: CartesianDrawingContext,
    axisDimensions: Map<Axis.Position, AxisDimensions>,
  ) {
    with(context) {
      canvas.save()
      val tickTop =
        if (position == Axis.Position.Horizontal.Top) {
          bounds.bottom - lineThickness - this.tickLength
        } else {
          bounds.top
        }
      val tickBottom = tickTop + lineThickness + this.tickLength
      val fullXRange = internalGetFullXRange(layerDimensions)
      val maxLabelWidth = getMaxLabelWidth(layerDimensions, fullXRange)

      val lineLeft = getLineLeft(context, maxLabelWidth, axisDimensions)
      val lineRight = getLineRight(context, maxLabelWidth, axisDimensions)

      clipPath.rewind()
      clipPath.addRect(
        Rect(
          lineLeft,
          min(bounds.top, layerBounds.top),
          lineRight,
          max(bounds.bottom, layerBounds.bottom),
        ),
        Path.Direction.Clockwise,
      )

      canvas.clipPath(clipPath)

      val textY = if (position == Axis.Position.Horizontal.Top) tickTop else tickBottom
      val baseCanvasX =
        bounds.getStart(isLtr) - scroll + layerDimensions.startPadding * layoutDirectionMultiplier
      val visibleXRange = getVisibleXRange()
      val labelValues = itemPlacer.getLabelValues(this, visibleXRange, fullXRange, maxLabelWidth)
      val lineValues = itemPlacer.getLineValues(this, visibleXRange, fullXRange, maxLabelWidth)

      labelValues.forEachIndexed { index, x ->
        val canvasX =
          baseCanvasX +
            ((x - ranges.minX) / ranges.xStep).toFloat() *
              layerDimensions.xSpacing *
              layoutDirectionMultiplier
        val previousX = labelValues.getOrNull(index - 1) ?: (fullXRange.start.doubled - x)
        val nextX = labelValues.getOrNull(index + 1) ?: (fullXRange.endInclusive.doubled - x)
        val maxWidth =
          ceil(min(x - previousX, nextX - x) / ranges.xStep * layerDimensions.xSpacing).toInt()

        label?.draw(
          context = this,
          text =
            valueFormatter.formatForAxis(context = this, value = x, verticalAxisPosition = null),
          x = canvasX,
          y = textY,
          verticalPosition = position.textVerticalPosition,
          maxWidth = maxWidth,
          maxHeight = (bounds.height - this.tickLength - lineThickness.half).toInt(),
          rotationDegrees = labelRotationDegrees,
        )

        if (lineValues == null) {
          tick?.drawVertical(
            context = this,
            x = canvasX + getLinesCorrectionX(x, fullXRange),
            top = tickTop,
            bottom = tickBottom,
          )
        }
      }

      lineValues?.forEach { x ->
        tick?.drawVertical(
          context = this,
          x =
            baseCanvasX +
              ((x - ranges.minX) / ranges.xStep).toFloat() *
                layerDimensions.xSpacing *
                layoutDirectionMultiplier +
              getLinesCorrectionX(x, fullXRange),
          top = tickTop,
          bottom = tickBottom,
        )
      }

      line?.drawHorizontal(
        context = this,
        left = lineLeft,
        right = lineRight,
        y =
          if (position == Axis.Position.Horizontal.Top) {
            bounds.bottom - lineThickness.half
          } else {
            bounds.top + lineThickness.half
          },
      )

      title?.let { title ->
        titleComponent?.draw(
          context = this,
          x = bounds.center.x,
          y = if (position == Axis.Position.Horizontal.Top) bounds.top else bounds.bottom,
          verticalPosition =
            if (position == Axis.Position.Horizontal.Top) {
              Position.Vertical.Bottom
            } else {
              Position.Vertical.Top
            },
          maxWidth = bounds.width.toInt(),
          text = title,
        )
      }

      canvas.restore()

      drawGuidelines(context, baseCanvasX, fullXRange, labelValues, lineValues)
    }
  }

  private fun getLineStart(
    context: CartesianDrawingContext,
    axisDimensions: Map<Axis.Position, AxisDimensions>,
  ): Float? {
    val startAxisDimensions = axisDimensions.getValue(Axis.Position.Vertical.Start)
    return if (startAxisDimensions.lineBounds.isEmpty) {
      null
    } else {
      startAxisDimensions.lineBounds.getStart(context.isLtr)
    }
  }

  private fun getLineEnd(
    context: CartesianDrawingContext,
    axisDimensions: Map<Axis.Position, AxisDimensions>,
  ): Float? {
    val endAxisDimensions = axisDimensions.getValue(Axis.Position.Vertical.End)
    return if (endAxisDimensions.lineBounds.isEmpty) {
      null
    } else {
      endAxisDimensions.lineBounds.getStart(context.isLtr)
    }
  }

  private fun getLineLeft(
    context: CartesianDrawingContext,
    maxLabelWidth: Float,
    axisDimensions: Map<Axis.Position, AxisDimensions>,
  ) =
    context.run {
      val lineLeft =
        if (context.isLtr) {
          getLineStart(context, axisDimensions)
        } else {
          getLineEnd(context, axisDimensions)
        }

      val defaultLineLeft =
        bounds.left -
          itemPlacer.getStartLayerMargin(this, layerDimensions, tickThickness, maxLabelWidth)

      if (lineLeft != null) {
        minOf(lineLeft, defaultLineLeft)
      } else {
        defaultLineLeft
      }
    }

  private fun getLineRight(
    context: CartesianDrawingContext,
    maxLabelWidth: Float,
    axisDimensions: Map<Axis.Position, AxisDimensions>,
  ) =
    context.run {
      val lineRight =
        if (context.isLtr) {
          getLineEnd(context, axisDimensions)
        } else {
          getLineStart(context, axisDimensions)
        }

      val defaultLineRight =
        bounds.right +
          itemPlacer.getEndLayerMargin(this, layerDimensions, tickThickness, maxLabelWidth)

      if (lineRight != null) {
        maxOf(lineRight, defaultLineRight)
      } else {
        defaultLineRight
      }
    }

  protected open fun drawGuidelines(
    context: CartesianDrawingContext,
    baseCanvasX: Float,
    fullXRange: ClosedFloatingPointRange<Double>,
    labelValues: List<Double>,
    lineValues: List<Double>?,
  ): Unit =
    with(context) {
      val guideline = guideline ?: return
      canvas.save()
      canvas.clipRect(layerBounds)

      if (lineValues == null) {
        labelValues.forEach { x ->
          val canvasX =
            baseCanvasX +
              ((x - ranges.minX) / ranges.xStep).toFloat() *
                layerDimensions.xSpacing *
                layoutDirectionMultiplier

          guideline
            .takeUnless { x.isBoundOf(fullXRange) }
            ?.drawVertical(this, canvasX, layerBounds.top, layerBounds.bottom)
        }
      } else {
        lineValues.forEach { x ->
          val canvasX =
            baseCanvasX +
              ((x - ranges.minX) / ranges.xStep).toFloat() *
                layerDimensions.xSpacing *
                layoutDirectionMultiplier +
              getLinesCorrectionX(x, fullXRange)

          guideline
            .takeUnless { x.isBoundOf(fullXRange) }
            ?.drawVertical(this, canvasX, layerBounds.top, layerBounds.bottom)
        }
      }

      canvas.restore()
    }

  protected fun CartesianDrawingContext.getLinesCorrectionX(
    entryX: Double,
    fullXRange: ClosedFloatingPointRange<Double>,
  ): Float =
    when {
      itemPlacer.getShiftExtremeLines(this).not() -> 0f
      entryX == fullXRange.start -> -tickThickness.half
      entryX == fullXRange.endInclusive -> tickThickness.half
      else -> 0f
    } * layoutDirectionMultiplier

  override fun drawOverLayers(
    context: CartesianDrawingContext,
    axisDimensions: Map<Axis.Position, AxisDimensions>,
  ) {}

  override fun updateLayerDimensions(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
  ) {
    val label = label ?: return
    val ranges = context.ranges
    val maxLabelWidth =
      context.getMaxLabelWidth(layerDimensions, context.internalGetFullXRange(layerDimensions))
    val firstLabelValue = itemPlacer.getFirstLabelValue(context, maxLabelWidth)
    val lastLabelValue = itemPlacer.getLastLabelValue(context, maxLabelWidth)
    if (firstLabelValue != null) {
      val text =
        valueFormatter.formatForAxis(
          context = context,
          value = firstLabelValue,
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
          (firstLabelValue - ranges.minX).toFloat() * layerDimensions.xSpacing
      }
      layerDimensions.ensureValuesAtLeast(unscalableStartPadding = unscalableStartPadding)
    }
    if (lastLabelValue != null) {
      val text =
        valueFormatter.formatForAxis(
          context = context,
          value = lastLabelValue,
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
          ((ranges.maxX - lastLabelValue) * layerDimensions.xSpacing).toFloat()
      }
      layerDimensions.ensureValuesAtLeast(unscalableEndPadding = unscalableEndPadding)
    }
  }

  override fun updateLayerMargins(
    context: CartesianMeasuringContext,
    layerMargins: CartesianLayerMargins,
    layerDimensions: CartesianLayerDimensions,
    model: CartesianChartModel,
  ) {
    val maxLabelWidth =
      context.getMaxLabelWidth(layerDimensions, context.internalGetFullXRange(layerDimensions))
    val height = getHeight(context, layerDimensions, maxLabelWidth)
    layerMargins.ensureValuesAtLeast(
      itemPlacer.getStartLayerMargin(
        context,
        layerDimensions,
        context.tickThickness,
        maxLabelWidth,
      ),
      itemPlacer.getEndLayerMargin(context, layerDimensions, context.tickThickness, maxLabelWidth),
    )
    when (position) {
      Axis.Position.Horizontal.Top -> layerMargins.ensureValuesAtLeast(top = height)
      Axis.Position.Horizontal.Bottom -> layerMargins.ensureValuesAtLeast(bottom = height)
    }
  }

  protected open fun getHeight(
    context: CartesianMeasuringContext,
    layerDimensions: CartesianLayerDimensions,
    maxLabelWidth: Float,
  ): Float =
    with(context) {
      val fullXRange = internalGetFullXRange(layerDimensions)

      when (size) {
        is Size.Auto -> {
          val labelHeight = getMaxLabelHeight(layerDimensions, fullXRange, maxLabelWidth)
          val titleComponentHeight =
            title
              ?.let { title ->
                titleComponent?.getHeight(
                  context = context,
                  maxWidth = bounds.width.toInt(),
                  text = title,
                )
              }
              .orZero
          (labelHeight +
              titleComponentHeight +
              (if (position == Axis.Position.Horizontal.Bottom) lineThickness else 0f) +
              this.tickLength)
            .coerceAtMost(canvasSize.height / MAX_HEIGHT_DIVISOR)
            .coerceIn(size.min.pixels, size.max.pixels)
        }
        is Size.Fixed -> size.value.pixels
        is Size.Fraction -> canvasSize.height * size.fraction
        is Size.Text ->
          label
            ?.getHeight(context = this, text = size.text, rotationDegrees = labelRotationDegrees)
            .orZero
      }
    }

  protected fun CartesianMeasuringContext.getMaxLabelWidth(
    layerDimensions: CartesianLayerDimensions,
    fullXRange: ClosedFloatingPointRange<Double>,
  ): Float {
    val label = label ?: return 0f
    return itemPlacer
      .getWidthMeasurementLabelValues(this, layerDimensions, fullXRange)
      .maxOfOrNull { value ->
        val text =
          valueFormatter.formatForAxis(context = this, value = value, verticalAxisPosition = null)
        label.getWidth(
          context = this,
          text = text,
          rotationDegrees = labelRotationDegrees,
          pad = true,
        )
      }
      .orZero
  }

  protected fun CartesianMeasuringContext.getMaxLabelHeight(
    layerDimensions: CartesianLayerDimensions,
    fullXRange: ClosedFloatingPointRange<Double>,
    maxLabelWidth: Float,
  ): Float {
    val label = label ?: return 0f
    return itemPlacer
      .getHeightMeasurementLabelValues(this, layerDimensions, fullXRange, maxLabelWidth)
      .maxOf { value ->
        val text =
          valueFormatter.formatForAxis(context = this, value = value, verticalAxisPosition = null)
        label.getHeight(
          context = this,
          text = text,
          rotationDegrees = labelRotationDegrees,
          pad = true,
        )
      }
  }

  /** Creates a new [HorizontalAxis] based on this one. */
  public fun copy(
    line: LineComponent? = this.line,
    label: TextComponent? = this.label,
    labelRotationDegrees: Float = this.labelRotationDegrees,
    valueFormatter: CartesianValueFormatter = this.valueFormatter,
    tick: LineComponent? = this.tick,
    tickLength: Dp = this.tickLength,
    guideline: LineComponent? = this.guideline,
    itemPlacer: ItemPlacer = this.itemPlacer,
    size: Size = this.size,
    titleComponent: TextComponent? = this.titleComponent,
    title: CharSequence? = this.title,
  ): HorizontalAxis<P> =
    HorizontalAxis(
      position,
      line,
      label,
      labelRotationDegrees,
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
    super.equals(other) && other is HorizontalAxis<*> && itemPlacer == other.itemPlacer

  override fun hashCode(): Int = 31 * super.hashCode() + itemPlacer.hashCode()

  /** Determines for what _x_ values a [HorizontalAxis] displays labels, ticks, and guidelines. */
  public interface ItemPlacer {
    /**
     * Returns a boolean indicating whether to shift the lines whose _x_ values are bounds of the
     * effective _x_ range, if such lines are present, such that they’re immediately outside of the
     * [CartesianLayer] bounds. (The effective _x_ range includes the [CartesianLayer] padding.) On
     * either side, if a [VerticalAxis] is present, the shifted tick will then be aligned with the
     * axis line, and the shifted guideline will be hidden.
     */
    public fun getShiftExtremeLines(context: CartesianDrawingContext): Boolean = true

    /**
     * If the [HorizontalAxis] is to reserve room for the first label, returns the first label’s _x_
     * value. Otherwise, returns `null`.
     */
    public fun getFirstLabelValue(
      context: CartesianMeasuringContext,
      maxLabelWidth: Float,
    ): Double? = null

    /**
     * If the [HorizontalAxis] is to reserve room for the last label, returns the last label’s _x_
     * value. Otherwise, returns `null`.
     */
    public fun getLastLabelValue(
      context: CartesianMeasuringContext,
      maxLabelWidth: Float,
    ): Double? = null

    /**
     * Returns, as a list, the _x_ values for which labels are to be displayed, restricted to
     * [visibleXRange] and with two extra values on either side (if applicable).
     */
    public fun getLabelValues(
      context: CartesianDrawingContext,
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
      context: CartesianMeasuringContext,
      layerDimensions: CartesianLayerDimensions,
      fullXRange: ClosedFloatingPointRange<Double>,
    ): List<Double>

    /**
     * Returns, as a list, the _x_ values for which the [HorizontalAxis] is to create labels and
     * measure their heights during the measuring phase. This affects how much vertical space the
     * [HorizontalAxis] requests.
     */
    public fun getHeightMeasurementLabelValues(
      context: CartesianMeasuringContext,
      layerDimensions: CartesianLayerDimensions,
      fullXRange: ClosedFloatingPointRange<Double>,
      maxLabelWidth: Float,
    ): List<Double>

    /**
     * Returns, as a list, the _x_ values for which ticks and guidelines are to be displayed,
     * restricted to [visibleXRange] and with an extra value on either side (if applicable). If
     * `null` is returned, the values returned by [getLabelValues] are used.
     */
    public fun getLineValues(
      context: CartesianDrawingContext,
      visibleXRange: ClosedFloatingPointRange<Double>,
      fullXRange: ClosedFloatingPointRange<Double>,
      maxLabelWidth: Float,
    ): List<Double>? = null

    /** Returns the start [CartesianLayer]-area margin required by the [HorizontalAxis]. */
    public fun getStartLayerMargin(
      context: CartesianMeasuringContext,
      layerDimensions: CartesianLayerDimensions,
      tickThickness: Float,
      maxLabelWidth: Float,
    ): Float

    /** Returns the end [CartesianLayer]-area margin required by the [HorizontalAxis]. */
    public fun getEndLayerMargin(
      context: CartesianMeasuringContext,
      layerDimensions: CartesianLayerDimensions,
      tickThickness: Float,
      maxLabelWidth: Float,
    ): Float

    /** Houses [ItemPlacer] factory functions. */
    public companion object {
      /**
       * Adds a label, tick, and guideline for each _x_ value given by [CartesianChartRanges.minX] +
       * (_k_ × spacing + offset) × [CartesianChartRanges.xStep], where _k_ ∈ ℕ, with these
       * components being horizontally centered relative to one another. [shiftExtremeLines] is used
       * as the return value of [ItemPlacer.getShiftExtremeLines]. [addExtremeLabelPadding]
       * specifies whether [CartesianLayer] padding should be added for the first and last labels,
       * ensuring their visibility.
       */
      public fun aligned(
        spacing: (ExtraStore) -> Int = { 1 },
        offset: (ExtraStore) -> Int = { 0 },
        shiftExtremeLines: Boolean = true,
        addExtremeLabelPadding: Boolean = true,
      ): ItemPlacer =
        AlignedHorizontalAxisItemPlacer(spacing, offset, shiftExtremeLines, addExtremeLabelPadding)

      /**
       * Adds a label for each major _x_ value, and adds ticks between the labels and for
       * [CartesianChartRanges.minX] − [CartesianChartRanges.xStep] ÷ 2 and
       * [CartesianChartRanges.maxX] + [CartesianChartRanges.xStep] ÷ 2. (Major _x_ values are given
       * by [CartesianChartRanges.minX] + _k_ × [CartesianChartRanges.xStep], where _k_ ∈ ℕ.)
       * [shiftExtremeLines] is used as the return value of [ItemPlacer.getShiftExtremeLines].
       */
      public fun segmented(shiftExtremeLines: Boolean = true): ItemPlacer =
        SegmentedHorizontalAxisItemPlacer(shiftExtremeLines)
    }
  }

  /** Houses [HorizontalAxis] factory functions. */
  public companion object {
    private const val MAX_HEIGHT_DIVISOR = 3

    /** Creates and remembers a top [HorizontalAxis]. */
    @Composable
    public fun rememberTop(
      line: LineComponent? = rememberAxisLineComponent(),
      label: TextComponent? = rememberAxisLabelComponent(),
      labelRotationDegrees: Float = Defaults.AXIS_LABEL_ROTATION_DEGREES,
      valueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
      tick: LineComponent? = rememberAxisTickComponent(),
      tickLength: Dp = Defaults.AXIS_TICK_LENGTH.dp,
      guideline: LineComponent? = rememberAxisGuidelineComponent(),
      itemPlacer: ItemPlacer = remember { ItemPlacer.aligned() },
      size: Size = Size.Auto(),
      titleComponent: TextComponent? = null,
      title: CharSequence? = null,
    ): HorizontalAxis<Axis.Position.Horizontal.Top> =
      remember(
        line,
        label,
        labelRotationDegrees,
        valueFormatter,
        tick,
        tickLength,
        guideline,
        itemPlacer,
        size,
        titleComponent,
        title,
      ) {
        HorizontalAxis(
          Axis.Position.Horizontal.Top,
          line,
          label,
          labelRotationDegrees,
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

    /** Creates and remembers a bottom [HorizontalAxis]. */
    @Composable
    public fun rememberBottom(
      line: LineComponent? = rememberAxisLineComponent(),
      label: TextComponent? = rememberAxisLabelComponent(),
      labelRotationDegrees: Float = Defaults.AXIS_LABEL_ROTATION_DEGREES,
      valueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
      tick: LineComponent? = rememberAxisTickComponent(),
      tickLength: Dp = Defaults.AXIS_TICK_LENGTH.dp,
      guideline: LineComponent? = rememberAxisGuidelineComponent(),
      itemPlacer: ItemPlacer = remember { ItemPlacer.aligned() },
      size: Size = Size.Auto(),
      titleComponent: TextComponent? = null,
      title: CharSequence? = null,
    ): HorizontalAxis<Axis.Position.Horizontal.Bottom> =
      remember(
        line,
        label,
        labelRotationDegrees,
        valueFormatter,
        tick,
        tickLength,
        guideline,
        itemPlacer,
        size,
        titleComponent,
        title,
      ) {
        HorizontalAxis(
          Axis.Position.Horizontal.Bottom,
          line,
          label,
          labelRotationDegrees,
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
