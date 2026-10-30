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
import com.patrykandpatrick.vico.compose.cartesian.accessibility.ColumnCartesianLayerAggregation
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
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore
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
 */
@Stable
public open class ColumnCartesianLayer
protected constructor(
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
  protected val drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<
      ColumnCartesianLayerDrawingModel.Entry,
      ColumnCartesianLayerDrawingModel,
    > =
    CartesianLayerDrawingModelInterpolator.column(),
  protected val drawingModelKey: ExtraStore.Key<ColumnCartesianLayerDrawingModel>,
) : BaseCartesianLayer<ColumnCartesianLayerModel>() {
  private val _markerTargets =
    mutableMapOf<Double, MutableList<MutableColumnCartesianLayerMarkerTarget>>()

  private val stackMarkerTargets = mutableMapOf<Double, MutableColumnCartesianLayerMarkerTarget>()

  private var markerTargetStackIndex = -1

  private var stackGeometry: ColumnStackGeometry? = null

  protected val stackInfo: MutableMap<Double, StackInfo> = mutableMapOf()

  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

  /** Creates a [ColumnCartesianLayer]. */
  public constructor(
    columnProvider: ColumnProvider,
    groupSpacing: Dp = Defaults.GROUP_SPACING.dp,
    stackKeyProvider: StackKeyProvider = StackKeyProvider.Grouped,
    stackSpacing: Dp = Defaults.STACK_SPACING.dp,
    dataLabel: TextComponent? = null,
    dataLabelPosition: Position.Vertical = Position.Vertical.Top,
    dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
    dataLabelRotationDegrees: Float = 0f,
    rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
    verticalAxisPosition: Axis.Position.Vertical? = null,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        ColumnCartesianLayerDrawingModel.Entry,
        ColumnCartesianLayerDrawingModel,
      > =
      CartesianLayerDrawingModelInterpolator.column(),
  ) : this(
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
    ExtraStore.Key(),
  )

  @Deprecated("Use the constructor with `groupSpacing`, `stackKeyProvider`, and `stackSpacing`.")
  @Suppress("DEPRECATION")
  protected constructor(
    columnProvider: ColumnProvider,
    columnCollectionSpacing: Dp = Defaults.GROUP_SPACING.dp,
    mergeMode: (ExtraStore) -> MergeMode,
    dataLabel: TextComponent? = null,
    dataLabelPosition: Position.Vertical = Position.Vertical.Top,
    dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
    dataLabelRotationDegrees: Float = 0f,
    rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
    verticalAxisPosition: Axis.Position.Vertical? = null,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        ColumnCartesianLayerDrawingModel.Entry,
        ColumnCartesianLayerDrawingModel,
      > =
      CartesianLayerDrawingModelInterpolator.column(),
    drawingModelKey: ExtraStore.Key<ColumnCartesianLayerDrawingModel>,
  ) : this(
    columnProvider,
    columnCollectionSpacing,
    MergeModeStackKeyProvider(mergeMode),
    Defaults.STACK_SPACING.dp,
    dataLabel,
    dataLabelPosition,
    dataLabelValueFormatter,
    dataLabelRotationDegrees,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
    drawingModelKey,
  )

  /** Creates a [ColumnCartesianLayer]. */
  @Deprecated("Use the constructor with `groupSpacing`, `stackKeyProvider`, and `stackSpacing`.")
  @Suppress("DEPRECATION")
  public constructor(
    columnProvider: ColumnProvider,
    columnCollectionSpacing: Dp = Defaults.GROUP_SPACING.dp,
    mergeMode: (ExtraStore) -> MergeMode,
    dataLabel: TextComponent? = null,
    dataLabelPosition: Position.Vertical = Position.Vertical.Top,
    dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
    dataLabelRotationDegrees: Float = 0f,
    rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
    verticalAxisPosition: Axis.Position.Vertical? = null,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        ColumnCartesianLayerDrawingModel.Entry,
        ColumnCartesianLayerDrawingModel,
      > =
      CartesianLayerDrawingModelInterpolator.column(),
  ) : this(
    columnProvider,
    columnCollectionSpacing,
    mergeMode,
    dataLabel,
    dataLabelPosition,
    dataLabelValueFormatter,
    dataLabelRotationDegrees,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
    ExtraStore.Key(),
  )

  @Deprecated("Use the constructor with `groupSpacing`.")
  protected constructor(
    columnProvider: ColumnProvider,
    columnCollectionSpacing: Dp?,
    stackKeyProvider: StackKeyProvider = StackKeyProvider.Grouped,
    stackSpacing: Dp = Defaults.STACK_SPACING.dp,
    dataLabel: TextComponent? = null,
    dataLabelPosition: Position.Vertical = Position.Vertical.Top,
    dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
    dataLabelRotationDegrees: Float = 0f,
    rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
    verticalAxisPosition: Axis.Position.Vertical? = null,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        ColumnCartesianLayerDrawingModel.Entry,
        ColumnCartesianLayerDrawingModel,
      > =
      CartesianLayerDrawingModelInterpolator.column(),
    drawingModelKey: ExtraStore.Key<ColumnCartesianLayerDrawingModel>,
  ) : this(
    columnProvider,
    columnCollectionSpacing ?: Defaults.GROUP_SPACING.dp,
    stackKeyProvider,
    stackSpacing,
    dataLabel,
    dataLabelPosition,
    dataLabelValueFormatter,
    dataLabelRotationDegrees,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
    drawingModelKey,
  )

  /** Creates a [ColumnCartesianLayer]. */
  @Deprecated("Use the constructor with `groupSpacing`.")
  public constructor(
    columnProvider: ColumnProvider,
    columnCollectionSpacing: Dp?,
    stackKeyProvider: StackKeyProvider = StackKeyProvider.Grouped,
    stackSpacing: Dp = Defaults.STACK_SPACING.dp,
    dataLabel: TextComponent? = null,
    dataLabelPosition: Position.Vertical = Position.Vertical.Top,
    dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
    dataLabelRotationDegrees: Float = 0f,
    rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
    verticalAxisPosition: Axis.Position.Vertical? = null,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        ColumnCartesianLayerDrawingModel.Entry,
        ColumnCartesianLayerDrawingModel,
      > =
      CartesianLayerDrawingModelInterpolator.column(),
  ) : this(
    columnProvider,
    columnCollectionSpacing ?: Defaults.GROUP_SPACING.dp,
    stackKeyProvider,
    stackSpacing,
    dataLabel,
    dataLabelPosition,
    dataLabelValueFormatter,
    dataLabelRotationDegrees,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
    ExtraStore.Key(),
  )

  override fun drawInternal(context: CartesianDrawingContext, model: ColumnCartesianLayerModel) {
    with(context) {
      _markerTargets.clear()
      stackMarkerTargets.clear()
      markerTargetStackIndex = -1
      drawChartInternal(model, ranges, extraStore.getOrNull(drawingModelKey))
      stackInfo.clear()
    }
  }

  protected open fun CartesianDrawingContext.drawChartInternal(
    model: ColumnCartesianLayerModel,
    ranges: CartesianChartRanges,
    drawingModel: ColumnCartesianLayerDrawingModel?,
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
    val geometry = getStackGeometry(model.seriesKeys, model.extraStore)
    val layout = geometry.layout

    saveLayer(opacity = drawingModel?.opacity ?: 1f)

    for (stackIndex in 0..<layout.stackCount) {
      val columnThickness = geometry.thicknesses[stackIndex]
      val halfColumnThickness = columnThickness.pixels.half

      layout.forEachSeriesIndex(stackIndex) { index ->
        val entryCollection = model.series[index]
        drawingStart = getDrawingStart(index, model.seriesKeys, model.extraStore) - scroll

        val visibleIndices =
          entryCollection.getSliceIndices(visibleXRange.start, visibleXRange.endInclusive)
        if (visibleIndices.isEmpty()) return@forEachSeriesIndex

        entryCollection.subList(visibleIndices.first, visibleIndices.last + 1).forEach { entry ->
          val columnInfo = drawingModel?.getOrNull(index)?.get(entry.x)
          height =
            (columnInfo?.height ?: (abs(entry.y) / yRange.length)).toFloat() * layerBounds.height
          val xSpacingMultiplier = ((entry.x - ranges.minX) / ranges.xStep).toFloat()
          val column = columnProvider.getColumnOrThrow(entry, model.extraStore)
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
          stackInfo.update(entry.y, height)

          val columnSignificantY = if (entry.y < 0f) columnBottom else columnTop

          updateMarkerTargets(
            entry = entry,
            stackIndex = stackIndex,
            canvasX = columnCenterX,
            canvasY = columnSignificantY,
            columnHeight = columnBottom - columnTop,
            column = column,
          )

          column.drawVertical(this, columnCenterX, columnTop, columnBottom, zoom)
        }
      }

      // The totals are drawn after the stack’s columns so that the columns can’t paint over them.
      stackInfo.forEach { (x, stackInfo) ->
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
          (columnThickness.pixels +
            min(groupSpacing.pixels, getStackSpacing(model.extraStore).pixels).half) * zoom
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

  override fun updateChartRanges(
    chartRanges: MutableCartesianChartRanges,
    model: ColumnCartesianLayerModel,
  ) {
    val yRange = model.getYRange(stackKeyProvider.getLayout(model.seriesKeys, model.extraStore))
    chartRanges.tryUpdate(
      rangeProvider.getMinX(model.minX, model.maxX, model.extraStore),
      rangeProvider.getMaxX(model.minX, model.maxX, model.extraStore),
      rangeProvider.getMinY(yRange.start, yRange.endInclusive, model.extraStore),
      rangeProvider.getMaxY(yRange.start, yRange.endInclusive, model.extraStore),
      verticalAxisPosition,
    )
  }

  override fun getAggregation(
    model: ColumnCartesianLayerModel,
    x: ClosedFloatingPointRange<Double>,
    layerIndex: Int,
  ): ColumnCartesianLayerAggregation? {
    val seriesAggregates = mutableListOf<ColumnCartesianLayerAggregation.Series>()
    model.series.forEachIndexed { seriesIndex, series ->
      val inRange = series.filterInRange(x)
      if (inRange.isNotEmpty()) {
        val sum = inRange.sumOf { it.y }
        seriesAggregates.add(
          ColumnCartesianLayerAggregation.Series(
            key = model.seriesKeys[seriesIndex],
            index = seriesIndex,
            min = inRange.minBy { it.y },
            max = inRange.maxBy { it.y },
            first = inRange.first(),
            last = inRange.last(),
            sum = sum,
            mean = sum / inRange.size,
            count = inRange.size,
          )
        )
      }
    }
    return if (seriesAggregates.isNotEmpty()) {
      ColumnCartesianLayerAggregation(seriesAggregates, layerIndex, verticalAxisPosition)
    } else {
      null
    }
  }

  override fun getSeriesKeys(model: ColumnCartesianLayerModel): List<Any> = model.seriesKeys

  override fun getXValues(model: ColumnCartesianLayerModel): List<Double> = model.xValues

  override fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
    model: ColumnCartesianLayerModel,
  ) {
    with(context) {
      val groupWidth = getGroupWidth(model.seriesKeys, model.extraStore)
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

  protected open fun CartesianMeasuringContext.getGroupWidth(
    seriesKeys: List<Any>,
    extraStore: ExtraStore,
  ): Float = getStackGeometry(seriesKeys, extraStore).width

  protected open fun CartesianDrawingContext.getDrawingStart(
    seriesIndex: Int,
    seriesKeys: List<Any>,
    extraStore: ExtraStore,
  ): Float {
    val geometry = getStackGeometry(seriesKeys, extraStore)
    return layerBounds.getStart(isLtr) +
      (layerDimensions.startPadding +
        (geometry.offsets[geometry.layout.stackIndices[seriesIndex]] -
          getGroupWidth(seriesKeys, extraStore).half) * zoom) * layoutDirectionMultiplier
  }

  private fun MeasuringContext.getStackGeometry(
    seriesKeys: List<Any>,
    extraStore: ExtraStore,
  ): ColumnStackGeometry {
    stackGeometry?.let { geometry ->
      if (
        geometry.seriesKeys === seriesKeys &&
          geometry.extraStore === extraStore &&
          geometry.density == density.density
      ) {
        return geometry
      }
    }
    val layout = stackKeyProvider.getLayout(seriesKeys, extraStore)
    val thicknesses = Array(layout.stackCount) { 0.dp }
    seriesKeys.forEachIndexed { seriesIndex, seriesKey ->
      val thickness =
        columnProvider.getWidestSeriesColumnOrThrow(seriesKey, seriesIndex, extraStore).thickness
      val stackIndex = layout.stackIndices[seriesIndex]
      if (thickness > thicknesses[stackIndex]) thicknesses[stackIndex] = thickness
    }
    val offsets = FloatArray(layout.stackCount)
    val stackSpacing = getStackSpacing(extraStore).pixels
    var width = 0f
    thicknesses.forEachIndexed { stackIndex, thickness ->
      if (stackIndex > 0) width += stackSpacing
      offsets[stackIndex] = width
      width += thickness.pixels
    }
    return ColumnStackGeometry(
        seriesKeys,
        extraStore,
        density.density,
        layout,
        thicknesses,
        offsets,
        width,
      )
      .also { stackGeometry = it }
  }

  private fun getStackSpacing(extraStore: ExtraStore): Dp =
    (stackKeyProvider as? MergeModeStackKeyProvider)?.getStackSpacing(extraStore) ?: stackSpacing

  /** Assigns the series of a [ColumnCartesianLayer] to stacks. */
  @Immutable
  public fun interface StackKeyProvider {
    /**
     * Returns the key of the stack to which the specified series belongs. Series whose stack keys
     * are equal (as determined by `equals`) share a stack. Stacks are ordered by their first
     * series, and each stack is as wide as its widest column. In each stack, positive and negative
     * values accumulate separately.
     */
    public fun getStackKey(seriesKey: Any, seriesIndex: Int, extraStore: ExtraStore): Any

    /** Houses [StackKeyProvider] instances. */
    public companion object {
      /**
       * Groups columns with matching _x_-values horizontally by giving each series a stack of its
       * own.
       */
      public val Grouped: StackKeyProvider = StackKeyProvider { _, seriesIndex, _ -> seriesIndex }

      /** Stacks columns with matching _x_-values by placing all series in one stack. */
      public val Stacked: StackKeyProvider = StackKeyProvider { _, _, _ -> Unit }
    }
  }

  /** Defines how a [ColumnCartesianLayer] should draw columns in groups. */
  @Deprecated("Use `StackKeyProvider` and `stackSpacing`.")
  @Immutable
  public sealed interface MergeMode {
    /** Returns the minimum _y_ value. */
    public fun getMinY(model: ColumnCartesianLayerModel): Double

    /** Returns the maximum _y_ value. */
    public fun getMaxY(model: ColumnCartesianLayerModel): Double

    /**
     * Groups columns with matching _x_ values horizontally, positioning them [columnSpacing] apart.
     */
    @Deprecated("Use `StackKeyProvider.Grouped`, and pass the spacing as `stackSpacing`.")
    @Suppress("DEPRECATION")
    public class Grouped(internal val columnSpacing: Dp = Defaults.STACK_SPACING.dp) : MergeMode {
      override fun getMinY(model: ColumnCartesianLayerModel): Double = model.minY

      override fun getMaxY(model: ColumnCartesianLayerModel): Double = model.maxY

      override fun equals(other: Any?): Boolean =
        this === other || other is Grouped && columnSpacing == other.columnSpacing

      override fun hashCode(): Int = columnSpacing.hashCode()
    }

    /** Stacks columns with matching _x_ values. */
    @Deprecated("Use `StackKeyProvider.Stacked`.")
    @Suppress("DEPRECATION")
    public data object Stacked : MergeMode {
      override fun getMinY(model: ColumnCartesianLayerModel): Double = model.minAggregateY

      override fun getMaxY(model: ColumnCartesianLayerModel): Double = model.maxAggregateY
    }

    /** Provides access to [MergeMode] factory functions. */
    public companion object
  }

  @Suppress("DEPRECATION")
  private data class MergeModeStackKeyProvider(private val mergeMode: (ExtraStore) -> MergeMode) :
    StackKeyProvider {
    override fun getStackKey(seriesKey: Any, seriesIndex: Int, extraStore: ExtraStore): Any =
      if (mergeMode(extraStore) == MergeMode.Stacked) Unit else seriesIndex

    fun getStackSpacing(extraStore: ExtraStore): Dp? =
      (mergeMode(extraStore) as? MergeMode.Grouped)?.columnSpacing
  }

  override fun prepareForTransformation(
    model: ColumnCartesianLayerModel?,
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

  private fun ColumnCartesianLayerModel.toDrawingModel(ranges: CartesianChartRanges) =
    series
      .map { series ->
        series.associate { entry ->
          entry.x to
            ColumnCartesianLayerDrawingModel.Entry(
              height = (abs(entry.y) / ranges.getYRange(verticalAxisPosition).length).toFloat()
            )
        }
      }
      .let { ColumnCartesianLayerDrawingModel(it, seriesKeys) }

  /** Creates a new [ColumnCartesianLayer] based on this one. */
  public fun copy(
    columnProvider: ColumnProvider = this.columnProvider,
    groupSpacing: Dp = this.groupSpacing,
    stackKeyProvider: StackKeyProvider = this.stackKeyProvider,
    stackSpacing: Dp = this.stackSpacing,
    dataLabel: TextComponent? = this.dataLabel,
    dataLabelPosition: Position.Vertical = this.dataLabelPosition,
    dataLabelValueFormatter: CartesianValueFormatter = this.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = this.dataLabelRotationDegrees,
    rangeProvider: CartesianLayerRangeProvider = this.rangeProvider,
    verticalAxisPosition: Axis.Position.Vertical? = this.verticalAxisPosition,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        ColumnCartesianLayerDrawingModel.Entry,
        ColumnCartesianLayerDrawingModel,
      > =
      this.drawingModelInterpolator,
  ): ColumnCartesianLayer =
    ColumnCartesianLayer(
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
      drawingModelKey,
    )

  /** Creates a new [ColumnCartesianLayer] based on this one. */
  @Deprecated(
    "Use the `copy` overload with `groupSpacing`, `stackKeyProvider`, and `stackSpacing`."
  )
  @Suppress("DEPRECATION")
  public fun copy(
    columnProvider: ColumnProvider = this.columnProvider,
    columnCollectionSpacing: Dp = this.groupSpacing,
    mergeMode: (ExtraStore) -> MergeMode,
    dataLabel: TextComponent? = this.dataLabel,
    dataLabelPosition: Position.Vertical = this.dataLabelPosition,
    dataLabelValueFormatter: CartesianValueFormatter = this.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = this.dataLabelRotationDegrees,
    rangeProvider: CartesianLayerRangeProvider = this.rangeProvider,
    verticalAxisPosition: Axis.Position.Vertical? = this.verticalAxisPosition,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        ColumnCartesianLayerDrawingModel.Entry,
        ColumnCartesianLayerDrawingModel,
      > =
      this.drawingModelInterpolator,
  ): ColumnCartesianLayer =
    copy(
      columnProvider,
      columnCollectionSpacing,
      MergeModeStackKeyProvider(mergeMode),
      Defaults.STACK_SPACING.dp,
      dataLabel,
      dataLabelPosition,
      dataLabelValueFormatter,
      dataLabelRotationDegrees,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
    )

  /** Creates a new [ColumnCartesianLayer] based on this one. */
  @Deprecated("Use the `copy` overload with `groupSpacing`.")
  public fun copy(
    columnProvider: ColumnProvider = this.columnProvider,
    columnCollectionSpacing: Dp?,
    stackKeyProvider: StackKeyProvider = this.stackKeyProvider,
    stackSpacing: Dp = this.stackSpacing,
    dataLabel: TextComponent? = this.dataLabel,
    dataLabelPosition: Position.Vertical = this.dataLabelPosition,
    dataLabelValueFormatter: CartesianValueFormatter = this.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = this.dataLabelRotationDegrees,
    rangeProvider: CartesianLayerRangeProvider = this.rangeProvider,
    verticalAxisPosition: Axis.Position.Vertical? = this.verticalAxisPosition,
    drawingModelInterpolator:
      CartesianLayerDrawingModelInterpolator<
        ColumnCartesianLayerDrawingModel.Entry,
        ColumnCartesianLayerDrawingModel,
      > =
      this.drawingModelInterpolator,
  ): ColumnCartesianLayer =
    copy(
      columnProvider,
      columnCollectionSpacing ?: groupSpacing,
      stackKeyProvider,
      stackSpacing,
      dataLabel,
      dataLabelPosition,
      dataLabelValueFormatter,
      dataLabelRotationDegrees,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
    )

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is ColumnCartesianLayer &&
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
        drawingModelInterpolator == other.drawingModelInterpolator

  override fun hashCode(): Int {
    var result = columnProvider.hashCode()
    result = 31 * result + groupSpacing.hashCode()
    result = 31 * result + stackKeyProvider.hashCode()
    result = 31 * result + stackSpacing.hashCode()
    result = 31 * result + (dataLabel?.hashCode() ?: 0)
    result = 31 * result + dataLabelPosition.hashCode()
    result = 31 * result + dataLabelValueFormatter.hashCode()
    result = 31 * result + dataLabelRotationDegrees.hashCode()
    result = 31 * result + rangeProvider.hashCode()
    result = 31 * result + (verticalAxisPosition?.hashCode() ?: 0)
    result = 31 * result + drawingModelInterpolator.hashCode()
    return result
  }

  protected data class StackInfo(
    var topY: Double = 0.0,
    var bottomY: Double = 0.0,
    var topHeight: Float = 0f,
    var bottomHeight: Float = 0f,
  ) {
    public fun update(y: Double, height: Float) {
      if (y >= 0f) {
        topY += y
        topHeight += height
      } else {
        bottomY += y
        bottomHeight += height
      }
    }
  }

  /** Provides column [LineComponent]s to [ColumnCartesianLayer]s. */
  @Immutable
  public interface ColumnProvider {
    /** Returns the [LineComponent] for the column with the given properties. */
    @Deprecated(
      "Override `getColumn(entry, extraStore)`. The series key and index are available as " +
        "`entry.seriesKey` and `entry.seriesIndex`."
    )
    public fun getColumn(
      entry: ColumnCartesianLayerModel.Entry,
      seriesIndex: Int,
      extraStore: ExtraStore,
    ): LineComponent = throw NotImplementedError()

    /** Returns the [LineComponent] for the column with the given properties. */
    @Suppress("DEPRECATION")
    public fun getColumn(
      entry: ColumnCartesianLayerModel.Entry,
      extraStore: ExtraStore,
    ): LineComponent = getColumn(entry, entry.seriesIndex, extraStore)

    /** Returns the widest column [LineComponent] for the specified series. */
    @Deprecated("Override `getWidestSeriesColumn(seriesKey, seriesIndex, extraStore)`.")
    public fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore): LineComponent =
      throw NotImplementedError()

    /** Returns the widest column [LineComponent] for the specified series. */
    @Suppress("DEPRECATION")
    public fun getWidestSeriesColumn(
      seriesKey: Any,
      seriesIndex: Int,
      extraStore: ExtraStore,
    ): LineComponent = getWidestSeriesColumn(seriesIndex, extraStore)

    /** Houses [ColumnProvider] factory functions. */
    public companion object {
      private data class Series(private val columns: List<LineComponent>) : ColumnProvider {
        override fun getColumn(entry: ColumnCartesianLayerModel.Entry, extraStore: ExtraStore) =
          columns.getRepeating(entry.seriesIndex)

        override fun getWidestSeriesColumn(
          seriesKey: Any,
          seriesIndex: Int,
          extraStore: ExtraStore,
        ) = columns.getRepeating(seriesIndex)
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

private fun ColumnCartesianLayer.ColumnProvider.getColumnOrThrow(
  entry: ColumnCartesianLayerModel.Entry,
  extraStore: ExtraStore,
): LineComponent =
  try {
    getColumn(entry, extraStore)
  } catch (e: NotImplementedError) {
    throw IllegalStateException(
      "`ColumnProvider` must implement `getColumn(entry, extraStore)` or the deprecated `getColumn(entry, seriesIndex, extraStore)`.",
      e,
    )
  }

private fun ColumnCartesianLayer.ColumnProvider.getWidestSeriesColumnOrThrow(
  seriesKey: Any,
  seriesIndex: Int,
  extraStore: ExtraStore,
): LineComponent =
  try {
    getWidestSeriesColumn(seriesKey, seriesIndex, extraStore)
  } catch (e: NotImplementedError) {
    throw IllegalStateException(
      "`ColumnProvider` must implement `getWidestSeriesColumn(seriesKey, seriesIndex, extraStore)` or the deprecated `getWidestSeriesColumn(seriesIndex, extraStore)`.",
      e,
    )
  }

/** Creates and remembers a [ColumnCartesianLayer]. */
@Composable
public fun rememberColumnCartesianLayer(
  columnProvider: ColumnCartesianLayer.ColumnProvider =
    ColumnCartesianLayer.ColumnProvider.series(
      vicoTheme.columnCartesianLayerColors.map { color ->
        rememberLineComponent(Fill(color), Defaults.COLUMN_WIDTH.dp)
      }
    ),
  groupSpacing: Dp = Defaults.GROUP_SPACING.dp,
  stackKeyProvider: ColumnCartesianLayer.StackKeyProvider =
    ColumnCartesianLayer.StackKeyProvider.Grouped,
  stackSpacing: Dp = Defaults.STACK_SPACING.dp,
  dataLabel: TextComponent? = null,
  dataLabelPosition: Position.Vertical = Position.Vertical.Top,
  dataLabelValueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
  dataLabelRotationDegrees: Float = 0f,
  rangeProvider: CartesianLayerRangeProvider = remember { CartesianLayerRangeProvider.auto() },
  verticalAxisPosition: Axis.Position.Vertical? = null,
  drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<
      ColumnCartesianLayerDrawingModel.Entry,
      ColumnCartesianLayerDrawingModel,
    > =
    remember {
      CartesianLayerDrawingModelInterpolator.column()
    },
): ColumnCartesianLayer {
  var columnCartesianLayerWrapper by remember { ValueWrapper<ColumnCartesianLayer?>(null) }
  return remember(
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
    val columnCartesianLayer =
      columnCartesianLayerWrapper?.copy(
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
      )
        ?: ColumnCartesianLayer(
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
        )
    columnCartesianLayerWrapper = columnCartesianLayer
    columnCartesianLayer
  }
}

/** Creates and remembers a [ColumnCartesianLayer]. */
@Deprecated(
  "Use the `rememberColumnCartesianLayer` overload with `groupSpacing`, `stackKeyProvider`, and " +
    "`stackSpacing`."
)
@Suppress("DEPRECATION")
@Composable
public fun rememberColumnCartesianLayer(
  columnProvider: ColumnCartesianLayer.ColumnProvider =
    ColumnCartesianLayer.ColumnProvider.series(
      vicoTheme.columnCartesianLayerColors.map { color ->
        rememberLineComponent(Fill(color), Defaults.COLUMN_WIDTH.dp)
      }
    ),
  columnCollectionSpacing: Dp = Defaults.GROUP_SPACING.dp,
  mergeMode: (ExtraStore) -> ColumnCartesianLayer.MergeMode,
  dataLabel: TextComponent? = null,
  dataLabelPosition: Position.Vertical = Position.Vertical.Top,
  dataLabelValueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
  dataLabelRotationDegrees: Float = 0f,
  rangeProvider: CartesianLayerRangeProvider = remember { CartesianLayerRangeProvider.auto() },
  verticalAxisPosition: Axis.Position.Vertical? = null,
  drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<
      ColumnCartesianLayerDrawingModel.Entry,
      ColumnCartesianLayerDrawingModel,
    > =
    remember {
      CartesianLayerDrawingModelInterpolator.column()
    },
): ColumnCartesianLayer {
  var columnCartesianLayerWrapper by remember { ValueWrapper<ColumnCartesianLayer?>(null) }
  return remember(
    columnProvider,
    columnCollectionSpacing,
    mergeMode,
    dataLabel,
    dataLabelPosition,
    dataLabelValueFormatter,
    dataLabelRotationDegrees,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
  ) {
    val columnCartesianLayer =
      columnCartesianLayerWrapper?.copy(
        columnProvider,
        columnCollectionSpacing,
        mergeMode,
        dataLabel,
        dataLabelPosition,
        dataLabelValueFormatter,
        dataLabelRotationDegrees,
        rangeProvider,
        verticalAxisPosition,
        drawingModelInterpolator,
      )
        ?: ColumnCartesianLayer(
          columnProvider,
          columnCollectionSpacing,
          mergeMode,
          dataLabel,
          dataLabelPosition,
          dataLabelValueFormatter,
          dataLabelRotationDegrees,
          rangeProvider,
          verticalAxisPosition,
          drawingModelInterpolator,
        )
    columnCartesianLayerWrapper = columnCartesianLayer
    columnCartesianLayer
  }
}

/** Creates and remembers a [ColumnCartesianLayer]. */
@Deprecated("Use the `rememberColumnCartesianLayer` overload with `groupSpacing`.")
@Composable
public fun rememberColumnCartesianLayer(
  columnProvider: ColumnCartesianLayer.ColumnProvider =
    ColumnCartesianLayer.ColumnProvider.series(
      vicoTheme.columnCartesianLayerColors.map { color ->
        rememberLineComponent(Fill(color), Defaults.COLUMN_WIDTH.dp)
      }
    ),
  columnCollectionSpacing: Dp?,
  stackKeyProvider: ColumnCartesianLayer.StackKeyProvider =
    ColumnCartesianLayer.StackKeyProvider.Grouped,
  stackSpacing: Dp = Defaults.STACK_SPACING.dp,
  dataLabel: TextComponent? = null,
  dataLabelPosition: Position.Vertical = Position.Vertical.Top,
  dataLabelValueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
  dataLabelRotationDegrees: Float = 0f,
  rangeProvider: CartesianLayerRangeProvider = remember { CartesianLayerRangeProvider.auto() },
  verticalAxisPosition: Axis.Position.Vertical? = null,
  drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<
      ColumnCartesianLayerDrawingModel.Entry,
      ColumnCartesianLayerDrawingModel,
    > =
    remember {
      CartesianLayerDrawingModelInterpolator.column()
    },
): ColumnCartesianLayer =
  rememberColumnCartesianLayer(
    columnProvider,
    columnCollectionSpacing ?: Defaults.GROUP_SPACING.dp,
    stackKeyProvider,
    stackSpacing,
    dataLabel,
    dataLabelPosition,
    dataLabelValueFormatter,
    dataLabelRotationDegrees,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
  )
