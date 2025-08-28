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

package com.patrykandpatrick.vico.core.cartesian.layer

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.data.getSliceIndices
import com.patrykandpatrick.vico.core.cartesian.getVisibleXRange
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.MutableColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore
import com.patrykandpatrick.vico.core.common.doubled
import com.patrykandpatrick.vico.core.common.extractColor
import com.patrykandpatrick.vico.core.common.getRepeating
import com.patrykandpatrick.vico.core.common.getStart
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.inBounds
import com.patrykandpatrick.vico.core.common.saveLayer
import com.patrykandpatrick.vico.core.common.unaryMinus
import java.util.Objects
import kotlin.math.abs
import kotlin.math.min
import android.graphics.RectF
import com.patrykandpatrick.vico.core.common.Point

/**
 * Displays data as vertical bars.
 *
 * @property columnProvider provides the column [LineComponent]s.
 * @property columnCollectionSpacingDp the spacing between neighboring column collections (in dp).
 * @property mergeMode defines how columns should be drawn in column collections.
 * @property dataLabel the [TextComponent] for the data labels. Use `null` for no data labels.
 * @property dataLabelPosition the vertical position of positive-column data labels relative to the
 *   top edges of the columns. For negative columns, this is inverted and interpreted relative to
 *   the bottom edges of the columns.
 * @property dataLabelValueFormatter the [CartesianValueFormatter] for the data labels.
 * @property dataLabelRotationDegrees the rotation of the data labels (in degrees).
 * @property rangeProvider defines the _x_ and _y_ ranges.
 * @property verticalAxisPosition the position of the [VerticalAxis] with which the
 *   [ColumnCartesianLayer] should be associated. Use this for independent [CartesianLayer] scaling.
 * @property drawingModelInterpolator interpolates the [ColumnCartesianLayer]'s
 *   [ColumnCartesianLayerDrawingModel]s.
 * @property onColumnClick called when a column is clicked. Receives the entry and series index.
 */
@Stable
public open class ColumnCartesianLayer
protected constructor(
  protected val columnProvider: ColumnProvider,
  protected val columnCollectionSpacingDp: Float = Defaults.COLUMN_COLLECTION_SPACING,
  protected val mergeMode: (ExtraStore) -> MergeMode = { MergeMode.Grouped() },
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
    CartesianLayerDrawingModelInterpolator.default(),
  protected val onColumnClick: ((ColumnCartesianLayerModel.Entry, Int) -> Unit)? = null,
  protected val drawingModelKey: ExtraStore.Key<ColumnCartesianLayerDrawingModel>,
) : BaseCartesianLayer<ColumnCartesianLayerModel>() {
  private val _markerTargets =
    mutableMapOf<Double, MutableList<MutableColumnCartesianLayerMarkerTarget>>()

  private val columnBounds: MutableMap<Pair<ColumnCartesianLayerModel.Entry, Int>, RectF> = mutableMapOf()

  protected val stackInfo: MutableMap<Double, StackInfo> = mutableMapOf()

  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = _markerTargets

  /** Creates a [ColumnCartesianLayer]. */
  public constructor(
    columnProvider: ColumnProvider,
    columnCollectionSpacingDp: Float = Defaults.COLUMN_COLLECTION_SPACING,
    mergeMode: (ExtraStore) -> MergeMode = { MergeMode.Grouped() },
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
      CartesianLayerDrawingModelInterpolator.default(),
    onColumnClick: ((ColumnCartesianLayerModel.Entry, Int) -> Unit)? = null,
  ) : this(
    columnProvider,
    columnCollectionSpacingDp,
    mergeMode,
    dataLabel,
    dataLabelPosition,
    dataLabelValueFormatter,
    dataLabelRotationDegrees,
    rangeProvider,
    verticalAxisPosition,
    drawingModelInterpolator,
    onColumnClick,
    ExtraStore.Key(),
  )

  override fun drawInternal(context: CartesianDrawingContext, model: ColumnCartesianLayerModel) {
    with(context) {
      _markerTargets.clear()
      columnBounds.clear()
      drawChartInternal(model, ranges, extraStore.getOrNull(drawingModelKey))
      stackInfo.clear()
    }
  }

  public fun handleColumnClick(clickPoint: Point): Boolean {
    onColumnClick?.let { clickHandler ->
      columnBounds.forEach { (entrySeriesPair, bounds) ->
        if (bounds.contains(clickPoint.x, clickPoint.y)) {
          val (entry, seriesIndex) = entrySeriesPair
          clickHandler(entry, seriesIndex)
          return true
        }
      }
    }
    return false
  }

  protected open fun CartesianDrawingContext.drawChartInternal(
    model: ColumnCartesianLayerModel,
    ranges: CartesianChartRanges,
    drawingModel: ColumnCartesianLayerDrawingModel?,
  ) {
    val yRange = ranges.getYRange(verticalAxisPosition)
    val heightMultiplier = layerBounds.height() / yRange.length.toFloat()

    var drawingStart: Float
    var height: Float
    var columnCenterX: Float
    var columnTop: Float
    var columnBottom: Float
    val zeroLinePosition =
      layerBounds.bottom + (yRange.minY / yRange.length).toFloat() * layerBounds.height()
    val mergeMode = mergeMode(model.extraStore)
    val visibleXRange = getVisibleXRange()

    canvas.saveLayer(opacity = drawingModel?.opacity ?: 1f)

    model.series.forEachIndexed { index, entryCollection ->
      drawingStart = getDrawingStart(index, model.series.size, mergeMode) - scroll

      val (_, firstVisibleIndex, lastVisibleIndex) =
        entryCollection.getSliceIndices(
          ranges.minX,
          ranges.maxX,
          visibleXRange.start,
          visibleXRange.endInclusive,
        )

      entryCollection.subList(firstVisibleIndex, lastVisibleIndex + 1).forEach { entry ->
        val columnInfo = drawingModel?.getOrNull(index)?.get(entry.x)
        height =
          (columnInfo?.height ?: (abs(entry.y) / yRange.length)).toFloat() * layerBounds.height()
        val xSpacingMultiplier = ((entry.x - ranges.minX) / ranges.xStep).toFloat()
        val column = columnProvider.getColumn(entry, index, model.extraStore)
        columnCenterX =
          drawingStart +
            (layerDimensions.xSpacing * xSpacingMultiplier +
              columnProvider
                .getWidestSeriesColumn(index, model.extraStore)
                .thicknessDp
                .half
                .pixels * zoom) * layoutDirectionMultiplier

        when (mergeMode) {
          MergeMode.Stacked -> {
            val stackInfo = stackInfo.getOrPut(entry.x) { StackInfo() }
            columnBottom =
              if (entry.y >= 0) {
                zeroLinePosition - stackInfo.topHeight
              } else {
                zeroLinePosition + stackInfo.bottomHeight + height
              }
            columnTop = (columnBottom - height).coerceAtMost(columnBottom)
            stackInfo.update(entry.y, height)
          }
          is MergeMode.Grouped -> {
            columnBottom = zeroLinePosition + if (entry.y < 0f) height else 0f
            columnTop = columnBottom - height
          }
        }

        val columnSignificantY = if (entry.y < 0f) columnBottom else columnTop

        updateMarkerTargets(
          entry = entry,
          canvasX = columnCenterX,
          canvasY = columnSignificantY,
          columnHeight = columnBottom - columnTop,
          column = column,
          mergeMode = mergeMode,
        )

        column.drawVertical(this, columnCenterX, columnTop, columnBottom, zoom)

        val columnWidth = column.thicknessDp.pixels * zoom
        columnBounds[Pair(entry, index)] = RectF(
          columnCenterX - columnWidth / 2f,
          columnTop,
          columnCenterX + columnWidth / 2f,
          columnBottom
        )

        if (mergeMode is MergeMode.Grouped) {
          drawDataLabel(
            modelEntriesSize = model.series.size,
            columnThicknessDp = column.thicknessDp,
            dataLabelValue = entry.y,
            x = columnCenterX,
            y = columnSignificantY,
            isFirst = index == 0 && entry.x == ranges.minX,
            isLast = index == model.series.lastIndex && entry.x == ranges.maxX,
            mergeMode = mergeMode,
          )
        } else if (index == model.series.lastIndex) {
          drawStackedDataLabel(
            modelEntriesSize = model.series.size,
            columnThicknessDp = column.thicknessDp,
            stackInfo = stackInfo.getValue(entry.x),
            x = columnCenterX,
            zeroLinePosition = zeroLinePosition,
            heightMultiplier = heightMultiplier,
            isFirst = entry.x == ranges.minX,
            isLast = entry.x == ranges.maxX,
            mergeMode = mergeMode,
          )
        }
      }
    }

    canvas.restore()
  }

  protected open fun CartesianDrawingContext.drawStackedDataLabel(
    modelEntriesSize: Int,
    columnThicknessDp: Float,
    stackInfo: StackInfo,
    x: Float,
    zeroLinePosition: Float,
    heightMultiplier: Float,
    isFirst: Boolean,
    isLast: Boolean,
    mergeMode: MergeMode,
  ) {
    if (stackInfo.topY > 0f) {
      drawDataLabel(
        modelEntriesSize = modelEntriesSize,
        columnThicknessDp = columnThicknessDp,
        dataLabelValue = stackInfo.topY,
        x = x,
        y = zeroLinePosition - stackInfo.topHeight,
        isFirst = isFirst,
        isLast = isLast,
        mergeMode = mergeMode,
      )
    }
    if (stackInfo.bottomY < 0f) {
      drawDataLabel(
        modelEntriesSize = modelEntriesSize,
        columnThicknessDp = columnThicknessDp,
        dataLabelValue = stackInfo.bottomY,
        x = x,
        y = zeroLinePosition + stackInfo.bottomHeight,
        isFirst = isFirst,
        isLast = isLast,
        mergeMode = mergeMode,
      )
    }
  }

  protected open fun CartesianDrawingContext.drawDataLabel(
    modelEntriesSize: Int,
    columnThicknessDp: Float,
    dataLabelValue: Double,
    x: Float,
    y: Float,
    isFirst: Boolean,
    isLast: Boolean,
    mergeMode: MergeMode,
  ) {
    dataLabel?.let { textComponent ->
      val canUseXSpacing =
        mergeMode == MergeMode.Stacked || mergeMode is MergeMode.Grouped && modelEntriesSize == 1
      var maxWidth =
        when {
          canUseXSpacing -> layerDimensions.xSpacing
          mergeMode is MergeMode.Grouped ->
            (columnThicknessDp + min(columnCollectionSpacingDp, mergeMode.columnSpacingDp).half)
              .pixels * zoom
          else -> error(message = "Encountered an unexpected `MergeMode`.")
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
    canvasX: Float,
    canvasY: Float,
    columnHeight: Float,
    column: LineComponent,
    mergeMode: MergeMode,
  ) {
    if (canvasX <= layerBounds.left - 1 || canvasX >= layerBounds.right + 1) return
    val limitedCanvasY = canvasY.coerceIn(layerBounds.top, layerBounds.bottom)
    val markerColor =
      column.effectiveStrokeFill.extractColor(
        context = this,
        width = column.thicknessDp.pixels,
        height = columnHeight,
        side = if (entry.y < 0) -1 else 1,
      )
    val targetColumn = ColumnCartesianLayerMarkerTarget.Column(entry, limitedCanvasY, markerColor)
    when (mergeMode) {
      is MergeMode.Grouped ->
        _markerTargets.getOrPut(entry.x) { mutableListOf() } +=
          MutableColumnCartesianLayerMarkerTarget(entry.x, canvasX, mutableListOf(targetColumn))
      MergeMode.Stacked ->
        _markerTargets
          .getOrPut(entry.x) {
            mutableListOf(MutableColumnCartesianLayerMarkerTarget(entry.x, canvasX))
          }
          .first()
          .columns += targetColumn
    }
  }

  override fun updateChartRanges(
    chartRanges: MutableCartesianChartRanges,
    model: ColumnCartesianLayerModel,
  ) {
    val mergeMode = mergeMode(model.extraStore)
    val minY = mergeMode.getMinY(model)
    val maxY = mergeMode.getMaxY(model)
    chartRanges.tryUpdate(
      rangeProvider.getMinX(model.minX, model.maxX, model.extraStore),
      rangeProvider.getMaxX(model.minX, model.maxX, model.extraStore),
      rangeProvider.getMinY(minY, maxY, model.extraStore),
      rangeProvider.getMaxY(minY, maxY, model.extraStore),
      verticalAxisPosition,
    )
  }

  override fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
    model: ColumnCartesianLayerModel,
  ) {
    with(context) {
      val columnCollectionWidth =
        getColumnCollectionWidth(
          if (model.series.isNotEmpty()) model.series.size else 1,
          mergeMode(model.extraStore),
        )
      val xSpacing = columnCollectionWidth + columnCollectionSpacingDp.pixels
      dimensions.ensureValuesAtLeast(
        xSpacing = xSpacing,
        scalableStartPadding = columnCollectionWidth.half + layerPadding.scalableStartDp.pixels,
        scalableEndPadding = columnCollectionWidth.half + layerPadding.scalableEndDp.pixels,
        unscalableStartPadding = layerPadding.unscalableStartDp.pixels,
        unscalableEndPadding = layerPadding.unscalableEndDp.pixels,
      )
    }
  }

  protected open fun CartesianMeasuringContext.getColumnCollectionWidth(
    entryCollectionSize: Int,
    mergeMode: MergeMode,
  ): Float =
    when (mergeMode) {
      is MergeMode.Stacked ->
        (0..<entryCollectionSize)
          .maxOf { seriesIndex ->
            columnProvider.getWidestSeriesColumn(seriesIndex, model.extraStore).thicknessDp
          }
          .pixels
      is MergeMode.Grouped ->
        getCumulatedThickness(entryCollectionSize) +
          mergeMode.columnSpacingDp.pixels * (entryCollectionSize - 1)
    }

  protected open fun CartesianDrawingContext.getDrawingStart(
    entryCollectionIndex: Int,
    entryCollectionCount: Int,
    mergeMode: MergeMode,
  ): Float {
    val mergeModeComponent =
      when (mergeMode) {
        is MergeMode.Grouped ->
          getCumulatedThickness(entryCollectionIndex) +
            mergeMode.columnSpacingDp.pixels * entryCollectionIndex
        MergeMode.Stacked -> 0f
      }
    return layerBounds.getStart(isLtr) +
      (layerDimensions.startPadding +
        (mergeModeComponent - getColumnCollectionWidth(entryCollectionCount, mergeMode).half) *
          zoom) * layoutDirectionMultiplier
  }

  protected open fun CartesianMeasuringContext.getCumulatedThickness(count: Int): Float {
    var thickness = 0f
    for (seriesIndex in 0..<count) {
      thickness += columnProvider.getWidestSeriesColumn(seriesIndex, model.extraStore).thicknessDp
    }
    return thickness.pixels
  }

  /** Defines how a [ColumnCartesianLayer] should draw columns in column collections. */
  @Immutable
  public sealed interface MergeMode {
    /** Returns the minimum _y_ value. */
    public fun getMinY(model: ColumnCartesianLayerModel): Double

    /** Returns the maximum _y_ value. */
    public fun getMaxY(model: ColumnCartesianLayerModel): Double

    /**
     * Groups columns with matching _x_ values horizontally, positioning them [columnSpacingDp] dp
     * apart.
     */
    public class Grouped(internal val columnSpacingDp: Float = Defaults.GROUPED_COLUMN_SPACING) :
      MergeMode {
      override fun getMinY(model: ColumnCartesianLayerModel): Double = model.minY

      override fun getMaxY(model: ColumnCartesianLayerModel): Double = model.maxY

      override fun equals(other: Any?): Boolean =
        this === other || other is Grouped && columnSpacingDp == other.columnSpacingDp

      override fun hashCode(): Int = columnSpacingDp.hashCode()
    }

    /** Stacks columns with matching _x_ values. */
    public data object Stacked : MergeMode {
      override fun getMinY(model: ColumnCartesianLayerModel): Double = model.minAggregateY

      override fun getMaxY(model: ColumnCartesianLayerModel): Double = model.maxAggregateY
    }

    /** Provides access to [MergeMode] factory functions. */
    public companion object
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
      .let(::ColumnCartesianLayerDrawingModel)

  /** Creates a new [ColumnCartesianLayer] based on this one. */
  public fun copy(
    columnProvider: ColumnProvider = this.columnProvider,
    columnCollectionSpacingDp: Float = this.columnCollectionSpacingDp,
    mergeMode: (ExtraStore) -> MergeMode = this.mergeMode,
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
    onColumnClick: ((ColumnCartesianLayerModel.Entry, Int) -> Unit)? = this.onColumnClick,
  ): ColumnCartesianLayer =
    ColumnCartesianLayer(
      columnProvider,
      columnCollectionSpacingDp,
      mergeMode,
      dataLabel,
      dataLabelPosition,
      dataLabelValueFormatter,
      dataLabelRotationDegrees,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
      onColumnClick,
      drawingModelKey,
    )

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is ColumnCartesianLayer &&
        columnProvider == other.columnProvider &&
        columnCollectionSpacingDp == other.columnCollectionSpacingDp &&
        mergeMode == other.mergeMode &&
        dataLabel == other.dataLabel &&
        dataLabelPosition == other.dataLabelPosition &&
        dataLabelValueFormatter == other.dataLabelValueFormatter &&
        dataLabelRotationDegrees == other.dataLabelRotationDegrees &&
        rangeProvider == other.rangeProvider &&
        verticalAxisPosition == other.verticalAxisPosition &&
        drawingModelInterpolator == other.drawingModelInterpolator &&
        onColumnClick == other.onColumnClick

  override fun hashCode(): Int =
    Objects.hash(
      columnProvider,
      columnCollectionSpacingDp,
      mergeMode,
      dataLabel,
      dataLabelPosition,
      dataLabelValueFormatter,
      dataLabelRotationDegrees,
      rangeProvider,
      verticalAxisPosition,
      drawingModelInterpolator,
      onColumnClick,
    )

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
    public fun getColumn(
      entry: ColumnCartesianLayerModel.Entry,
      seriesIndex: Int,
      extraStore: ExtraStore,
    ): LineComponent

    /** Returns the widest column [LineComponent] for the specified series. */
    public fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore): LineComponent

    /** Houses [ColumnProvider] factory functions. */
    public companion object {
      private data class Series(private val columns: List<LineComponent>) : ColumnProvider {
        override fun getColumn(
          entry: ColumnCartesianLayerModel.Entry,
          seriesIndex: Int,
          extraStore: ExtraStore,
        ) = columns.getRepeating(seriesIndex)

        override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore) =
          columns.getRepeating(seriesIndex)
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
