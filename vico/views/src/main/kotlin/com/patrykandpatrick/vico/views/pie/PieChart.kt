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

package com.patrykandpatrick.vico.views.pie

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.views.common.Bounded
import com.patrykandpatrick.vico.views.common.Defaults
import com.patrykandpatrick.vico.views.common.DrawingContext
import com.patrykandpatrick.vico.views.common.ELLIPSIS
import com.patrykandpatrick.vico.views.common.MAX_HEX_VALUE
import com.patrykandpatrick.vico.views.common.MeasuringContext
import com.patrykandpatrick.vico.views.common.PI_RAD
import com.patrykandpatrick.vico.views.common.Point
import com.patrykandpatrick.vico.views.common.Position
import com.patrykandpatrick.vico.views.common.component.TextComponent
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import com.patrykandpatrick.vico.views.common.data.MutableExtraStore
import com.patrykandpatrick.vico.views.common.getRepeating
import com.patrykandpatrick.vico.views.common.half
import com.patrykandpatrick.vico.views.common.opacity
import com.patrykandpatrick.vico.views.common.saveLayer
import com.patrykandpatrick.vico.views.common.shader.ShaderProvider
import com.patrykandpatrick.vico.views.pie.data.PieChartModel
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/** Draws a pie chart. */
public open class PieChart
private constructor(
  public val sliceProvider: SliceProvider,
  public val spacingDp: Float,
  public val outerSize: PieSize.Outer,
  public val innerSize: PieSize.Inner,
  public val startAngle: Float,
  public val valueFormatter: PieValueFormatter,
  internal val drawingModelInterpolator: PieChartDrawingModelInterpolator,
  internal val drawingModelKey: ExtraStore.Key<PieChartDrawingModel>,
) : Bounded {

  override val bounds: RectF = RectF()

  protected val oval: RectF = RectF()
  protected val spacingPathBuilder: Path = Path()
  protected val spacingMatrix: Matrix = Matrix()
  private val insets: PieInsets = PieInsets()

  init {
    checkParameters()
  }

  /** Draws a pie chart. */
  public constructor(
    sliceProvider: SliceProvider,
    spacingDp: Float = 0f,
    outerSize: PieSize.Outer = PieSize.Outer.Fill,
    innerSize: PieSize.Inner = PieSize.Inner.Zero,
    startAngle: Float = Defaults.PIE_CHART_START_ANGLE,
    valueFormatter: PieValueFormatter = PieValueFormatter.Value,
  ) : this(
    sliceProvider = sliceProvider,
    spacingDp = spacingDp,
    outerSize = outerSize,
    innerSize = innerSize,
    startAngle = startAngle,
    valueFormatter = valueFormatter,
    drawingModelInterpolator = defaultPieChartDrawingModelInterpolator(),
    drawingModelKey = ExtraStore.Key(),
  )

  protected fun checkParameters() {
    require(spacingDp >= 0f) { "The spacing cannot be negative." }
  }

  internal fun updateOvalBounds(
    context: PieChartDrawingContext,
    model: PieChartModel,
    sliceInfo: List<PieChartDrawingModel.SliceInfo>?,
  ) {
    with(context) {
      checkParameters()
      insets.clear()

      var ovalRadius = outerSize.getRadius(context, bounds.width(), bounds.height())
      var currentAngle = startAngle
      val sliceCount = sliceInfo?.size ?: model.entries.size
      var maxOffsetFromCenter = 0f

      for (index in 0 until sliceCount) {
        val slice = sliceProvider.getSlice(model.entries[index], index, model.extraStore)
        val info = sliceInfo?.get(index)
        val entry = model.entries.getOrNull(index)
        val sweepAngle =
          info?.degrees ?: entry?.value?.let { value -> value / model.sum * FULL_DEGREES } ?: 0f

        slice.label?.let { labelComponent ->
          val label = info?.value?.let { valueFormatter.format(context, it, index) }
          if (label != null) {
            oval.set(
              bounds.centerX() - ovalRadius,
              bounds.centerY() - ovalRadius,
              bounds.centerX() + ovalRadius,
              bounds.centerY() + ovalRadius,
            )
            labelComponent.getInsets(
              context = context,
              contentBounds = bounds,
              oval = oval,
              angle = currentAngle + sweepAngle.half,
              label = label,
              outInsets = insets,
            )
          }
        }

        currentAngle += sweepAngle
        maxOffsetFromCenter = max(slice.offsetFromCenterDp.pixels, maxOffsetFromCenter)
      }

      ovalRadius -= maxOffsetFromCenter + insets.largestEdge
      ovalRadius = ovalRadius.roundToInt().toFloat()
      oval.set(
        bounds.centerX() - ovalRadius,
        bounds.centerY() - ovalRadius,
        bounds.centerX() + ovalRadius,
        bounds.centerY() + ovalRadius,
      )
    }
  }

  internal fun draw(context: PieChartDrawingContext, model: PieChartModel) {
    if (model.entries.isEmpty() || model.sum <= 0f) return
    with(context) {
      val drawingModel = model.extraStore.getOrNull(drawingModelKey)
      val sliceInfo = drawingModel?.slices
      updateOvalBounds(context, model, sliceInfo)
      val innerRadius = innerSize.getRadius(context, oval.width(), oval.height())

      require(oval.radius > innerRadius) { "The outer size must be greater than the inner size." }

      val restoreCount = if (spacingDp > 0f) canvas.saveLayer() else -1
      val sliceCount = sliceInfo?.size ?: model.entries.size
      var drawAngle = startAngle

      for (index in 0 until sliceCount) {
        val slice = sliceProvider.getSlice(model.entries[index], index, model.extraStore)
        val info = sliceInfo?.get(index)
        val entry = model.entries.getOrNull(index)
        val sweepAngle =
          info?.degrees ?: entry?.value?.let { value -> value / model.sum * FULL_DEGREES } ?: 0f

        spacingPathBuilder.rewind()
        if (spacingDp > 0f) {
          addSpacingSegment(spacingPathBuilder, sweepAngle, sweepAngle)
          addSpacingSegment(spacingPathBuilder, drawAngle, sweepAngle)
        }
        if (innerRadius > 0f) {
          addHole(spacingPathBuilder, innerRadius)
        }

        slice.draw(
          context = context,
          contentBounds = bounds,
          oval = oval,
          startAngle = drawAngle,
          sweepAngle = sweepAngle,
          holeRadius = innerRadius,
          label = info?.value?.let { valueFormatter.format(context, it, index) },
          spacingPath = spacingPathBuilder,
          sliceOpacity = info?.sliceOpacity ?: 1f,
          labelOpacity = info?.labelOpacity ?: 1f,
        )
        drawAngle += sweepAngle
      }

      if (restoreCount >= 0) {
        canvas.restoreToCount(restoreCount)
      }
    }
  }

  protected open fun DrawingContext.addSpacingSegment(
    pathBuilder: Path,
    startAngle: Float,
    sweepAngle: Float,
  ) {
    val spacing = spacingDp.pixels
    with(pathBuilder) {
      spacingMatrix.postRotate(startAngle, oval.centerX(), oval.centerY())
      if (sweepAngle > PI_RAD.half) {
        val correctedSpacing = spacing / sin(sweepAngle.half.radians)
        val correctedAngle =
          if (sweepAngle == startAngle) PI_RAD - sweepAngle.half else sweepAngle.half
        val correctedSpacingFactor =
          if (startAngle == sweepAngle && sweepAngle > PI_RAD) -1f else 1f
        val point =
          translatePointByAngle(
            center = oval.centerPoint,
            point =
              com.patrykandpatrick.vico.views.common.Point(
                oval.centerX() + correctedSpacing.half * correctedSpacingFactor,
                oval.centerY(),
              ),
            angle = correctedAngle.radiansDouble,
          )
        moveTo(point.x, point.y)
        lineTo(oval.centerX(), oval.centerY() + spacing.half)
      } else {
        moveTo(oval.centerX(), oval.centerY() + spacing.half)
      }
      lineTo(bounds.right, oval.centerY() + spacing.half)
      lineTo(bounds.right + spacing, oval.centerY() - spacing.half)
      lineTo(oval.centerX(), oval.centerY() - spacing.half)
      close()
      transform(spacingMatrix)
      spacingMatrix.reset()
    }
  }

  protected open fun DrawingContext.addHole(pathBuilder: Path, innerRadius: Float) {
    pathBuilder.addCircle(oval.centerX(), oval.centerY(), innerRadius, Path.Direction.CCW)
  }

  internal fun prepareForTransformation(model: PieChartModel?, extraStore: MutableExtraStore) {
    val oldModel = extraStore.getOrNull(drawingModelKey)
    val customSize =
      if (oldModel != null) {
        max(oldModel.slices.count { it.degrees > 0f }, model?.entries?.size ?: 0)
      } else {
        model?.entries?.size
      }
    drawingModelInterpolator.setModels(oldModel, model?.toDrawingModel(customSize))
  }

  internal suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {
    drawingModelInterpolator.transform(fraction)?.let { extraStore[drawingModelKey] = it }
      ?: extraStore.remove(drawingModelKey)
  }

  /** Defines the appearance of a pie slice. */
  public open class Slice(
    public var color: Int = Color.LTGRAY,
    public var shaderProvider: ShaderProvider? = null,
    public var strokeWidthDp: Float = 0f,
    public var strokeColor: Int = Color.TRANSPARENT,
    public var offsetFromCenterDp: Float = 0f,
    public var label: SliceLabel? = null,
  ) {
    protected val fillPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    protected val strokePaint: Paint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = strokeColor }
    protected val drawOval: RectF = RectF()
    protected val slicePath: Path = Path()
    protected val sliceBounds: RectF = RectF()

    /** Draws the slice. */
    public open fun draw(
      context: DrawingContext,
      contentBounds: RectF,
      oval: RectF,
      startAngle: Float,
      sweepAngle: Float,
      holeRadius: Float,
      label: CharSequence?,
      spacingPath: Path,
      sliceOpacity: Float,
      labelOpacity: Float,
    ) {
      with(context) {
        drawOval.set(oval)
        applyOffset(drawOval, startAngle + sweepAngle.half)

        if (color.isNotTransparent || shaderProvider != null) {
          drawFilledSlice(context, startAngle, sweepAngle, spacingPath, sliceOpacity)
        }

        if (strokeColor.isNotTransparent && strokeWidthDp > 0f) {
          drawStrokedSlice(context, startAngle, sweepAngle, spacingPath, sliceOpacity)
        }

        this@Slice.label?.let { labelComponent ->
          label?.let {
            labelComponent.drawLabel(
              context = context,
              oval = drawOval,
              holeRadius = holeRadius,
              angle = startAngle + sweepAngle.half,
              slicePath = slicePath,
              label = it,
              sliceOpacity = sliceOpacity,
              labelOpacity = labelOpacity,
            )
          }
        }
      }
    }

    protected open fun drawFilledSlice(
      context: DrawingContext,
      startAngle: Float,
      sweepAngle: Float,
      spacingPath: Path,
      sliceOpacity: Float,
    ) {
      with(context) {
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = color
        fillPaint.opacity = sliceOpacity

        slicePath.rewind()
        slicePath.addArc(drawOval, startAngle, sweepAngle)
        slicePath.lineTo(drawOval.centerX(), drawOval.centerY())
        slicePath.close()

        if (shaderProvider != null) {
          slicePath.computeBounds(sliceBounds, false)
          fillPaint.shader =
            shaderProvider?.getShader(
              context,
              sliceBounds.left,
              sliceBounds.top,
              sliceBounds.right,
              sliceBounds.bottom,
            )
        } else {
          fillPaint.shader = null
        }

        if (!spacingPath.isEmpty) {
          slicePath.op(spacingPath, Path.Op.DIFFERENCE)
        }

        canvas.drawPath(slicePath, fillPaint)
      }
    }

    protected open fun drawStrokedSlice(
      context: DrawingContext,
      startAngle: Float,
      sweepAngle: Float,
      spacingPath: Path,
      sliceOpacity: Float,
    ) {
      with(context) {
        val strokeWidth = strokeWidthDp.pixels
        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = strokeColor
        strokePaint.strokeWidth = strokeWidth
        strokePaint.opacity = sliceOpacity

        drawOval.updateBounds(
          left = drawOval.left + strokeWidth.half,
          top = drawOval.top + strokeWidth.half,
          right = drawOval.right - strokeWidth.half,
          bottom = drawOval.bottom - strokeWidth.half,
        )

        slicePath.rewind()
        slicePath.addArc(drawOval, startAngle, sweepAngle)
        slicePath.lineTo(drawOval.centerX(), drawOval.centerY())
        slicePath.close()

        if (!spacingPath.isEmpty) {
          slicePath.op(spacingPath, Path.Op.DIFFERENCE)
        }

        canvas.drawPath(slicePath, strokePaint)
      }
    }

    protected fun MeasuringContext.applyOffset(rectF: RectF, angle: Float) {
      val translatedPoint =
        translatePointByAngle(
          center = Point(0f, 0f),
          point = Point(offsetFromCenterDp.pixels, 0f),
          angle = angle.radiansDouble,
        )
      rectF.offset(
        translatedPoint.x.roundToInt().toFloat(),
        translatedPoint.y.roundToInt().toFloat(),
      )
    }
  }

  /** A base class for slice labels. */
  public sealed class SliceLabel {
    internal abstract fun drawLabel(
      context: DrawingContext,
      oval: RectF,
      holeRadius: Float,
      angle: Float,
      slicePath: Path,
      label: CharSequence,
      sliceOpacity: Float,
      labelOpacity: Float,
    )

    internal open fun getInsets(
      context: MeasuringContext,
      contentBounds: RectF,
      oval: RectF,
      angle: Float,
      label: CharSequence,
      outInsets: PieInsets,
    ) {}

    /** Draws the label inside the slice. */
    public class Inside(public var textComponent: TextComponent = TextComponent()) : SliceLabel() {
      private val layoutHelper: PieLayoutHelper = PieLayoutHelper()
      protected val sliceBounds: RectF = RectF()
      protected val transform: Matrix = Matrix()

      override fun drawLabel(
        context: DrawingContext,
        oval: RectF,
        holeRadius: Float,
        angle: Float,
        slicePath: Path,
        label: CharSequence,
        sliceOpacity: Float,
        labelOpacity: Float,
      ) {
        with(context) {
          val radius = oval.width().half

          transform.setRotate(-angle, oval.centerX(), oval.centerY())
          slicePath.transform(transform)
          slicePath.computeBounds(sliceBounds, false)
          transform.setRotate(angle, oval.centerX(), oval.centerY())
          slicePath.transform(transform)

          val textCenter =
            translatePointByAngle(
              center = oval.centerPoint,
              point =
                Point(oval.centerX() + holeRadius + (radius - holeRadius).half, oval.centerY()),
              angle = angle.radiansDouble,
            )

          var minWidth = textComponent.getBounds(this, ELLIPSIS).width()
          val textBounds = textComponent.getBounds(this, label)
          minWidth = minWidth.coerceAtMost(textBounds.width())
          textBounds.offset(
            textCenter.x - textBounds.width().toInt().half,
            textCenter.y - textBounds.height().toInt().half,
          )

          layoutHelper.adjustTextBounds(textBounds, slicePath)

          val maxTextWidth = ceil(textBounds.width()).toInt()
          if (maxTextWidth > minWidth) {
            val canvasSaveCount = canvas.saveLayer(opacity = labelOpacity)
            textComponent.draw(
              context = this,
              text = label,
              x = textBounds.centerX(),
              y = textCenter.y,
              maxWidth = maxTextWidth,
            )
            canvas.restoreToCount(canvasSaveCount)
          }
        }
      }
    }

    /** Draws the label outside the slice and connects it with a line. */
    public class Outside(
      public var textComponent: TextComponent = TextComponent(),
      lineColor: Int = Color.BLACK,
      initialLineWidthDp: Float = 1f,
      public var angledSegmentLengthDp: Float = Defaults.SLICE_ANGLED_SEGMENT_LENGTH,
      public var horizontalSegmentLengthDp: Float = Defaults.SLICE_HORIZONTAL_SEGMENT_LENGTH,
      public var maxWidthToBoundsRatio: Float =
        Defaults.SLICE_OUTSIDE_LABEL_MAX_WIDTH_TO_BOUNDS_RATIO,
    ) : SliceLabel() {
      protected var measuredTextWidth: Int = 0
      private var lineWidthDpValue: Float = initialLineWidthDp

      protected val linePaint: Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          color = lineColor
          strokeWidth = initialLineWidthDp
        }

      public var lineColor: Int
        get() = linePaint.color
        set(value) {
          linePaint.color = value
        }

      public var lineWidthDp: Float
        get() = lineWidthDpValue
        set(value) {
          lineWidthDpValue = value
        }

      init {
        require(maxWidthToBoundsRatio < 1f) {
          "The `maxWidthToBoundsRatio` cannot be greater than or equal to 1."
        }
      }

      protected fun getTextMaxWidth(contentBounds: RectF): Int =
        ceil(contentBounds.width() * maxWidthToBoundsRatio).toInt()

      override fun getInsets(
        context: MeasuringContext,
        contentBounds: RectF,
        oval: RectF,
        angle: Float,
        label: CharSequence,
        outInsets: PieInsets,
      ) {
        with(context) {
          val finalLinePoint = getFinalLinePoint(oval, angle)
          val leftBound = contentBounds.left
          val topBound = contentBounds.top
          val rightBound = contentBounds.right
          val bottomBound = contentBounds.bottom

          val availableWidth =
            if (finalLinePoint.x < oval.centerX()) {
                finalLinePoint.x - leftBound
              } else {
                rightBound - finalLinePoint.x
              }
              .toInt()

          measuredTextWidth =
            getTextMaxWidth(contentBounds).coerceAtMost(availableWidth).coerceAtLeast(0)

          val textBounds =
            textComponent.getBounds(context = this, text = label, maxWidth = measuredTextWidth)
          val alpha = angle.radians

          val left: Float =
            if (finalLinePoint.x < oval.centerX()) {
              val leftInset =
                (leftBound + finalLinePoint.x - textBounds.width()) / sin(-Math.PI.half - alpha)
              if (leftInset < 0) abs(leftInset).toFloat() else 0f
            } else {
              0f
            }

          val top: Float =
            if (finalLinePoint.y < oval.centerY()) {
              val topInset = (topBound + finalLinePoint.y - textBounds.height().half) / sin(-alpha)
              if (topInset < 0) abs(topInset) else 0f
            } else {
              0f
            }

          val right: Float =
            if (finalLinePoint.x > oval.centerX()) {
              val rightInset =
                (finalLinePoint.x + textBounds.width() - rightBound) / sin(Math.PI.half - alpha)
              if (rightInset > 0) rightInset.toFloat() else 0f
            } else {
              0f
            }

          val bottom: Float =
            if (finalLinePoint.y > oval.centerY()) {
              val bottomInset =
                (finalLinePoint.y + textBounds.height().half - bottomBound) / sin(alpha)
              if (bottomInset > 0) bottomInset else 0f
            } else {
              0f
            }

          outInsets.setAllIfGreater(
            start = if (isLtr) left else right,
            top = top,
            end = if (isLtr) right else left,
            bottom = bottom,
          )
        }
      }

      override fun drawLabel(
        context: DrawingContext,
        oval: RectF,
        holeRadius: Float,
        angle: Float,
        slicePath: Path,
        label: CharSequence,
        sliceOpacity: Float,
        labelOpacity: Float,
      ) {
        with(context) {
          val textPoint = drawLine(oval, angle, labelOpacity)
          val horizontalPosition =
            when (angle % FULL_DEGREES) {
              in horizontalPositionStartDegreesRange -> Position.Horizontal.Start
              else -> Position.Horizontal.End
            }

          val canvasSaveCount = canvas.saveLayer(opacity = labelOpacity)
          textComponent.draw(
            context = this,
            text = label,
            x = textPoint.x,
            y = textPoint.y,
            horizontalPosition = horizontalPosition,
            maxWidth = measuredTextWidth,
          )
          canvas.restoreToCount(canvasSaveCount)
        }
      }

      private fun MeasuringContext.getFinalLinePoint(drawOval: RectF, angle: Float): Point {
        val radiusWithTranslation = drawOval.radius + angledSegmentLengthDp.pixels
        val basePoint =
          translatePointByAngle(
            center = drawOval.centerPoint,
            point = Point(drawOval.centerX() + radiusWithTranslation, drawOval.centerY()),
            angle = angle.radiansDouble,
          )
        return Point(
          basePoint.x +
            horizontalSegmentLengthDp.pixels * if (basePoint.x < drawOval.centerX()) -1f else 1f,
          basePoint.y,
        )
      }

      private fun DrawingContext.drawLine(drawOval: RectF, angle: Float, opacity: Float): Point {
        var linePoint =
          translatePointByAngle(
            center = drawOval.centerPoint,
            point = Point(drawOval.centerX() + drawOval.radius, drawOval.centerY()),
            angle = angle.radiansDouble,
          )

        val elbowPoint =
          translatePointByAngle(
            center = drawOval.centerPoint,
            point =
              Point(
                drawOval.centerX() + drawOval.radius + angledSegmentLengthDp.pixels,
                drawOval.centerY(),
              ),
            angle = angle.radiansDouble,
          )
        val endPoint =
          Point(
            elbowPoint.x +
              horizontalSegmentLengthDp.pixels * if (elbowPoint.x < drawOval.centerX()) -1f else 1f,
            elbowPoint.y,
          )

        linePaint.strokeWidth = lineWidthDpValue.pixels
        linePaint.opacity = opacity * (linePaint.color ushr 24) / MAX_HEX_VALUE
        canvas.drawLine(linePoint.x, linePoint.y, elbowPoint.x, elbowPoint.y, linePaint)
        canvas.drawLine(elbowPoint.x, elbowPoint.y, endPoint.x, endPoint.y, linePaint)
        linePoint = endPoint
        return linePoint
      }

      protected companion object {
        public val horizontalPositionStartDegreesRange: ClosedFloatingPointRange<Float> = 90f..270f
      }
    }
  }

  /** Provides [Slice]s to [PieChart]s. */
  public fun interface SliceProvider {
    /** Returns the [Slice] for the specified entry and slice index. */
    public fun getSlice(entry: PieChartModel.Entry, index: Int, extraStore: ExtraStore): Slice

    /** Houses [SliceProvider] factory functions. */
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

  /** Creates a new [PieChart] based on this one. */
  public fun copy(
    sliceProvider: SliceProvider = this.sliceProvider,
    spacingDp: Float = this.spacingDp,
    outerSize: PieSize.Outer = this.outerSize,
    innerSize: PieSize.Inner = this.innerSize,
    startAngle: Float = this.startAngle,
    valueFormatter: PieValueFormatter = this.valueFormatter,
  ): PieChart =
    PieChart(
      sliceProvider = sliceProvider,
      spacingDp = spacingDp,
      outerSize = outerSize,
      innerSize = innerSize,
      startAngle = startAngle,
      valueFormatter = valueFormatter,
      drawingModelInterpolator = drawingModelInterpolator,
      drawingModelKey = drawingModelKey,
    )

  private fun PieChartModel.toDrawingModel(customSize: Int?): PieChartDrawingModel =
    PieChartDrawingModel(
      List(customSize ?: entries.size) { index ->
        val entry = entries.getOrNull(index)
        PieChartDrawingModel.SliceInfo(
          degrees = entry?.value?.let { it / sum * FULL_DEGREES } ?: 0f,
          value = entry?.value,
        )
      }
    )
}
