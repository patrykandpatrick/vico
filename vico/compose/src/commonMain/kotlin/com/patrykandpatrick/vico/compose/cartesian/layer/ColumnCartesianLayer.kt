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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.*
import com.patrykandpatrick.vico.compose.cartesian.getVisibleXRange
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.MutableColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.doubled
import com.patrykandpatrick.vico.compose.common.getRepeating
import com.patrykandpatrick.vico.compose.common.half
import kotlin.math.abs
import kotlin.math.min

/**
 * Displays data as vertical bars. The columns with matching _x_-values form a group. The series are
 * assigned to stacks by [stackKeyProvider]. The columns in each stack are stacked, and the stacks
 * of each group are placed side by side. Data labels show the stack totals, and markers get one
 * target per stack.
 *
 * @property model the data.
 * @property columnProvider provides the column [LineComponent]s.
 * @property groupSpacing the spacing between neighboring groups.
 * @property stackKeyProvider assigns the series to stacks.
 * @property stackSpacing the spacing between neighboring stacks in a group.
 * @property dataLabel the [TextComponent] for the data labels. Use `null` for no data labels.
 * @property dataLabelPosition the vertical position of positive-column data labels relative to the
 *   top edges of the columns. For negative columns, this is inverted and interpreted relative to
 *   the bottom edges of the columns.
 * @property dataLabelValueFormatter the [CartesianValueFormatter] for the data labels.
 * @property dataLabelRotationDegrees the rotation of the data labels (in degrees).
 * @property rangeProvider defines the _x_ and _y_ ranges.
 * @property verticalAxisPosition the position of the [VerticalAxis] with which the
 *   [ColumnCartesianLayer] should be associated. Use this for independent [CartesianLayer] scaling.
 * @property drawingModelInterpolator interpolates the [ColumnCartesianLayer]’s
 *   [ColumnCartesianLayerDrawingModel]s.
 * @property key identifies this [ColumnCartesianLayer] across data updates. See
 *   [AnimatedCartesianLayer.key].
 */
@Stable
public open class ColumnCartesianLayer(
  override val model: ColumnCartesianLayerModel,
  protected val columnProvider: ColumnProvider,
  protected val groupSpacing: Dp = Defaults.GROUP_SPACING.dp,
  protected val stackKeyProvider: StackKeyProvider = StackKeyProvider.Grouped,
  protected val stackSpacing: Dp = Defaults.STACK_SPACING.dp,
  protected val dataLabel: TextComponent? = null,
  protected val dataLabelPosition: Position.Vertical = Position.Vertical.Top,
  protected val dataLabelValueFormatter: CartesianValueFormatter =
    CartesianValueFormatter.decimal(),
  protected val dataLabelRotationDegrees: Float = 0f,
  protected val rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
  protected val verticalAxisPosition: Axis.Position.Vertical? = null,
  override val drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<ColumnCartesianLayerDrawingModel> =
    CartesianLayerDrawingModelInterpolator.column(),
  override val key: Any,
) :
  BaseCartesianLayer<ColumnCartesianLayerModel>(),
  DerivationInputs,
  AnimatedCartesianLayer<ColumnCartesianLayerModel, ColumnCartesianLayerDrawingModel> {
  private val _markerTargets =
    mutableMapOf<Double, MutableList<MutableColumnCartesianLayerMarkerTarget>>()

  private val stackMarkerTargets = mutableMapOf<Double, MutableColumnCartesianLayerMarkerTarget>()

  private var markerTargetStackIndex = -1

  private var stackGeometry: ColumnStackGeometry? = null

  protected val stackInfo: MutableMap<Double, StackInfo> = mutableMapOf()

  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

  /** The widest column per series, computed once per [ColumnCartesianLayer]. */
  private val seriesThicknesses: List<Dp> by lazy { model.series.map(::getMaxThickness) }

  /** The stack layout of [model]’s series, computed once per [ColumnCartesianLayer]. */
  private val modelStackLayout: ColumnStackLayout by lazy {
    stackKeyProvider.getLayout(model.seriesKeys)
  }

  private val modelSeriesIndices: Map<Any, Int> by lazy {
    model.seriesKeys.withIndex().associate { (index, key) -> key to index }
  }

  /** The [DrawnSeries] for drawing models without exiting series, computed once. */
  private val modelDrawnSeries: DrawnSeries by lazy {
    DrawnSeries(
      model.seriesKeys,
      model.series,
      List(model.series.size) { columnProvider },
      BooleanArray(model.series.size),
      seriesThicknesses,
      modelStackLayout,
    )
  }

  private fun getMaxThickness(series: List<ColumnCartesianLayerModel.Entry>): Dp {
    var thickness = 0.dp
    series.forEach { entry ->
      val entryThickness = columnProvider.getColumn(entry).thickness
      if (entryThickness > thickness) thickness = entryThickness
    }
    return thickness
  }

  override fun drawInternal(context: CartesianDrawingContext) {
    with(context) {
      _markerTargets.clear()
      stackMarkerTargets.clear()
      markerTargetStackIndex = -1
      drawChartInternal(getDrawingModel(this@ColumnCartesianLayer))
      stackInfo.clear()
    }
  }

  protected open fun CartesianDrawingContext.drawChartInternal(
    drawingModel: ColumnCartesianLayerDrawingModel,
  ) {
    val yRange = ranges.getYRange(verticalAxisPosition)
    val heightMultiplier = layerBounds.height / yRange.length.toFloat()

    var drawingStart = 0f
    var height: Float
    var columnCenterX: Float
    var columnTop: Float
    var columnBottom: Float
    val zeroLinePosition =
      layerBounds.bottom + (yRange.minY / yRange.length).toFloat() * layerBounds.height
    val visibleXRange = getVisibleXRange()
    val drawnSeries = getDrawnSeries(drawingModel)
    val columnInfoMaps: List<Map<Double, ColumnCartesianLayerDrawingModel.Entry>?> =
      if (drawingModel.seriesKeys == drawnSeries.keys) {
        drawingModel
      } else {
        val indices = drawingModel.seriesKeys.withIndex().associate { (index, key) -> key to index }
        drawnSeries.keys.map { key -> indices[key]?.let(drawingModel::get) }
      }
    val geometry = getStackGeometry(drawnSeries)
    val layout = geometry.layout

    saveLayer(opacity = drawingModel.opacity)

    for (stackIndex in 0..<layout.stackCount) {
      val columnThickness = geometry.thicknesses[stackIndex]
      val halfColumnThickness = columnThickness.pixels.half

      layout.forEachSeriesIndex(stackIndex) { index ->
        val entryCollection = drawnSeries.series[index]
        val columnInfoMap = columnInfoMaps[index]
        val isExiting = drawnSeries.isExiting[index]
        drawingStart = getDrawingStart(index, geometry) - scroll

        val visibleIndices =
          entryCollection.getSliceIndices(visibleXRange.start, visibleXRange.endInclusive)
        if (visibleIndices.isEmpty()) return@forEachSeriesIndex

        entryCollection.subList(visibleIndices.first, visibleIndices.last + 1).forEach { entry ->
          val columnInfo = columnInfoMap?.get(entry.x)
          height =
            (columnInfo?.height ?: (abs(entry.y) / yRange.length)).toFloat() * layerBounds.height
          val xSpacingMultiplier = ((entry.x - ranges.minX) / ranges.xStep).toFloat()
          val column = drawnSeries.columnProviders[index].getColumn(entry)
          columnCenterX =
            drawingStart +
              (layerDimensions.xSpacing * xSpacingMultiplier + halfColumnThickness * zoom) *
                layoutDirectionMultiplier

          val stackInfo = stackInfo.getOrPut(entry.x) { StackInfo() }
          columnBottom =
            if (entry.y >= 0) {
              zeroLinePosition - stackInfo.topHeight
            } else {
              zeroLinePosition + stackInfo.bottomHeight + height
            }
          columnTop = (columnBottom - height).coerceAtMost(columnBottom)
          stackInfo.update(entry.y, height, isExiting)

          val columnSignificantY = if (entry.y < 0f) columnBottom else columnTop

          if (!isExiting) {
            updateMarkerTargets(
              entry = entry,
              stackIndex = stackIndex,
              canvasX = columnCenterX,
              canvasY = columnSignificantY,
              columnHeight = columnBottom - columnTop,
              column = column,
            )
          }

          column.drawVertical(this, columnCenterX, columnTop, columnBottom, zoom)
        }
      }

      // The totals are drawn after the stack’s columns so that the columns can’t paint over them.
      stackInfo.forEach { (x, stackInfo) ->
        if (stackInfo.isExiting) return@forEach
        drawStackedDataLabel(
          stackCount = layout.stackCount,
          columnThickness = columnThickness,
          stackInfo = stackInfo,
          x =
            drawingStart +
              (layerDimensions.xSpacing * ((x - ranges.minX) / ranges.xStep).toFloat() +
                halfColumnThickness * zoom) * layoutDirectionMultiplier,
          zeroLinePosition = zeroLinePosition,
          heightMultiplier = heightMultiplier,
          isFirst = stackIndex == 0 && x == ranges.minX,
          isLast = stackIndex == layout.stackCount - 1 && x == ranges.maxX,
        )
      }
      stackInfo.clear()
    }

    canvas.restore()
  }

  protected open fun CartesianDrawingContext.drawStackedDataLabel(
    stackCount: Int,
    columnThickness: Dp,
    stackInfo: StackInfo,
    x: Float,
    zeroLinePosition: Float,
    heightMultiplier: Float,
    isFirst: Boolean,
    isLast: Boolean,
  ) {
    if (stackInfo.topY > 0f || stackInfo.bottomY == 0.0) {
      drawDataLabel(
        stackCount = stackCount,
        columnThickness = columnThickness,
        dataLabelValue = stackInfo.topY,
        x = x,
        y = zeroLinePosition - stackInfo.topHeight,
        isFirst = isFirst,
        isLast = isLast,
      )
    }
    if (stackInfo.bottomY < 0f) {
      drawDataLabel(
        stackCount = stackCount,
        columnThickness = columnThickness,
        dataLabelValue = stackInfo.bottomY,
        x = x,
        y = zeroLinePosition + stackInfo.bottomHeight,
        isFirst = isFirst,
        isLast = isLast,
      )
    }
  }

  protected open fun CartesianDrawingContext.drawDataLabel(
    stackCount: Int,
    columnThickness: Dp,
    dataLabelValue: Double,
    x: Float,
    y: Float,
    isFirst: Boolean,
    isLast: Boolean,
  ) {
    dataLabel?.let { textComponent ->
      var maxWidth =
        if (stackCount == 1) {
          layerDimensions.xSpacing
        } else {
          (columnThickness.pixels + min(groupSpacing.pixels, stackSpacing.pixels).half) * zoom
        }
      if (isFirst) maxWidth = maxWidth.coerceAtMost(layerDimensions.startPadding.doubled)
      if (isLast) maxWidth = maxWidth.coerceAtMost(layerDimensions.endPadding.doubled)
      val text = dataLabelValueFormatter.format(this, dataLabelValue, verticalAxisPosition)
      val dataLabelWidth =
        textComponent
          .getWidth(context = this, text = text, rotationDegrees = dataLabelRotationDegrees)
          .coerceAtMost(maximumValue = maxWidth)

      if (
        x - dataLabelWidth.half > layerBounds.right || x + dataLabelWidth.half < layerBounds.left
      ) {
        return
      }

      val labelVerticalPosition = if (dataLabelValue < 0f) -dataLabelPosition else dataLabelPosition

      val verticalPosition =
        labelVerticalPosition.inBounds(
          bounds = layerBounds,
          componentHeight =
            textComponent.getHeight(
              context = this,
              text = text,
              maxWidth = maxWidth.toInt(),
              rotationDegrees = dataLabelRotationDegrees,
            ),
          referenceY = y,
        )
      textComponent.draw(
        context = this,
        text = text,
        x = x,
        y = y,
        verticalPosition = verticalPosition,
        maxWidth = maxWidth.toInt(),
        rotationDegrees = dataLabelRotationDegrees,
      )
    }
  }

  protected open fun CartesianDrawingContext.updateMarkerTargets(
    entry: ColumnCartesianLayerModel.Entry,
    stackIndex: Int,
    canvasX: Float,
    canvasY: Float,
    columnHeight: Float,
    column: LineComponent,
  ) {
    if (stackIndex != markerTargetStackIndex) {
      stackMarkerTargets.clear()
      markerTargetStackIndex = stackIndex
    }
    if (canvasX <= layerBounds.left - 1 || canvasX >= layerBounds.right + 1) return
    val limitedCanvasY = canvasY.coerceIn(layerBounds.top, layerBounds.bottom)
    val markerColor =
      column.effectiveStrokeFill.extractColor(
        context = this,
        width = column.thickness.pixels,
        height = columnHeight,
        side = if (entry.y < 0) -1 else 1,
      )
    val targetColumn = ColumnCartesianLayerMarkerTarget.Column(entry, limitedCanvasY, markerColor)
    val target = stackMarkerTargets[entry.x]
    if (target != null) {
      target.columns += targetColumn
    } else {
      val newTarget =
        MutableColumnCartesianLayerMarkerTarget(entry.x, canvasX, mutableListOf(targetColumn))
      stackMarkerTargets[entry.x] = newTarget
      _markerTargets.getOrPut(entry.x) { mutableListOf() } += newTarget
    }
  }

  override fun updateChartRanges(chartRanges: MutableCartesianChartRanges) {
    // Initialize the per-model cache here, which runs off the main thread for asynchronous updates.
    seriesThicknesses
    val yRange = if (model.isEmpty) 0.0..0.0 else model.getYRange(modelStackLayout)
    chartRanges.tryUpdate(
      rangeProvider,
      model,
      model.minX,
      model.maxX,
      yRange.start,
      yRange.endInclusive,
      verticalAxisPosition,
    )
  }

  override fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
  ) {
    with(context) {
      val groupWidth =
        getStackGeometry(getDrawnSeries(getDrawingModel(this@ColumnCartesianLayer))).width
      val xSpacing = groupWidth + groupSpacing.pixels
      dimensions.ensureValuesAtLeast(
        xSpacing = xSpacing,
        scalableStartPadding = groupWidth.half + layerPadding.scalableStart.pixels,
        scalableEndPadding = groupWidth.half + layerPadding.scalableEnd.pixels,
        unscalableStartPadding = layerPadding.unscalableStart.pixels,
        unscalableEndPadding = layerPadding.unscalableEnd.pixels,
      )
    }
  }

  private fun CartesianDrawingContext.getDrawingStart(
    seriesIndex: Int,
    geometry: ColumnStackGeometry,
  ): Float =
    layerBounds.getStart(isLtr) +
      (layerDimensions.startPadding +
        (geometry.offsets[geometry.layout.stackIndices[seriesIndex]] - geometry.width.half) *
          zoom) * layoutDirectionMultiplier

  /**
   * Lists the series to draw: those in [model] and, while they’re exiting, those that are only in
   * [drawingModel], in the drawing model’s order.
   */
  private fun getDrawnSeries(drawingModel: ColumnCartesianLayerDrawingModel): DrawnSeries {
    if (drawingModel.seriesKeys == model.seriesKeys) return modelDrawnSeries
    val drawingModelSeriesIndices =
      drawingModel.seriesKeys.withIndex().associate { (index, key) -> key to index }
    val keys = mutableListOf<Any>()
    val series = mutableListOf<List<ColumnCartesianLayerModel.Entry>>()
    val columnProviders = mutableListOf<ColumnProvider>()
    val isExiting = mutableListOf<Boolean>()
    val thicknesses = mutableListOf<Dp>()
    val stackKeys = mutableListOf<Any>()
    mergeKeys(model.seriesKeys, drawingModel.seriesKeys).forEach { key ->
      val modelIndex = modelSeriesIndices[key]
      if (modelIndex != null) {
        keys += key
        series += model.series[modelIndex]
        columnProviders += columnProvider
        isExiting += false
        thicknesses += seriesThicknesses[modelIndex]
        stackKeys += CurrentStack(modelStackLayout.stackIndices[modelIndex])
        return@forEach
      }
      val source =
        drawingModelSeriesIndices[key]?.let(drawingModel::getSeriesSource) as? ColumnCartesianLayer
          ?: return@forEach
      val sourceIndex = source.modelSeriesIndices[key] ?: return@forEach
      keys += key
      series += source.model.series[sourceIndex]
      columnProviders += source.columnProvider
      isExiting += true
      thicknesses += source.seriesThicknesses[sourceIndex]
      stackKeys += getExitingStackKey(source, sourceIndex)
    }
    return DrawnSeries(
      keys,
      series,
      columnProviders,
      isExiting.toBooleanArray(),
      thicknesses,
      getColumnStackLayout(stackKeys),
    )
  }

  /**
   * Returns the stack key for the exiting series at [sourceIndex] in [source]. It joins the stack
   * of the first series that shared its stack in [source] and is still in [model]; otherwise, it
   * keeps a stack of its own (shared with any other exiting series from that stack). Only
   * [source]’s stack key provider is asked about it.
   */
  private fun getExitingStackKey(source: ColumnCartesianLayer, sourceIndex: Int): Any {
    val sourceLayout = source.modelStackLayout
    val sourceStackIndex = sourceLayout.stackIndices[sourceIndex]
    sourceLayout.forEachSeriesIndex(sourceStackIndex) { mateIndex ->
      val mateModelIndex = modelSeriesIndices[source.model.seriesKeys[mateIndex]]
      if (mateModelIndex != null) return CurrentStack(modelStackLayout.stackIndices[mateModelIndex])
    }
    return ExitingStack(source, sourceStackIndex)
  }

  private data class CurrentStack(val index: Int)

  private data class ExitingStack(val source: ColumnCartesianLayer, val index: Int)

  private fun MeasuringContext.getStackGeometry(drawnSeries: DrawnSeries): ColumnStackGeometry {
    val seriesKeys = drawnSeries.keys
    stackGeometry?.let { geometry ->
      if (geometry.seriesKeys == seriesKeys && geometry.density == density.density) {
        return geometry
      }
    }
    val layout = drawnSeries.layout
    val thicknesses = Array(layout.stackCount) { 0.dp }
    drawnSeries.thicknesses.forEachIndexed { seriesIndex, thickness ->
      val stackIndex = layout.stackIndices[seriesIndex]
      if (thickness > thicknesses[stackIndex]) thicknesses[stackIndex] = thickness
    }
    val offsets = FloatArray(layout.stackCount)
    val stackSpacing = stackSpacing.pixels
    var width = 0f
    thicknesses.forEachIndexed { stackIndex, thickness ->
      if (stackIndex > 0) width += stackSpacing
      offsets[stackIndex] = width
      width += thickness.pixels
    }
    return ColumnStackGeometry(seriesKeys, density.density, layout, thicknesses, offsets, width)
      .also { stackGeometry = it }
  }

  /**
   * The series to draw. Exiting series come with the providers, widths, and stacks of the layers
   * they came from, so this layer’s providers aren’t asked about them.
   */
  private class DrawnSeries(
    val keys: List<Any>,
    val series: List<List<ColumnCartesianLayerModel.Entry>>,
    val columnProviders: List<ColumnProvider>,
    val isExiting: BooleanArray,
    val thicknesses: List<Dp>,
    val layout: ColumnStackLayout,
  )

  /**
   * Assigns the series of a [ColumnCartesianLayer] to stacks. Implementations must be pure
   * functions of their arguments and of the values they capture.
   *
   * It may be called off the main thread. In a composable function, a lambda converted to
   * [StackKeyProvider] is memoized while the values it captures are unchanged (compared via
   * `equals` for stable types and by identity otherwise), so it needs no explicit `remember`.
   *
   * It’s asked only about the series and entries in its layer’s model: a series that exits after a
   * data update keeps the stack it had, from the layer it came from.
   */
  @Immutable
  public fun interface StackKeyProvider {
    /**
     * Returns the key of the stack to which the specified series belongs. Series whose stack keys
     * are equal (as determined by `equals`) share a stack. Stacks are ordered by their first
     * series, and each stack is as wide as its widest column. In each stack, positive and negative
     * values accumulate separately.
     */
    public fun getStackKey(seriesKey: Any, seriesIndex: Int): Any

    /** Houses [StackKeyProvider] instances. */
    public companion object {
      /**
       * Groups columns with matching _x_-values horizontally by giving each series a stack of its
       * own.
       */
      public val Grouped: StackKeyProvider = StackKeyProvider { _, seriesIndex -> seriesIndex }

      /** Stacks columns with matching _x_-values by placing all series in one stack. */
      public val Stacked: StackKeyProvider = StackKeyProvider { _, _ -> Unit }
    }
  }

  /**
   * Compares the configuration and data (not the drawing caches), so that an equal
   * [ColumnCartesianLayer] doesn’t cause the chart to be recreated. This applies to
   * [ColumnCartesianLayer] itself; instances of subclasses compare by identity unless the subclass
   * overrides `equals` and `hashCode` (covering the settings it adds).
   */
  override fun equals(other: Any?): Boolean =
    this === other ||
      this::class == ColumnCartesianLayer::class &&
        other is ColumnCartesianLayer &&
        other::class == ColumnCartesianLayer::class &&
        model == other.model &&
        columnProvider == other.columnProvider &&
        groupSpacing == other.groupSpacing &&
        stackKeyProvider == other.stackKeyProvider &&
        stackSpacing == other.stackSpacing &&
        dataLabel == other.dataLabel &&
        dataLabelPosition == other.dataLabelPosition &&
        dataLabelValueFormatter == other.dataLabelValueFormatter &&
        dataLabelRotationDegrees == other.dataLabelRotationDegrees &&
        rangeProvider == other.rangeProvider &&
        verticalAxisPosition == other.verticalAxisPosition &&
        drawingModelInterpolator == other.drawingModelInterpolator &&
        key == other.key

  override fun hashCode(): Int {
    if (this::class != ColumnCartesianLayer::class) return super.hashCode()
    var result = model.hashCode()
    result = 31 * result + columnProvider.hashCode()
    result = 31 * result + groupSpacing.hashCode()
    result = 31 * result + stackKeyProvider.hashCode()
    result = 31 * result + stackSpacing.hashCode()
    result = 31 * result + dataLabel.hashCode()
    result = 31 * result + dataLabelPosition.hashCode()
    result = 31 * result + dataLabelValueFormatter.hashCode()
    result = 31 * result + dataLabelRotationDegrees.hashCode()
    result = 31 * result + rangeProvider.hashCode()
    result = 31 * result + verticalAxisPosition.hashCode()
    result = 31 * result + drawingModelInterpolator.hashCode()
    result = 31 * result + key.hashCode()
    return result
  }

  override fun hasSameDerivationInputs(other: CartesianLayer<*>): Boolean =
    // Subclasses may derive differently, so only the exact class qualifies.
    this::class == ColumnCartesianLayer::class &&
      other::class == ColumnCartesianLayer::class &&
      other is ColumnCartesianLayer &&
      key == other.key &&
      model.isSameDataAs(other.model) &&
      rangeProvider == other.rangeProvider &&
      verticalAxisPosition == other.verticalAxisPosition &&
      stackKeyProvider == other.stackKeyProvider &&
      drawingModelInterpolator == other.drawingModelInterpolator

  override fun createDrawingModel(ranges: CartesianChartRanges): ColumnCartesianLayerDrawingModel {
    val yRangeLength = ranges.getYRange(verticalAxisPosition).length
    return ColumnCartesianLayerDrawingModel(
      model.series.map { series ->
        series.associate { entry ->
          entry.x to
            ColumnCartesianLayerDrawingModel.Entry(
              height = (abs(entry.y) / yRangeLength).toFloat(),
              entry = entry,
            )
        }
      },
      model.seriesKeys,
      opacity = 1f,
      seriesSources = List(model.series.size) { this },
    )
  }

  /**
   * Accumulates the heights and _y_-values of a stack’s columns at one _x_-value. [isExiting] is
   * `true` while the stack consists only of exiting columns, which don’t get data labels.
   */
  protected data class StackInfo(
    var topY: Double = 0.0,
    var bottomY: Double = 0.0,
    var topHeight: Float = 0f,
    var bottomHeight: Float = 0f,
    var isExiting: Boolean = true,
  ) {
    /** Adds a column. Exiting columns contribute their heights but not their _y_-values. */
    public fun update(y: Double, height: Float, isExiting: Boolean = false) {
      if (y >= 0f) {
        if (!isExiting) topY += y
        topHeight += height
      } else {
        if (!isExiting) bottomY += y
        bottomHeight += height
      }
      if (!isExiting) this.isExiting = false
    }
  }

  /**
   * Provides column [LineComponent]s to [ColumnCartesianLayer]s. Implementations must be pure
   * functions of their arguments and of the values they capture; to change the style, pass a new
   * [ColumnProvider]. The [ColumnCartesianLayer] determines each series’s widest column by calling
   * [getColumn] for every entry once per [ColumnCartesianLayerModel], possibly off the main thread.
   *
   * In a composable function, a lambda converted to [ColumnProvider] is memoized while the values
   * it captures are unchanged (compared via `equals` for stable types and by identity otherwise),
   * so it needs no explicit `remember`.
   *
   * It’s asked only about the series and entries in its layer’s model: a series that exits after a
   * data update keeps the style it had, from the layer it came from.
   */
  @Immutable
  public fun interface ColumnProvider {
    /**
     * Returns the [LineComponent] for the column representing [entry]. The series key and index are
     * available as [ColumnCartesianLayerModel.Entry.seriesKey] and
     * [ColumnCartesianLayerModel.Entry.seriesIndex].
     */
    public fun getColumn(entry: ColumnCartesianLayerModel.Entry): LineComponent

    /** Houses [ColumnProvider] factory functions. */
    public companion object {
      private data class Series(private val columns: List<LineComponent>) : ColumnProvider {
        override fun getColumn(entry: ColumnCartesianLayerModel.Entry) =
          columns.getRepeating(entry.seriesIndex)
      }

      /**
       * Uses one [LineComponent] per series. The [LineComponent]s and series are associated by
       * index. If there are more series than [LineComponent]s, [columns] is iterated multiple
       * times.
       */
      public fun series(columns: List<LineComponent>): ColumnProvider = Series(columns)

      /**
       * Uses one [LineComponent] per series. The [LineComponent]s and series are associated by
       * index. If there are more series than [LineComponent]s, the [LineComponent] list is iterated
       * multiple times.
       */
      public fun series(vararg columns: LineComponent): ColumnProvider = series(columns.toList())
    }
  }
}

/**
 * Creates and remembers a [ColumnCartesianLayer] that displays [model]. A new
 * [ColumnCartesianLayer] is created whenever an argument changes. Its [AnimatedCartesianLayer.key]
 * is tied to the composition slot, so data updates are animated.
 */
@Composable
public fun rememberColumnCartesianLayer(
  model: ColumnCartesianLayerModel,
  columnProvider: ColumnCartesianLayer.ColumnProvider =
    ColumnCartesianLayer.ColumnProvider.series(
      vicoTheme.columnCartesianLayerColors.map { color ->
        rememberLineComponent(Fill(color), Defaults.COLUMN_WIDTH.dp)
      },
    ),
  groupSpacing: Dp = Defaults.GROUP_SPACING.dp,
  stackKeyProvider: ColumnCartesianLayer.StackKeyProvider =
    ColumnCartesianLayer.StackKeyProvider.Grouped,
  stackSpacing: Dp = Defaults.STACK_SPACING.dp,
  dataLabel: TextComponent? = null,
  dataLabelPosition: Position.Vertical = Position.Vertical.Top,
  dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
  dataLabelRotationDegrees: Float = 0f,
  rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
  verticalAxisPosition: Axis.Position.Vertical? = null,
  drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<ColumnCartesianLayerDrawingModel> =
    CartesianLayerDrawingModelInterpolator.column(),
): ColumnCartesianLayer {
  val key = rememberCartesianLayerKey()
  return remember(
    model,
    columnProvider,
    groupSpacing,
    stackKeyProvider,
    stackSpacing,
    dataLabel,
    dataLabelPosition,
    dataLabelValueFormatter,
    dataLabelRotationDegrees,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
  ) {
    ColumnCartesianLayer(
      model,
      columnProvider,
      groupSpacing,
      stackKeyProvider,
      stackSpacing,
      dataLabel,
      dataLabelPosition,
      dataLabelValueFormatter,
      dataLabelRotationDegrees,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
      key,
    )
  }
}
