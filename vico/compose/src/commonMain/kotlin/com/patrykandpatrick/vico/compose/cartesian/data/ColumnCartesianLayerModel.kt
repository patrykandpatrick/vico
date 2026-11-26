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

package com.patrykandpatrick.vico.compose.cartesian.data

import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.common.rangeOf
import com.patrykandpatrick.vico.compose.common.rangeOfPair

/**
 * Stores a [ColumnCartesianLayer]’s data. Use [build] to create instances. A model without entries
 * (e.g., from `build { }`) is valid: the [ColumnCartesianLayer] then draws nothing, and the chart
 * shows an empty coordinate plane. Series may be empty, too.
 */
public class ColumnCartesianLayerModel : CartesianLayerModel {
  private val entries: List<Entry>

  private val hashCode: Int

  /** The series (lists of [Entry] instances). */
  public val series: List<List<Entry>>

  /** The keys identifying the series. */
  public val seriesKeys: List<Any>

  override val minX: Double

  override val maxX: Double

  override val minY: Double

  override val maxY: Double

  /** The minimum sum of all _y_-values associated with a given _x_-value. */
  public val minAggregateY: Double

  /** The maximum sum of all _y_-values associated with a given _x_-value. */
  public val maxAggregateY: Double

  /** Creates a [ColumnCartesianLayerModel] with the given series, using their indices as keys. */
  public constructor(series: List<List<Entry>>) : this(series, series.indices.toList())

  /** Creates a [ColumnCartesianLayerModel] with the given series and series keys. */
  public constructor(series: List<List<Entry>>, seriesKeys: List<Any>) {
    require(series.size == seriesKeys.size) { "`series` and `seriesKeys` must have the same size." }
    require(seriesKeys.toSet().size == seriesKeys.size) { "Series keys must be unique." }
    this.seriesKeys = seriesKeys.toList()
    this.series = series.mapIndexed { seriesIndex, entries ->
      val seriesKey = seriesKeys[seriesIndex]
      // Entries from the builder already carry their series key and index, so they’re reused.
      val sorted =
        if ((1..<entries.size).all { entries[it - 1].x <= entries[it].x }) entries
        else entries.sortedBy { it.x }
      if (sorted.all { it.seriesKey == seriesKey && it.seriesIndex == seriesIndex }) {
        sorted
      } else {
        sorted.map { entry -> Entry(entry.x, entry.y, seriesKey, seriesIndex) }
      }
    }
    this.entries = this.series.flatten()
    val xRange = this.series.filter { it.isNotEmpty() }.rangeOfPair { it.first().x to it.last().x }
    val yRange = entries.rangeOf { it.y }
    val aggregateYRange = entries.getAggregateYRange()
    this.minX = xRange.start
    this.maxX = xRange.endInclusive
    this.minY = yRange.start
    this.maxY = yRange.endInclusive
    this.minAggregateY = aggregateYRange.start
    this.maxAggregateY = aggregateYRange.endInclusive
    this.hashCode = 31 * this.series.hashCode() + this.seriesKeys.hashCode()
  }

  override val isEmpty: Boolean
    get() = entries.isEmpty()

  override fun getXDeltaGcd(): Double = entries.getXDeltaGcd()

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is ColumnCartesianLayerModel &&
        hashCode == other.hashCode &&
        series == other.series &&
        seriesKeys == other.seriesKeys

  override fun hashCode(): Int = hashCode

  override fun toString(): String =
    "ColumnCartesianLayerModel(" +
      "series=$series, seriesKeys=$seriesKeys, minX=$minX, maxX=$maxX, minY=$minY, maxY=$maxY, " +
      "minAggregateY=$minAggregateY, maxAggregateY=$maxAggregateY)"

  /** Represents a column of height [y] at [x]. */
  public class Entry
  internal constructor(
    override val x: Double,
    public val y: Double,
    public val seriesKey: Any = 0,
    public val seriesIndex: Int = 0,
  ) : CartesianLayerModel.Entry {
    public constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())

    override fun equals(other: Any?): Boolean =
      this === other ||
        other is Entry &&
          x == other.x &&
          y == other.y &&
          seriesKey == other.seriesKey &&
          seriesIndex == other.seriesIndex

    override fun hashCode(): Int {
      var result = x.hashCode()
      result = 31 * result + y.hashCode()
      result = 31 * result + seriesKey.hashCode()
      result = 31 * result + seriesIndex
      return result
    }

    override fun toString(): String =
      "Entry(x=$x, y=$y, seriesKey=$seriesKey, seriesIndex=$seriesIndex)"
  }

  /** Facilitates the creation of [ColumnCartesianLayerModel]s. */
  public class BuilderScope internal constructor() {
    internal val series = mutableListOf<List<Entry>>()
    internal val seriesKeys = mutableListOf<Any>()

    /**
     * Adds a series with the provided _x_ values ([x]), _y_ values ([y]), and [key]. [x] and [y]
     * should have the same size.
     */
    public fun series(x: Collection<Number>, y: Collection<Number>, key: Any = series.size) {
      val seriesIndex = series.size
      series.add(x.zip(y) { x, y -> Entry(x.toDouble(), y.toDouble(), key, seriesIndex) })
      seriesKeys.add(key)
    }

    /**
     * Adds a series with the provided _y_ values ([y]) and [key], using the _y_ values’ indices as
     * the _x_ values.
     */
    public fun series(y: Collection<Number>, key: Any = series.size) {
      series(y.indices.toList(), y, key)
    }

    /**
     * Adds a series with the provided _y_ values ([y]) and [key], using the _y_ values’ indices as
     * the _x_ values.
     */
    public fun series(vararg y: Number, key: Any = series.size) {
      series(y.toList(), key)
    }
  }

  public companion object {
    /** Creates a [ColumnCartesianLayerModel]. */
    public fun build(block: BuilderScope.() -> Unit): ColumnCartesianLayerModel =
      BuilderScope().apply(block).let { ColumnCartesianLayerModel(it.series, it.seriesKeys) }
  }
}

internal fun Iterable<ColumnCartesianLayerModel.Entry>.getAggregateYRange() =
  fold(mutableMapOf<Double, Pair<Double, Double>>()) { map, entry ->
      val (negativeY, positiveY) = map.getOrElse(entry.x) { 0.0 to 0.0 }
      map[entry.x] =
        if (entry.y < 0f) negativeY + entry.y to positiveY else negativeY to positiveY + entry.y
      map
    }
    .values
    .rangeOfPair { it }
