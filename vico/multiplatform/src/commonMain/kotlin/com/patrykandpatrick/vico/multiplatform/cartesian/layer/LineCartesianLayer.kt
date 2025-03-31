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

package com.patrykandpatrick.vico.multiplatform.cartesian.layer

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.Axis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.LineCartesianLayerDrawingModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.multiplatform.cartesian.data.forEachIn
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer.Line
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer.PointConnector
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.MutableLineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.multiplatform.common.Defaults
import com.patrykandpatrick.vico.multiplatform.common.EmptyPaint
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.Position
import com.patrykandpatrick.vico.multiplatform.common.ValueWrapper
import com.patrykandpatrick.vico.multiplatform.common.component.Component
import com.patrykandpatrick.vico.multiplatform.common.component.TextComponent
import com.patrykandpatrick.vico.multiplatform.common.data.CacheStore
import com.patrykandpatrick.vico.multiplatform.common.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.data.MutableExtraStore
import com.patrykandpatrick.vico.multiplatform.common.doubled
import com.patrykandpatrick.vico.multiplatform.common.getBitmap
import com.patrykandpatrick.vico.multiplatform.common.getPixel
import com.patrykandpatrick.vico.multiplatform.common.getRepeating
import com.patrykandpatrick.vico.multiplatform.common.getStart
import com.patrykandpatrick.vico.multiplatform.common.getValue
import com.patrykandpatrick.vico.multiplatform.common.half
import com.patrykandpatrick.vico.multiplatform.common.inBounds
import com.patrykandpatrick.vico.multiplatform.common.orZero
import com.patrykandpatrick.vico.multiplatform.common.saveLayer
import com.patrykandpatrick.vico.multiplatform.common.setValue
import com.patrykandpatrick.vico.multiplatform.common.vicoTheme
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Draws the content of line charts.
 *
 * @property lineProvider provides the [Line]s.
 * @property pointSpacing the point spacing.
 * @property rangeProvider overrides the _x_ and _y_ ranges.
 * @property verticalAxisPosition the position of the [VerticalAxis] with which the
 *   [LineCartesianLayer] should be associated. Use this for independent [CartesianLayer] scaling.
 * @property drawingModelInterpolator interpolates the [LineCartesianLayerDrawingModel]s.
 */
@Stable
public open class LineCartesianLayer
protected constructor(
  protected val lineProvider: LineProvider,
  protected val pointSpacing: Dp = Defaults.POINT_SPACING.dp,
  protected val rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
  protected val verticalAxisPosition: Axis.Position.Vertical? = null,
  protected val drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<
      LineCartesianLayerDrawingModel.Entry,
      LineCartesianLayerDrawingModel,
    > =
    CartesianLayerDrawingModelInterpolator.default(),
  protected val drawingModelKey: ExtraStore.Key<LineCartesianLayerDrawingModel>,
) : BaseCartesianLayer<LineCartesianLayerModel>() {
  /**
   * Defines the appearance of a line in a line chart.
   *
   * @property fill draws the line fill.
   * @property stroke defines the style of the stroke.
   * @property areaFill draws the area fill.
   * @property pointProvider provides the [Point]s.
   * @property pointConnector connects the line’s points, thus defining its shape.
   * @property dataLabel used for the data labels.
   * @property dataLabelPosition the vertical position of the data labels relative to the points.
   * @property dataLabelValueFormatter formats the data-label values.
   * @property dataLabelRotationDegrees the data-label rotation (in degrees).
   */
  public open class Line(
    protected val fill: LineFill,
    public val stroke: LineStroke = LineStroke.Continuous(),
    protected val areaFill: AreaFill? = null,
    public val pointProvider: PointProvider? = null,
    public val pointConnector: PointConnector = PointConnector.Sharp,
    public val dataLabel: TextComponent? = null,
    public val dataLabelPosition: Position.Vertical = Position.Vertical.Top,
    public val dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
    public val dataLabelRotationDegrees: Float = 0f,
  ) {
    protected val linePaint: Paint = Paint().apply { style = PaintingStyle.Stroke }

    /** Draws the line. */
    public fun draw(
      context: CartesianDrawingContext,
      path: Path,
      lineCanvas: Canvas,
      fillCanvas: Canvas,
      verticalAxisPosition: Axis.Position.Vertical?,
    ) {
      with(context) {
        stroke.apply(this, linePaint)
        val halfThickness = stroke.thickness.pixels.half
        areaFill?.draw(context, path, halfThickness, verticalAxisPosition)
        lineCanvas.drawPath(path, linePaint)
        withCanvas(fillCanvas) { fill.draw(context, halfThickness, verticalAxisPosition) }
      }
    }
  }

  /** Draws a [LineCartesianLayer] line’s fill. */
  public interface LineFill {
    /** Draws the line fill. */
    public fun draw(
      context: CartesianDrawingContext,
      halfLineThickness: Float,
      verticalAxisPosition: Axis.Position.Vertical?,
    )

    /** Houses [LineFill] factory functions. */
    public companion object {
      /** Uses a single [Fill]. */
      public fun single(fill: Fill): LineFill = SingleLineFill(fill)

      /**
       * Uses [topFill] for the portions of the line that are above the [splitY] line, and
       * analogously for [bottomFill]. (The [splitY] line is an imaginary horizontal line whose _y_
       * value is determined by [splitY].)
       */
      public fun double(
        topFill: Fill,
        bottomFill: Fill,
        splitY: (ExtraStore) -> Number = { 0 },
      ): LineFill = DoubleLineFill(topFill, bottomFill, splitY)
    }
  }

  /** Defines the style of a [LineCartesianLayer] line’s stroke. */
  @Immutable
  public sealed interface LineStroke {

    /** The stroke thickness. */
    public val thickness: Dp

    /** Applies the stroke style to [paint]. */
    public fun apply(context: CartesianDrawingContext, paint: Paint)

    /**
     * Produces a continuous stroke.
     *
     * @property cap the stroke cap.
     */
    public data class Continuous(
      override val thickness: Dp = Defaults.LINE_SPEC_THICKNESS_DP.dp,
      public val cap: StrokeCap = StrokeCap.Butt,
    ) : LineStroke {
      override fun apply(context: CartesianDrawingContext, paint: Paint) {
        with(context) {
          paint.strokeWidth = thickness.pixels
          paint.strokeCap = cap
          paint.pathEffect = null
        }
      }
    }

    /**
     * Produces a dashed stroke.
     *
     * @property cap the stroke cap.
     * @property dashLength the dash length.
     * @property gapLength the gap length.
     */
    public data class Dashed(
      public override val thickness: Dp = Defaults.LINE_SPEC_THICKNESS_DP.dp,
      public val cap: StrokeCap = StrokeCap.Butt,
      public val dashLength: Dp = Defaults.LINE_DASH_LENGTH.dp,
      public val gapLength: Dp = Defaults.LINE_GAP_LENGTH.dp,
    ) : LineStroke {
      override fun apply(context: CartesianDrawingContext, paint: Paint) {
        with(context) {
          paint.strokeWidth = thickness.pixels
          paint.strokeCap = cap
          paint.pathEffect =
            PathEffect.dashPathEffect(floatArrayOf(dashLength.pixels, gapLength.pixels), 0f)
        }
      }
    }
  }

  /** Draws a [LineCartesianLayer] line’s area fill. */
  public interface AreaFill {
    /** Draws the area fill. */
    public fun draw(
      context: CartesianDrawingContext,
      linePath: Path,
      halfLineThickness: Float,
      verticalAxisPosition: Axis.Position.Vertical?,
    )

    /** Houses [AreaFill] factory functions. */
    public companion object {
      /**
       * Uses [fill] for the areas bounded by the [LineCartesianLayer] line and the [splitY] line.
       * (The [splitY] line is an imaginary horizontal line whose _y_ value is determined by
       * [splitY].)
       */
      public fun single(fill: Fill, splitY: (ExtraStore) -> Number = { 0 }): AreaFill =
        SingleAreaFill(fill, splitY)

      /**
       * Uses [topFill] for those areas bounded by the [LineCartesianLayer] line and the [splitY]
       * line that are above the [splitY] line, and analogously for [bottomFill]. (The [splitY] line
       * is an imaginary horizontal line whose _y_ value is determined by [splitY].)
       */
      public fun double(
        topFill: Fill,
        bottomFill: Fill,
        splitY: (ExtraStore) -> Number = { 0 },
      ): AreaFill = DoubleAreaFill(topFill, bottomFill, splitY)
    }
  }

  /** Connects a [LineCartesianLayer] line’s points, thus defining its shape. */
  public fun interface PointConnector {
    /** Connects ([x1], [y2]) and ([x2], [y2]). */
    public fun connect(
      context: CartesianDrawingContext,
      path: Path,
      x1: Float,
      y1: Float,
      x2: Float,
      y2: Float,
    )

    /** Houses a [PointConnector] factory function. */
    public companion object {
      /** Uses line segments. */
      public val Sharp: PointConnector = PointConnector { _, path, _, _, x2, y2 ->
        path.lineTo(x2, y2)
      }

      /**
       * Uses cubic Bézier curves. [curvature], which must be in ([0, 1]], defines their strength.
       */
      public fun cubic(
        @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) curvature: Float = 0.5f
      ): PointConnector = CubicPointConnector(curvature)
    }
  }

  /** Provides [Line]s to [LineCartesianLayer]s. */
  public fun interface LineProvider {
    /** Returns the [Line] for the specified series. */
    public fun getLine(seriesIndex: Int, extraStore: ExtraStore): Line

    /** Houses [LineProvider] factory functions. */
    public companion object {
      private data class Series(private val lines: List<Line>) : LineProvider {
        override fun getLine(seriesIndex: Int, extraStore: ExtraStore) =
          lines.getRepeating(seriesIndex)
      }

      /**
       * Uses the provided [Line]s. The [Line]s and series are associated by index. If there are
       * more series than [Line]s, [lines] is iterated multiple times.
       */
      public fun series(lines: List<Line>): LineProvider = Series(lines)

      /**
       * Uses the provided [Line]s. The [Line]s and series are associated by index. If there are
       * more series than [Line]s, the [Line] list is iterated multiple times.
       */
      public fun series(vararg lines: Line): LineProvider = series(lines.toList())
    }
  }

  /**
   * Defines a point style.
   *
   * @param component the point [Component].
   * @property size the point size.
   */
  @Immutable
  public data class Point(
    private val component: Component,
    public val size: Dp = Defaults.POINT_SIZE.dp,
  ) {
    /** Draws a point at ([x], [y]). */
    public fun draw(context: CartesianDrawingContext, x: Float, y: Float) {
      val halfSize = context.run { size.pixels.half }
      component.draw(
        context = context,
        left = x - halfSize,
        top = y - halfSize,
        right = x + halfSize,
        bottom = y + halfSize,
      )
    }
  }

  /** Provides [Point]s to [LineCartesianLayer]s. */
  @Immutable
  public interface PointProvider {
    /** Returns the [Point] for the point with the given properties. */
    public fun getPoint(
      entry: LineCartesianLayerModel.Entry,
      seriesIndex: Int,
      extraStore: ExtraStore,
    ): Point?

    /** Returns the largest [Point]. */
    public fun getLargestPoint(extraStore: ExtraStore): Point?

    /** Houses a [PointProvider] factory function. */
    public companion object {
      private data class Single(private val point: Point) : PointProvider {
        override fun getPoint(
          entry: LineCartesianLayerModel.Entry,
          seriesIndex: Int,
          extraStore: ExtraStore,
        ) = point

        override fun getLargestPoint(extraStore: ExtraStore) = point
      }

      /** Uses [point] for each point. */
      public fun single(point: Point): PointProvider = Single(point)
    }
  }

  private val _markerTargets = mutableMapOf<Double, List<MutableLineCartesianLayerMarkerTarget>>()

  protected val linePath: Path = Path()

  private val srcInPaint = Paint().apply { blendMode = BlendMode.SrcIn }

  protected val cacheKeyNamespace: CacheStore.KeyNamespace = CacheStore.KeyNamespace()

  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

  /** Creates a [LineCartesianLayer]. */
  public constructor(
    lineProvider: LineProvider,
    pointSpacing: Dp = Defaults.POINT_SPACING.dp,
    rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
    verticalAxisPosition: Axis.Position.Vertical? = null,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        LineCartesianLayerDrawingModel.Entry,
        LineCartesianLayerDrawingModel,
      > =
      CartesianLayerDrawingModelInterpolator.default(),
  ) : this(
    lineProvider,
    pointSpacing,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
    ExtraStore.Key(),
  )

  override fun drawInternal(context: CartesianDrawingContext, model: LineCartesianLayerModel) {
    with(context) {
      resetTempData()

      val drawingModel = extraStore.getOrNull(drawingModelKey)

      model.series.forEachIndexed { seriesIndex, series ->
        val pointInfoMap = drawingModel?.getOrNull(seriesIndex)

        linePath.rewind()
        val line = lineProvider.getLine(seriesIndex, model.extraStore)

        var prevX = layerBounds.getStart(isLtr = isLtr)
        var prevY = layerBounds.bottom

        val drawingStartAlignmentCorrection =
          layoutDirectionMultiplier * layerDimensions.startPadding

        val drawingStart =
          layerBounds.getStart(isLtr = isLtr) + drawingStartAlignmentCorrection - scroll

        forEachPointInBounds(
          series = series,
          drawingStart = drawingStart,
          pointInfoMap = pointInfoMap,
          drawFullLineLength = line.stroke is LineStroke.Dashed,
        ) { _, x, y, _, _ ->
          if (linePath.isEmpty) {
            linePath.moveTo(x, y)
          } else {
            line.pointConnector.connect(this, linePath, prevX, prevY, x, y)
          }
          prevX = x
          prevY = y
        }

        saveLayer(opacity = drawingModel?.opacity ?: 1f)

        val (lineBitmap, lineCanvas) = getBitmap(cacheKeyNamespace, seriesIndex, "line")
        val (lineFillBitmap, lineFillCanvas) = getBitmap(cacheKeyNamespace, seriesIndex, "lineFill")
        line.draw(context, linePath, lineCanvas, lineFillCanvas, verticalAxisPosition)
        lineCanvas.drawImage(lineFillBitmap, Offset.Zero, srcInPaint)
        canvas.drawImage(lineBitmap, Offset.Zero, EmptyPaint)

        forEachPointInBounds(series, drawingStart, pointInfoMap) { entry, x, y, _, _ ->
          updateMarkerTargets(entry, x, y, lineFillBitmap)
        }

        drawPointsAndDataLabels(line, series, seriesIndex, drawingStart, pointInfoMap)

        canvas.restore()
      }
    }
  }

  protected open fun CartesianDrawingContext.updateMarkerTargets(
    entry: LineCartesianLayerModel.Entry,
    canvasX: Float,
    canvasY: Float,
    lineFillBitmap: ImageBitmap,
  ) {
    if (canvasX <= layerBounds.left - 1 || canvasX >= layerBounds.right + 1) return
    val limitedCanvasY = canvasY.coerceIn(layerBounds.top, layerBounds.bottom)
    _markerTargets
      .getOrPut(entry.x) { listOf(MutableLineCartesianLayerMarkerTarget(entry.x, canvasX)) }
      .first()
      .points +=
      LineCartesianLayerMarkerTarget.Point(
        entry,
        limitedCanvasY,
        lineFillBitmap.getPixel(
          canvasX
            .roundToInt()
            .coerceIn(ceil(layerBounds.left).toInt(), layerBounds.right.toInt() - 1),
          limitedCanvasY.roundToInt(),
        ),
      )
  }

  protected open fun CartesianDrawingContext.drawPointsAndDataLabels(
    line: Line,
    series: List<LineCartesianLayerModel.Entry>,
    seriesIndex: Int,
    drawingStart: Float,
    pointInfoMap: Map<Double, LineCartesianLayerDrawingModel.Entry>?,
  ) {
    forEachPointInBounds(
      series = series,
      drawingStart = drawingStart,
      pointInfoMap = pointInfoMap,
    ) { chartEntry, x, y, previousX, nextX ->
      val point = line.pointProvider?.getPoint(chartEntry, seriesIndex, model.extraStore)
      point?.draw(this, x, y)

      line.dataLabel
        .takeIf {
          chartEntry.x != ranges.minX && chartEntry.x != ranges.maxX ||
            chartEntry.x == ranges.minX && layerDimensions.startPadding > 0 ||
            chartEntry.x == ranges.maxX && layerDimensions.endPadding > 0
        }
        ?.let { textComponent ->
          val distanceFromLine = max(line.stroke.thickness.pixels, point?.size.orZero.pixels).half

          val text = line.dataLabelValueFormatter.format(this, chartEntry.y, verticalAxisPosition)
          val maxWidth = getMaxDataLabelWidth(chartEntry, x, previousX, nextX)
          val verticalPosition =
            line.dataLabelPosition.inBounds(
              bounds = layerBounds,
              componentHeight =
                textComponent.getHeight(
                  context = this,
                  text = text,
                  maxWidth = maxWidth,
                  rotationDegrees = line.dataLabelRotationDegrees,
                ),
              referenceY = y,
              referenceDistance = distanceFromLine,
            )
          val dataLabelY =
            y +
              when (verticalPosition) {
                Position.Vertical.Top -> -distanceFromLine
                Position.Vertical.Center -> 0f
                Position.Vertical.Bottom -> distanceFromLine
              }
          textComponent.draw(
            context = this,
            x = x,
            y = dataLabelY,
            text = text,
            verticalPosition = verticalPosition,
            maxWidth = maxWidth,
            rotationDegrees = line.dataLabelRotationDegrees,
          )
        }
    }
  }

  protected fun CartesianDrawingContext.getMaxDataLabelWidth(
    entry: LineCartesianLayerModel.Entry,
    x: Float,
    previousX: Float?,
    nextX: Float?,
  ): Int =
    when {
      previousX != null && nextX != null -> min(x - previousX, nextX - x)
      previousX == null && nextX == null ->
        min(layerDimensions.startPadding, layerDimensions.endPadding).doubled
      nextX != null -> {
        ((entry.x - ranges.minX) / ranges.xStep * layerDimensions.xSpacing +
            layerDimensions.startPadding)
          .doubled
          .toFloat()
          .coerceAtMost(nextX - x)
      }
      else -> {
        ((ranges.maxX - entry.x) / ranges.xStep * layerDimensions.xSpacing +
            layerDimensions.endPadding)
          .doubled
          .toFloat()
          .coerceAtMost(x - previousX!!)
      }
    }.toInt()

  protected fun resetTempData() {
    _markerTargets.clear()
    linePath.rewind()
  }

  protected open fun CartesianDrawingContext.forEachPointInBounds(
    series: List<LineCartesianLayerModel.Entry>,
    drawingStart: Float,
    pointInfoMap: Map<Double, LineCartesianLayerDrawingModel.Entry>?,
    drawFullLineLength: Boolean = false,
    action:
      (
        entry: LineCartesianLayerModel.Entry, x: Float, y: Float, previousX: Float?, nextX: Float?,
      ) -> Unit,
  ) {
    val minX = ranges.minX
    val maxX = ranges.maxX
    val xStep = ranges.xStep

    var x: Float? = null
    var nextX: Float? = null

    val boundsStart = layerBounds.getStart(isLtr = isLtr)
    val boundsEnd = boundsStart + layoutDirectionMultiplier * layerBounds.width

    fun getDrawX(entry: LineCartesianLayerModel.Entry): Float =
      drawingStart +
        layoutDirectionMultiplier * layerDimensions.xSpacing * ((entry.x - minX) / xStep).toFloat()

    fun getDrawY(entry: LineCartesianLayerModel.Entry): Float {
      val yRange = ranges.getYRange(verticalAxisPosition)
      return layerBounds.bottom -
        (pointInfoMap?.get(entry.x)?.y ?: ((entry.y - yRange.minY) / yRange.length).toFloat()) *
          layerBounds.height
    }

    series.forEachIn(minX = minX, maxX = maxX, padding = 1) { entry, next ->
      val previousX = x
      val immutableX = nextX ?: getDrawX(entry)
      val immutableNextX = next?.let(::getDrawX)
      x = immutableX
      nextX = immutableNextX
      if (
        drawFullLineLength.not() &&
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

  override fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
    model: LineCartesianLayerModel,
  ) {
    with(context) {
      val maxPointSize =
        (0..<model.series.size)
          .maxOf {
            lineProvider
              .getLine(it, model.extraStore)
              .pointProvider
              ?.getLargestPoint(model.extraStore)
              ?.size
              .orZero
          }
          .pixels
      val xSpacing = maxPointSize + pointSpacing.pixels
      dimensions.ensureValuesAtLeast(
        xSpacing = xSpacing,
        scalableStartPadding = layerPadding.scalableStart.pixels,
        scalableEndPadding = layerPadding.scalableEnd.pixels,
        unscalableStartPadding = maxPointSize.half + layerPadding.unscalableStart.pixels,
        unscalableEndPadding = maxPointSize.half + layerPadding.unscalableEnd.pixels,
      )
    }
  }

  override fun updateChartRanges(
    chartRanges: MutableCartesianChartRanges,
    model: LineCartesianLayerModel,
  ) {
    chartRanges.tryUpdate(
      rangeProvider.getMinX(model.minX, model.maxX, model.extraStore),
      rangeProvider.getMaxX(model.minX, model.maxX, model.extraStore),
      rangeProvider.getMinY(model.minY, model.maxY, model.extraStore),
      rangeProvider.getMaxY(model.minY, model.maxY, model.extraStore),
      verticalAxisPosition,
    )
  }

  override fun updateLayerMargins(
    context: CartesianMeasuringContext,
    layerMargins: CartesianLayerMargins,
    layerDimensions: CartesianLayerDimensions,
    model: LineCartesianLayerModel,
  ) {
    with(context) {
      val verticalMargin =
        (0..<model.series.size)
          .mapNotNull { lineProvider.getLine(it, model.extraStore) }
          .maxOf {
            max(
              it.stroke.thickness.pixels,
              it.pointProvider?.getLargestPoint(model.extraStore)?.size.orZero.pixels,
            )
          }
          .half
      layerMargins.ensureValuesAtLeast(top = verticalMargin, bottom = verticalMargin)
    }
  }

  override fun prepareForTransformation(
    model: LineCartesianLayerModel?,
    ranges: CartesianChartRanges,
    extraStore: MutableExtraStore,
  ) {
    drawingModelInterpolator.setModels(
      old = extraStore.getOrNull(drawingModelKey),
      new = model?.toDrawingModel(ranges),
    )
  }

  override suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {
    drawingModelInterpolator.transform(fraction)?.let { extraStore[drawingModelKey] = it }
      ?: extraStore.remove(drawingModelKey)
  }

  private fun LineCartesianLayerModel.toDrawingModel(
    ranges: CartesianChartRanges
  ): LineCartesianLayerDrawingModel {
    val yRange = ranges.getYRange(verticalAxisPosition)
    return LineCartesianLayerDrawingModel(
      series.map { series ->
        series.associate { entry ->
          entry.x to
            LineCartesianLayerDrawingModel.Entry(
              ((entry.y - yRange.minY) / yRange.length).toFloat()
            )
        }
      }
    )
  }

  /** Creates a new [LineCartesianLayer] based on this one. */
  public fun copy(
    lineProvider: LineProvider = this.lineProvider,
    pointSpacing: Dp = this.pointSpacing,
    rangeProvider: CartesianLayerRangeProvider = this.rangeProvider,
    verticalAxisPosition: Axis.Position.Vertical? = this.verticalAxisPosition,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        LineCartesianLayerDrawingModel.Entry,
        LineCartesianLayerDrawingModel,
      > =
      this.drawingModelInterpolator,
  ): LineCartesianLayer =
    LineCartesianLayer(
      lineProvider,
      pointSpacing,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
      drawingModelKey,
    )

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LineCartesianLayer &&
        lineProvider == other.lineProvider &&
        pointSpacing == other.pointSpacing &&
        rangeProvider == other.rangeProvider &&
        verticalAxisPosition == other.verticalAxisPosition &&
        drawingModelInterpolator == other.drawingModelInterpolator

  override fun hashCode(): Int {
    var result = lineProvider.hashCode()
    result = 31 * result + pointSpacing.hashCode()
    result = 31 * result + rangeProvider.hashCode()
    result = 31 * result + (verticalAxisPosition?.hashCode() ?: 0)
    result = 31 * result + drawingModelInterpolator.hashCode()
    return result
  }

  /** Provides access to [Line] factory functions. */
  public companion object
}

internal fun CartesianDrawingContext.getCanvasSplitY(
  splitY: (ExtraStore) -> Number,
  halfLineThickness: Float,
  verticalAxisPosition: Axis.Position.Vertical?,
): Float {
  val yRange = ranges.getYRange(verticalAxisPosition)
  val base =
    layerBounds.bottom -
      ((splitY(model.extraStore).toDouble() - yRange.minY) / yRange.length).toFloat() *
        layerBounds.height
  return ceil(base).coerceIn(layerBounds.top..layerBounds.bottom) + ceil(halfLineThickness)
}

/** Creates and remembers a [LineCartesianLayer]. */
@Composable
public fun rememberLineCartesianLayer(
  lineProvider: LineCartesianLayer.LineProvider =
    LineCartesianLayer.LineProvider.series(
      vicoTheme.lineCartesianLayerColors.map { color ->
        LineCartesianLayer.rememberLine(LineCartesianLayer.LineFill.single(Fill(color)))
      }
    ),
  pointSpacing: Dp = Defaults.POINT_SPACING.dp,
  rangeProvider: CartesianLayerRangeProvider = remember { CartesianLayerRangeProvider.auto() },
  verticalAxisPosition: Axis.Position.Vertical? = null,
  drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<
      LineCartesianLayerDrawingModel.Entry,
      LineCartesianLayerDrawingModel,
    > =
    remember {
      CartesianLayerDrawingModelInterpolator.default()
    },
): LineCartesianLayer {
  var lineCartesianLayerWrapper by remember { ValueWrapper<LineCartesianLayer?>(null) }
  return remember(
    lineProvider,
    pointSpacing,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
  ) {
    val lineCartesianLayer =
      lineCartesianLayerWrapper?.copy(
        lineProvider,
        pointSpacing,
        rangeProvider,
        verticalAxisPosition,
        drawingModelInterpolator,
      )
        ?: LineCartesianLayer(
          lineProvider,
          pointSpacing,
          rangeProvider,
          verticalAxisPosition,
          drawingModelInterpolator,
        )
    lineCartesianLayerWrapper = lineCartesianLayer
    lineCartesianLayer
  }
}

/** Creates and remembers a [LineCartesianLayer.Line]. */
@Composable
public fun LineCartesianLayer.Companion.rememberLine(
  fill: LineCartesianLayer.LineFill =
    vicoTheme.lineCartesianLayerColors.first().let { color ->
      remember(color) { LineCartesianLayer.LineFill.single(Fill(color)) }
    },
  stroke: LineCartesianLayer.LineStroke = LineCartesianLayer.LineStroke.Continuous(),
  areaFill: LineCartesianLayer.AreaFill? = null,
  pointProvider: LineCartesianLayer.PointProvider? = null,
  pointConnector: PointConnector = PointConnector.Sharp,
  dataLabel: TextComponent? = null,
  dataLabelPosition: Position.Vertical = Position.Vertical.Top,
  dataLabelValueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
  dataLabelRotationDegrees: Float = 0f,
): Line =
  remember(
    fill,
    stroke,
    areaFill,
    pointProvider,
    pointConnector,
    dataLabel,
    dataLabelPosition,
    dataLabelRotationDegrees,
    dataLabelRotationDegrees,
  ) {
    Line(
      fill,
      stroke,
      areaFill,
      pointProvider,
      pointConnector,
      dataLabel,
      dataLabelPosition,
      dataLabelValueFormatter,
      dataLabelRotationDegrees,
    )
  }
