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

import com.patrykandpatrick.vico.compose.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.common.rangeOfPair

/**
 * Stores a [CandlestickCartesianLayer]’s data. Use [build] to create instances. A model without
 * entries (e.g., from an empty series) is valid: the [CandlestickCartesianLayer] then draws
 * nothing, and the chart shows an empty coordinate plane.
 */
public class CandlestickCartesianLayerModel : CartesianLayerModel {
  private val hashCode: Int

  /** The series (list of [Entry] instances). */
  public val series: List<Entry>

  /**
   * Identifies this model’s series (a [CandlestickCartesianLayerModel] has one), like the series
   * keys of other models. When a data update changes it, the previous candles exit and the new ones
   * enter, rather than morphing into one another. It’s also exposed to markers as
   * `CandlestickCartesianLayerMarkerTarget.seriesKey`. (It’s unrelated to the layer’s
   * `AnimatedCartesianLayer.key`, which pairs layers across updates.)
   */
  public val seriesKey: Any

  override val minX: Double

  override val maxX: Double

  override val minY: Double

  override val maxY: Double

  /** Creates a [CandlestickCartesianLayerModel] with the given series and a series key of 0. */
  public constructor(series: List<Entry>) : this(series, 0)

  /** Creates a [CandlestickCartesianLayerModel] with the given series and series key. */
  public constructor(series: List<Entry>, seriesKey: Any) {
    this.seriesKey = seriesKey
    this.series = series.sortedBy { it.x }
    val yRange = this.series.rangeOfPair { it.low to it.high }
    this.minX = this.series.firstOrNull()?.x ?: 0.0
    this.maxX = this.series.lastOrNull()?.x ?: 0.0
    this.minY = yRange.start
    this.maxY = yRange.endInclusive
    this.hashCode = 31 * this.series.hashCode() + seriesKey.hashCode()
  }

  override val isEmpty: Boolean
    get() = series.isEmpty()

  override fun getXDeltaGcd(): Double = series.getXDeltaGcd()

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is CandlestickCartesianLayerModel &&
        hashCode == other.hashCode &&
        series == other.series &&
        seriesKey == other.seriesKey

  override fun hashCode(): Int = hashCode

  override fun toString(): String =
    "CandlestickCartesianLayerModel(series=$series, seriesKey=$seriesKey, minX=$minX, maxX=$maxX, minY=$minY, maxY=$maxY)"

  /** Represents an [Entry]’s absolute or relative price change. */
  public enum class Change {
    Bullish,
    Bearish,
    Neutral;

    internal companion object {
      private fun forPrices(old: Float, new: Float) =
        when {
          new > old -> Bullish
          new < old -> Bearish
          else -> Neutral
        }

      fun forPrices(old: Number, new: Number) = forPrices(old.toFloat(), new.toFloat())
    }
  }

  /**
   * Houses a single candle’s data.
   *
   * @property x the _x_ value.
   * @property opening the opening price.
   * @property closing the closing price.
   * @property low the low price.
   * @property high the high price.
   * @property absoluteChange represents the absolute price change ([closing] vs. [opening]).
   * @property relativeChange represents the relative price change (this [Entry]’s [closing] vs. the
   *   previous [Entry]’s [closing]).
   */
  public open class Entry
  internal constructor(
    override val x: Double,
    public val opening: Double,
    public val closing: Double,
    public val low: Double,
    public val high: Double,
    public val absoluteChange: Change,
    public val relativeChange: Change,
  ) : CartesianLayerModel.Entry {
    public constructor(
      x: Number,
      opening: Number,
      closing: Number,
      low: Number,
      high: Number,
      absoluteChange: Change,
      relativeChange: Change,
    ) : this(
      x.toDouble(),
      opening.toDouble(),
      closing.toDouble(),
      low.toDouble(),
      high.toDouble(),
      absoluteChange,
      relativeChange,
    )

    init {
      require(low <= opening && low <= closing && low <= high) {
        "`low` can’t be greater than `opening`, `closing`, or `high`."
      }
      require(high >= opening && high >= closing) {
        "`high` can’t be less than `opening` or `closing`."
      }
    }

    override fun equals(other: Any?): Boolean =
      this === other ||
        other is Entry &&
          x == other.x &&
          opening == other.opening &&
          closing == other.closing &&
          low == other.low &&
          high == other.high &&
          absoluteChange == other.absoluteChange &&
          relativeChange == other.relativeChange

    override fun hashCode(): Int {
      var result = x.hashCode()
      result = 31 * result + opening.hashCode()
      result = 31 * result + closing.hashCode()
      result = 31 * result + low.hashCode()
      result = 31 * result + high.hashCode()
      result = 31 * result + absoluteChange.hashCode()
      result = 31 * result + relativeChange.hashCode()
      return result
    }

    override fun toString(): String =
      "Entry(" +
        "x=$x, opening=$opening, closing=$closing, low=$low, high=$high, " +
        "absoluteChange=$absoluteChange, relativeChange=$relativeChange)"
  }

  public companion object {
    private fun series(
      x: Collection<Number>,
      opening: Collection<Number>,
      closing: Collection<Number>,
      low: Collection<Number>,
      high: Collection<Number>,
    ) = buildList {
      var previousClosingPrice: Number? = null
      x.forEachIndexed { index, x ->
        val openingPrice = opening.elementAt(index)
        val closingPrice = closing.elementAt(index)
        add(
          Entry(
            x = x,
            opening = openingPrice,
            closing = closingPrice,
            low = low.elementAt(index),
            high = high.elementAt(index),
            absoluteChange = Change.forPrices(old = openingPrice, new = closingPrice),
            relativeChange = Change.forPrices(old = previousClosingPrice ?: 0, new = closingPrice),
          ),
        )
        previousClosingPrice = closingPrice
      }
    }

    /**
     * Creates a [CandlestickCartesianLayerModel] with the provided _x_ values ([x]), prices, and
     * [seriesKey]. [opening], [closing], [low], and [high] should have the same sizes.
     */
    public fun build(
      x: Collection<Number>,
      opening: Collection<Number>,
      closing: Collection<Number>,
      low: Collection<Number>,
      high: Collection<Number>,
      seriesKey: Any = 0,
    ): CandlestickCartesianLayerModel =
      CandlestickCartesianLayerModel(series(x, opening, closing, low, high), seriesKey)

    /**
     * Creates a [CandlestickCartesianLayerModel] with the provided prices and [seriesKey], using
     * the prices’ indices as the _x_ values. [opening], [closing], [low], and [high] should have
     * the same sizes.
     */
    public fun build(
      opening: Collection<Number>,
      closing: Collection<Number>,
      low: Collection<Number>,
      high: Collection<Number>,
      seriesKey: Any = 0,
    ): CandlestickCartesianLayerModel =
      build(
        x = opening.indices.toList(),
        opening = opening,
        closing = closing,
        low = low,
        high = high,
        seriesKey = seriesKey,
      )
  }
}
