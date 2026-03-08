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

package com.patrykandpatrick.vico.views.cartesian.layer

import android.graphics.*
import androidx.annotation.FloatRange
import androidx.core.graphics.get
import com.patrykandpatrick.vico.views.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.views.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.views.cartesian.axis.Axis
import com.patrykandpatrick.vico.views.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.views.cartesian.data.*
import com.patrykandpatrick.vico.views.cartesian.layer.LineCartesianLayer.PointConnector
import com.patrykandpatrick.vico.views.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.views.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.views.cartesian.marker.MutableLineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.views.common.*
import com.patrykandpatrick.vico.views.common.component.Component
import com.patrykandpatrick.vico.views.common.component.TextComponent
import com.patrykandpatrick.vico.views.common.data.CacheStore
import com.patrykandpatrick.vico.views.common.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import com.patrykandpatrick.vico.views.common.data.MutableExtraStore
import java.util.*
import kotlin.math.*

/**
 * Draws the content of line charts.
 *
 * @property lineProvider provides the [Line]s.
 * @property pointSpacingDp the point spacing (in dp).
 * @property rangeProvider overrides the _x_ and _y_ ranges.
 * @property verticalAxisPosition the position of the [VerticalAxis] with which the
 *   [LineCartesianLayer] should be associated. Use this for independent [CartesianLayer] scaling.
 * @property drawingModelInterpolator interpolates the [LineCartesianLayerDrawingModel]s.
 */
public open class LineCartesianLayer
protected constructor(
  protected val lineProvider: LineProvider,
  protected val pointSpacingDp: Float = Defaults.POINT_SPACING,
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
   * @property interpolator interpolates between the line’s points, defining its shape.
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
    public val interpolator: Interpolator = Interpolator.Sharp,
    public val dataLabel: TextComponent? = null,
    public val dataLabelPosition: Position.Vertical = Position.Vertical.Top,
    public val dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
    public val dataLabelRotationDegrees: Float = 0f,
  ) {
    /** Creates a [Line] with a [PointConnector]. */
    @Suppress("DEPRECATION")
    @Deprecated("Use the constructor with `interpolator`.")
    public constructor(
      fill: LineFill,
      stroke: LineStroke = LineStroke.Continuous(),
      areaFill: AreaFill? = null,
      pointProvider: PointProvider? = null,
      pointConnector: PointConnector,
      dataLabel: TextComponent? = null,
      dataLabelPosition: Position.Vertical = Position.Vertical.Top,
      dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
      dataLabelRotationDegrees: Float = 0f,
    ) : this(
      fill,
      stroke,
      areaFill,
      pointProvider,
      PointConnectorAdapter(pointConnector),
      dataLabel,
      dataLabelPosition,
      dataLabelValueFormatter,
      dataLabelRotationDegrees,
    )

    /** Connects the line’s points, defining its shape. */
    @Suppress("DEPRECATION")
    @Deprecated("Use `interpolator`.", ReplaceWith("interpolator"))
    public val pointConnector: PointConnector
      get() = (interpolator as? PointConnectorAdapter)?.pointConnector ?: PointConnector.Sharp

    protected val linePaint: Paint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

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
        val halfThickness = stroke.thicknessDp.pixels.half
        areaFill?.draw(context, path, halfThickness, verticalAxisPosition)
        lineCanvas.drawPath(path, linePaint)
        withCanvas(fillCanvas) { fill.draw(context, halfThickness, verticalAxisPosition) }
      }
    }

    /** The [LineFill]’s solid color, or `null` if the [LineFill] has no single solid color. */
    public val fillColor: Int?
      get() = (fill as? SingleLineFill)?.takeIf { it.fill.shaderProvider == null }?.fill?.color

    /** Draws the line. */
    public fun draw(
      context: CartesianDrawingContext,
      path: Path,
      color: Int,
      verticalAxisPosition: Axis.Position.Vertical?,
    ) {
      with(context) {
        stroke.apply(this, linePaint)
        val halfThickness = stroke.thicknessDp.pixels.half
        areaFill?.draw(context, path, halfThickness, verticalAxisPosition)
        linePaint.color = color
        canvas.drawPath(path, linePaint)
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
  public sealed interface LineStroke {

    /** The stroke thickness (in dp). */
    public val thicknessDp: Float

    /** Applies the stroke style to [paint]. */
    public fun apply(context: CartesianDrawingContext, paint: Paint)

    /**
     * Produces a continuous stroke.
     *
     * @property cap the stroke cap.
     */
    public data class Continuous(
      override val thicknessDp: Float = Defaults.LINE_SPEC_THICKNESS_DP,
      public val cap: Paint.Cap = Paint.Cap.BUTT,
    ) : LineStroke {
      override fun apply(context: CartesianDrawingContext, paint: Paint) {
        with(context) {
          paint.strokeWidth = thicknessDp.pixels
          paint.strokeCap = cap
          paint.pathEffect = null
        }
      }
    }

    /**
     * Produces a dashed stroke.
     *
     * @property cap the stroke cap.
     * @property dashLengthDp the dash length (in dp).
     * @property gapLengthDp the gap length (in dp).
     */
    public data class Dashed(
      public override val thicknessDp: Float = Defaults.LINE_SPEC_THICKNESS_DP,
      public val cap: Paint.Cap = Paint.Cap.BUTT,
      public val dashLengthDp: Float = Defaults.LINE_DASH_LENGTH,
      public val gapLengthDp: Float = Defaults.LINE_GAP_LENGTH,
    ) : LineStroke {
      override fun apply(context: CartesianDrawingContext, paint: Paint) {
        with(context) {
          paint.strokeWidth = thicknessDp.pixels
          paint.strokeCap = cap
          paint.pathEffect =
            DashPathEffect(floatArrayOf(dashLengthDp.pixels, gapLengthDp.pixels), 0f)
        }
      }
    }

    /** Provides access to [LineStroke] factory functions. */
    public companion object
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

  /** Connects a [LineCartesianLayer] line’s points, defining its shape. */
  @Suppress("DEPRECATION")
  @Deprecated("Use `Interpolator`.")
  public fun interface PointConnector {
    /** Connects ([x1], [y1]) and ([x2], [y2]). */
    public fun connect(
      context: CartesianDrawingContext,
      path: Path,
      x1: Float,
      y1: Float,
      x2: Float,
      y2: Float,
    )

    /** Houses [PointConnector] singletons and factory functions. */
    public companion object {
      /** Uses line segments. */
      @Deprecated("Use `Interpolator.Sharp`.", ReplaceWith("Interpolator.Sharp"))
      public val Sharp: PointConnector = PointConnector { _, path, _, _, x2, y2 ->
        path.lineTo(x2, y2)
      }

      /**
       * Uses cubic Bézier curves. [curvature], which must be in ([0, 1]], defines their strength.
       */
      @Suppress("DEPRECATION")
      @Deprecated("Use `Interpolator.cubic`.", ReplaceWith("Interpolator.cubic()"))
      public fun cubic(
        @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) curvature: Float = 0.5f
      ): PointConnector = CubicPointConnector(curvature)
    }
  }

  /** Interpolates between a [LineCartesianLayer] line’s points, defining its shape. */
  public interface Interpolator {
    /**
     * Draws [path] through [points]. Only the points in [visibleIndexRange] need to produce path
     * operations, but the remaining points are available for use in interpolation.
     */
    public fun interpolate(
      context: CartesianDrawingContext,
      path: Path,
      points: List<PointF>,
      visibleIndexRange: IntRange,
    )

    /**
     * Returns the _y_-value range of the interpolated curve for the given [y] values. This may be
     * wider than the range of [y] if the interpolation overshoots (e.g., for splines). The default
     * implementation returns the range of [y].
     */
    public fun getYRange(y: List<Double>): ClosedRange<Double> = y.min()..y.max()

    /** Houses [Interpolator] singletons and factory functions. */
    public companion object {
      /** Uses line segments. */
      public val Sharp: Interpolator =
        object : Interpolator {
          override fun interpolate(
            context: CartesianDrawingContext,
            path: Path,
            points: List<PointF>,
            visibleIndexRange: IntRange,
          ) {
            for (index in visibleIndexRange) {
              val point = points[index]
              if (index == visibleIndexRange.first) {
                path.moveTo(point.x, point.y)
              } else {
                path.lineTo(point.x, point.y)
              }
            }
          }
        }

      /**
       * Uses cubic Bézier curves. [curvature], which must be in ([0, 1]], defines their strength.
       */
      public fun cubic(
        @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) curvature: Float = 0.5f
      ): Interpolator = CubicInterpolator(curvature)

      /**
       * Uses a Catmull–Rom spline. [alpha], which must be in [[0, 1)], controls the tightness: 0
       * (the default) produces the standard Catmull–Rom spline, and values approaching 1 produce
       * near-straight lines. Catmull–Rom splines pass through all data points and produce straight
       * segments for collinear points.
       */
      public fun catmullRom(
        @FloatRange(from = 0.0, to = 1.0, toInclusive = false) alpha: Float = 0f
      ): Interpolator = CatmullRomInterpolator(alpha)
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
   * @property sizeDp the point size (in dp).
   */
  public data class Point(
    private val component: Component,
    public val sizeDp: Float = Defaults.POINT_SIZE,
  ) {
    /** Draws a point at ([x], [y]). */
    public fun draw(context: CartesianDrawingContext, x: Float, y: Float) {
      val halfSize = context.run { sizeDp.half.pixels }
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

  protected val lineCanvas: Canvas = Canvas()

  protected val lineFillCanvas: Canvas = Canvas()

  private val srcInPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) }

  protected val cacheKeyNamespace: CacheStore.KeyNamespace = CacheStore.KeyNamespace()

  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

  /** Creates a [LineCartesianLayer]. */
  public constructor(
    lineProvider: LineProvider,
    pointSpacingDp: Float = Defaults.POINT_SPACING,
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
    pointSpacingDp,
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

        val drawingStartAlignmentCorrection =
          layoutDirectionMultiplier * layerDimensions.startPadding

        val drawingStart =
          layerBounds.getStart(isLtr = isLtr) + drawingStartAlignmentCorrection - scroll

        val points = mutableListOf<PointF>()
        val visibleIndexRange =
          collectPointsAndVisibleIndexRange(
            series = series,
            drawingStart = drawingStart,
            pointInfoMap = pointInfoMap,
            drawFullLineLength = line.stroke is LineStroke.Dashed,
            points = points,
          )

        if (points.isNotEmpty() && !visibleIndexRange.isEmpty()) {
          connectPoints(line.interpolator, points, visibleIndexRange)
        }

        canvas.saveLayer(opacity = drawingModel?.opacity ?: 1f)

        line.fillColor?.let { color ->
          line.draw(context, linePath, color, verticalAxisPosition)
          forEachPointInBounds(series, drawingStart, pointInfoMap) { entry, x, y, _, _ ->
            updateMarkerTargets(entry, x, y, color)
          }
        }
          ?: run {
            val lineBitmap = getBitmap(cacheKeyNamespace, seriesIndex, "line")
            lineCanvas.setBitmap(lineBitmap)
            val lineFillBitmap = getBitmap(cacheKeyNamespace, seriesIndex, "lineFill")
            lineFillCanvas.setBitmap(lineFillBitmap)
            line.draw(context, linePath, lineCanvas, lineFillCanvas, verticalAxisPosition)
            lineCanvas.drawBitmap(lineFillBitmap, 0f, 0f, srcInPaint)
            canvas.drawBitmap(lineBitmap, 0f, 0f, null)
            forEachPointInBounds(series, drawingStart, pointInfoMap) { entry, x, y, _, _ ->
              updateMarkerTargets(entry, x, y, lineFillBitmap)
            }
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
    lineFillBitmap: Bitmap,
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
        lineFillBitmap[
          canvasX
            .roundToInt()
            .coerceIn(ceil(layerBounds.left).toInt(), layerBounds.right.toInt() - 1),
          limitedCanvasY.roundToInt(),
        ],
      )
  }

  protected open fun CartesianDrawingContext.updateMarkerTargets(
    entry: LineCartesianLayerModel.Entry,
    canvasX: Float,
    canvasY: Float,
    color: Int,
  ) {
    if (canvasX <= layerBounds.left - 1 || canvasX >= layerBounds.right + 1) return
    val limitedCanvasY = canvasY.coerceIn(layerBounds.top, layerBounds.bottom)
    _markerTargets
      .getOrPut(entry.x) { listOf(MutableLineCartesianLayerMarkerTarget(entry.x, canvasX)) }
      .first()
      .points += LineCartesianLayerMarkerTarget.Point(entry, limitedCanvasY, color)
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
          val distanceFromLine = max(line.stroke.thicknessDp, point?.sizeDp.orZero).half.pixels

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
      previousX != null && nextX != null -> min(abs(x - previousX), abs(nextX - x))
      previousX == null && nextX == null ->
        min(layerDimensions.startPadding, layerDimensions.endPadding).doubled

      nextX != null -> {
        ((entry.x - ranges.minX) / ranges.xStep * layerDimensions.xSpacing +
            layerDimensions.startPadding)
          .doubled
          .toFloat()
          .coerceAtMost(abs(nextX - x))
      }
      else -> {
        ((ranges.maxX - entry.x) / ranges.xStep * layerDimensions.xSpacing +
            layerDimensions.endPadding)
          .doubled
          .toFloat()
          .coerceAtMost(abs(x - previousX!!))
      }
    }.toInt()

  protected fun resetTempData() {
    _markerTargets.clear()
    linePath.rewind()
  }

  protected fun CartesianDrawingContext.collectPointsAndVisibleIndexRange(
    series: List<LineCartesianLayerModel.Entry>,
    drawingStart: Float,
    pointInfoMap: Map<Double, LineCartesianLayerDrawingModel.Entry>?,
    drawFullLineLength: Boolean = false,
    points: MutableList<PointF>,
  ): IntRange {
    val minX = ranges.minX
    val maxX = ranges.maxX
    val xStep = ranges.xStep

    val boundsStart = layerBounds.getStart(isLtr = isLtr)
    val boundsEnd = boundsStart + layoutDirectionMultiplier * layerBounds.width()

    fun getDrawX(entry: LineCartesianLayerModel.Entry): Float =
      drawingStart +
        layoutDirectionMultiplier * layerDimensions.xSpacing * ((entry.x - minX) / xStep).toFloat()

    fun getDrawY(entry: LineCartesianLayerModel.Entry): Float {
      val yRange = ranges.getYRange(verticalAxisPosition)
      return layerBounds.bottom -
        (pointInfoMap?.get(entry.x)?.y ?: ((entry.y - yRange.minY) / yRange.length).toFloat()) *
          layerBounds.height()
    }

    var visibleStart = -1
    var visibleEnd = -1

    series.forEachIn(minX = minX, maxX = maxX) { entry, _ ->
      points += PointF(getDrawX(entry), getDrawY(entry))
    }

    for (index in points.indices) {
      val px = points[index].x
      val nextPx = points.getOrNull(index + 1)?.x
      val inBounds =
        drawFullLineLength ||
          nextPx == null ||
          !(isLtr && px < boundsStart || !isLtr && px > boundsStart) ||
          !(isLtr && nextPx < boundsStart || !isLtr && nextPx > boundsStart)
      val pastEnd = isLtr && px > boundsEnd || !isLtr && px < boundsEnd
      if (inBounds && visibleStart == -1) visibleStart = index
      if (inBounds) visibleEnd = index
      if (pastEnd) {
        if (visibleEnd == -1) visibleEnd = index
        break
      }
    }

    return if (visibleStart == -1) IntRange.EMPTY else visibleStart..visibleEnd
  }

  protected fun CartesianDrawingContext.connectPoints(
    interpolator: Interpolator,
    points: List<PointF>,
    visibleIndexRange: IntRange,
  ) {
    interpolator.interpolate(this, linePath, points, visibleIndexRange)
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
    val boundsEnd = boundsStart + layoutDirectionMultiplier * layerBounds.width()

    fun getDrawX(entry: LineCartesianLayerModel.Entry): Float =
      drawingStart +
        layoutDirectionMultiplier * layerDimensions.xSpacing * ((entry.x - minX) / xStep).toFloat()

    fun getDrawY(entry: LineCartesianLayerModel.Entry): Float {
      val yRange = ranges.getYRange(verticalAxisPosition)
      return layerBounds.bottom -
        (pointInfoMap?.get(entry.x)?.y ?: ((entry.y - yRange.minY) / yRange.length).toFloat()) *
          layerBounds.height()
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
              ?.sizeDp
              .orZero
          }
          .pixels
      val xSpacing = maxPointSize + pointSpacingDp.pixels
      dimensions.ensureValuesAtLeast(
        xSpacing = xSpacing,
        scalableStartPadding = layerPadding.scalableStartDp.pixels,
        scalableEndPadding = layerPadding.scalableEndDp.pixels,
        unscalableStartPadding = maxPointSize.half + layerPadding.unscalableStartDp.pixels,
        unscalableEndPadding = maxPointSize.half + layerPadding.unscalableEndDp.pixels,
      )
    }
  }

  override fun updateChartRanges(
    chartRanges: MutableCartesianChartRanges,
    model: LineCartesianLayerModel,
  ) {
    var minY = model.minY
    var maxY = model.maxY
    model.series.forEachIndexed { seriesIndex, series ->
      val interpolator = lineProvider.getLine(seriesIndex, model.extraStore).interpolator
      val yRange = interpolator.getYRange(series.map { it.y })
      minY = kotlin.math.min(minY, yRange.start)
      maxY = kotlin.math.max(maxY, yRange.endInclusive)
    }
    chartRanges.tryUpdate(
      rangeProvider.getMinX(model.minX, model.maxX, model.extraStore),
      rangeProvider.getMaxX(model.minX, model.maxX, model.extraStore),
      rangeProvider.getMinY(minY, maxY, model.extraStore),
      rangeProvider.getMaxY(minY, maxY, model.extraStore),
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
              it.stroke.thicknessDp,
              it.pointProvider?.getLargestPoint(model.extraStore)?.sizeDp.orZero,
            )
          }
          .half
          .pixels
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
    pointSpacingDp: Float = this.pointSpacingDp,
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
      pointSpacingDp,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
      drawingModelKey,
    )

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LineCartesianLayer &&
        lineProvider == other.lineProvider &&
        pointSpacingDp == other.pointSpacingDp &&
        rangeProvider == other.rangeProvider &&
        verticalAxisPosition == other.verticalAxisPosition &&
        drawingModelInterpolator == other.drawingModelInterpolator

  override fun hashCode(): Int =
    Objects.hash(
      lineProvider,
      pointSpacingDp,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
    )

  /** Provides access to [Line] and [Point] factory functions. */
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
        layerBounds.height()
  return ceil(base).coerceIn(layerBounds.top..layerBounds.bottom) + ceil(halfLineThickness)
}
