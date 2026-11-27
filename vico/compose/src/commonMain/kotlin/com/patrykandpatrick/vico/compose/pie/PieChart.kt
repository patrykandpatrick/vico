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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Bounded
import com.patrykandpatrick.vico.compose.common.Defaults
import com.patrykandpatrick.vico.compose.common.DrawingContext
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Legend
import com.patrykandpatrick.vico.compose.common.MeasuringContext
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.getRepeating
import com.patrykandpatrick.vico.compose.common.half
import com.patrykandpatrick.vico.compose.common.orZero
import com.patrykandpatrick.vico.compose.common.saveLayer
import com.patrykandpatrick.vico.compose.common.toRadians
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel
import com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * A Compose Multiplatform pie chart, which displays a [PieChartModel]. Use [rememberPieChart] to
 * create [PieChart]s.
 *
 * A [PieChart] is opaque: its configuration is set via [rememberPieChart] and read only by the
 * library. It holds draw-time state, so display an instance in at most one host at a time.
 */
@Stable
public class PieChart
internal constructor(
  internal val model: PieChartModel,
  internal val sliceProvider: SliceProvider,
  internal val spacing: Dp,
  internal val outerSize: PieSize.Outer,
  internal val innerSize: PieSize.Inner,
  internal val valueFormatter: PieValueFormatter,
  internal val legend: Legend<PieChartMeasuringContext, PieChartDrawingContext>?,
) : Bounded {
  override var bounds: Rect = Rect.Zero

  internal var gestureGeometry: PieGestureGeometry = PieGestureGeometry.Zero

  init {
    require(spacing >= 0.dp) { "Slice spacing must be nonnegative." }
  }

  internal fun getLegendHeight(context: PieChartMeasuringContext): Float =
    legend?.getHeight(context, context.canvasWidth).orZero

  /**
   * A slice to draw: a [PieChartDrawingModel.SliceInfo] with the entry, index, and style it
   * represents. Current slices are styled via [sliceProvider]; exiting slices, which are no longer
   * in the model, keep the entry and style they had.
   */
  private class ResolvedSlice(
    val info: PieChartDrawingModel.SliceInfo,
    val entry: PieChartModel.Entry,
    val index: Int,
    val slice: Slice,
  )

  private fun resolve(drawingModel: PieChartDrawingModel): List<ResolvedSlice> {
    val entries = model.entries.withIndex().associateBy { it.value.key }
    return drawingModel.slices.mapNotNull { info ->
      val current = entries[info.key]
      if (current != null) {
        ResolvedSlice(
          info,
          current.value,
          current.index,
          sliceProvider.getSlice(current.value, current.index),
        )
      } else {
        val entry = info.entry ?: return@mapNotNull null
        val slice = info.slice ?: return@mapNotNull null
        ResolvedSlice(info, entry, info.index, slice)
      }
    }
  }

  internal fun draw(
    context: PieChartDrawingContext,
    drawingModel: PieChartDrawingModel,
    startAngle: Float = -90f,
    fitLabelsInBounds: Boolean = true,
  ) {
    val slices = resolve(drawingModel)
    val circleBounds =
      if (fitLabelsInBounds) {
        getCircleBounds(context, slices, startAngle)
      } else {
        getCircleBounds(context, bounds, slices)
      }
    val outerRadius = circleBounds.width / 2f
    val holeRadius = innerSize.getRadius(context, circleBounds.width, circleBounds.height)
    require(outerRadius > holeRadius) { "The outer size must be greater than the inner size." }
    // Exiting slices don’t count for rotation gestures: an empty model has no gesture geometry.
    gestureGeometry =
      if (model.entries.isEmpty()) {
        PieGestureGeometry.Zero
      } else {
        PieGestureGeometry(circleBounds.center, outerRadius, holeRadius)
      }
    val spacingPx = with(context) { spacing.pixels }
    val donut = holeRadius > 0f
    val outerTrim =
      if (spacingPx > 0f && outerRadius > 0f) asinDegrees((spacingPx / 2f) / outerRadius) else 0f
    val innerTrim = if (donut && spacingPx > 0f) asinDegrees((spacingPx / 2f) / holeRadius) else 0f
    val spacingDegrees = if (outerRadius > 0f) spacingToDegrees(context, outerRadius) else 0f
    var currentAngle = startAngle
    slices.forEach { resolved ->
      val sliceInfo = resolved.info
      val entry = resolved.entry
      val index = resolved.index
      val degrees = sliceInfo.degrees
      val slice = resolved.slice
      val centerAngle = currentAngle + degrees / 2f
      val offset = slice.getOffset(context, centerAngle)
      val correctedBounds = circleBounds.translate(offset)

      val sweepAngle: Float
      val drawn: Boolean
      if (donut && degrees < 360f) {
        sweepAngle = (degrees - 2f * outerTrim).coerceAtLeast(0f)
        drawn = sweepAngle > 0f
        if (drawn) {
          slice.buildParallelPath(
            circleBounds = correctedBounds,
            holeRadius = holeRadius,
            startAngle = currentAngle,
            sweepAngle = degrees,
            outerTrim = outerTrim,
            innerTrim = innerTrim,
            destination = slice.path,
          )
        }
      } else {
        sweepAngle = (degrees - spacingDegrees).coerceAtLeast(0f)
        drawn = sweepAngle > 0f
        if (drawn) {
          val centerOffset = tipOffset(spacingPx, degrees, centerAngle)
          slice.buildPath(
            circleBounds = correctedBounds,
            holeRadius = holeRadius,
            startAngle = currentAngle + spacingDegrees / 2f,
            sweepAngle = sweepAngle,
            centerOffset = centerOffset,
            destination = slice.path,
          )
        }
      }

      if (drawn) {
        slice.draw(context, slice.path, correctedBounds, sliceInfo.sliceOpacity)
        slice.label?.draw(
          context = context,
          chartBounds = bounds,
          circleBounds = correctedBounds,
          holeRadius = holeRadius,
          angle = centerAngle,
          sweepAngle = sweepAngle,
          label = valueFormatter.format(context, entry.value, index),
          slicePath = slice.path,
          opacity = sliceInfo.labelOpacity,
        )
      }
      currentAngle += degrees
    }
    legend?.draw(context)
  }

  /** Offsets a holeless slice’s tip so trimmed edges don’t just meet at the true center. */
  private fun tipOffset(spacingPx: Float, sweepDegrees: Float, bisectorAngle: Float): Offset {
    if (spacingPx <= 0f || sweepDegrees >= 360f) return Offset.Zero
    val halfSweepRadians = (sweepDegrees / 2f).toRadians()
    val sinHalf = sin(halfSweepRadians).toFloat()
    if (sinHalf <= 0.001f) return Offset.Zero
    val tipDistance = spacingPx / sinHalf
    val bisectorRadians = bisectorAngle.toRadians()
    return Offset(
      cos(bisectorRadians).toFloat() * tipDistance,
      sin(bisectorRadians).toFloat() * tipDistance,
    )
  }

  private fun spacingToDegrees(context: PieChartDrawingContext, outerRadius: Float): Float {
    val spacingPx = with(context) { spacing.pixels }
    return (spacingPx / outerRadius * 180f / kotlin.math.PI).toFloat() * 2f
  }

  private fun getCircleBounds(
    context: PieChartDrawingContext,
    slices: List<ResolvedSlice>,
    startAngle: Float,
  ): Rect {
    val baseRadius = outerSize.getRadius(context, bounds.width, bounds.height)
    val maxOffset = slices.maxOfOrNull { with(context) { it.slice.offsetFromCenter.pixels } } ?: 0f
    val provisionalBounds =
      Rect(
        left = bounds.center.x - baseRadius,
        top = bounds.center.y - baseRadius,
        right = bounds.center.x + baseRadius,
        bottom = bounds.center.y + baseRadius,
      )
    var insets = PieInsets()
    var currentAngle = startAngle
    slices.forEach { resolved ->
      val degrees = resolved.info.degrees
      val sliceLabel = resolved.slice.label
      if (sliceLabel != null) {
        insets =
          insets.plus(
            sliceLabel.getInsets(
              context = context,
              chartBounds = bounds,
              circleBounds = provisionalBounds,
              angle = currentAngle + degrees / 2f,
              label = valueFormatter.format(context, resolved.entry.value, resolved.index),
            )
          )
      }
      currentAngle += degrees
    }
    val radius = max(0f, baseRadius - insets.largestEdge - maxOffset)
    return Rect(
      left = bounds.center.x - radius,
      top = bounds.center.y - radius,
      right = bounds.center.x + radius,
      bottom = bounds.center.y + radius,
    )
  }

  /**
   * Returns the room needed for outside labels: for each slice in [drawingModel] (including
   * entering and exiting ones), its label’s worst-case vertical room (as if it sat at the very top
   * and the very bottom), at full size. This doesn’t depend on the slices’ angles or the animation
   * progress, so it changes only when slices are added to or dropped from the drawing model.
   */
  internal fun getLabelInsets(
    context: PieChartMeasuringContext,
    chartBounds: Rect,
    drawingModel: PieChartDrawingModel,
  ): PieInsets {
    val slices = resolve(drawingModel)
    val circleBounds = getCircleBounds(context, chartBounds, slices)
    var insets = PieInsets()
    val topAngle = -90f
    val bottomAngle = 90f
    slices.forEach { resolved ->
      val slice = resolved.slice
      val sliceLabel = slice.label as? SliceLabel.Outside ?: return@forEach
      val label = valueFormatter.format(context, resolved.entry.value, resolved.index)
      val top =
        sliceLabel.getInsets(
          context = context,
          chartBounds = chartBounds,
          circleBounds = circleBounds.translate(slice.getOffset(context, topAngle)),
          angle = topAngle,
          label = label,
        )
      val bottom =
        sliceLabel.getInsets(
          context = context,
          chartBounds = chartBounds,
          circleBounds = circleBounds.translate(slice.getOffset(context, bottomAngle)),
          angle = bottomAngle,
          label = label,
        )
      insets = insets.plus(PieInsets(top = top.top, bottom = bottom.bottom))
    }
    return insets
  }

  private fun getCircleBounds(
    context: PieChartMeasuringContext,
    availableBounds: Rect,
    slices: List<ResolvedSlice>,
  ): Rect {
    val maxOffset = slices.maxOfOrNull { with(context) { it.slice.offsetFromCenter.pixels } } ?: 0f
    val radius =
      max(
        0f,
        outerSize.getRadius(context, availableBounds.width, availableBounds.height) - maxOffset,
      )
    return Rect(
      left = availableBounds.center.x - radius,
      top = availableBounds.center.y - radius,
      right = availableBounds.center.x + radius,
      bottom = availableBounds.center.y + radius,
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

    internal fun getOffset(context: MeasuringContext, angle: Float): Offset {
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
      centerOffset: Offset = Offset.Zero,
      destination: Path,
    ) {
      destination.rewind()
      val path = createSlicePath(circleBounds, holeRadius, startAngle, sweepAngle, centerOffset)
      destination.fillType = path.fillType
      destination.addPath(path)
    }

    internal fun buildParallelPath(
      circleBounds: Rect,
      holeRadius: Float,
      startAngle: Float,
      sweepAngle: Float,
      outerTrim: Float,
      innerTrim: Float,
      destination: Path,
    ) {
      destination.rewind()
      destination.addPath(
        createParallelSlicePath(
          circleBounds,
          holeRadius,
          startAngle,
          sweepAngle,
          outerTrim,
          innerTrim,
        )
      )
    }

    public open fun copy(
      fill: Fill = this.fill,
      strokeFill: Fill = this.strokeFill,
      strokeThickness: Dp = this.strokeThickness,
      offsetFromCenter: Dp = this.offsetFromCenter,
      label: SliceLabel? = this.label,
    ): Slice = Slice(fill, strokeFill, strokeThickness, offsetFromCenter, label)

    /**
     * Compares the configuration. This applies to [Slice] itself; instances of subclasses compare
     * by identity unless the subclass overrides `equals` and `hashCode`.
     */
    override fun equals(other: Any?): Boolean =
      this === other ||
        this::class == Slice::class &&
          other is Slice &&
          other::class == Slice::class &&
          fill == other.fill &&
          strokeFill == other.strokeFill &&
          strokeThickness == other.strokeThickness &&
          offsetFromCenter == other.offsetFromCenter &&
          label == other.label

    override fun hashCode(): Int {
      if (this::class != Slice::class) return super.hashCode()
      var result = fill.hashCode()
      result = 31 * result + strokeFill.hashCode()
      result = 31 * result + strokeThickness.hashCode()
      result = 31 * result + offsetFromCenter.hashCode()
      result = 31 * result + label.hashCode()
      return result
    }
  }

  /** Defines how a pie-slice label is measured and drawn. */
  @Immutable
  public sealed class SliceLabel {
    internal abstract fun getInsets(
      context: MeasuringContext,
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
      override fun equals(other: Any?): Boolean =
        this === other || other is Inside && textComponent == other.textComponent

      override fun hashCode(): Int = textComponent.hashCode()

      override fun getInsets(
        context: MeasuringContext,
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
          getInsideLabelMaxWidth(
            textRadius = textRadius,
            ringThickness = radius - holeRadius,
            sweepAngle = sweepAngle,
          )
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
      public val maxWidthToBoundsRatio: Float =
        Defaults.PIE_OUTSIDE_LABEL_MAX_WIDTH_TO_BOUNDS_RATIO,
    ) : SliceLabel() {
      init {
        require(maxWidthToBoundsRatio < 1f) { "`maxWidthToBoundsRatio` must be below 1." }
      }

      override fun equals(other: Any?): Boolean =
        this === other ||
          other is Outside &&
            textComponent == other.textComponent &&
            lineColor == other.lineColor &&
            lineWidth == other.lineWidth &&
            angledSegmentLength == other.angledSegmentLength &&
            horizontalSegmentLength == other.horizontalSegmentLength &&
            maxWidthToBoundsRatio == other.maxWidthToBoundsRatio

      override fun hashCode(): Int {
        var result = textComponent.hashCode()
        result = 31 * result + lineColor.hashCode()
        result = 31 * result + lineWidth.hashCode()
        result = 31 * result + angledSegmentLength.hashCode()
        result = 31 * result + horizontalSegmentLength.hashCode()
        result = 31 * result + maxWidthToBoundsRatio.hashCode()
        return result
      }

      override fun getInsets(
        context: MeasuringContext,
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
        context: MeasuringContext,
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

      private fun getFinalPoint(
        context: MeasuringContext,
        circleBounds: Rect,
        angle: Float,
      ): Offset {
        val bend = getAngledPoint(context, circleBounds, angle)
        val horizontalDistance = with(context) { horizontalSegmentLength.pixels }
        return Offset(
          bend.x + if (bend.x < circleBounds.center.x) -horizontalDistance else horizontalDistance,
          bend.y,
        )
      }
    }
  }

  /**
   * Provides [Slice]s to [PieChart]s. Implementations must be pure functions of their arguments and
   * of the values they capture; to change the style, pass a new [SliceProvider].
   *
   * It’s called on the main thread, while measuring and drawing. In a composable function, a lambda
   * converted to [SliceProvider] is memoized while the values it captures are unchanged (compared
   * via `equals` for stable types and by identity otherwise), so it needs no explicit `remember`.
   */
  @Immutable
  public fun interface SliceProvider {
    /**
     * Returns the [Slice] for the specified entry and slice index. To style slices by identity
     * rather than position, use [PieChartModel.Entry.key]. This is asked only about the model’s
     * current entries: exiting slices keep the [Slice] they had.
     */
    public fun getSlice(entry: PieChartModel.Entry, index: Int): Slice

    public companion object {
      private data class Series(private val slices: List<Slice>) : SliceProvider {
        init {
          require(slices.isNotEmpty()) { "At least one slice should be added." }
        }

        override fun getSlice(entry: PieChartModel.Entry, index: Int): Slice =
          slices.getRepeating(index)
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
  centerOffset: Offset = Offset.Zero,
): Path {
  val fullSweep = sweepAngle >= 360f
  val path = Path()
  if (fullSweep) {
    path.fillType = PathFillType.EvenOdd
    path.addOval(circleBounds)
    if (holeRadius > 0f) {
      val innerBounds =
        Rect(
          left = circleBounds.center.x - holeRadius,
          top = circleBounds.center.y - holeRadius,
          right = circleBounds.center.x + holeRadius,
          bottom = circleBounds.center.y + holeRadius,
        )
      path.addOval(innerBounds)
    }
    return path
  }

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
    path.lineTo(circleBounds.center.x + centerOffset.x, circleBounds.center.y + centerOffset.y)
  }
  path.close()
  return path
}

internal fun asinDegrees(value: Float): Float =
  (asin(value.coerceIn(-1f, 1f)) * 180.0 / kotlin.math.PI).toFloat()

/**
 * Builds a donut-slice path whose straight edges are offset by half the spacing, so the gaps to the
 * neighboring slices have a constant width. [outerTrim] and [innerTrim] are the angles (in degrees)
 * by which the outer and inner arcs are shortened at each end.
 */
internal fun createParallelSlicePath(
  circleBounds: Rect,
  holeRadius: Float,
  startAngle: Float,
  sweepAngle: Float,
  outerTrim: Float,
  innerTrim: Float,
): Path {
  val path = Path()
  val outerSweep = (sweepAngle - 2f * outerTrim).coerceAtLeast(0f)
  path.arcTo(circleBounds, startAngle + outerTrim, outerSweep, forceMoveTo = true)
  val innerSweep = sweepAngle - 2f * innerTrim
  if (holeRadius > 0f && innerSweep > 0f) {
    val innerBounds =
      Rect(
        left = circleBounds.center.x - holeRadius,
        top = circleBounds.center.y - holeRadius,
        right = circleBounds.center.x + holeRadius,
        bottom = circleBounds.center.y + holeRadius,
      )
    path.arcTo(innerBounds, startAngle + sweepAngle - innerTrim, -innerSweep, forceMoveTo = false)
  } else {
    val halfSweepRadians = (sweepAngle / 2f).toRadians()
    val sinHalfSweep = sin(halfSweepRadians).toFloat()
    val halfSpacing = circleBounds.width.half * sin(outerTrim.toRadians()).toFloat()
    val tipRadius = if (sinHalfSweep > 0.0001f) halfSpacing / sinHalfSweep else holeRadius
    val midRadians = (startAngle + sweepAngle / 2f).toRadians()
    path.lineTo(
      circleBounds.center.x + cos(midRadians).toFloat() * tipRadius,
      circleBounds.center.y + sin(midRadians).toFloat() * tipRadius,
    )
  }
  path.close()
  return path
}

internal fun getInsideLabelMaxWidth(
  textRadius: Float,
  ringThickness: Float,
  sweepAngle: Float,
): Int {
  val sweepWidth =
    if (sweepAngle >= 180f) {
      ringThickness
    } else {
      2f * textRadius * sin(sweepAngle.toRadians() / 2.0)
    }
  return sweepWidth.toInt().coerceAtLeast(0)
}

/**
 * Creates and remembers a [PieChart] that displays [model]. A new [PieChart] is created whenever an
 * argument changes—e.g., because of a data update—and the [PieChartHost] applies it.
 */
@Composable
public fun rememberPieChart(
  model: PieChartModel,
  sliceProvider: PieChart.SliceProvider =
    vicoTheme.pieChartColors.let { colors ->
      remember(colors) {
        PieChart.SliceProvider.series(colors.map { color -> PieChart.Slice(fill = Fill(color)) })
      }
    },
  spacing: Dp = 0.dp,
  outerSize: PieSize.Outer = PieSize.Outer.Fill,
  innerSize: PieSize.Inner = PieSize.Inner.Zero,
  valueFormatter: PieValueFormatter = PieValueFormatter.Value,
  legend: Legend<PieChartMeasuringContext, PieChartDrawingContext>? = null,
): PieChart =
  remember(model, sliceProvider, spacing, outerSize, innerSize, valueFormatter, legend) {
    PieChart(model, sliceProvider, spacing, outerSize, innerSize, valueFormatter, legend)
  }
