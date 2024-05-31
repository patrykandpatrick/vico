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

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ChartValues
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.MutableChartValues
import com.patrykandpatrick.vico.core.cartesian.data.forEachIn
import com.patrykandpatrick.vico.core.cartesian.data.getXSpacingMultiplier
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.MutableColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.DefaultDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.data.DrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore
import com.patrykandpatrick.vico.core.common.doubled
import com.patrykandpatrick.vico.core.common.getRepeating
import com.patrykandpatrick.vico.core.common.getStart
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.inBounds
import com.patrykandpatrick.vico.core.common.unaryMinus
import kotlin.math.abs

/**
 * Displays data as vertical bars.
 *
 * @property columnProvider provides the column [LineComponent]s.
 * @property spacingDp the spacing between neighboring column collections.
 * @property innerSpacingDp the spacing between neighboring grouped columns.
 * @property mergeMode defines how columns should be drawn in column collections.
 * @property verticalAxisPosition the position of the [VerticalAxis] with which the
 *   [ColumnCartesianLayer] should be associated. Use this for independent [CartesianLayer] scaling.
 * @property dataLabel the [TextComponent] for the data labels. Use `null` for no data labels.
 * @property dataLabelVerticalPosition the vertical position of each data label relative to its
 *   column’s top edge.
 * @property dataLabelValueFormatter the [CartesianValueFormatter] for the data labels.
 * @property dataLabelRotationDegrees the rotation of the data labels (in degrees).
 * @property drawingModelInterpolator interpolates the [ColumnCartesianLayer]’s
 *   [ColumnCartesianLayerDrawingModel]s.
 */
public open class ColumnCartesianLayer(
  public var columnProvider: ColumnProvider,
  public var spacingDp: Float = Defaults.COLUMN_OUTSIDE_SPACING,
  public var innerSpacingDp: Float = Defaults.COLUMN_INSIDE_SPACING,
  public var mergeMode: (ExtraStore) -> MergeMode = { MergeMode.Grouped },
  public var verticalAxisPosition: AxisPosition.Vertical? = null,
  public var dataLabel: TextComponent? = null,
  public var dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
  public var dataLabelValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
  public var dataLabelRotationDegrees: Float = 0f,
  public var drawingModelInterpolator:
    DrawingModelInterpolator<
      ColumnCartesianLayerDrawingModel.ColumnInfo,
      ColumnCartesianLayerDrawingModel,
    > =
    DefaultDrawingModelInterpolator(),
) : BaseCartesianLayer<ColumnCartesianLayerModel>() {
  private val _markerTargets =
    mutableMapOf<Float, MutableList<MutableColumnCartesianLayerMarkerTarget>>()

  protected val stackInfo: MutableMap<Float, StackInfo> = mutableMapOf()

  /** Holds information on the [ColumnCartesianLayer]’s horizontal dimensions. */
  protected val horizontalDimensions: MutableHorizontalDimensions = MutableHorizontalDimensions()

  protected val drawingModelKey: ExtraStore.Key<ColumnCartesianLayerDrawingModel> = ExtraStore.Key()

  override val markerTargets: Map<Float, List<CartesianMarker.Target>> = _markerTargets

  override fun drawInternal(context: CartesianDrawContext, model: ColumnCartesianLayerModel): Unit =
    with(context) {
      _markerTargets.clear()
      drawChartInternal(
        chartValues = chartValues,
        model = model,
        drawingModel = model.extraStore.getOrNull(drawingModelKey),
      )
      stackInfo.clear()
    }

  protected open fun CartesianDrawContext.drawChartInternal(
    chartValues: ChartValues,
    model: ColumnCartesianLayerModel,
    drawingModel: ColumnCartesianLayerDrawingModel?,
  ) {
    val yRange = chartValues.getYRange(verticalAxisPosition)
    val heightMultiplier = bounds.height() / yRange.length

    var drawingStart: Float
    var height: Float
    var columnCenterX: Float
    var columnTop: Float
    var columnBottom: Float
    val zeroLinePosition = bounds.bottom + yRange.minY / yRange.length * bounds.height()
    val mergeMode = mergeMode(model.extraStore)

    model.series.forEachIndexed { index, entryCollection ->
      drawingStart = getDrawingStart(index, model.series.size, mergeMode) - horizontalScroll

      entryCollection.forEachIn(chartValues.minX..chartValues.maxX) { entry, _ ->
        val columnInfo = drawingModel?.getOrNull(index)?.get(entry.x)
        height = (columnInfo?.height ?: (abs(entry.y) / yRange.length)) * bounds.height()
        val xSpacingMultiplier = chartValues.getXSpacingMultiplier(entry.x)
        val column = columnProvider.getColumn(entry, index, model.extraStore)
        columnCenterX =
          drawingStart +
            (horizontalDimensions.xSpacing * xSpacingMultiplier +
              columnProvider
                .getWidestSeriesColumn(index, model.extraStore)
                .thicknessDp
                .half
                .pixels * zoom) * layoutDirectionMultiplier

        when (mergeMode) {
          MergeMode.Stacked -> {
            val stackInfo = stackInfo.getOrPut(entry.x) { StackInfo() }
            columnBottom =
              if (entry.y >= 0f) {
                zeroLinePosition - stackInfo.topHeight
              } else {
                zeroLinePosition + stackInfo.bottomHeight + height
              }
            columnTop = (columnBottom - height).coerceAtMost(columnBottom)
            stackInfo.update(entry.y, height)
          }
          MergeMode.Grouped -> {
            columnBottom = zeroLinePosition + if (entry.y < 0f) height else 0f
            columnTop = columnBottom - height
          }
        }

        val columnSignificantY = if (entry.y < 0f) columnBottom else columnTop

        if (
          column.intersectsVertical(
            context = this,
            top = columnTop,
            bottom = columnBottom,
            centerX = columnCenterX,
            boundingBox = bounds,
            thicknessScale = zoom,
          )
        ) {
          updateMarkerTargets(entry, columnCenterX, columnSignificantY, column, mergeMode)
          column.drawVertical(
            this,
            columnTop,
            columnBottom,
            columnCenterX,
            zoom,
            drawingModel?.opacity ?: 1f,
          )
        }

        if (mergeMode == MergeMode.Grouped) {
          drawDataLabel(
            modelEntriesSize = model.series.size,
            columnThicknessDp = column.thicknessDp,
            dataLabelValue = entry.y,
            x = columnCenterX,
            y = columnSignificantY,
            isFirst = index == 0 && entry.x == chartValues.minX,
            isLast = index == model.series.lastIndex && entry.x == chartValues.maxX,
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
            isFirst = entry.x == chartValues.minX,
            isLast = entry.x == chartValues.maxX,
            mergeMode = mergeMode,
          )
        }
      }
    }
  }

  protected open fun CartesianDrawContext.drawStackedDataLabel(
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

  protected open fun CartesianDrawContext.drawDataLabel(
    modelEntriesSize: Int,
    columnThicknessDp: Float,
    dataLabelValue: Float,
    x: Float,
    y: Float,
    isFirst: Boolean,
    isLast: Boolean,
    mergeMode: MergeMode,
  ) {
    dataLabel?.let { textComponent ->
      val canUseXSpacing =
        mergeMode == MergeMode.Stacked || mergeMode == MergeMode.Grouped && modelEntriesSize == 1
      var maxWidth =
        when {
          canUseXSpacing -> horizontalDimensions.xSpacing
          mergeMode == MergeMode.Grouped ->
            (columnThicknessDp + minOf(spacingDp, innerSpacingDp).half).pixels * zoom
          else -> error(message = "Encountered an unexpected `MergeMode`.")
        }
      if (isFirst && horizontalLayout is HorizontalLayout.FullWidth) {
        maxWidth = maxWidth.coerceAtMost(horizontalDimensions.startPadding.doubled)
      }
      if (isLast && horizontalLayout is HorizontalLayout.FullWidth) {
        maxWidth = maxWidth.coerceAtMost(horizontalDimensions.endPadding.doubled)
      }
      val text =
        dataLabelValueFormatter.format(
          value = dataLabelValue,
          chartValues = chartValues,
          verticalAxisPosition = verticalAxisPosition,
        )
      val dataLabelWidth =
        textComponent
          .getWidth(context = this, text = text, rotationDegrees = dataLabelRotationDegrees)
          .coerceAtMost(maximumValue = maxWidth)

      if (x - dataLabelWidth.half > bounds.right || x + dataLabelWidth.half < bounds.left) return

      val labelVerticalPosition =
        if (dataLabelValue < 0f) -dataLabelVerticalPosition else dataLabelVerticalPosition

      val verticalPosition =
        labelVerticalPosition.inBounds(
          y = y,
          bounds = bounds,
          componentHeight =
            textComponent.getHeight(
              context = this,
              text = text,
              width = maxWidth.toInt(),
              rotationDegrees = dataLabelRotationDegrees,
            ),
        )
      textComponent.drawText(
        context = this,
        text = text,
        textX = x,
        textY = y,
        verticalPosition = verticalPosition,
        maxTextWidth = maxWidth.toInt(),
        rotationDegrees = dataLabelRotationDegrees,
      )
    }
  }

  protected open fun updateMarkerTargets(
    entry: ColumnCartesianLayerModel.Entry,
    canvasX: Float,
    canvasY: Float,
    column: LineComponent,
    mergeMode: MergeMode,
  ) {
    if (canvasX <= bounds.left - 1 || canvasX >= bounds.right + 1) return
    val targetColumn =
      ColumnCartesianLayerMarkerTarget.Column(
        entry,
        canvasY.coerceIn(bounds.top, bounds.bottom),
        column.solidOrStrokeColor,
      )
    when (mergeMode) {
      MergeMode.Grouped ->
        _markerTargets.getOrPut(entry.x) { mutableListOf() } +=
          MutableColumnCartesianLayerMarkerTarget(entry.x, canvasX, mutableListOf(targetColumn))
      MergeMode.Stacked ->
        _markerTargets
          .getOrPut(entry.x) {
            mutableListOf(MutableColumnCartesianLayerMarkerTarget(entry.x, canvasX))
          }
          .first()
          .columns +=
          ColumnCartesianLayerMarkerTarget.Column(
            entry,
            canvasY.coerceIn(bounds.top, bounds.bottom),
            column.solidOrStrokeColor,
          )
    }
  }

  override fun updateChartValues(
    chartValues: MutableChartValues,
    model: ColumnCartesianLayerModel,
  ) {
    val mergeMode = mergeMode(model.extraStore)
    val minY = mergeMode.getMinY(model)
    val maxY = mergeMode.getMaxY(model)
    chartValues.tryUpdate(
      axisValueOverrider.getMinX(model.minX, model.maxX, model.extraStore),
      axisValueOverrider.getMaxX(model.minX, model.maxX, model.extraStore),
      axisValueOverrider.getMinY(minY, maxY, model.extraStore),
      axisValueOverrider.getMaxY(minY, maxY, model.extraStore),
      verticalAxisPosition,
    )
  }

  override fun updateHorizontalDimensions(
    context: CartesianMeasureContext,
    horizontalDimensions: MutableHorizontalDimensions,
    model: ColumnCartesianLayerModel,
  ) {
    with(context) {
      val columnCollectionWidth =
        getColumnCollectionWidth(
          if (model.series.isNotEmpty()) model.series.size else 1,
          mergeMode(model.extraStore),
        )
      val xSpacing = columnCollectionWidth + spacingDp.pixels
      when (val horizontalLayout = horizontalLayout) {
        is HorizontalLayout.Segmented ->
          horizontalDimensions.ensureSegmentedValues(xSpacing, chartValues)
        is HorizontalLayout.FullWidth -> {
          horizontalDimensions.ensureValuesAtLeast(
            xSpacing = xSpacing,
            scalableStartPadding =
              columnCollectionWidth.half + horizontalLayout.scalableStartPaddingDp.pixels,
            scalableEndPadding =
              columnCollectionWidth.half + horizontalLayout.scalableEndPaddingDp.pixels,
            unscalableStartPadding = horizontalLayout.unscalableStartPaddingDp.pixels,
            unscalableEndPadding = horizontalLayout.unscalableEndPaddingDp.pixels,
          )
        }
      }
    }
  }

  protected open fun CartesianMeasureContext.getColumnCollectionWidth(
    entryCollectionSize: Int,
    mergeMode: MergeMode,
  ): Float =
    when (mergeMode) {
      MergeMode.Stacked ->
        (0..<entryCollectionSize)
          .maxOf { seriesIndex ->
            columnProvider
              .getWidestSeriesColumn(seriesIndex, chartValues.model.extraStore)
              .thicknessDp
          }
          .pixels
      MergeMode.Grouped ->
        getCumulatedThickness(entryCollectionSize) +
          innerSpacingDp.pixels * (entryCollectionSize - 1)
    }

  protected open fun CartesianDrawContext.getDrawingStart(
    entryCollectionIndex: Int,
    entryCollectionCount: Int,
    mergeMode: MergeMode,
  ): Float {
    val mergeModeComponent =
      when (mergeMode) {
        MergeMode.Grouped ->
          getCumulatedThickness(entryCollectionIndex) + innerSpacingDp.pixels * entryCollectionIndex
        MergeMode.Stacked -> 0f
      }
    return bounds.getStart(isLtr) +
      (horizontalDimensions.startPadding +
        (mergeModeComponent - getColumnCollectionWidth(entryCollectionCount, mergeMode).half) *
          zoom) * layoutDirectionMultiplier
  }

  protected open fun CartesianMeasureContext.getCumulatedThickness(count: Int): Float {
    var thickness = 0f
    for (seriesIndex in 0..<count) {
      thickness +=
        columnProvider.getWidestSeriesColumn(seriesIndex, chartValues.model.extraStore).thicknessDp
    }
    return thickness.pixels
  }

  /** Defines how a [ColumnCartesianLayer] should draw columns in column collections. */
  public enum class MergeMode {
    /** Columns with the same x-axis values will be placed next to each other in groups. */
    Grouped,

    /** Columns with the same x-axis values will be placed on top of each other. */
    Stacked;

    /** Returns the minimum y-axis value, taking into account the current [MergeMode]. */
    public fun getMinY(model: ColumnCartesianLayerModel): Float =
      when (this) {
        Grouped -> model.minY
        Stacked -> model.minAggregateY
      }

    /** Returns the maximum y-axis value, taking into account the current [MergeMode]. */
    public fun getMaxY(model: ColumnCartesianLayerModel): Float =
      when (this) {
        Grouped -> model.maxY
        Stacked -> model.maxAggregateY
      }
  }

  override fun prepareForTransformation(
    model: ColumnCartesianLayerModel?,
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

  private fun ColumnCartesianLayerModel.toDrawingModel(chartValues: ChartValues) =
    series
      .map { series ->
        series.associate { entry ->
          entry.x to
            ColumnCartesianLayerDrawingModel.ColumnInfo(
              height = abs(entry.y) / chartValues.getYRange(verticalAxisPosition).length
            )
        }
      }
      .let(::ColumnCartesianLayerDrawingModel)

  protected data class StackInfo(
    var topY: Float = 0f,
    var bottomY: Float = 0f,
    var topHeight: Float = 0f,
    var bottomHeight: Float = 0f,
  ) {
    public fun update(y: Float, height: Float) {
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
      internal data class Series(val columns: List<LineComponent>) : ColumnProvider {
        override fun getColumn(
          entry: ColumnCartesianLayerModel.Entry,
          seriesIndex: Int,
          extraStore: ExtraStore,
        ): LineComponent = columns.getRepeating(seriesIndex)

        override fun getWidestSeriesColumn(
          seriesIndex: Int,
          extraStore: ExtraStore,
        ): LineComponent = columns.getRepeating(seriesIndex)
      }

      /**
       * Uses one [LineComponent] per series. The [LineComponent]s ([columns]) and series are
       * associated by index. If there are more series than [LineComponent]s, [columns] is iterated
       * multiple times.
       */
      public fun series(columns: List<LineComponent>): ColumnProvider = Series(columns)

      /**
       * Uses one [LineComponent] per series. The [LineComponent]s ([columns]) and series are
       * associated by index. If there are more series than [LineComponent]s, the [LineComponent]
       * list is iterated multiple times.
       */
      public fun series(vararg columns: LineComponent): ColumnProvider = series(columns.toList())
    }
  }
}
