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

package com.patrykandpatrick.vico.multiplatform.cartesian.data

import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.rangeOf
import com.patrykandpatrick.vico.multiplatform.common.rangeOfPair

/** Stores a [ColumnCartesianLayer]’s data. */
public class ColumnCartesianLayerModel : CartesianLayerModel {
  private val entries: List<Entry>

  /** The series (lists of [Entry] instances). */
  public val series: List<List<Entry>>

  override val id: Int

  override val minX: Double

  override val maxX: Double

  override val minY: Double

  override val maxY: Double

  /** The minimum sum of all _y_ values associated with a given _x_ value. */
  public val minAggregateY: Double

  /** The maximum sum of all _y_ values associated with a given _x_ value. */
  public val maxAggregateY: Double

  override val extraStore: ExtraStore

  public constructor(series: List<List<Entry>>) : this(series, ExtraStore.Empty)

  private constructor(series: List<List<Entry>>, extraStore: ExtraStore) {
    require(series.isNotEmpty()) { "At least one series should be added." }
    this.series =
      series.map { entries ->
        require(entries.isNotEmpty()) { "Series can’t be empty." }
        entries.sortedBy { entry -> entry.x }
      }
    this.entries = this.series.flatten()
    val xRange = this.series.rangeOfPair { it.first().x to it.last().x }
    val yRange = entries.rangeOf { it.y }
    val aggregateYRange = entries.getAggregateYRange()
    this.id = this.series.hashCode()
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
    id: Int,
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
    this.id = id
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
      id,
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
        id == other.id &&
        minX == other.minX &&
        maxX == other.maxX &&
        minY == other.minY &&
        maxY == other.maxY &&
        minAggregateY == other.minAggregateY &&
        maxAggregateY == other.maxAggregateY &&
        extraStore == other.extraStore

  override fun hashCode(): Int {
    var result = series.hashCode()
    result = 31 * result + id
    result = 31 * result + minX.hashCode()
    result = 31 * result + maxX.hashCode()
    result = 31 * result + minY.hashCode()
    result = 31 * result + maxY.hashCode()
    result = 31 * result + minAggregateY.hashCode()
    result = 31 * result + maxAggregateY.hashCode()
    result = 31 * result + extraStore.hashCode()
    return result
  }

  /** Represents a column of height [y] at [x]. */
  public class Entry internal constructor(override val x: Double, public val y: Double) :
    CartesianLayerModel.Entry {
    public constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())

    override fun equals(other: Any?): Boolean =
      this === other || other is Entry && x == other.x && y == other.y

    override fun hashCode(): Int = 31 * x.hashCode() + y.hashCode()
  }

  /**
   * Stores the minimum amount of data required to create a [ColumnCartesianLayerModel] and
   * facilitates this creation.
   */
  public class Partial(private val series: List<List<Entry>>) : CartesianLayerModel.Partial {
    override fun complete(extraStore: ExtraStore): CartesianLayerModel =
      ColumnCartesianLayerModel(series, extraStore)

    override fun equals(other: Any?): Boolean =
      this === other || other is Partial && series == other.series

    override fun hashCode(): Int = series.hashCode()
  }

  /** Facilitates the creation of [ColumnCartesianLayerModel]s and [Partial]s. */
  public class BuilderScope internal constructor() {
    internal val series = mutableListOf<List<Entry>>()

    /**
     * Adds a series with the provided _x_ values ([x]) and _y_ values ([y]). [x] and [y] should
     * have the same size.
     */
    public fun series(x: Collection<Number>, y: Collection<Number>) {
      series.add(x.zip(y, ColumnCartesianLayerModel::Entry))
    }

    /** Adds a series with the provided _y_ values ([y]), using their indices as the _x_ values. */
    public fun series(y: Collection<Number>) {
      series(y.indices.toList(), y)
    }

    /** Adds a series with the provided _y_ values ([y]), using their indices as the _x_ values. */
    public fun series(vararg y: Number) {
      series(y.toList())
    }
  }

  public companion object {
    /** Creates a [ColumnCartesianLayerModel]. */
    public fun build(block: BuilderScope.() -> Unit): ColumnCartesianLayerModel =
      ColumnCartesianLayerModel(BuilderScope().apply(block).series)

    /** Creates a [Partial]. */
    public fun partial(block: BuilderScope.() -> Unit): Partial =
      Partial(BuilderScope().apply(block).series)
  }
}

/**
 * Calls [block] to create a [ColumnCartesianLayerModel.Partial] and adds it to the
 * [CartesianChartModelProducer.Transaction]’s [CartesianLayerModel.Partial] list.
 */
public fun CartesianChartModelProducer.Transaction.columnSeries(
  block: ColumnCartesianLayerModel.BuilderScope.() -> Unit
) {
  add(ColumnCartesianLayerModel.partial(block))
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
