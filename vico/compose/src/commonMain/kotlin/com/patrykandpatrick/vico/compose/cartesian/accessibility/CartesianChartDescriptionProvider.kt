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
import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel

/**
 * Provides content descriptions for Cartesian charts and their intervals.
 */
@Immutable
public interface CartesianChartDescriptionProvider {

  /**
   * Returns a content description summarizing the entire chart.
   */
  public fun getSummary(
    context: CartesianMeasuringContext,
    summary: CartesianAggregation,
  ): CharSequence

  /**
   * Returns a content description for a specific visible interval.
   */
  public fun getIntervalDescription(
    context: CartesianMeasuringContext,
    interval: CartesianAggregation,
  ): CharSequence

  /**
   * Houses factory functions for [CartesianChartDescriptionProvider].
   */
  public companion object {
    /**
     * Creates the default [CartesianChartDescriptionProvider] implementation.
     *
     * @param xValueFormatter formats x values.
     * @param yValueFormatter formats y values.
     * @param seriesLabel generates labels for individual series. When null or returning null, the
     *   series name is omitted for single-series charts and falls back to "Series 1", "Series 2", etc.
     *   for multi-series charts.
     */
    public fun default(
      xValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
      yValueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
      seriesLabel: ((context: CartesianMeasuringContext, layerIndex: Int, seriesKey: Any) -> CharSequence?)? =
        null,
    ): CartesianChartDescriptionProvider =
      DefaultCartesianChartDescriptionProvider(xValueFormatter, yValueFormatter, seriesLabel)
  }
}

/**
 * An alias for [CartesianChartDescriptionProvider].
 */
public typealias ContentDescriptionProvider = CartesianChartDescriptionProvider

/**
 * An alias for [CartesianChartDescriptionProvider].
 */
public typealias CartesianContentDescriptionProvider = CartesianChartDescriptionProvider

internal class DefaultCartesianChartDescriptionProvider(
  private val xValueFormatter: CartesianValueFormatter,
  private val yValueFormatter: CartesianValueFormatter,
  private val seriesLabel: ((context: CartesianMeasuringContext, layerIndex: Int, seriesKey: Any) -> CharSequence?)?,
) : CartesianChartDescriptionProvider {

  private fun getTotalSeriesCount(
    context: CartesianMeasuringContext,
    aggregation: CartesianAggregation,
  ): Int {
    val fromModel = context.model.models.sumOf { model ->
      when (model) {
        is LineCartesianLayerModel -> model.seriesKeys.size
        is ColumnCartesianLayerModel -> model.seriesKeys.size
        is CandlestickCartesianLayerModel -> 1
        else -> 0
      }
    }
    if (fromModel > 0) return fromModel

    return aggregation.layerAggregations.sumOf { layerAggregation ->
      when (layerAggregation) {
        is LineCartesianLayerAggregation -> layerAggregation.series.size
        is ColumnCartesianLayerAggregation -> layerAggregation.series.size
        is CandlestickCartesianLayerAggregation -> 1
        else -> 0
      }
    }
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
        number += when (val model = context.model.models[i]) {
          is LineCartesianLayerModel -> model.seriesKeys.size
          is ColumnCartesianLayerModel -> model.seriesKeys.size
          is CandlestickCartesianLayerModel -> 1
          else -> 0
        }
      }
    }
    if (layerIndex < context.model.models.size) {
      val model = context.model.models[layerIndex]
      val keyIndex = when (model) {
        is LineCartesianLayerModel -> model.seriesKeys.indexOf(seriesKey)
        is ColumnCartesianLayerModel -> model.seriesKeys.indexOf(seriesKey)
        is CandlestickCartesianLayerModel -> 0
        else -> -1
      }
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

  override fun getSummary(
    context: CartesianMeasuringContext,
    summary: CartesianAggregation,
  ): CharSequence = buildString {
    append(getChartType(summary))
    append(". ")

    val startX = xValueFormatter.format(context, summary.x.start, null)
    val endX = xValueFormatter.format(context, summary.x.endInclusive, null)
    if (summary.x.start == summary.x.endInclusive) {
      append("X: ").append(startX).append(".")
    } else {
      append("X from ").append(startX).append(" to ").append(endX).append(".")
    }

    val totalSeriesCount = getTotalSeriesCount(context, summary)
    var seriesCounter = 1

    summary.layerAggregations.forEach { layerAggregation ->
      when (layerAggregation) {
        is LineCartesianLayerAggregation -> {
          layerAggregation.series.forEach { series ->
            append(" ")
            val label =
              getSeriesLabel(
                context = context,
                layerIndex = layerAggregation.layerIndex,
                seriesKey = series.key,
                totalSeriesCount = totalSeriesCount,
                fallbackSeriesNumber = seriesCounter,
              )
            seriesCounter++
            if (label != null) {
              append(label).append(": ")
            }

            val minY = yValueFormatter.format(context, series.min.y, layerAggregation.verticalAxisPosition)
            val minX = xValueFormatter.format(context, series.min.x, null)
            val maxY = yValueFormatter.format(context, series.max.y, layerAggregation.verticalAxisPosition)
            val maxX = xValueFormatter.format(context, series.max.x, null)
            val firstY = yValueFormatter.format(context, series.first.y, layerAggregation.verticalAxisPosition)
            val lastY = yValueFormatter.format(context, series.last.y, layerAggregation.verticalAxisPosition)
            val change = series.last.y - series.first.y
            val changeSign = if (change > 0) "+" else ""
            val changeStr = "$changeSign${yValueFormatter.format(context, change, layerAggregation.verticalAxisPosition)}"

            append("Min: ").append(minY).append(" at ").append(minX)
            append(", max: ").append(maxY).append(" at ").append(maxX)
            append(", first: ").append(firstY).append(", last: ").append(lastY)
            append(" (change: ").append(changeStr).append(").")
          }
        }

        is ColumnCartesianLayerAggregation -> {
          layerAggregation.series.forEach { series ->
            append(" ")
            val label =
              getSeriesLabel(
                context = context,
                layerIndex = layerAggregation.layerIndex,
                seriesKey = series.key,
                totalSeriesCount = totalSeriesCount,
                fallbackSeriesNumber = seriesCounter,
              )
            seriesCounter++
            if (label != null) {
              append(label).append(": ")
            }

            val sum = yValueFormatter.format(context, series.sum, layerAggregation.verticalAxisPosition)
            val mean = yValueFormatter.format(context, series.mean, layerAggregation.verticalAxisPosition)

            append("Sum: ").append(sum).append(", mean: ").append(mean).append(".")
          }
        }

        is CandlestickCartesianLayerAggregation -> {
          append(" ")
          val label =
            getSeriesLabel(
              context = context,
              layerIndex = layerAggregation.layerIndex,
              seriesKey = layerAggregation.key,
              totalSeriesCount = totalSeriesCount,
              fallbackSeriesNumber = seriesCounter,
            )
          seriesCounter++
          if (label != null) {
            append(label).append(": ")
          }

          val open = yValueFormatter.format(context, layerAggregation.opening, layerAggregation.verticalAxisPosition)
          val high = yValueFormatter.format(context, layerAggregation.high, layerAggregation.verticalAxisPosition)
          val low = yValueFormatter.format(context, layerAggregation.low, layerAggregation.verticalAxisPosition)
          val close = yValueFormatter.format(context, layerAggregation.closing, layerAggregation.verticalAxisPosition)
          val change = layerAggregation.closing - layerAggregation.opening
          val changeSign = if (change > 0) "+" else ""
          val changeStr = "$changeSign${yValueFormatter.format(context, change, layerAggregation.verticalAxisPosition)}"

          append("Open: ").append(open).append(", high: ").append(high)
          append(", low: ").append(low).append(", close: ").append(close)
          append(" (change: ").append(changeStr).append(").")
        }

        else -> {
          // Unknown layer aggregation is safely ignored
        }
      }
    }
  }

  override fun getIntervalDescription(
    context: CartesianMeasuringContext,
    interval: CartesianAggregation,
  ): CharSequence = buildString {
    val startX = xValueFormatter.format(context, interval.x.start, null)
    val endX = xValueFormatter.format(context, interval.x.endInclusive, null)
    if (interval.x.start == interval.x.endInclusive) {
      append("X: ").append(startX).append(".")
    } else {
      append("X: ").append(startX).append(" to ").append(endX).append(".")
    }

    val totalSeriesCount = getTotalSeriesCount(context, interval)
    var seriesCounter = 1

    interval.layerAggregations.forEach { layerAggregation ->
      when (layerAggregation) {
        is LineCartesianLayerAggregation -> {
          layerAggregation.series.forEach { series ->
            append(" ")
            val label =
              getSeriesLabel(
                context = context,
                layerIndex = layerAggregation.layerIndex,
                seriesKey = series.key,
                totalSeriesCount = totalSeriesCount,
                fallbackSeriesNumber = seriesCounter,
              )
            seriesCounter++
            if (label != null) {
              append(label).append(": ")
            }

            if (series.first.x == series.last.x) {
              val y = yValueFormatter.format(context, series.first.y, layerAggregation.verticalAxisPosition)
              append("Y: ").append(y).append(".")
            } else {
              val minY = yValueFormatter.format(context, series.min.y, layerAggregation.verticalAxisPosition)
              val maxY = yValueFormatter.format(context, series.max.y, layerAggregation.verticalAxisPosition)
              val firstY = yValueFormatter.format(context, series.first.y, layerAggregation.verticalAxisPosition)
              val lastY = yValueFormatter.format(context, series.last.y, layerAggregation.verticalAxisPosition)
              append("Min: ").append(minY).append(", max: ").append(maxY)
              append(", first: ").append(firstY).append(", last: ").append(lastY).append(".")
            }
          }
        }

        is ColumnCartesianLayerAggregation -> {
          layerAggregation.series.forEach { series ->
            append(" ")
            val label =
              getSeriesLabel(
                context = context,
                layerIndex = layerAggregation.layerIndex,
                seriesKey = series.key,
                totalSeriesCount = totalSeriesCount,
                fallbackSeriesNumber = seriesCounter,
              )
            seriesCounter++
            if (label != null) {
              append(label).append(": ")
            }

            if (series.count == 1) {
              val y = yValueFormatter.format(context, series.firstY, layerAggregation.verticalAxisPosition)
              append("Y: ").append(y).append(".")
            } else {
              val sum = yValueFormatter.format(context, series.sum, layerAggregation.verticalAxisPosition)
              val mean = yValueFormatter.format(context, series.mean, layerAggregation.verticalAxisPosition)
              append("Sum: ").append(sum).append(", mean: ").append(mean).append(".")
            }
          }
        }

        is CandlestickCartesianLayerAggregation -> {
          append(" ")
          val label =
            getSeriesLabel(
              context = context,
              layerIndex = layerAggregation.layerIndex,
              seriesKey = layerAggregation.key,
              totalSeriesCount = totalSeriesCount,
              fallbackSeriesNumber = seriesCounter,
            )
          seriesCounter++
          if (label != null) {
            append(label).append(": ")
          }

          val open = yValueFormatter.format(context, layerAggregation.opening, layerAggregation.verticalAxisPosition)
          val high = yValueFormatter.format(context, layerAggregation.high, layerAggregation.verticalAxisPosition)
          val low = yValueFormatter.format(context, layerAggregation.low, layerAggregation.verticalAxisPosition)
          val close = yValueFormatter.format(context, layerAggregation.closing, layerAggregation.verticalAxisPosition)

          append("Open: ").append(open).append(", high: ").append(high)
          append(", low: ").append(low).append(", close: ").append(close).append(".")
        }

        else -> {
          // Unknown layer aggregation is safely ignored
        }
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
