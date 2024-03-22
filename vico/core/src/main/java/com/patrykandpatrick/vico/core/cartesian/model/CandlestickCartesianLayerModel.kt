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

package com.patrykandpatrick.vico.core.cartesian.model

import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.common.ExtraStore
import com.patrykandpatrick.vico.core.common.extension.mapWithPrevious
import com.patrykandpatrick.vico.core.common.extension.orZero
import com.patrykandpatrick.vico.core.common.extension.rangeOf
import com.patrykandpatrick.vico.core.common.extension.rangeOfRanged

/** Stores a [CandlestickCartesianLayer]’s data. */
public class CandlestickCartesianLayerModel : CartesianLayerModel {
    /**
     * The series (lists of [TypedEntry] instances).
     */
    public val series: List<TypedEntry>

    override val id: Int

    override val minX: Float

    override val maxX: Float

    override val minY: Float

    override val maxY: Float

    override val extraStore: ExtraStore

    public constructor(series: List<Entry>) : this(series, ExtraStore.empty)

    private constructor(series: List<Entry>, extraStore: ExtraStore) {
        val entries = series
        val xRange = entries.rangeOf { it.x }
        val yRange = entries.rangeOfRanged { it.yRange }
        this.series = series.mapWithPrevious { previous, current -> TypedEntry(current, previous?.close) }
        this.id = series.hashCode()
        this.minX = xRange.start
        this.maxX = xRange.endInclusive
        this.minY = yRange.start
        this.maxY = yRange.endInclusive
        this.extraStore = extraStore
    }

    private constructor(
        series: List<TypedEntry>,
        id: Int,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float,
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

    override fun getXDeltaGcd(): Float = series.getXDeltaGcd()

    override fun copy(extraStore: ExtraStore): CartesianLayerModel =
        CandlestickCartesianLayerModel(
            series,
            id,
            minX,
            maxX,
            minY,
            maxY,
            extraStore,
        )

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

    /**
     * TODO
     */
    public open class Entry internal constructor(
        override val x: Float,
        public val low: Float,
        public val high: Float,
        public val open: Float,
        public val close: Float,
    ) : CartesianLayerModel.Entry {
        public constructor(
            x: Number,
            low: Number,
            high: Number,
            open: Number,
            close: Number,
        ) : this(x.toFloat(), low.toFloat(), high.toFloat(), open.toFloat(), close.toFloat())

        /**
         * TODO
         */
        public val yRange: ClosedFloatingPointRange<Float>
            get() = minOf(low, open, close)..maxOf(high, open, close)

        override fun equals(other: Any?): Boolean =
            this === other || other is Entry && x == other.x && low == other.low && high == other.high &&
                open == other.open && close == other.close

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + low.hashCode()
            result = 31 * result + high.hashCode()
            result = 31 * result + open.hashCode()
            result = 31 * result + close.hashCode()
            return result
        }
    }

    public open class TypedEntry internal constructor(
        x: Float,
        low: Float,
        high: Float,
        open: Float,
        close: Float,
        public val type: Type,
    ) : Entry(x, low, high, open, close) {
        public constructor(
            entry: Entry,
            previousClose: Float?,
        ) : this(
            x = entry.x,
            low = entry.low,
            high = entry.high,
            open = entry.open,
            close = entry.close,
            type =
                Type.fromValues(
                    previousClose = previousClose,
                    currentClose = entry.close,
                    currentOpen = entry.open,
                ),
        )

        /**
         * TODO
         */
        public data class Type(
            public val absoluteChange: Change,
            public val relativeChange: Change,
        ) {
            public enum class Change {
                Increase,
                Decrease,
                Zero,
                ;

                public companion object {
                    public fun from(
                        a: Float,
                        b: Float,
                    ): Change =
                        when {
                            a < b -> Increase
                            a > b -> Decrease
                            else -> Zero
                        }
                }
            }

            public companion object {
                public fun fromValues(
                    previousClose: Float?,
                    currentClose: Float,
                    currentOpen: Float,
                ): Type =
                    Type(
                        absoluteChange = Change.from(currentClose, currentOpen),
                        relativeChange = Change.from(currentClose, previousClose.orZero),
                    )
            }
        }

        override fun equals(other: Any?): Boolean =
            this === other || other is TypedEntry && super.equals(other) && type == other.type

        override fun hashCode(): Int = 31 * super.hashCode() + type.hashCode()
    }

    /**
     * Stores the minimum amount of data required to create a [CandlestickCartesianLayerModel] and facilitates this creation.
     */
    public class Partial(private val series: List<Entry>) : CartesianLayerModel.Partial {
        override fun complete(extraStore: ExtraStore): CartesianLayerModel =
            CandlestickCartesianLayerModel(series, extraStore)
    }

    public companion object {
        /**
         * Creates a [CandlestickCartesianLayerModel].
         */
        public fun build(series: List<Entry>): CandlestickCartesianLayerModel = CandlestickCartesianLayerModel(series)

        /**
         * Creates a [Partial].
         */
        public fun partial(series: List<Entry>): Partial = Partial(series)
    }
}

/**
 * Creates a [CandlestickCartesianLayerModel.Partial] with [series] and adds it to the
 * [CartesianChartModelProducer.Transaction]’s [CartesianLayerModel.Partial] list.
 */
public fun CartesianChartModelProducer.Transaction.candlestickSeries(
    series: List<CandlestickCartesianLayerModel.Entry>,
) {
    add(CandlestickCartesianLayerModel.partial(series))
}
