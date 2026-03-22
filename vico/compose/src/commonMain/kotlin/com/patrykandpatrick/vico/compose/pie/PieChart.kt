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

@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.patrykandpatrick.vico.compose.pie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Bounded
import com.patrykandpatrick.vico.compose.common.Defaults
import com.patrykandpatrick.vico.compose.common.DrawingContext
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Legend
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.ValueWrapper
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.getRepeating
import com.patrykandpatrick.vico.compose.common.half
import com.patrykandpatrick.vico.compose.common.saveLayer
import com.patrykandpatrick.vico.compose.common.toRadians
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.uuid.Uuid

/** A Compose Multiplatform pie chart. */
@Stable
public class PieChart
internal constructor(
  public val sliceProvider: SliceProvider,
  public val spacing: Dp,
  public val outerSize: PieSize.Outer,
  public val innerSize: PieSize.Inner,
  public val startAngle: Float,
  internal val valueFormatter: PieValueFormatter,
  internal val legend: Legend<PieChartMeasuringContext, PieChartDrawingContext>?,
  internal val drawingModelInterpolator: PieChartDrawingModelInterpolator,
  internal val id: Uuid = Uuid.random(),
) : Bounded {
  override var bounds: Rect = Rect.Zero

  init {
    require(spacing >= 0.dp) { "Slice spacing must be nonnegative." }
  }

  internal fun getLegendHeight(context: PieChartMeasuringContext): Float =
    legend?.getHeight(context, context.canvasSize.width).orZero

  internal fun draw(context: PieChartDrawingContext, drawingModel: PieChartDrawingModel) {
    val circleBounds = getCircleBounds(context, drawingModel)
    val outerRadius = circleBounds.width / 2f
    val holeRadius = innerSize.getRadius(context, circleBounds.width, circleBounds.height)
    require(outerRadius > holeRadius) { "The outer size must be greater than the inner size." }
    val spacingDegrees = if (outerRadius > 0f) spacingToDegrees(context, outerRadius) else 0f
    var currentAngle = startAngle
    drawingModel.slices.forEachIndexed { index, sliceInfo ->
      val value = context.model.entries[index].value
      val slice =
        sliceProvider.getSlice(context.model.entries[index], index, context.model.extraStore)
      val centerAngle = currentAngle + sliceInfo.degrees / 2f
      val offset = slice.getOffset(context, centerAngle)
      val correctedBounds = circleBounds.translate(offset)
      val correctedStartAngle = currentAngle + spacingDegrees / 2f
      val correctedSweepAngle = (sliceInfo.degrees - spacingDegrees).coerceAtLeast(0f)
      if (correctedSweepAngle > 0f) {
        slice.buildPath(
          circleBounds = correctedBounds,
          holeRadius = holeRadius,
          startAngle = correctedStartAngle,
          sweepAngle = correctedSweepAngle,
          destination = slice.path,
        )
        slice.draw(context, slice.path, correctedBounds, sliceInfo.sliceOpacity)
        slice.label?.draw(
          context = context,
          chartBounds = bounds,
          circleBounds = correctedBounds,
          holeRadius = holeRadius,
          angle = centerAngle,
          sweepAngle = correctedSweepAngle,
          label = valueFormatter.format(context, value, index),
          slicePath = slice.path,
          opacity = sliceInfo.labelOpacity,
        )
      }
      currentAngle += sliceInfo.degrees
    }
    legend?.draw(context)
  }

  private fun spacingToDegrees(context: PieChartDrawingContext, outerRadius: Float): Float {
    val spacingPx = with(context) { spacing.pixels }
    return (spacingPx / outerRadius * 180f / kotlin.math.PI).toFloat() * 2f
  }

  private fun getCircleBounds(
    context: PieChartDrawingContext,
    drawingModel: PieChartDrawingModel,
  ): Rect {
    val baseRadius = outerSize.getRadius(context, bounds.width, bounds.height)
    val maxOffset =
      drawingModel.slices.indices.maxOfOrNull { index ->
        with(context) {
          sliceProvider
            .getSlice(context.model.entries[index], index, context.model.extraStore)
            .offsetFromCenter
            .pixels
        }
      } ?: 0f
    val provisionalBounds =
      Rect(
        left = bounds.center.x - baseRadius,
        top = bounds.center.y - baseRadius,
        right = bounds.center.x + baseRadius,
        bottom = bounds.center.y + baseRadius,
      )
    var insets = PieInsets()
    var currentAngle = startAngle
    drawingModel.slices.forEachIndexed { index, sliceInfo ->
      val value = context.model.entries[index].value
      val sliceLabel =
        sliceProvider.getSlice(context.model.entries[index], index, context.model.extraStore).label
      if (sliceLabel != null) {
        insets =
          insets.plus(
            sliceLabel.getInsets(
              context = context,
              chartBounds = bounds,
              circleBounds = provisionalBounds,
              angle = currentAngle + sliceInfo.degrees / 2f,
              label = valueFormatter.format(context, value, index),
            )
          )
      }
      currentAngle += sliceInfo.degrees
    }
    val radius = max(0f, baseRadius - insets.largestEdge - maxOffset)
    return Rect(
      left = bounds.center.x - radius,
      top = bounds.center.y - radius,
      right = bounds.center.x + radius,
      bottom = bounds.center.y + radius,
    )
  }

  /** Defines the appearance of a pie slice. */
  @Immutable
  public open class Slice(
    public val fill: Fill = Fill.Black,
    public val strokeFill: Fill = Fill.Transparent,
    public val strokeThickness: Dp = 0.dp,
    public val offsetFromCenter: Dp = 0.dp,
    public val label: SliceLabel? = null,
  ) {
    private val fillPaint: Paint = Paint()
    private val strokePaint: Paint = Paint()
    internal val path: Path = Path()

    internal fun getOffset(context: DrawingContext, angle: Float): Offset {
      val distance = with(context) { offsetFromCenter.pixels }
      val radians = angle.toRadians()
      return Offset((cos(radians) * distance).toFloat(), (sin(radians) * distance).toFloat())
    }

    internal fun draw(context: DrawingContext, path: Path, bounds: Rect, opacity: Float) {
      val size = bounds.size
      fill.brush?.applyTo(size = size, p = fillPaint, alpha = opacity)
        ?: run {
          fillPaint.color = fill.color
          fillPaint.alpha = opacity
        }
      context.canvas.drawPath(path, fillPaint)
      if (strokeThickness > 0.dp && strokeFill.color.alpha > 0f) {
        strokeFill.brush?.applyTo(size = size, p = strokePaint, alpha = opacity)
          ?: run {
            strokePaint.color = strokeFill.color
            strokePaint.alpha = opacity
          }
        strokePaint.style = PaintingStyle.Stroke
        strokePaint.strokeWidth = with(context) { strokeThickness.pixels }
        context.canvas.drawPath(path, strokePaint)
      }
    }

    internal fun buildPath(
      circleBounds: Rect,
      holeRadius: Float,
      startAngle: Float,
      sweepAngle: Float,
      destination: Path,
    ) {
      destination.rewind()
      destination.addPath(createSlicePath(circleBounds, holeRadius, startAngle, sweepAngle))
    }

    public open fun copy(
      fill: Fill = this.fill,
      strokeFill: Fill = this.strokeFill,
      strokeThickness: Dp = this.strokeThickness,
      offsetFromCenter: Dp = this.offsetFromCenter,
      label: SliceLabel? = this.label,
    ): Slice = Slice(fill, strokeFill, strokeThickness, offsetFromCenter, label)
  }

  /** Defines how a pie-slice label is measured and drawn. */
  @Immutable
  public sealed class SliceLabel {
    internal abstract fun getInsets(
      context: DrawingContext,
      chartBounds: Rect,
      circleBounds: Rect,
      angle: Float,
      label: CharSequence,
    ): PieInsets

    internal abstract fun draw(
      context: DrawingContext,
      chartBounds: Rect,
      circleBounds: Rect,
      holeRadius: Float,
      angle: Float,
      sweepAngle: Float,
      label: CharSequence,
      slicePath: Path,
      opacity: Float,
    )

    /** Draws the label inside the slice. */
    @Immutable
    public class Inside(public val textComponent: TextComponent = TextComponent()) : SliceLabel() {
      override fun getInsets(
        context: DrawingContext,
        chartBounds: Rect,
        circleBounds: Rect,
        angle: Float,
        label: CharSequence,
      ): PieInsets = PieInsets()

      override fun draw(
        context: DrawingContext,
        chartBounds: Rect,
        circleBounds: Rect,
        holeRadius: Float,
        angle: Float,
        sweepAngle: Float,
        label: CharSequence,
        slicePath: Path,
        opacity: Float,
      ) {
        val radius = circleBounds.width.half
        val textRadius = holeRadius + (radius - holeRadius).half
        val radians = angle.toRadians()
        val center = circleBounds.center
        val x = center.x + cos(radians).toFloat() * textRadius
        val y = center.y + sin(radians).toFloat() * textRadius
        val maxWidth =
          (2f * textRadius * sin(sweepAngle.toRadians() / 2.0)).toInt().coerceAtLeast(0)
        if (maxWidth <= 0) return
        context.saveLayer(opacity)
        textComponent.draw(
          context = context,
          text = label,
          x = x,
          y = y,
          horizontalPosition = Position.Horizontal.Center,
          verticalPosition = Position.Vertical.Center,
          maxWidth = maxWidth,
        )
        context.canvas.restore()
      }
    }

    /** Draws the label outside the slice and connects it with a line. */
    @Immutable
    public class Outside(
      public val textComponent: TextComponent = TextComponent(),
      public val lineColor: Color = Color.Black,
      public val lineWidth: Dp = Defaults.PIE_OUTSIDE_LABEL_LINE_WIDTH.dp,
      public val angledSegmentLength: Dp = Defaults.PIE_OUTSIDE_LABEL_ANGLED_SEGMENT_LENGTH.dp,
      public val horizontalSegmentLength: Dp =
        Defaults.PIE_OUTSIDE_LABEL_HORIZONTAL_SEGMENT_LENGTH.dp,
      public val maxWidthToBoundsRatio: Float = Defaults.PIE_OUTSIDE_LABEL_MAX_WIDTH_TO_BOUNDS_RATIO,
    ) : SliceLabel() {
      init {
        require(maxWidthToBoundsRatio < 1f) { "`maxWidthToBoundsRatio` must be below 1." }
      }

      override fun getInsets(
        context: DrawingContext,
        chartBounds: Rect,
        circleBounds: Rect,
        angle: Float,
        label: CharSequence,
      ): PieInsets {
        val maxWidth = (chartBounds.width * maxWidthToBoundsRatio).toInt().coerceAtLeast(0)
        val textBounds = textComponent.getBounds(context, label, maxWidth = maxWidth)
        val point = getFinalPoint(context, circleBounds, angle)
        val left = (chartBounds.left - (point.x - textBounds.width.half)).coerceAtLeast(0f)
        val top = (chartBounds.top - (point.y - textBounds.height.half)).coerceAtLeast(0f)
        val right = ((point.x + textBounds.width.half) - chartBounds.right).coerceAtLeast(0f)
        val bottom = ((point.y + textBounds.height.half) - chartBounds.bottom).coerceAtLeast(0f)
        return PieInsets(start = left, top = top, end = right, bottom = bottom)
      }

      override fun draw(
        context: DrawingContext,
        chartBounds: Rect,
        circleBounds: Rect,
        holeRadius: Float,
        angle: Float,
        sweepAngle: Float,
        label: CharSequence,
        slicePath: Path,
        opacity: Float,
      ) {
        val start = getOuterPoint(circleBounds, angle)
        val bend = getAngledPoint(context, circleBounds, angle)
        val end = getFinalPoint(context, circleBounds, angle)
        val paint =
          Paint().apply {
            color = lineColor
            alpha = opacity
            strokeWidth = with(context) { lineWidth.pixels }
            style = androidx.compose.ui.graphics.PaintingStyle.Stroke
          }
        context.canvas.drawLine(start, bend, paint)
        context.canvas.drawLine(bend, end, paint)
        val horizontalPosition =
          if (end.x < circleBounds.center.x) Position.Horizontal.Start else Position.Horizontal.End
        context.saveLayer(opacity)
        textComponent.draw(
          context = context,
          text = label,
          x = end.x,
          y = end.y,
          horizontalPosition = horizontalPosition,
          verticalPosition = Position.Vertical.Center,
          maxWidth = (chartBounds.width * maxWidthToBoundsRatio).toInt().coerceAtLeast(0),
        )
        context.canvas.restore()
      }

      private fun getOuterPoint(circleBounds: Rect, angle: Float): Offset {
        val radians = angle.toRadians()
        val radius = circleBounds.width.half
        return Offset(
          circleBounds.center.x + cos(radians).toFloat() * radius,
          circleBounds.center.y + sin(radians).toFloat() * radius,
        )
      }

      private fun getAngledPoint(
        context: DrawingContext,
        circleBounds: Rect,
        angle: Float,
      ): Offset {
        val radians = angle.toRadians()
        val radius = circleBounds.width.half + with(context) { angledSegmentLength.pixels }
        return Offset(
          circleBounds.center.x + cos(radians).toFloat() * radius,
          circleBounds.center.y + sin(radians).toFloat() * radius,
        )
      }

      private fun getFinalPoint(context: DrawingContext, circleBounds: Rect, angle: Float): Offset {
        val bend = getAngledPoint(context, circleBounds, angle)
        val horizontalDistance = with(context) { horizontalSegmentLength.pixels }
        return Offset(
          bend.x + if (bend.x < circleBounds.center.x) -horizontalDistance else horizontalDistance,
          bend.y,
        )
      }
    }
  }

  @Immutable
  /** Provides [Slice]s to [PieChart]s. */
  public fun interface SliceProvider {
    /** Returns the [Slice] for the specified entry and slice index. */
    public fun getSlice(entry: PieChartModel.Entry, index: Int, extraStore: ExtraStore): Slice

    public companion object {
      private data class Series(private val slices: List<Slice>) : SliceProvider {
        init {
          require(slices.isNotEmpty()) { "At least one slice should be added." }
        }

        override fun getSlice(
          entry: PieChartModel.Entry,
          index: Int,
          extraStore: ExtraStore,
        ): Slice = slices.getRepeating(index)
      }

      /**
       * Uses the provided [Slice]s. The [Slice]s and pie slices are associated by index. If there
       * are more slices in the data model than [Slice]s, [slices] is iterated multiple times.
       */
      public fun series(slices: List<Slice>): SliceProvider = Series(slices)

      /**
       * Uses the provided [Slice]s. The [Slice]s and pie slices are associated by index. If there
       * are more slices in the data model than [Slice]s, the [Slice] list is iterated multiple
       * times.
       */
      public fun series(vararg slices: Slice): SliceProvider = series(slices.toList())
    }
  }

  public fun copy(
    sliceProvider: SliceProvider = this.sliceProvider,
    spacing: Dp = this.spacing,
    outerSize: PieSize.Outer = this.outerSize,
    innerSize: PieSize.Inner = this.innerSize,
    startAngle: Float = this.startAngle,
    valueFormatter: PieValueFormatter = this.valueFormatter,
    legend: Legend<PieChartMeasuringContext, PieChartDrawingContext>? = this.legend,
  ): PieChart =
    PieChart(
      sliceProvider,
      spacing,
      outerSize,
      innerSize,
      startAngle,
      valueFormatter,
      legend,
      drawingModelInterpolator,
      id,
    )
}

internal data class PieInsets(
  val start: Float = 0f,
  val top: Float = 0f,
  val end: Float = 0f,
  val bottom: Float = 0f,
) {
  fun plus(other: PieInsets): PieInsets =
    PieInsets(
      start = max(start, other.start),
      top = max(top, other.top),
      end = max(end, other.end),
      bottom = max(bottom, other.bottom),
    )

  val largestEdge: Float
    get() = max(max(start, top), max(end, bottom))
}

internal fun createSlicePath(
  circleBounds: Rect,
  holeRadius: Float,
  startAngle: Float,
  sweepAngle: Float,
): Path {
  val path = Path()
  path.arcTo(circleBounds, startAngle, sweepAngle, false)
  if (holeRadius > 0f) {
    val innerBounds =
      Rect(
        left = circleBounds.center.x - holeRadius,
        top = circleBounds.center.y - holeRadius,
        right = circleBounds.center.x + holeRadius,
        bottom = circleBounds.center.y + holeRadius,
      )
    path.arcTo(innerBounds, startAngle + sweepAngle, -sweepAngle, false)
  } else {
    path.lineTo(circleBounds.center.x, circleBounds.center.y)
  }
  path.close()
  return path
}

internal data class PieChartDrawingModel(val slices: List<PieChartSliceDrawingModel>)

internal data class PieChartSliceDrawingModel(
  val degrees: Float,
  val sliceOpacity: Float = 1f,
  val labelOpacity: Float = 1f,
)

internal fun PieChartModel.toDrawingModel(): PieChartDrawingModel =
  PieChartDrawingModel(
    entries.mapIndexed { index, entry ->
      PieChartSliceDrawingModel(degrees = if (sum == 0f) 0f else entry.value / sum * 360f)
    }
  )

internal fun interpolate(
  old: PieChartDrawingModel?,
  new: PieChartDrawingModel,
  fraction: Float,
): PieChartDrawingModel {
  val oldSlices = old?.slices.orEmpty()
  val size = max(oldSlices.size, new.slices.size)
  return PieChartDrawingModel(
    List(size) { index ->
      val newSlice = new.slices.getOrNull(index) ?: PieChartSliceDrawingModel(0f)
      val oldSlice = oldSlices.getOrNull(index)
      val oldDegrees = oldSlice?.degrees.orZero
      PieChartSliceDrawingModel(
        degrees = oldDegrees + (newSlice.degrees - oldDegrees) * fraction,
        sliceOpacity =
          when {
            oldSlice == null || oldSlice.degrees == 0f -> fraction
            newSlice.degrees == 0f -> 1f - fraction
            else -> 1f
          },
        labelOpacity =
          when {
            oldSlice == null || oldSlice.degrees == 0f -> fraction
            newSlice.degrees == 0f -> 1f - fraction
            else -> 1f
          },
      )
    }
  )
}

private val Float?.orZero: Float
  get() = this ?: 0f

/** Creates and remembers a [PieChart]. */
@Composable
public fun rememberPieChart(
  sliceProvider: PieChart.SliceProvider,
  spacing: Dp = Defaults.PIE_SPACING.dp,
  outerSize: PieSize.Outer = PieSize.Outer.Fill,
  innerSize: PieSize.Inner = PieSize.Inner.Zero,
  startAngle: Float = -90f,
  valueFormatter: PieValueFormatter = PieValueFormatter.Value,
  legend: Legend<PieChartMeasuringContext, PieChartDrawingContext>? = null,
): PieChart {
  val wrapper = remember { ValueWrapper<PieChart?>(null) }
  return remember(
    sliceProvider,
    spacing,
    outerSize,
    innerSize,
    startAngle,
    valueFormatter,
    legend,
  ) {
    val pieChart =
      wrapper.value?.copy(
        sliceProvider = sliceProvider,
        spacing = spacing,
        outerSize = outerSize,
        innerSize = innerSize,
        startAngle = startAngle,
        valueFormatter = valueFormatter,
        legend = legend,
      )
        ?: PieChart(
          sliceProvider,
          spacing,
          outerSize,
          innerSize,
          startAngle,
          valueFormatter,
          legend,
          defaultPieChartDrawingModelInterpolator(),
        )
    wrapper.value = pieChart
    pieChart
  }
}
