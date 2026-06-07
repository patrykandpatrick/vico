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

package com.patrykandpatrick.vico.shared.cartesian.data

import com.patrykandpatrick.vico.shared.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.shared.common.data.ExtraStore
import com.patrykandpatrick.vico.shared.common.rangeOf
import com.patrykandpatrick.vico.shared.common.rangeOfPair

/** Stores a [ColumnCartesianLayer]’s data. */
public class ColumnCartesianLayerModel : CartesianLayerModel {
  private val entries: List<Entry>

  /** The series (lists of [Entry] instances). */
  public val series: List<List<Entry>>

  /** The keys identifying the series. */
  public val seriesKeys: List<Any>

  override val minX: Double

  override val maxX: Double

  override val minY: Double

  override val maxY: Double

  /** The minimum sum of all _y_ values associated with a given _x_ value. */
  public val minAggregateY: Double

  /** The maximum sum of all _y_ values associated with a given _x_ value. */
  public val maxAggregateY: Double

  override val extraStore: ExtraStore

  public constructor(series: List<List<Entry>>) : this(series, series.indices.toList())

  public constructor(
    series: List<List<Entry>>,
    seriesKeys: List<Any>,
  ) : this(series, seriesKeys, ExtraStore.Empty)

  private constructor(series: List<List<Entry>>, seriesKeys: List<Any>, extraStore: ExtraStore) {
    require(series.isNotEmpty()) { "At least one series should be added." }
    require(series.size == seriesKeys.size) { "`series` and `seriesKeys` must have the same size." }
    require(seriesKeys.toSet().size == seriesKeys.size) { "Series keys must be unique." }
    this.seriesKeys = seriesKeys.toList()
    this.series =
      series.mapIndexed { seriesIndex, entries ->
        require(entries.isNotEmpty()) { "Series can’t be empty." }
        entries
          .sortedBy { entry -> entry.x }
          .map { entry -> Entry(entry.x, entry.y, seriesKeys[seriesIndex], seriesIndex) }
      }
    this.entries = this.series.flatten()
    val xRange = this.series.rangeOfPair { it.first().x to it.last().x }
    val yRange = entries.rangeOf { it.y }
    val aggregateYRange = entries.getAggregateYRange()
    this.minX = xRange.start
    this.maxX = xRange.endInclusive
    this.minY = yRange.start
    this.maxY = yRange.endInclusive
    this.minAggregateY = aggregateYRange.start
    this.maxAggregateY = aggregateYRange.endInclusive
    this.extraStore = extraStore
  }

  private constructor(
    entries: List<Entry>,
    series: List<List<Entry>>,
    seriesKeys: List<Any>,
    minX: Double,
    maxX: Double,
    minY: Double,
    maxY: Double,
    minAggregateY: Double,
    maxAggregateY: Double,
    extraStore: ExtraStore,
  ) {
    this.entries = entries
    this.series = series
    this.seriesKeys = seriesKeys
    this.minX = minX
    this.maxX = maxX
    this.minY = minY
    this.maxY = maxY
    this.minAggregateY = minAggregateY
    this.maxAggregateY = maxAggregateY
    this.extraStore = extraStore
  }

  override fun getXDeltaGcd(): Double = entries.getXDeltaGcd()

  override fun copy(extraStore: ExtraStore): CartesianLayerModel =
    ColumnCartesianLayerModel(
      entries,
      series,
      seriesKeys,
      minX,
      maxX,
      minY,
      maxY,
      minAggregateY,
      maxAggregateY,
      extraStore,
    )

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is ColumnCartesianLayerModel &&
        series == other.series &&
        seriesKeys == other.seriesKeys &&
        minX == other.minX &&
        maxX == other.maxX &&
        minY == other.minY &&
        maxY == other.maxY &&
        minAggregateY == other.minAggregateY &&
        maxAggregateY == other.maxAggregateY &&
        extraStore == other.extraStore

  override fun hashCode(): Int {
    var result = series.hashCode()
    result = 31 * result + seriesKeys.hashCode()
    result = 31 * result + minX.hashCode()
    result = 31 * result + maxX.hashCode()
    result = 31 * result + minY.hashCode()
    result = 31 * result + maxY.hashCode()
    result = 31 * result + minAggregateY.hashCode()
    result = 31 * result + maxAggregateY.hashCode()
    result = 31 * result + extraStore.hashCode()
    return result
  }

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

  /**
   * Stores the minimum amount of data required to create a [ColumnCartesianLayerModel] and
   * facilitates this creation.
   */
  public class Partial(private val series: List<List<Entry>>, private val seriesKeys: List<Any>) :
    CartesianLayerModel.Partial {
    override fun complete(extraStore: ExtraStore): CartesianLayerModel =
      ColumnCartesianLayerModel(series, seriesKeys, extraStore)

    override fun equals(other: Any?): Boolean =
      this === other || other is Partial && series == other.series && seriesKeys == other.seriesKeys

    override fun hashCode(): Int = 31 * series.hashCode() + seriesKeys.hashCode()
  }

  /** Facilitates the creation of [ColumnCartesianLayerModel]s and [Partial]s. */
  public class BuilderScope internal constructor() {
    internal val series = mutableListOf<List<Entry>>()
    internal val seriesKeys = mutableListOf<Any>()

    /**
     * Adds a series with the provided _x_ values ([x]), _y_ values ([y]), and [key]. [x] and [y]
     * should have the same size.
     */
    public fun series(x: Collection<Number>, y: Collection<Number>, key: Any = series.size) {
      series.add(x.zip(y, ColumnCartesianLayerModel::Entry))
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

    /** Creates a [Partial]. */
    public fun partial(block: BuilderScope.() -> Unit): Partial =
      BuilderScope().apply(block).let { Partial(it.series, it.seriesKeys) }
  }
}

/**
 * Calls [block] to create a [ColumnCartesianLayerModel.Partial] and adds it to the
 * [CartesianChartModelProducer.Transaction]’s [CartesianLayerModel.Partial] list.
 */
public fun CartesianChartModelProducer.Transaction.columnModel(
  block: ColumnCartesianLayerModel.BuilderScope.() -> Unit
) {
  add(ColumnCartesianLayerModel.partial(block))
}

/**
 * Calls [block] to create a [ColumnCartesianLayerModel.Partial] and adds it to the
 * [CartesianChartModelProducer.Transaction]’s [CartesianLayerModel.Partial] list.
 */
@Deprecated("Use `columnModel`.", ReplaceWith("columnModel(block)"))
public fun CartesianChartModelProducer.Transaction.columnSeries(
  block: ColumnCartesianLayerModel.BuilderScope.() -> Unit
) {
  columnModel(block)
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
