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

package com.patrykandpatrick.vico.core.cartesian.data

import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.rangeOf
import com.patrykandpatrick.vico.core.common.rangeOfPair

/** Stores a [LineCartesianLayer]’s data. */
public class LineCartesianLayerModel : CartesianLayerModel {
  private val entries: List<Entry>

  /** The series (lists of [Entry] instances). */
  public val series: List<List<Entry>>

  override val id: Int

  override val minX: Double

  override val maxX: Double

  override val minY: Double

  override val maxY: Double

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
    this.id = this.series.hashCode()
    this.minX = xRange.start
    this.maxX = xRange.endInclusive
    this.minY = yRange.start
    this.maxY = yRange.endInclusive
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
    extraStore: ExtraStore,
  ) {
    this.entries = entries
    this.series = series
    this.id = id
    this.minX = minX
    this.maxX = maxX
    this.minY = minY
    this.maxY = maxY
    this.extraStore = extraStore
  }

  override fun getXDeltaGcd(): Double = entries.getXDeltaGcd()

  override fun copy(extraStore: ExtraStore): CartesianLayerModel =
    LineCartesianLayerModel(entries, series, id, minX, maxX, minY, maxY, extraStore)

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LineCartesianLayerModel &&
        series == other.series &&
        id == other.id &&
        minX == other.minX &&
        maxX == other.maxX &&
        minY == other.minY &&
        maxY == other.maxY &&
        extraStore == other.extraStore

  override fun hashCode(): Int {
    var result = series.hashCode()
    result = 31 * result + id
    result = 31 * result + minX.hashCode()
    result = 31 * result + maxX.hashCode()
    result = 31 * result + minY.hashCode()
    result = 31 * result + maxY.hashCode()
    result = 31 * result + extraStore.hashCode()
    return result
  }

  /** Represents a line node at ([x], [y]). */
  public class Entry internal constructor(override val x: Double, public val y: Double) :
    CartesianLayerModel.Entry {
    public constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())

    override fun equals(other: Any?): Boolean =
      this === other || other is Entry && x == other.x && y == other.y

    override fun hashCode(): Int = 31 * x.hashCode() + y.hashCode()
  }

  /**
   * Stores the minimum amount of data required to create a [LineCartesianLayerModel] and
   * facilitates this creation.
   */
  public class Partial(private val series: List<List<Entry>>) : CartesianLayerModel.Partial {
    override fun complete(extraStore: ExtraStore): CartesianLayerModel =
      LineCartesianLayerModel(series, extraStore)

    override fun equals(other: Any?): Boolean =
      this === other || other is Partial && series == other.series

    override fun hashCode(): Int = series.hashCode()
  }

  /** Facilitates the creation of [LineCartesianLayerModel]s and [Partial]s. */
  public class BuilderScope {
    internal val series = mutableListOf<List<Entry>>()

    /**
     * Adds a series with the provided _x_ values ([x]) and _y_ values ([y]). [x] and [y] should
     * have the same size.
     */
    public fun series(x: Collection<Number>, y: Collection<Number>) {
      series.add(x.zip(y, LineCartesianLayerModel::Entry))
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
    /** Creates a [LineCartesianLayerModel]. */
    public fun build(block: BuilderScope.() -> Unit): LineCartesianLayerModel =
      LineCartesianLayerModel(BuilderScope().apply(block).series)

    /** Creates a [Partial]. */
    public fun partial(block: BuilderScope.() -> Unit): Partial =
      Partial(BuilderScope().apply(block).series)
  }
}

/**
 * Calls [block] to create a [LineCartesianLayerModel.Partial] and adds it to the
 * [CartesianChartModelProducer.Transaction]’s [CartesianLayerModel.Partial] list.
 */
public fun CartesianChartModelProducer.Transaction.lineSeries(
  block: LineCartesianLayerModel.BuilderScope.() -> Unit
) {
  add(LineCartesianLayerModel.partial(block))
}
