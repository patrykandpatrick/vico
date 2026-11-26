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

import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.common.rangeOf
import com.patrykandpatrick.vico.compose.common.rangeOfPair

/**
 * Stores a [LineCartesianLayer]’s data. Use [build] to create instances. A model without entries
 * (e.g., from `build { }`) is valid: the [LineCartesianLayer] then draws nothing, and the chart
 * shows an empty coordinate plane. Series may be empty, too.
 */
public class LineCartesianLayerModel : CartesianLayerModel {
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

  /** Creates a [LineCartesianLayerModel] with the given series, using their indices as keys. */
  public constructor(series: List<List<Entry>>) : this(series, series.indices.toList())

  /** Creates a [LineCartesianLayerModel] with the given series and series keys. */
  public constructor(series: List<List<Entry>>, seriesKeys: List<Any>) {
    require(series.size == seriesKeys.size) { "`series` and `seriesKeys` must have the same size." }
    require(seriesKeys.toSet().size == seriesKeys.size) { "Series keys must be unique." }
    this.seriesKeys = seriesKeys.toList()
    this.series = series.mapIndexed { seriesIndex, entries ->
      entries
        .sortedBy { entry -> entry.x }
        .map { entry -> Entry(entry.x, entry.y, seriesKeys[seriesIndex], seriesIndex) }
    }
    this.entries = this.series.flatten()
    val xRange = this.series.filter { it.isNotEmpty() }.rangeOfPair { it.first().x to it.last().x }
    val yRange = entries.rangeOf { it.y }
    this.minX = xRange.start
    this.maxX = xRange.endInclusive
    this.minY = yRange.start
    this.maxY = yRange.endInclusive
    this.hashCode = 31 * this.series.hashCode() + this.seriesKeys.hashCode()
  }

  override val isEmpty: Boolean
    get() = entries.isEmpty()

  override fun getXDeltaGcd(): Double = entries.getXDeltaGcd()

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LineCartesianLayerModel &&
        hashCode == other.hashCode &&
        series == other.series &&
        seriesKeys == other.seriesKeys

  override fun hashCode(): Int = hashCode

  override fun toString(): String =
    "LineCartesianLayerModel(series=$series, seriesKeys=$seriesKeys, minX=$minX, maxX=$maxX, minY=$minY, maxY=$maxY)"

  /** Represents a line node at ([x], [y]). */
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

  /** Facilitates the creation of [LineCartesianLayerModel]s. */
  public class BuilderScope internal constructor() {
    internal val series = mutableListOf<List<Entry>>()
    internal val seriesKeys = mutableListOf<Any>()

    /**
     * Adds a series with the provided _x_ values ([x]), _y_ values ([y]), and [key]. [x] and [y]
     * should have the same size.
     */
    public fun series(x: Collection<Number>, y: Collection<Number>, key: Any = series.size) {
      series.add(x.zip(y, LineCartesianLayerModel::Entry))
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
    /** Creates a [LineCartesianLayerModel]. */
    public fun build(block: BuilderScope.() -> Unit): LineCartesianLayerModel =
      BuilderScope().apply(block).let { LineCartesianLayerModel(it.series, it.seriesKeys) }
  }
}
