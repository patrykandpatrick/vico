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

import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.rangeOfPair

/** Stores a [CandlestickCartesianLayer]’s data. */
public class CandlestickCartesianLayerModel : CartesianLayerModel {
  /** The series (list of [Entry] instances). */
  public val series: List<Entry>

  override val id: Int

  override val minX: Double

  override val maxX: Double

  override val minY: Double

  override val maxY: Double

  override val extraStore: ExtraStore

  public constructor(series: List<Entry>) : this(series, ExtraStore.Empty)

  private constructor(series: List<Entry>, extraStore: ExtraStore) {
    require(series.isNotEmpty()) { "Series can’t be empty." }
    this.series = series.sortedBy { it.x }
    val yRange = this.series.rangeOfPair { it.low to it.high }
    this.id = series.hashCode()
    this.minX = this.series.first().x
    this.maxX = this.series.last().x
    this.minY = yRange.start
    this.maxY = yRange.endInclusive
    this.extraStore = extraStore
  }

  private constructor(
    series: List<Entry>,
    id: Int,
    minX: Double,
    maxX: Double,
    minY: Double,
    maxY: Double,
    extraStore: ExtraStore,
  ) {
    this.series = series
    this.id = id
    this.minX = minX
    this.maxX = maxX
    this.minY = minY
    this.maxY = maxY
    this.extraStore = extraStore
  }

  override fun getXDeltaGcd(): Double = series.getXDeltaGcd()

  override fun copy(extraStore: ExtraStore): CartesianLayerModel =
    CandlestickCartesianLayerModel(series, id, minX, maxX, minY, maxY, extraStore)

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is CandlestickCartesianLayerModel &&
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
  }

  /**
   * Stores the minimum amount of data required to create a [CandlestickCartesianLayerModel] and
   * facilitates this creation.
   */
  public class Partial(private val series: List<Entry>) : CartesianLayerModel.Partial {
    override fun complete(extraStore: ExtraStore): CartesianLayerModel =
      CandlestickCartesianLayerModel(series, extraStore)
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
          )
        )
        previousClosingPrice = closingPrice
      }
    }

    /**
     * Creates a [CandlestickCartesianLayerModel] with the provided _x_ values ([x]) and prices.
     * [opening], [closing], [low], and [high] should have the same sizes.
     */
    public fun build(
      x: Collection<Number>,
      opening: Collection<Number>,
      closing: Collection<Number>,
      low: Collection<Number>,
      high: Collection<Number>,
    ): CandlestickCartesianLayerModel =
      CandlestickCartesianLayerModel(series(x, opening, closing, low, high))

    /**
     * Creates a [CandlestickCartesianLayerModel] with the provided prices, using their indices as
     * the _x_ values. [opening], [closing], [low], and [high] should have the same sizes.
     */
    public fun build(
      opening: Collection<Number>,
      closing: Collection<Number>,
      low: Collection<Number>,
      high: Collection<Number>,
    ): CandlestickCartesianLayerModel =
      build(
        x = opening.indices.toList(),
        opening = opening,
        closing = closing,
        low = low,
        high = high,
      )

    /**
     * Creates a [Partial] with the provided _x_ values ([x]) and prices. [opening], [closing],
     * [low], and [high] should have the same sizes.
     */
    public fun partial(
      x: Collection<Number>,
      opening: Collection<Number>,
      closing: Collection<Number>,
      low: Collection<Number>,
      high: Collection<Number>,
    ): Partial = Partial(series(x, opening, closing, low, high))

    /**
     * Creates a [Partial] with the provided prices, using their indices as the _x_ values.
     * [opening], [closing], [low], and [high] should have the same sizes.
     */
    public fun partial(
      opening: Collection<Number>,
      closing: Collection<Number>,
      low: Collection<Number>,
      high: Collection<Number>,
    ): Partial =
      partial(
        x = opening.indices.toList(),
        opening = opening,
        closing = closing,
        low = low,
        high = high,
      )
  }
}

/**
 * Creates a [CandlestickCartesianLayerModel.Partial] with the provided _x_ values ([x]) and prices
 * and adds it to the [CartesianChartModelProducer.Transaction]’s [CartesianLayerModel.Partial]
 * list. [opening], [closing], [low], and [high] should have the same sizes.
 */
public fun CartesianChartModelProducer.Transaction.candlestickSeries(
  x: Collection<Number>,
  opening: Collection<Number>,
  closing: Collection<Number>,
  low: Collection<Number>,
  high: Collection<Number>,
) {
  add(CandlestickCartesianLayerModel.partial(x, opening, closing, low, high))
}

/**
 * Creates a [CandlestickCartesianLayerModel.Partial] with the provided prices, using their indices
 * as the _x_ values, and adds it to the [CartesianChartModelProducer.Transaction]’s
 * [CartesianLayerModel.Partial] list. [opening], [closing], [low], and [high] should have the same
 * sizes.
 */
public fun CartesianChartModelProducer.Transaction.candlestickSeries(
  opening: Collection<Number>,
  closing: Collection<Number>,
  low: Collection<Number>,
  high: Collection<Number>,
) {
  candlestickSeries(
    x = opening.indices.toList(),
    opening = opening,
    closing = closing,
    low = low,
    high = high,
  )
}
