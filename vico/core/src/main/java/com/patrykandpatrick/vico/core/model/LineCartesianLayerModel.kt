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

import com.patrykandpatrick.vico.core.chart.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.extension.rangeOf
import com.patrykandpatrick.vico.core.extension.rangeOfPair

/**
 * Stores a [LineCartesianLayer]’s data.
 */
public class LineCartesianLayerModel : CartesianLayerModel {
    /**
     * The series (lists of [Entry] instances).
     */
    public val series: List<List<Entry>>

    override val id: Int

    override val minX: Float

    override val maxX: Float

    override val minY: Float

    override val maxY: Float

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
        this.id = this.series.hashCode()
        this.minX = xRange.start
        this.maxX = xRange.endInclusive
        this.minY = yRange.start
        this.maxY = yRange.endInclusive
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
        xDeltaGcd: Float,
        extraStore: ExtraStore,
    ) {
        this.series = series
        this.id = id
        this.minX = minX
        this.maxX = maxX
        this.minY = minY
        this.maxY = maxY
        this.xDeltaGcd = xDeltaGcd
        this.extraStore = extraStore
    }

    override fun copy(extraStore: ExtraStore): CartesianLayerModel =
        LineCartesianLayerModel(series, id, minX, maxX, minY, maxY, xDeltaGcd, extraStore)

    /**
     * Represents a line node at ([x], [y]).
     */
    public class Entry internal constructor(override val x: Float, public val y: Float) : CartesianLayerModel.Entry {
        public constructor(x: Number, y: Number) : this(x.toFloat(), y.toFloat())
    }

    /**
     * Stores the minimum amount of data required to create a [LineCartesianLayerModel] and facilitates this creation.
     */
    public class Partial(private val series: List<List<Entry>>) : CartesianLayerModel.Partial {
        override fun complete(extraStore: ExtraStore): CartesianLayerModel = LineCartesianLayerModel(series, extraStore)
    }

    /**
     * Facilitates the creation of [LineCartesianLayerModel]s and [Partial]s.
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
            series.add(x.zip(y, LineCartesianLayerModel::Entry))
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
         * Creates a [LineCartesianLayerModel].
         */
        public fun build(block: BuilderScope.() -> Unit): LineCartesianLayerModel =
            LineCartesianLayerModel(BuilderScope().apply(block).series)

        /**
         * Creates a [Partial].
         */
        public fun partial(block: BuilderScope.() -> Unit): Partial = Partial(BuilderScope().apply(block).series)
    }
}

/**
 * Calls [block] to create a [LineCartesianLayerModel.Partial] and adds it to the
 * [CartesianChartModelProducer.Transaction]’s [CartesianLayerModel.Partial] list.
 */
public fun CartesianChartModelProducer.Transaction.lineSeries(block: LineCartesianLayerModel.BuilderScope.() -> Unit) {
    add(LineCartesianLayerModel.partial(block))
}
