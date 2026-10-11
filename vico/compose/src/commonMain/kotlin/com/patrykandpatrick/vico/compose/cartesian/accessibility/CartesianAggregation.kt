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
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer

/**
 * Houses aggregated data for a Cartesian chart over the x range [x].
 *
 * @property x the range of x values covered by this aggregate.
 * @property layerAggregations the aggregates produced by the chart's layers.
 */
@Immutable
public class CartesianAggregation(
  public val x: ClosedFloatingPointRange<Double>,
  public val layerAggregations: List<CartesianLayerAggregation>,
) {
  override fun equals(other: Any?): Boolean =
    this === other ||
      other is CartesianAggregation &&
        x == other.x &&
        layerAggregations == other.layerAggregations

  override fun hashCode(): Int = 31 * x.hashCode() + layerAggregations.hashCode()

  override fun toString(): String =
    "CartesianAggregation(x=$x, layerAggregations=$layerAggregations)"
}

/**
 * Houses aggregated data for a single [CartesianLayer].
 */
@Immutable
public interface CartesianLayerAggregation {
  /**
   * The index of the layer in the chart.
   */
  public val layerIndex: Int

  /**
   * The position of the vertical axis associated with the layer, if any.
   */
  public val verticalAxisPosition: Axis.Position.Vertical?
}

/**
 * Houses aggregated data for a [LineCartesianLayer].
 *
 * @property series the aggregates for each series in the layer.
 * @property layerIndex the index of the layer in the chart.
 * @property verticalAxisPosition the position of the vertical axis associated with the layer.
 */
@Immutable
public class LineCartesianLayerAggregation(
  public val series: List<Series>,
  override val layerIndex: Int = 0,
  override val verticalAxisPosition: Axis.Position.Vertical? = null,
) : CartesianLayerAggregation {

  /**
   * Houses aggregated data for a single series in a [LineCartesianLayer].
   *
   * @property key the series key.
   * @property min the entry with the minimum y value in the range.
   * @property max the entry with the maximum y value in the range.
   * @property first the first entry in the range.
   * @property last the last entry in the range.
   */
  public data class Series(
    public val key: Any,
    public val min: LineCartesianLayerModel.Entry,
    public val max: LineCartesianLayerModel.Entry,
    public val first: LineCartesianLayerModel.Entry,
    public val last: LineCartesianLayerModel.Entry,
  )

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LineCartesianLayerAggregation &&
        series == other.series &&
        layerIndex == other.layerIndex &&
        verticalAxisPosition == other.verticalAxisPosition

  override fun hashCode(): Int {
    var result = series.hashCode()
    result = 31 * result + layerIndex.hashCode()
    result = 31 * result + (verticalAxisPosition?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String =
    "LineCartesianLayerAggregation(series=$series, layerIndex=$layerIndex, verticalAxisPosition=$verticalAxisPosition)"
}

/**
 * Houses aggregated data for a [ColumnCartesianLayer].
 *
 * @property series the aggregates for each series in the layer.
 * @property layerIndex the index of the layer in the chart.
 * @property verticalAxisPosition the position of the vertical axis associated with the layer.
 */
@Immutable
public class ColumnCartesianLayerAggregation(
  public val series: List<Series>,
  override val layerIndex: Int = 0,
  override val verticalAxisPosition: Axis.Position.Vertical? = null,
) : CartesianLayerAggregation {

  /**
   * Houses aggregated data for a single series in a [ColumnCartesianLayer].
   *
   * @property key the series key.
   * @property sum the sum of all y values in the range.
   * @property mean the arithmetic mean of all y values in the range.
   * @property count the number of entries in the range.
   * @property firstY the y value of the first entry in the range.
   */
  public data class Series(
    public val key: Any,
    public val sum: Double,
    public val mean: Double,
    public val count: Int = 1,
    public val firstY: Double = 0.0,
  )

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is ColumnCartesianLayerAggregation &&
        series == other.series &&
        layerIndex == other.layerIndex &&
        verticalAxisPosition == other.verticalAxisPosition

  override fun hashCode(): Int {
    var result = series.hashCode()
    result = 31 * result + layerIndex.hashCode()
    result = 31 * result + (verticalAxisPosition?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String =
    "ColumnCartesianLayerAggregation(series=$series, layerIndex=$layerIndex, verticalAxisPosition=$verticalAxisPosition)"
}

/**
 * Houses aggregated data for a [CandlestickCartesianLayer].
 *
 * @property key the key of the layer data.
 * @property opening the opening value of the first entry in the range.
 * @property closing the closing value of the last entry in the range.
 * @property low the lowest low value in the range.
 * @property high the highest high value in the range.
 * @property count the number of entries in the range.
 * @property layerIndex the index of the layer in the chart.
 * @property verticalAxisPosition the position of the vertical axis associated with the layer.
 */
@Immutable
public class CandlestickCartesianLayerAggregation(
  public val key: Any,
  public val opening: Double,
  public val closing: Double,
  public val low: Double,
  public val high: Double,
  public val count: Int = 1,
  override val layerIndex: Int = 0,
  override val verticalAxisPosition: Axis.Position.Vertical? = null,
) : CartesianLayerAggregation {

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is CandlestickCartesianLayerAggregation &&
        key == other.key &&
        opening == other.opening &&
        closing == other.closing &&
        low == other.low &&
        high == other.high &&
        count == other.count &&
        layerIndex == other.layerIndex &&
        verticalAxisPosition == other.verticalAxisPosition

  override fun hashCode(): Int {
    var result = key.hashCode()
    result = 31 * result + opening.hashCode()
    result = 31 * result + closing.hashCode()
    result = 31 * result + low.hashCode()
    result = 31 * result + high.hashCode()
    result = 31 * result + count.hashCode()
    result = 31 * result + layerIndex.hashCode()
    result = 31 * result + (verticalAxisPosition?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String =
    "CandlestickCartesianLayerAggregation(key=$key, opening=$opening, closing=$closing, low=$low, high=$high, count=$count, layerIndex=$layerIndex, verticalAxisPosition=$verticalAxisPosition)"
}

internal fun com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel.getXValues(): List<Double> {
  val xValues = mutableSetOf<Double>()
  models.forEach { model ->
    when (model) {
      is LineCartesianLayerModel -> {
        model.series.forEach { series ->
          series.forEach { entry -> xValues.add(entry.x) }
        }
      }
      is com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel -> {
        model.series.forEach { series ->
          series.forEach { entry -> xValues.add(entry.x) }
        }
      }
      is com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel -> {
        model.series.forEach { entry -> xValues.add(entry.x) }
      }
      else -> {}
    }
  }
  return xValues.sorted()
}
