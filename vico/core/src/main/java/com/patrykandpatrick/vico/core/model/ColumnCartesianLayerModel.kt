/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.model

import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.extension.rangeOf
import com.patrykandpatrick.vico.core.extension.rangeOfPair

/**
 * Stores a [ColumnCartesianLayer]’s data.
 */
public class ColumnCartesianLayerModel : CartesianLayerModel {
    /**
     * The series (lists of [Entry] instances).
     */
    public val series: List<List<Entry>>

    override val id: Int

    override val minX: Float

    override val maxX: Float

    override val minY: Float

    override val maxY: Float

    /**
     * The minimum sum of all _y_ values associated with a given _x_ value.
     */
    public val minAggregateY: Float

    /**
     * The maximum sum of all _y_ values associated with a given _x_ value.
     */
    public val maxAggregateY: Float

    override val xDeltaGcd: Float

    override val extraStore: ExtraStore

    public constructor(series: List<List<Entry>>) : this(series, ExtraStore.empty)

    private constructor(series: List<List<Entry>>, extraStore: ExtraStore) {
        require(series.isNotEmpty()) { "At least one series should be added." }
        this.series =
            series.map { entries ->
                require(entries.isNotEmpty()) { "Series can’t be empty." }
                entries.sortedBy { entry -> entry.x }
            }
        val entries = this.series.flatten()
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
        this.xDeltaGcd = entries.getXDeltaGcd()
        this.extraStore = extraStore
    }

    private constructor(
        series: List<List<Entry>>,
        id: Int,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float,
        minAggregateY: Float,
        maxAggregateY: Float,
        xDeltaGcd: Float,
        extraStore: ExtraStore,
    ) {
        this.series = series
        this.id = id
        this.minX = minX
        this.maxX = maxX
        this.minY = minY
        this.maxY = maxY
        this.minAggregateY = minAggregateY
        this.maxAggregateY = maxAggregateY
        this.xDeltaGcd = xDeltaGcd
        this.extraStore = extraStore
    }

    override fun copy(extraStore: ExtraStore): CartesianLayerModel =
        ColumnCartesianLayerModel(
            series,
            id,
            minX,
            maxX,
            minY,
            maxY,
            minAggregateY,
            maxAggregateY,
            xDeltaGcd,
            extraStore,
        )

    /**
     * Represents a column of height [y] at [x].
     */
    public class Entry internal constructor(override val x: Float, public val y: Float) : CartesianLayerModel.Entry {
        public constructor(x: Number, y: Number) : this(x.toFloat(), y.toFloat())
    }

    /**
     * Stores the minimum amount of data required to create a [ColumnCartesianLayerModel] and facilitates this creation.
     */
    public class Partial(private val series: List<List<Entry>>) : CartesianLayerModel.Partial {
        override fun complete(extraStore: ExtraStore): CartesianLayerModel =
            ColumnCartesianLayerModel(series, extraStore)
    }

    /**
     * Facilitates the creation of [ColumnCartesianLayerModel]s and [Partial]s.
     */
    public class BuilderScope {
        internal val series = mutableListOf<List<Entry>>()

        /**
         * Adds a series with the provided _x_ values ([x]) and _y_ values ([y]). [x] and [y] should have the same size.
         */
        public fun series(
            x: Collection<Number>,
            y: Collection<Number>,
        ) {
            series.add(x.zip(y, ColumnCartesianLayerModel::Entry))
        }

        /**
         * Adds a series with the provided _y_ values ([y]), using their indices as the _x_ values.
         */
        public fun series(y: Collection<Number>) {
            series(y.indices.toList(), y)
        }

        /**
         * Adds a series with the provided _y_ values ([y]), using their indices as the _x_ values.
         */
        public fun series(vararg y: Number) {
            series(y.toList())
        }
    }

    public companion object {
        /**
         * Creates a [ColumnCartesianLayerModel].
         */
        public fun build(block: BuilderScope.() -> Unit): ColumnCartesianLayerModel =
            ColumnCartesianLayerModel(BuilderScope().apply(block).series)

        /**
         * Creates a [Partial].
         */
        public fun partial(block: BuilderScope.() -> Unit): Partial = Partial(BuilderScope().apply(block).series)
    }
}

/**
 * Calls [block] to create a [ColumnCartesianLayerModel.Partial] and adds it to the
 * [CartesianChartModelProducer.Transaction]’s [CartesianLayerModel.Partial] list.
 */
public fun CartesianChartModelProducer.Transaction.columnSeries(
    block: ColumnCartesianLayerModel.BuilderScope.() -> Unit,
) {
    add(ColumnCartesianLayerModel.partial(block))
}

internal fun Iterable<ColumnCartesianLayerModel.Entry>.getAggregateYRange() =
    fold(mutableMapOf<Float, Pair<Float, Float>>()) { map, entry ->
        val (negativeY, positiveY) = map.getOrElse(entry.x) { 0f to 0f }
        map[entry.x] = if (entry.y < 0f) negativeY + entry.y to positiveY else negativeY to positiveY + entry.y
        map
    }.values.rangeOfPair { it }
