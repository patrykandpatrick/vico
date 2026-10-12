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
     * @param yValueFormatter formats y values.
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

  private fun getTotalSeriesCount(
    context: CartesianMeasuringContext,
    aggregation: CartesianAggregation,
  ): Int {
    val fromModel = context.model.models.sumOf { it.seriesKeys.size }
    if (fromModel > 0) return fromModel
    return aggregation.layerAggregations.sumOf { it.seriesCount }
  }

  private fun getSeriesLabel(
    context: CartesianMeasuringContext,
    layerIndex: Int,
    seriesKey: Any,
    totalSeriesCount: Int,
    fallbackSeriesNumber: Int,
  ): String? {
    val customLabel = seriesLabel?.invoke(context, layerIndex, seriesKey)?.toString()
    if (customLabel != null) return customLabel
    if (totalSeriesCount <= 1) return null

    var number = 0
    var found = false
    for (i in 0 until layerIndex) {
      if (i < context.model.models.size) {
        number += context.model.models[i].seriesKeys.size
      }
    }
    if (layerIndex < context.model.models.size) {
      val model = context.model.models[layerIndex]
      val keyIndex = model.seriesKeys.indexOf(seriesKey)
      if (keyIndex >= 0) {
        number += keyIndex + 1
        found = true
      }
    }

    val seriesNumber = if (found) number else fallbackSeriesNumber
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
    seriesKey: Any,
    totalSeriesCount: Int,
    fallbackSeriesNumber: Int,
  ) {
    append(" ")
    val label =
      getSeriesLabel(
        context = context,
        layerIndex = layerIndex,
        seriesKey = seriesKey,
        totalSeriesCount = totalSeriesCount,
        fallbackSeriesNumber = fallbackSeriesNumber,
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
      "Y: $firstY."
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
    return if (!isSummary && series.count == 1) {
      val y = yValueFormatter.format(context, series.firstY, verticalAxisPosition)
      "Y: $y."
    } else {
      val sum = yValueFormatter.format(context, series.sum, verticalAxisPosition)
      val mean = yValueFormatter.format(context, series.mean, verticalAxisPosition)
      "Sum: $sum, mean: $mean."
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

  override fun getSummary(
    context: CartesianMeasuringContext,
    summary: CartesianAggregation,
  ): CharSequence = buildString {
    append(getChartType(summary)).append(". ")
    append(formatXRange(context, summary.x, isSummary = true))

    val totalSeriesCount = getTotalSeriesCount(context, summary)
    var seriesCounter = 1

    summary.layerAggregations.forEach { layerAggregation ->
      when (layerAggregation) {
        is LineCartesianLayerAggregation -> {
          layerAggregation.series.forEach { series ->
            appendSeriesPrefix(
              context,
              layerAggregation.layerIndex,
              series.key,
              totalSeriesCount,
              seriesCounter,
            )
            seriesCounter += 1
            append(
              formatLineSeries(
                context,
                series,
                layerAggregation.verticalAxisPosition,
                isSummary = true,
              )
            )
          }
        }
        is ColumnCartesianLayerAggregation -> {
          layerAggregation.series.forEach { series ->
            appendSeriesPrefix(
              context,
              layerAggregation.layerIndex,
              series.key,
              totalSeriesCount,
              seriesCounter,
            )
            seriesCounter += 1
            append(
              formatColumnSeries(
                context,
                series,
                layerAggregation.verticalAxisPosition,
                isSummary = true,
              )
            )
          }
        }
        is CandlestickCartesianLayerAggregation -> {
          appendSeriesPrefix(
            context,
            layerAggregation.layerIndex,
            layerAggregation.key,
            totalSeriesCount,
            seriesCounter,
          )
          seriesCounter += 1
          append(formatCandlestick(context, layerAggregation, isSummary = true))
        }
        else -> {}
      }
    }
  }

  override fun getIntervalDescription(
    context: CartesianMeasuringContext,
    interval: CartesianAggregation,
  ): CharSequence = buildString {
    append(formatXRange(context, interval.x, isSummary = false))

    val totalSeriesCount = getTotalSeriesCount(context, interval)
    var seriesCounter = 1

    interval.layerAggregations.forEach { layerAggregation ->
      when (layerAggregation) {
        is LineCartesianLayerAggregation -> {
          layerAggregation.series.forEach { series ->
            appendSeriesPrefix(
              context,
              layerAggregation.layerIndex,
              series.key,
              totalSeriesCount,
              seriesCounter,
            )
            seriesCounter += 1
            append(
              formatLineSeries(
                context,
                series,
                layerAggregation.verticalAxisPosition,
                isSummary = false,
              )
            )
          }
        }
        is ColumnCartesianLayerAggregation -> {
          layerAggregation.series.forEach { series ->
            appendSeriesPrefix(
              context,
              layerAggregation.layerIndex,
              series.key,
              totalSeriesCount,
              seriesCounter,
            )
            seriesCounter += 1
            append(
              formatColumnSeries(
                context,
                series,
                layerAggregation.verticalAxisPosition,
                isSummary = false,
              )
            )
          }
        }
        is CandlestickCartesianLayerAggregation -> {
          appendSeriesPrefix(
            context,
            layerAggregation.layerIndex,
            layerAggregation.key,
            totalSeriesCount,
            seriesCounter,
          )
          seriesCounter += 1
          append(formatCandlestick(context, layerAggregation, isSummary = false))
        }
        else -> {}
      }
    }
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
