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

package com.patrykandpatrick.vico.core.cartesian.layer

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.DefaultPointConnector
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ChartValues
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.MutableChartValues
import com.patrykandpatrick.vico.core.cartesian.data.forEachIn
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineSpec
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineSpec.PointConnector
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.MutableLineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.Point
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.DefaultDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.data.DrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore
import com.patrykandpatrick.vico.core.common.doubled
import com.patrykandpatrick.vico.core.common.getEnd
import com.patrykandpatrick.vico.core.common.getRepeating
import com.patrykandpatrick.vico.core.common.getStart
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.inBounds
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shader.TopBottomShader
import com.patrykandpatrick.vico.core.common.withOpacity
import kotlin.math.max
import kotlin.math.min

/**
 * [LineCartesianLayer] displays data as a continuous line.
 *
 * @param lines a [List] of [LineSpec]s defining the style of each line.
 * @param spacingDp the spacing between each [LineSpec.point] (in dp).
 * @param verticalAxisPosition the position of the [VerticalAxis] with which the
 *   [LineCartesianLayer] should be associated. Use this for independent [CartesianLayer] scaling.
 * @param drawingModelInterpolator interpolates the [LineCartesianLayer]’s
 *   [LineCartesianLayerDrawingModel]s.
 */
public open class LineCartesianLayer(
  public var lines: List<LineSpec>,
  public var spacingDp: Float = Defaults.POINT_SPACING,
  public var verticalAxisPosition: AxisPosition.Vertical? = null,
  public var drawingModelInterpolator:
    DrawingModelInterpolator<
      LineCartesianLayerDrawingModel.PointInfo,
      LineCartesianLayerDrawingModel,
    > =
    DefaultDrawingModelInterpolator(),
) : BaseCartesianLayer<LineCartesianLayerModel>() {
  /**
   * Creates a [LineCartesianLayer] with a common style for all lines.
   *
   * @param line a [LineSpec] defining the style of each line.
   * @param spacingDp the spacing between each [LineSpec.point] (in dp).
   * @param verticalAxisPosition the position of the [VerticalAxis] with which the
   *   [LineCartesianLayer] should be associated. Use this for independent [CartesianLayer] scaling.
   */
  public constructor(
    line: LineSpec,
    spacingDp: Float,
    verticalAxisPosition: AxisPosition.Vertical? = null,
  ) : this(listOf(line), spacingDp, verticalAxisPosition)

  /**
   * Defines the appearance of a line in a line chart.
   *
   * @param shader the [DynamicShader] for the line.
   * @param thicknessDp the thickness of the line (in dp).
   * @param backgroundShader an optional [DynamicShader] to use for the areas bounded by the
   *   [LineCartesianLayer] line and the zero line (_y_ = 0).
   * @param cap the stroke cap for the line.
   * @param point an optional [Component] that can be drawn at a given point on the line.
   * @param pointSizeDp the size of the [point] (in dp).
   * @param dataLabel an optional [TextComponent] to use for data labels.
   * @param dataLabelVerticalPosition the vertical position of data labels relative to the line.
   * @param dataLabelValueFormatter the [CartesianValueFormatter] to use for data labels.
   * @param dataLabelRotationDegrees the rotation of data labels (in degrees).
   * @param pointConnector the [PointConnector] for the line.
   */
  public open class LineSpec(
    public var shader: DynamicShader,
    public var thicknessDp: Float = Defaults.LINE_SPEC_THICKNESS_DP,
    public var backgroundShader: DynamicShader? = null,
    public var cap: Paint.Cap = Paint.Cap.ROUND,
    public var point: Component? = null,
    public var pointSizeDp: Float = Defaults.POINT_SIZE,
    public var dataLabel: TextComponent? = null,
    public var dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
    public var dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
    public var dataLabelRotationDegrees: Float = 0f,
    public var pointConnector: PointConnector = DefaultPointConnector(),
  ) {
    /** Returns `true` if the [backgroundShader] is not null, and `false` otherwise. */
    public val hasLineBackgroundShader: Boolean
      get() = backgroundShader != null

    protected val linePaint: Paint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = cap
      }

    protected val lineBackgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected val lineBackgroundPath: Path = Path()

    protected val clipPath: Path = Path()

    protected val pathBounds: RectF = RectF()

    /** The stroke cap for the line. */
    public var lineStrokeCap: Paint.Cap by linePaint::strokeCap

    /**
     * Draws a [point] at the given [x] and [y] coordinates, using the provided [context].
     *
     * @see Component
     */
    public fun drawPoint(context: CartesianDrawContext, x: Float, y: Float): Unit =
      with(context) { point?.drawPoint(context, x, y, pointSizeDp.pixels.half) }

    /** Draws the line. */
    public fun drawLine(
      context: DrawContext,
      bounds: RectF,
      zeroLineYFraction: Float,
      path: Path,
      opacity: Float = 1f,
    ) {
      with(context) {
        linePaint.strokeWidth = thicknessDp.pixels
        setSplitY(zeroLineYFraction)
        linePaint.shader = shader.provideShader(context, bounds)
        linePaint.withOpacity(opacity) { canvas.drawPath(path, it) }
      }
    }

    /** Draws the line background. */
    public fun drawBackgroundLine(
      context: DrawContext,
      bounds: RectF,
      zeroLineYFraction: Float,
      path: Path,
      opacity: Float = 1f,
    ) {
      val fill = backgroundShader ?: return
      with(lineBackgroundPaint) {
        if (zeroLineYFraction > 0) {
          val zeroLineY = bounds.top + (zeroLineYFraction * bounds.height())
          setSplitY(1f)
          shader = fill.provideShader(context, bounds.left, bounds.top, bounds.right, zeroLineY)
          lineBackgroundPath.set(path)
          lineBackgroundPath.computeBounds(pathBounds, false)
          lineBackgroundPath.lineTo(pathBounds.getEnd(context.isLtr), bounds.bottom)
          lineBackgroundPath.lineTo(pathBounds.getStart(context.isLtr), bounds.bottom)
          lineBackgroundPath.close()
          clipPath.rewind()
          clipPath.addRect(bounds.left, bounds.top, bounds.right, zeroLineY, Path.Direction.CW)
          lineBackgroundPath.op(clipPath, Path.Op.INTERSECT)
          withOpacity(opacity) { context.canvas.drawPath(lineBackgroundPath, it) }
        }

        if (zeroLineYFraction < 1f) {
          val zeroLineY = bounds.top + (zeroLineYFraction * bounds.height())
          setSplitY(0f)
          shader = fill.provideShader(context, bounds.left, zeroLineY, bounds.right, bounds.bottom)
          lineBackgroundPath.set(path)
          lineBackgroundPath.computeBounds(pathBounds, false)
          lineBackgroundPath.lineTo(pathBounds.getEnd(context.isLtr), bounds.top)
          lineBackgroundPath.lineTo(pathBounds.getStart(context.isLtr), bounds.top)
          lineBackgroundPath.close()
          clipPath.rewind()
          clipPath.addRect(bounds.left, zeroLineY, bounds.right, bounds.bottom, Path.Direction.CW)
          lineBackgroundPath.op(clipPath, Path.Op.INTERSECT)
          withOpacity(opacity) { context.canvas.drawPath(lineBackgroundPath, it) }
        }
      }
    }

    /**
     * For [shader] and [backgroundShader], if the [DynamicShader] is a [TopBottomShader], updates
     * [TopBottomShader.splitY] to match the position of the zero line (_y_ = 0).
     */
    public fun setSplitY(splitY: Float) {
      (shader as? TopBottomShader)?.splitY = splitY
      (backgroundShader as? TopBottomShader)?.splitY = splitY
    }

    /**
     * Defines the shape of a line in a line chart by specifying how points are to be connected.
     *
     * @see DefaultPointConnector
     */
    public interface PointConnector {
      /** Draws a line between two points. */
      public fun connect(
        path: Path,
        prevX: Float,
        prevY: Float,
        x: Float,
        y: Float,
        horizontalDimensions: HorizontalDimensions,
        bounds: RectF,
      )
    }
  }

  private val _markerTargets = mutableMapOf<Float, List<MutableLineCartesianLayerMarkerTarget>>()

  /** The [Path] used to draw the lines, each of which corresponds to a [LineSpec]. */
  protected val linePath: Path = Path()

  /**
   * The [Path] used to draw the backgrounds of the lines, each of which corresponds to a
   * [LineSpec].
   */
  protected val lineBackgroundPath: Path = Path()

  protected val drawingModelKey: ExtraStore.Key<LineCartesianLayerDrawingModel> = ExtraStore.Key()

  override val markerTargets: Map<Float, List<CartesianMarker.Target>> = _markerTargets

  override fun drawInternal(context: CartesianDrawContext, model: LineCartesianLayerModel): Unit =
    with(context) {
      resetTempData()

      val drawingModel = model.extraStore.getOrNull(drawingModelKey)
      val yRange = chartValues.getYRange(verticalAxisPosition)
      val zeroLineYFraction =
        drawingModel?.zeroY ?: (yRange.minY / yRange.length + 1f).coerceIn(0f..1f)

      model.series.forEachIndexed { entryListIndex, entries ->
        val pointInfoMap = drawingModel?.getOrNull(entryListIndex)

        linePath.rewind()
        lineBackgroundPath.rewind()
        val component = lines.getRepeating(entryListIndex).apply { setSplitY(zeroLineYFraction) }

        var prevX = bounds.getStart(isLtr = isLtr)
        var prevY = bounds.bottom

        val drawingStartAlignmentCorrection =
          layoutDirectionMultiplier * horizontalDimensions.startPadding

        val drawingStart = bounds.getStart(isLtr = isLtr) + drawingStartAlignmentCorrection - scroll

        forEachPointInBounds(
          series = entries,
          drawingStart = drawingStart,
          pointInfoMap = pointInfoMap,
        ) { entry, x, y, _, _ ->
          if (linePath.isEmpty) {
            linePath.moveTo(x, y)
          } else {
            component.pointConnector.connect(
              path = linePath,
              prevX = prevX,
              prevY = prevY,
              x = x,
              y = y,
              horizontalDimensions = horizontalDimensions,
              bounds = bounds,
            )
          }
          prevX = x
          prevY = y

          updateMarkerTargets(entry, x, y, component)
        }

        if (component.hasLineBackgroundShader) {
          lineBackgroundPath.addPath(linePath)
          lineBackgroundPath.lineTo(prevX, bounds.bottom)
          component.drawBackgroundLine(
            context,
            bounds,
            zeroLineYFraction,
            lineBackgroundPath,
            drawingModel?.opacity ?: 1f,
          )
        }

        component.drawLine(
          context,
          bounds,
          zeroLineYFraction,
          linePath,
          drawingModel?.opacity ?: 1f,
        )

        drawPointsAndDataLabels(
          lineSpec = component,
          series = entries,
          drawingStart = drawingStart,
          pointInfoMap = pointInfoMap,
        )
      }
    }

  protected open fun CartesianDrawContext.updateMarkerTargets(
    entry: LineCartesianLayerModel.Entry,
    canvasX: Float,
    canvasY: Float,
    lineSpec: LineSpec,
  ) {
    if (canvasX <= bounds.left - 1 || canvasX >= bounds.right + 1) return
    val limitedCanvasY = canvasY.coerceIn(bounds.top, bounds.bottom)
    _markerTargets
      .getOrPut(entry.x) { listOf(MutableLineCartesianLayerMarkerTarget(entry.x, canvasX)) }
      .first()
      .points +=
      LineCartesianLayerMarkerTarget.Point(
        entry,
        limitedCanvasY,
        lineSpec.shader.getColorAt(Point(canvasX, limitedCanvasY), this, bounds),
      )
  }

  /**
   * Draws a line’s points ([LineSpec.point]) and their corresponding data labels
   * ([LineSpec.dataLabel]).
   */
  protected open fun CartesianDrawContext.drawPointsAndDataLabels(
    lineSpec: LineSpec,
    series: List<LineCartesianLayerModel.Entry>,
    drawingStart: Float,
    pointInfoMap: Map<Float, LineCartesianLayerDrawingModel.PointInfo>?,
  ) {
    if (lineSpec.point == null && lineSpec.dataLabel == null) return

    forEachPointInBounds(
      series = series,
      drawingStart = drawingStart,
      pointInfoMap = pointInfoMap,
    ) { chartEntry, x, y, previousX, nextX ->
      if (lineSpec.point != null) lineSpec.drawPoint(context = this, x = x, y = y)

      lineSpec.dataLabel
        .takeIf {
          horizontalLayout is HorizontalLayout.Segmented ||
            chartEntry.x != chartValues.minX && chartEntry.x != chartValues.maxX ||
            chartEntry.x == chartValues.minX && horizontalDimensions.startPadding > 0 ||
            chartEntry.x == chartValues.maxX && horizontalDimensions.endPadding > 0
        }
        ?.let { textComponent ->
          val distanceFromLine =
            maxOf(a = lineSpec.thicknessDp, b = lineSpec.pointSizeDpOrZero).half.pixels

          val text =
            lineSpec.dataLabelValueFormatter.format(
              value = chartEntry.y,
              chartValues = chartValues,
              verticalAxisPosition = verticalAxisPosition,
            )
          val maxWidth = getMaxDataLabelWidth(chartEntry, x, previousX, nextX)
          val verticalPosition =
            lineSpec.dataLabelVerticalPosition.inBounds(
              bounds = bounds,
              distanceFromPoint = distanceFromLine,
              componentHeight =
                textComponent.getHeight(
                  context = this,
                  text = text,
                  width = maxWidth,
                  rotationDegrees = lineSpec.dataLabelRotationDegrees,
                ),
              y = y,
            )
          val dataLabelY =
            y +
              when (verticalPosition) {
                VerticalPosition.Top -> -distanceFromLine
                VerticalPosition.Center -> 0f
                VerticalPosition.Bottom -> distanceFromLine
              }
          textComponent.drawText(
            context = this,
            textX = x,
            textY = dataLabelY,
            text = text,
            verticalPosition = verticalPosition,
            maxTextWidth = maxWidth,
            rotationDegrees = lineSpec.dataLabelRotationDegrees,
          )
        }
    }
  }

  protected fun CartesianDrawContext.getMaxDataLabelWidth(
    entry: LineCartesianLayerModel.Entry,
    x: Float,
    previousX: Float?,
    nextX: Float?,
  ): Int =
    when {
      previousX != null && nextX != null -> min(x - previousX, nextX - x)
      previousX == null && nextX == null ->
        min(horizontalDimensions.startPadding, horizontalDimensions.endPadding).doubled
      nextX != null -> {
        val extraSpace =
          when (horizontalLayout) {
            is HorizontalLayout.Segmented -> horizontalDimensions.xSpacing.half
            is HorizontalLayout.FullWidth -> horizontalDimensions.startPadding
          }
        ((entry.x - chartValues.minX) / chartValues.xStep * horizontalDimensions.xSpacing +
            extraSpace)
          .doubled
          .coerceAtMost(nextX - x)
      }
      else -> {
        val extraSpace =
          when (horizontalLayout) {
            is HorizontalLayout.Segmented -> horizontalDimensions.xSpacing.half
            is HorizontalLayout.FullWidth -> horizontalDimensions.endPadding
          }
        ((chartValues.maxX - entry.x) / chartValues.xStep * horizontalDimensions.xSpacing +
            extraSpace)
          .doubled
          .coerceAtMost(x - previousX!!)
      }
    }.toInt()

  /** Clears the temporary data saved during a single [drawInternal] run. */
  protected fun resetTempData() {
    _markerTargets.clear()
    linePath.rewind()
    lineBackgroundPath.rewind()
  }

  protected open fun CartesianDrawContext.forEachPointInBounds(
    series: List<LineCartesianLayerModel.Entry>,
    drawingStart: Float,
    pointInfoMap: Map<Float, LineCartesianLayerDrawingModel.PointInfo>?,
    action:
      (
        entry: LineCartesianLayerModel.Entry, x: Float, y: Float, previousX: Float?, nextX: Float?,
      ) -> Unit,
  ) {
    val minX = chartValues.minX
    val maxX = chartValues.maxX
    val xStep = chartValues.xStep

    var x: Float? = null
    var nextX: Float? = null

    val boundsStart = bounds.getStart(isLtr = isLtr)
    val boundsEnd = boundsStart + layoutDirectionMultiplier * bounds.width()

    fun getDrawX(entry: LineCartesianLayerModel.Entry): Float =
      drawingStart +
        layoutDirectionMultiplier * horizontalDimensions.xSpacing * (entry.x - minX) / xStep

    fun getDrawY(entry: LineCartesianLayerModel.Entry): Float {
      val yRange = chartValues.getYRange(verticalAxisPosition)
      return bounds.bottom -
        (pointInfoMap?.get(entry.x)?.y ?: ((entry.y - yRange.minY) / yRange.length)) *
          bounds.height()
    }

    series.forEachIn(range = minX..maxX, padding = 1) { entry, next ->
      val previousX = x
      val immutableX = nextX ?: getDrawX(entry)
      val immutableNextX = next?.let(::getDrawX)
      x = immutableX
      nextX = immutableNextX
      if (
        immutableNextX != null &&
          (isLtr && immutableX < boundsStart || !isLtr && immutableX > boundsStart) &&
          (isLtr && immutableNextX < boundsStart || !isLtr && immutableNextX > boundsStart)
      ) {
        return@forEachIn
      }
      action(entry, immutableX, getDrawY(entry), previousX, nextX)
      if (isLtr && immutableX > boundsEnd || isLtr.not() && immutableX < boundsEnd) return
    }
  }

  override fun updateHorizontalDimensions(
    context: CartesianMeasureContext,
    horizontalDimensions: MutableHorizontalDimensions,
    model: LineCartesianLayerModel,
  ) {
    with(context) {
      val maxPointSize = lines.maxOf { it.pointSizeDpOrZero }.pixels
      val xSpacing = maxPointSize + spacingDp.pixels
      when (val horizontalLayout = horizontalLayout) {
        is HorizontalLayout.Segmented ->
          horizontalDimensions.ensureSegmentedValues(xSpacing, chartValues)
        is HorizontalLayout.FullWidth -> {
          horizontalDimensions.ensureValuesAtLeast(
            xSpacing = xSpacing,
            scalableStartPadding = horizontalLayout.scalableStartPaddingDp.pixels,
            scalableEndPadding = horizontalLayout.scalableEndPaddingDp.pixels,
            unscalableStartPadding =
              maxPointSize.half + horizontalLayout.unscalableStartPaddingDp.pixels,
            unscalableEndPadding =
              maxPointSize.half + horizontalLayout.unscalableEndPaddingDp.pixels,
          )
        }
      }
    }
  }

  override fun updateChartValues(chartValues: MutableChartValues, model: LineCartesianLayerModel) {
    chartValues.tryUpdate(
      axisValueOverrider.getMinX(model.minX, model.maxX, model.extraStore),
      axisValueOverrider.getMaxX(model.minX, model.maxX, model.extraStore),
      axisValueOverrider.getMinY(model.minY, model.maxY, model.extraStore),
      axisValueOverrider.getMaxY(model.minY, model.maxY, model.extraStore),
      verticalAxisPosition,
    )
  }

  override fun updateInsets(
    context: CartesianMeasureContext,
    horizontalDimensions: HorizontalDimensions,
    insets: Insets,
  ): Unit =
    with(context) {
      val verticalInset =
        lines
          .maxOf { if (it.point != null) max(it.thicknessDp, it.pointSizeDp) else it.thicknessDp }
          .pixels
      insets.ensureValuesAtLeast(top = verticalInset, bottom = verticalInset)
    }

  override fun prepareForTransformation(
    model: LineCartesianLayerModel?,
    extraStore: MutableExtraStore,
    chartValues: ChartValues,
  ) {
    drawingModelInterpolator.setModels(
      old = extraStore.getOrNull(drawingModelKey),
      new = model?.toDrawingModel(chartValues),
    )
  }

  override suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {
    drawingModelInterpolator.transform(fraction)?.let { extraStore[drawingModelKey] = it }
      ?: extraStore.remove(drawingModelKey)
  }

  private fun LineCartesianLayerModel.toDrawingModel(
    chartValues: ChartValues
  ): LineCartesianLayerDrawingModel {
    val yRange = chartValues.getYRange(verticalAxisPosition)
    return series
      .map { series ->
        series.associate { entry ->
          entry.x to
            LineCartesianLayerDrawingModel.PointInfo((entry.y - yRange.minY) / yRange.length)
        }
      }
      .let { pointInfo ->
        LineCartesianLayerDrawingModel(
          pointInfo,
          (yRange.minY / yRange.length + 1f).coerceIn(0f..1f),
        )
      }
  }
}

/** Creates a new [LineCartesianLayer.LineSpec] based on this one, updating select properties. */
public fun LineSpec.copy(
  shader: DynamicShader = this.shader,
  thicknessDp: Float = this.thicknessDp,
  backgroundShader: DynamicShader? = this.backgroundShader,
  cap: Paint.Cap = this.cap,
  point: Component? = this.point,
  pointSizeDp: Float = this.pointSizeDp,
  dataLabel: TextComponent? = this.dataLabel,
  dataLabelVerticalPosition: VerticalPosition = this.dataLabelVerticalPosition,
  dataLabelValueFormatter: CartesianValueFormatter = this.dataLabelValueFormatter,
  dataLabelRotationDegrees: Float = this.dataLabelRotationDegrees,
  pointConnector: PointConnector = this.pointConnector,
): LineSpec =
  LineSpec(
    shader = shader,
    thicknessDp = thicknessDp,
    backgroundShader = backgroundShader,
    cap = cap,
    point = point,
    pointSizeDp = pointSizeDp,
    dataLabel = dataLabel,
    dataLabelVerticalPosition = dataLabelVerticalPosition,
    dataLabelValueFormatter = dataLabelValueFormatter,
    dataLabelRotationDegrees = dataLabelRotationDegrees,
    pointConnector = pointConnector,
  )

internal fun Component.drawPoint(
  context: CartesianDrawContext,
  x: Float,
  y: Float,
  halfPointSize: Float,
) {
  draw(
    context = context,
    left = x - halfPointSize,
    top = y - halfPointSize,
    right = x + halfPointSize,
    bottom = y + halfPointSize,
  )
}

internal inline val LineSpec.pointSizeDpOrZero: Float
  get() = if (point != null) pointSizeDp else 0f
