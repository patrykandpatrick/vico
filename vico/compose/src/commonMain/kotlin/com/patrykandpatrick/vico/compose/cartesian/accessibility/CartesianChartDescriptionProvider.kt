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

package com.patrykandpatrick.vico.compose.cartesian.accessibility

import androidx.compose.runtime.Immutable
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter

/** Provides content descriptions for Cartesian charts and their intervals. */
@Immutable
public interface CartesianChartDescriptionProvider {

  /** Returns a content description summarizing the entire chart. */
  public fun getSummary(
    context: CartesianMeasuringContext,
    summary: CartesianAggregation,
  ): CharSequence

  /** Returns a content description for a specific visible interval. */
  public fun getIntervalDescription(
    context: CartesianMeasuringContext,
    interval: CartesianAggregation,
  ): CharSequence

  /** Houses factory functions for [CartesianChartDescriptionProvider]. */
  public companion object {
    /**
     * Creates the default [CartesianChartDescriptionProvider] implementation.
     *
     * @param xValueFormatter formats x values.
     * @param yValueFormatter formats y values. It also receives derived values (such as changes,
     *   sums, and means), so a formatter that indexes labels by y.toInt() must handle values that
     *   are not raw data values. Formatting is evaluated inside semantics blocks, running
     *   only when an accessibility service queries the chart.
     * @param seriesLabel generates labels for individual series. When null or returning null, the
     *   series name is omitted for single-series charts and falls back to "Series 1", "Series 2",
     *   etc. for multi-series charts.
     */
    public fun default(
      xValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
      yValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
      seriesLabel:
        ((context: CartesianMeasuringContext, layerIndex: Int, seriesKey: Any) -> CharSequence?)? =
        null,
    ): CartesianChartDescriptionProvider =
      DefaultCartesianChartDescriptionProvider(xValueFormatter, yValueFormatter, seriesLabel)
  }
}

internal class DefaultCartesianChartDescriptionProvider(
  private val xValueFormatter: CartesianValueFormatter,
  private val yValueFormatter: CartesianValueFormatter,
  private val seriesLabel:
    ((context: CartesianMeasuringContext, layerIndex: Int, seriesKey: Any) -> CharSequence?)?,
) : CartesianChartDescriptionProvider {

  private fun getSeriesLabel(
    context: CartesianMeasuringContext,
    layerIndex: Int,
    seriesIndex: Int,
    seriesKey: Any,
    aggregation: CartesianAggregation,
  ): String? {
    val customLabel = seriesLabel?.invoke(context, layerIndex, seriesKey)?.toString()
    if (customLabel != null) return customLabel
    if (aggregation.seriesCount <= 1) return null
    val seriesNumber = aggregation.getSeriesNumber(layerIndex, seriesIndex)
    return "Series $seriesNumber"
  }

  private fun getChartType(summary: CartesianAggregation): String {
    val aggregations = summary.layerAggregations
    if (aggregations.isEmpty()) return "Cartesian chart"
    val allLine = aggregations.all { it is LineCartesianLayerAggregation }
    val allColumn = aggregations.all { it is ColumnCartesianLayerAggregation }
    val allCandle = aggregations.all { it is CandlestickCartesianLayerAggregation }
    return when {
      allLine -> "Line chart"
      allColumn -> "Column chart"
      allCandle -> "Candlestick chart"
      else -> "Cartesian chart"
    }
  }

  private fun formatXRange(
    context: CartesianMeasuringContext,
    range: ClosedFloatingPointRange<Double>,
    isSummary: Boolean,
  ): String {
    val startX = xValueFormatter.format(context, range.start, null)
    val endX = xValueFormatter.format(context, range.endInclusive, null)
    return if (range.start == range.endInclusive) {
      "X: $startX."
    } else if (isSummary) {
      "X from $startX to $endX."
    } else {
      "X: $startX to $endX."
    }
  }

  private fun StringBuilder.appendSeriesPrefix(
    context: CartesianMeasuringContext,
    layerIndex: Int,
    seriesIndex: Int,
    seriesKey: Any,
    aggregation: CartesianAggregation,
  ) {
    append(" ")
    val label =
      getSeriesLabel(
        context = context,
        layerIndex = layerIndex,
        seriesIndex = seriesIndex,
        seriesKey = seriesKey,
        aggregation = aggregation,
      )
    if (label != null) {
      append(label).append(": ")
    }
  }

  private fun formatLineSeries(
    context: CartesianMeasuringContext,
    series: LineCartesianLayerAggregation.Series,
    verticalAxisPosition: Axis.Position.Vertical?,
    isSummary: Boolean,
  ): String {
    val firstY = yValueFormatter.format(context, series.first.y, verticalAxisPosition)
    val lastY = yValueFormatter.format(context, series.last.y, verticalAxisPosition)
    val minY = yValueFormatter.format(context, series.min.y, verticalAxisPosition)
    val maxY = yValueFormatter.format(context, series.max.y, verticalAxisPosition)
    return if (isSummary) {
      val minX = xValueFormatter.format(context, series.min.x, null)
      val maxX = xValueFormatter.format(context, series.max.x, null)
      val change = series.last.y - series.first.y
      val changeSign = if (change > 0) "+" else ""
      val changeStr = "$changeSign${yValueFormatter.format(context, change, verticalAxisPosition)}"
      "Min: $minY at $minX, max: $maxY at $maxX, first: $firstY, last: $lastY (change: $changeStr)."
    } else if (series.first.x == series.last.x) {
      if (series.min.y != series.max.y) {
        "Min: $minY, max: $maxY."
      } else {
        "Y: $firstY."
      }
    } else {
      "Min: $minY, max: $maxY, first: $firstY, last: $lastY."
    }
  }

  private fun formatColumnSeries(
    context: CartesianMeasuringContext,
    series: ColumnCartesianLayerAggregation.Series,
    verticalAxisPosition: Axis.Position.Vertical?,
    isSummary: Boolean,
  ): String {
    return if (isSummary) {
      val minY = yValueFormatter.format(context, series.min.y, verticalAxisPosition)
      val minX = xValueFormatter.format(context, series.min.x, null)
      val maxY = yValueFormatter.format(context, series.max.y, verticalAxisPosition)
      val maxX = xValueFormatter.format(context, series.max.x, null)
      val firstY = yValueFormatter.format(context, series.first.y, verticalAxisPosition)
      val lastY = yValueFormatter.format(context, series.last.y, verticalAxisPosition)
      val change = series.last.y - series.first.y
      val changeSign = if (change > 0) "+" else ""
      val changeStr = "$changeSign${yValueFormatter.format(context, change, verticalAxisPosition)}"
      val sum = yValueFormatter.format(context, series.sum, verticalAxisPosition)
      val mean = yValueFormatter.format(context, series.mean, verticalAxisPosition)
      "Min: $minY at $minX, max: $maxY at $maxX, first: $firstY, last: $lastY (change: $changeStr), sum: $sum, mean: $mean."
    } else if (series.count == 1) {
      val y = yValueFormatter.format(context, series.first.y, verticalAxisPosition)
      "Y: $y."
    } else {
      val minY = yValueFormatter.format(context, series.min.y, verticalAxisPosition)
      val maxY = yValueFormatter.format(context, series.max.y, verticalAxisPosition)
      val sum = yValueFormatter.format(context, series.sum, verticalAxisPosition)
      val mean = yValueFormatter.format(context, series.mean, verticalAxisPosition)
      "Min: $minY, max: $maxY, sum: $sum, mean: $mean."
    }
  }

  private fun formatCandlestick(
    context: CartesianMeasuringContext,
    aggregation: CandlestickCartesianLayerAggregation,
    isSummary: Boolean,
  ): String {
    val open =
      yValueFormatter.format(context, aggregation.opening, aggregation.verticalAxisPosition)
    val high = yValueFormatter.format(context, aggregation.high, aggregation.verticalAxisPosition)
    val low = yValueFormatter.format(context, aggregation.low, aggregation.verticalAxisPosition)
    val close =
      yValueFormatter.format(context, aggregation.closing, aggregation.verticalAxisPosition)
    return if (isSummary) {
      val change = aggregation.closing - aggregation.opening
      val changeSign = if (change > 0) "+" else ""
      val changeStr =
        "$changeSign${yValueFormatter.format(context, change, aggregation.verticalAxisPosition)}"
      "Open: $open, high: $high, low: $low, close: $close (change: $changeStr)."
    } else {
      "Open: $open, high: $high, low: $low, close: $close."
    }
  }

  private fun StringBuilder.appendLayerAggregations(
    context: CartesianMeasuringContext,
    aggregation: CartesianAggregation,
    isSummary: Boolean,
  ) {
    aggregation.layerAggregations.forEach { layerAggregation ->
      when (layerAggregation) {
        is LineCartesianLayerAggregation -> {
          layerAggregation.series.forEach { series ->
            appendSeriesPrefix(
              context = context,
              layerIndex = layerAggregation.layerIndex,
              seriesIndex = series.index,
              seriesKey = series.key,
              aggregation = aggregation,
            )
            append(
              formatLineSeries(
                context = context,
                series = series,
                verticalAxisPosition = layerAggregation.verticalAxisPosition,
                isSummary = isSummary,
              )
            )
          }
        }
        is ColumnCartesianLayerAggregation -> {
          layerAggregation.series.forEach { series ->
            appendSeriesPrefix(
              context = context,
              layerIndex = layerAggregation.layerIndex,
              seriesIndex = series.index,
              seriesKey = series.key,
              aggregation = aggregation,
            )
            append(
              formatColumnSeries(
                context = context,
                series = series,
                verticalAxisPosition = layerAggregation.verticalAxisPosition,
                isSummary = isSummary,
              )
            )
          }
        }
        is CandlestickCartesianLayerAggregation -> {
          appendSeriesPrefix(
            context = context,
            layerIndex = layerAggregation.layerIndex,
            seriesIndex = 0,
            seriesKey = layerAggregation.key,
            aggregation = aggregation,
          )
          append(formatCandlestick(context, layerAggregation, isSummary = isSummary))
        }
        else -> {}
      }
    }
  }

  override fun getSummary(
    context: CartesianMeasuringContext,
    summary: CartesianAggregation,
  ): CharSequence = buildString {
    append(getChartType(summary)).append(". ")
    append(formatXRange(context, summary.x, isSummary = true))
    appendLayerAggregations(context, summary, isSummary = true)
  }

  override fun getIntervalDescription(
    context: CartesianMeasuringContext,
    interval: CartesianAggregation,
  ): CharSequence = buildString {
    append(formatXRange(context, interval.x, isSummary = false))
    appendLayerAggregations(context, interval, isSummary = false)
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is DefaultCartesianChartDescriptionProvider &&
        xValueFormatter == other.xValueFormatter &&
        yValueFormatter == other.yValueFormatter &&
        seriesLabel == other.seriesLabel

  override fun hashCode(): Int {
    var result = xValueFormatter.hashCode()
    result = 31 * result + yValueFormatter.hashCode()
    result = 31 * result + (seriesLabel?.hashCode() ?: 0)
    return result
  }
}
