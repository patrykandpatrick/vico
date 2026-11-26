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

package com.patrykandpatrick.vico.compose.cartesian.layer

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.ColorScale
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.*
import com.patrykandpatrick.vico.compose.cartesian.getVisibleXRange
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.Line
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.MutableLineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.component.Component
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.data.CacheStore
import com.patrykandpatrick.vico.compose.common.doubled
import com.patrykandpatrick.vico.compose.common.getRepeating
import com.patrykandpatrick.vico.compose.common.half
import com.patrykandpatrick.vico.compose.common.orZero
import kotlin.math.*

/**
 * Draws the content of line charts.
 *
 * @property model the data.
 * @property lineProvider provides the [Line]s.
 * @property pointSpacing the point spacing.
 * @property rangeProvider overrides the _x_ and _y_ ranges.
 * @property verticalAxisPosition the position of the [VerticalAxis] with which the
 *   [LineCartesianLayer] should be associated. Use this for independent [CartesianLayer] scaling.
 * @property drawingModelInterpolator interpolates the [LineCartesianLayerDrawingModel]s.
 * @property key identifies this [LineCartesianLayer] across data updates. See
 *   [AnimatedCartesianLayer.key].
 */
@Stable
public open class LineCartesianLayer(
  override val model: LineCartesianLayerModel,
  protected val lineProvider: LineProvider,
  protected val pointSpacing: Dp = Defaults.POINT_SPACING.dp,
  protected val rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
  protected val verticalAxisPosition: Axis.Position.Vertical? = null,
  override val drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<LineCartesianLayerDrawingModel> =
    CartesianLayerDrawingModelInterpolator.line(),
  override val key: Any,
) :
  BaseCartesianLayer<LineCartesianLayerModel>(),
  AnimatedCartesianLayer<LineCartesianLayerModel, LineCartesianLayerDrawingModel> {
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
    public val fill: LineFill,
    public val stroke: LineStroke = LineStroke.Continuous(),
    public val areaFill: AreaFill? = null,
    public val pointProvider: PointProvider? = null,
    public val interpolator: Interpolator = Interpolator.Sharp,
    public val dataLabel: TextComponent? = null,
    public val dataLabelPosition: Position.Vertical = Position.Vertical.Top,
    public val dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
    public val dataLabelRotationDegrees: Float = 0f,
  ) {
    protected val linePaint: Paint = Paint().apply { style = PaintingStyle.Stroke }

    /**
     * Compares the configuration. This applies to [Line] itself; instances of subclasses compare by
     * identity unless the subclass overrides `equals` and `hashCode`.
     */
    override fun equals(other: Any?): Boolean =
      this === other ||
        this::class == Line::class &&
          other is Line &&
          other::class == Line::class &&
          fill == other.fill &&
          stroke == other.stroke &&
          areaFill == other.areaFill &&
          pointProvider == other.pointProvider &&
          interpolator == other.interpolator &&
          dataLabel == other.dataLabel &&
          dataLabelPosition == other.dataLabelPosition &&
          dataLabelValueFormatter == other.dataLabelValueFormatter &&
          dataLabelRotationDegrees == other.dataLabelRotationDegrees

    override fun hashCode(): Int {
      if (this::class != Line::class) return super.hashCode()
      var result = fill.hashCode()
      result = 31 * result + stroke.hashCode()
      result = 31 * result + areaFill.hashCode()
      result = 31 * result + pointProvider.hashCode()
      result = 31 * result + interpolator.hashCode()
      result = 31 * result + dataLabel.hashCode()
      result = 31 * result + dataLabelPosition.hashCode()
      result = 31 * result + dataLabelValueFormatter.hashCode()
      result = 31 * result + dataLabelRotationDegrees.hashCode()
      return result
    }

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

    /** The [LineFill]’s solid [Color], or `null` if the [LineFill] has no single solid [Color]. */
    public val fillColor: Color?
      get() = (fill as? SingleLineFill)?.takeIf { it.fill.brush == null }?.fill?.color

    /** Draws the line. */
    public fun draw(
      context: CartesianDrawingContext,
      path: Path,
      color: Color,
      verticalAxisPosition: Axis.Position.Vertical?,
    ) {
      with(context) {
        stroke.apply(this, linePaint)
        val halfThickness = stroke.thickness.pixels.half
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
       * Uses a color scale. [colors] maps _y_-values to [Color]s, between which the color is
       * interpolated.
       */
      public fun colorScale(
        colors: Map<out Number, Color>,
        verticalAxisPosition: Axis.Position.Vertical? = null,
      ): LineFill = ColorScaleLineFill(ColorScale(colors, verticalAxisPosition))

      /**
       * Uses [topFill] for the portions of the line that are above the [splitY] line, and
       * analogously for [bottomFill]. (The [splitY] line is an imaginary horizontal line at the
       * _y_-value [splitY].)
       */
      public fun double(topFill: Fill, bottomFill: Fill, splitY: Number = 0): LineFill =
        DoubleLineFill(topFill, bottomFill, splitY)
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
       * (The [splitY] line is an imaginary horizontal line at the _y_-value [splitY].)
       */
      public fun single(fill: Fill, splitY: Number = 0): AreaFill = SingleAreaFill(fill, splitY)

      /**
       * Uses a color scale for the areas bounded by the [LineCartesianLayer] line and zero.
       * [colors] maps _y_-values to [Color]s, between which the color is interpolated.
       */
      public fun colorScale(
        colors: Map<out Number, Color>,
        verticalAxisPosition: Axis.Position.Vertical? = null,
      ): AreaFill = ColorScaleAreaFill(ColorScale(colors, verticalAxisPosition))

      /**
       * Uses [topFill] for those areas bounded by the [LineCartesianLayer] line and the [splitY]
       * line that are above the [splitY] line, and analogously for [bottomFill]. (The [splitY] line
       * is an imaginary horizontal line at the _y_-value [splitY].)
       */
      public fun double(topFill: Fill, bottomFill: Fill, splitY: Number = 0): AreaFill =
        DoubleAreaFill(topFill, bottomFill, splitY)
    }
  }

  /** Interpolates between a [LineCartesianLayer] line’s points, defining its shape. */
  public interface Interpolator {
    /**
     * Returns the number of extra points required on either side of the visible range, beyond the
     * standard single extra point already included in `visibleIndexRange`. `null` indicates that
     * the full series should be provided.
     */
    public val visiblePadding: Int?
      get() = 0

    /**
     * Draws [path] through [points].
     *
     * [points] contains the visible _x_ range slice, plus horizontal padding as requested by
     * [visiblePadding]. If [visiblePadding] is `null`, [points] contains the full series.
     *
     * Only the points in [visibleIndexRange] need to produce path operations, but the remaining
     * points are available for interpolation context.
     */
    public fun interpolate(
      context: CartesianDrawingContext,
      path: Path,
      points: List<Offset>,
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
            points: List<Offset>,
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
        @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) curvature: Float = 0.5f,
      ): Interpolator = CubicInterpolator(curvature)

      /**
       * Uses a Catmull–Rom spline. [alpha], which must be in [[0, 1)], controls the tightness: 0
       * (the default) produces the standard Catmull–Rom spline, and values approaching 1 produce
       * near-straight lines. Catmull–Rom splines pass through all data points and produce straight
       * segments for collinear points.
       */
      public fun catmullRom(
        @FloatRange(from = 0.0, to = 1.0, toInclusive = false) alpha: Float = 0f,
      ): Interpolator = CatmullRomInterpolator(alpha)
    }
  }

  /**
   * Provides [Line]s to [LineCartesianLayer]s. Implementations must be pure functions of their
   * arguments and of the values they capture, as they may be called off the main thread; to change
   * the style, pass a new [LineProvider].
   *
   * In a composable function, a lambda converted to [LineProvider] is memoized while the values it
   * captures are unchanged (compared via `equals` for stable types and by identity otherwise), so
   * it needs no explicit `remember`.
   */
  public fun interface LineProvider {
    /** Returns the [Line] for the specified series. */
    public fun getLine(seriesKey: Any, seriesIndex: Int): Line

    /** Houses [LineProvider] factory functions. */
    public companion object {
      private data class Series(private val lines: List<Line>) : LineProvider {
        override fun getLine(seriesKey: Any, seriesIndex: Int) = lines.getRepeating(seriesIndex)
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

  /**
   * Provides [Point]s to [LineCartesianLayer]s. Implementations must be pure functions of their
   * arguments and of the values they capture; to change the style, pass a new [PointProvider]. The
   * [LineCartesianLayer] determines the largest [Point] by calling [getPoint] for every entry once
   * per [LineCartesianLayerModel], possibly off the main thread.
   *
   * In a composable function, a lambda converted to [PointProvider] is memoized while the values it
   * captures are unchanged (compared via `equals` for stable types and by identity otherwise), so
   * it needs no explicit `remember`.
   */
  @Immutable
  public fun interface PointProvider {
    /** Returns the [Point] for [entry], or `null` for no point. */
    public fun getPoint(entry: LineCartesianLayerModel.Entry): Point?

    /** Houses a [PointProvider] factory function. */
    public companion object {
      private data class Single(private val point: Point) : PointProvider {
        override fun getPoint(entry: LineCartesianLayerModel.Entry) = point
      }

      /** Uses [point] for each point. */
      public fun single(point: Point): PointProvider = Single(point)
    }
  }

  private val _markerTargets = mutableMapOf<Double, List<MutableLineCartesianLayerMarkerTarget>>()

  protected val linePath: Path = Path()

  private val srcInPaint = Paint().apply { blendMode = BlendMode.SrcIn }

  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

  private val seriesIndices: Map<Any, Int> by lazy {
    model.seriesKeys.withIndex().associate { (index, key) -> key to index }
  }

  private val lines: List<Line> by lazy {
    model.seriesKeys.mapIndexed { index, key -> lineProvider.getLine(key, index) }
  }

  /** The size of the largest point, computed once per [LineCartesianLayer]. */
  private val maxPointSize: Dp by lazy {
    var maxSize = 0.dp
    model.series.forEachIndexed { index, series ->
      val pointProvider = lines[index].pointProvider ?: return@forEachIndexed
      series.forEach { entry ->
        val size = pointProvider.getPoint(entry)?.size ?: return@forEach
        if (size > maxSize) maxSize = size
      }
    }
    maxSize
  }

  override fun drawInternal(context: CartesianDrawingContext) {
    with(context) {
      resetTempData()

      val drawingModel = getDrawingModel(this@LineCartesianLayer)
      val sweepFraction = drawingModel.sweepFraction
      // Evaluate this once to work around an Android 10 ART JIT bug that can make repeated float
      // comparisons disagree, unbalancing the save and restore calls. See b/553511933.
      val isSweepInProgress = sweepFraction < 1f
      if (isSweepInProgress) {
        val sweepRight =
          if (isLtr) {
            layerBounds.left + layerBounds.width * sweepFraction
          } else {
            layerBounds.right - layerBounds.width * sweepFraction
          }
        canvas.save()
        if (isLtr) {
          canvas.clipRect(layerBounds.copy(right = sweepRight))
        } else {
          canvas.clipRect(layerBounds.copy(left = sweepRight))
        }
      }

      // Composite all series onto a single layer. During the fade-in (`opacity` < 1), this
      // allocates one offscreen buffer per frame rather than one per series. With a shared layer,
      // overlapping series also composite opaquely before the group is faded, which avoids
      // inter-series bleed-through.
      saveLayer(opacity = drawingModel.opacity)

      val drawingModelSeriesIndices =
        drawingModel.seriesKeys.withIndex().associate { (index, key) -> key to index }
      mergeKeys(model.seriesKeys, drawingModel.seriesKeys).forEach { seriesKey ->
        val pointInfoMap = drawingModelSeriesIndices[seriesKey]?.let(drawingModel::get)
        val modelSeriesIndex = seriesIndices[seriesKey]
        val isExiting = modelSeriesIndex == null
        val series =
          if (modelSeriesIndex != null) {
            model.series[modelSeriesIndex]
          } else {
            pointInfoMap!!.values.map { it.entry }
          }
        val seriesIndex = modelSeriesIndex ?: series.firstOrNull()?.seriesIndex ?: return@forEach

        linePath.rewind()
        val line =
          if (modelSeriesIndex != null) {
            lines[modelSeriesIndex]
          } else {
            lineProvider.getLine(seriesKey, seriesIndex)
          }

        val drawingStartAlignmentCorrection =
          layoutDirectionMultiplier * layerDimensions.startPadding

        val drawingStart =
          layerBounds.getStart(isLtr = isLtr) + drawingStartAlignmentCorrection - scroll

        val points = mutableListOf<Offset>()
        val visibleIndexRange =
          collectPointsAndVisibleIndexRange(
            interpolator = line.interpolator,
            series = series,
            drawingStart = drawingStart,
            pointInfoMap = pointInfoMap,
            drawFullLineLength = line.stroke is LineStroke.Dashed,
            points = points,
          )

        if (points.isNotEmpty() && !visibleIndexRange.isEmpty()) {
          connectPoints(line.interpolator, points, visibleIndexRange)
        }

        line.fillColor?.let { color ->
          line.draw(context, linePath, color, verticalAxisPosition)
          if (!isExiting) {
            forEachPointInBounds(series, drawingStart, pointInfoMap) { entry, x, y, _, _ ->
              updateMarkerTargets(entry, seriesKey, x, y, color)
            }
          }
        }
          ?: run {
            val (lineBitmap, lineCanvas) = getBitmap(cacheKeyNamespace, key, seriesKey, "line")
            val (lineFillBitmap, lineFillCanvas) =
              getBitmap(cacheKeyNamespace, key, seriesKey, "lineFill")
            line.draw(context, linePath, lineCanvas, lineFillCanvas, verticalAxisPosition)
            lineCanvas.drawImage(lineFillBitmap, Offset.Zero, srcInPaint)
            canvas.drawImage(lineBitmap, Offset.Zero, EmptyPaint)
            if (!isExiting) {
              forEachPointInBounds(series, drawingStart, pointInfoMap) { entry, x, y, _, _ ->
                updateMarkerTargets(entry, seriesKey, x, y, lineFillBitmap)
              }
            }
          }

        drawPointsAndDataLabels(line, series, seriesKey, seriesIndex, drawingStart, pointInfoMap)
      }

      canvas.restore()
      if (isSweepInProgress) canvas.restore()
    }
  }

  protected open fun CartesianDrawingContext.updateMarkerTargets(
    entry: LineCartesianLayerModel.Entry,
    seriesKey: Any,
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

  protected open fun CartesianDrawingContext.updateMarkerTargets(
    entry: LineCartesianLayerModel.Entry,
    seriesKey: Any,
    canvasX: Float,
    canvasY: Float,
    color: Color,
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
    seriesKey: Any,
    seriesIndex: Int,
    drawingStart: Float,
    pointInfoMap: Map<Double, LineCartesianLayerDrawingModel.Entry>?,
  ) {
    forEachPointInBounds(
      series = series,
      drawingStart = drawingStart,
      pointInfoMap = pointInfoMap,
    ) { chartEntry, x, y, previousX, nextX ->
      val point = line.pointProvider?.getPoint(chartEntry)
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
    interpolator: Interpolator,
    series: List<LineCartesianLayerModel.Entry>,
    drawingStart: Float,
    pointInfoMap: Map<Double, LineCartesianLayerDrawingModel.Entry>?,
    drawFullLineLength: Boolean = false,
    points: MutableList<Offset>,
  ): IntRange {
    val minX = ranges.minX
    val xStep = ranges.xStep
    val visibleXRange = getVisibleXRange()
    val visiblePointPadding = interpolator.visiblePadding?.plus(1)

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

    var visibleStart = -1
    var visibleEnd = -1

    val visibleIndices =
      series.getSliceIndices(visibleXRange.start, visibleXRange.endInclusive, visiblePointPadding)

    if (visibleIndices.isEmpty()) return IntRange.EMPTY

    series.subList(visibleIndices.first, visibleIndices.last + 1).forEach { entry ->
      points += Offset(getDrawX(entry), getDrawY(entry))
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
    points: List<Offset>,
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
        entry: LineCartesianLayerModel.Entry,
        x: Float,
        y: Float,
        previousX: Float?,
        nextX: Float?,
      ) -> Unit,
  ) {
    val minX = ranges.minX
    val xStep = ranges.xStep
    val visibleXRange = getVisibleXRange()

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

    val visibleIndices = series.getSliceIndices(visibleXRange.start, visibleXRange.endInclusive)

    if (visibleIndices.isEmpty()) return

    val actionEndIndex = visibleIndices.last

    val actionStartIndex = visibleIndices.first

    if (actionEndIndex < actionStartIndex) return

    for (index in actionStartIndex..actionEndIndex) {
      val entry = series[index]
      val next = series.getOrNull(index + 1)
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
        continue
      }
      action(entry, immutableX, getDrawY(entry), previousX, nextX)
      if (isLtr && immutableX > boundsEnd || isLtr.not() && immutableX < boundsEnd) return
    }
  }

  override fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
  ) {
    with(context) {
      val maxPointSize = maxPointSize.pixels
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

  override fun updateChartRanges(chartRanges: MutableCartesianChartRanges) {
    // Initialize the per-model caches here, which runs off the main thread for asynchronous
    // updates.
    maxPointSize
    var minY = model.minY
    var maxY = model.maxY
    model.series.forEachIndexed { seriesIndex, series ->
      val interpolator = lines[seriesIndex].interpolator
      if (series.isEmpty()) return@forEachIndexed
      val yRange = interpolator.getYRange(series.map { it.y })
      minY = min(minY, yRange.start)
      maxY = max(maxY, yRange.endInclusive)
    }
    chartRanges.tryUpdate(
      rangeProvider,
      model,
      model.minX,
      model.maxX,
      minY,
      maxY,
      verticalAxisPosition,
    )
  }

  override fun updateLayerMargins(
    context: CartesianMeasuringContext,
    layerMargins: CartesianLayerMargins,
    layerDimensions: CartesianLayerDimensions,
  ) {
    with(context) {
      val verticalMargin =
        max(lines.maxOfOrNull { it.stroke.thickness.pixels } ?: 0f, maxPointSize.pixels).half
      layerMargins.ensureValuesAtLeast(top = verticalMargin, bottom = verticalMargin)
    }
  }

  /**
   * Compares the configuration and data (not the drawing caches), so that an equal
   * [LineCartesianLayer] doesn’t cause the chart to be recreated. This applies to
   * [LineCartesianLayer] itself; instances of subclasses compare by identity unless the subclass
   * overrides `equals` and `hashCode` (covering the settings it adds).
   */
  override fun equals(other: Any?): Boolean =
    this === other ||
      this::class == LineCartesianLayer::class &&
        other is LineCartesianLayer &&
        other::class == LineCartesianLayer::class &&
        model == other.model &&
        lineProvider == other.lineProvider &&
        pointSpacing == other.pointSpacing &&
        rangeProvider == other.rangeProvider &&
        verticalAxisPosition == other.verticalAxisPosition &&
        drawingModelInterpolator == other.drawingModelInterpolator &&
        key == other.key

  override fun hashCode(): Int {
    if (this::class != LineCartesianLayer::class) return super.hashCode()
    var result = model.hashCode()
    result = 31 * result + lineProvider.hashCode()
    result = 31 * result + pointSpacing.hashCode()
    result = 31 * result + rangeProvider.hashCode()
    result = 31 * result + verticalAxisPosition.hashCode()
    result = 31 * result + drawingModelInterpolator.hashCode()
    result = 31 * result + key.hashCode()
    return result
  }

  override fun createDrawingModel(ranges: CartesianChartRanges): LineCartesianLayerDrawingModel {
    val yRange = ranges.getYRange(verticalAxisPosition)
    return LineCartesianLayerDrawingModel(
      model.series.map { series ->
        series.associate { entry ->
          entry.x to
            LineCartesianLayerDrawingModel.Entry(
              ((entry.y - yRange.minY) / yRange.length).toFloat(),
              entry,
            )
        }
      },
      model.seriesKeys,
    )
  }

  /** Provides access to [Line] factory functions. */
  public companion object {
    private val cacheKeyNamespace = CacheStore.KeyNamespace()
  }
}

internal fun CartesianDrawingContext.getCanvasSplitY(
  splitY: Number,
  halfLineThickness: Float,
  verticalAxisPosition: Axis.Position.Vertical?,
): Float {
  val yRange = ranges.getYRange(verticalAxisPosition)
  val base =
    layerBounds.bottom -
      ((splitY.toDouble() - yRange.minY) / yRange.length).toFloat() * layerBounds.height
  return ceil(base).coerceIn(layerBounds.top..layerBounds.bottom) + ceil(halfLineThickness)
}

/**
 * Creates and remembers a [LineCartesianLayer] that displays [model]. A new [LineCartesianLayer] is
 * created whenever an argument changes. Its [AnimatedCartesianLayer.key] is tied to the composition
 * slot, so data updates are animated.
 */
@Composable
public fun rememberLineCartesianLayer(
  model: LineCartesianLayerModel,
  lineProvider: LineCartesianLayer.LineProvider =
    LineCartesianLayer.LineProvider.series(
      vicoTheme.lineCartesianLayerColors.map { color ->
        LineCartesianLayer.rememberLine(LineCartesianLayer.LineFill.single(Fill(color)))
      },
    ),
  pointSpacing: Dp = Defaults.POINT_SPACING.dp,
  rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
  verticalAxisPosition: Axis.Position.Vertical? = null,
  drawingModelInterpolator: CartesianLayerDrawingModelInterpolator<LineCartesianLayerDrawingModel> =
    CartesianLayerDrawingModelInterpolator.line(),
): LineCartesianLayer {
  val key = rememberCartesianLayerKey()
  return remember(
    model,
    lineProvider,
    pointSpacing,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
  ) {
    LineCartesianLayer(
      model,
      lineProvider,
      pointSpacing,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
      key,
    )
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
  interpolator: LineCartesianLayer.Interpolator = LineCartesianLayer.Interpolator.Sharp,
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
    interpolator,
    dataLabel,
    dataLabelPosition,
    dataLabelValueFormatter,
    dataLabelRotationDegrees,
  ) {
    Line(
      fill,
      stroke,
      areaFill,
      pointProvider,
      interpolator,
      dataLabel,
      dataLabelPosition,
      dataLabelValueFormatter,
      dataLabelRotationDegrees,
    )
  }
