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

package com.patrykandpatrick.vico.core.model

import com.patrykandpatrick.vico.core.extension.sumOf
import com.patrykandpatrick.vico.core.model.drawing.DrawingModel
import com.patrykandpatrick.vico.core.pie.PieChart

/**
 * Stores a [PieChart]’s data.
 */
public open class PieModel {
    /**
     * Identifies this [PieModel].
     */
    public val id: Int

    /**
     * The pie chart entries.
     */
    public val entries: List<Entry>

    /**
     * The sum of all values of the [entries].
     */
    public val sumOfValues: Float

    /**
     * Stores auxiliary data, including [DrawingModel]s.
     */
    public val extraStore: ExtraStore

    public constructor(series: List<Entry>) : this(series, ExtraStore.empty)

    protected constructor(series: List<Entry>, extraStore: ExtraStore) {
        this.entries = series
        this.id = series.hashCode()
        this.sumOfValues = series.sumOf { it.value }
        this.extraStore = extraStore
    }

    protected constructor(id: Int, entries: List<Entry>, sumOfValues: Float, extraStore: ExtraStore) {
        this.id = id
        this.entries = entries
        this.sumOfValues = sumOfValues
        this.extraStore = extraStore
    }

    /**
     * Creates a copy of this [PieModel] with the given [ExtraStore].
     */
    public fun copy(extraStore: ExtraStore): PieModel = PieModel(id, entries, sumOfValues, extraStore)

    /**
     * Represents the pie slice value and its label.
     */
    public open class Entry(
        public open val value: Float,
    ) {
        override fun equals(other: Any?): Boolean =
            when {
                this === other -> true
                other !is Entry -> false
                else -> value == other.value
            }

        override fun hashCode(): Int = value.hashCode()
    }

    /**
     * Stores the minimum amount of data required to create a [PieModel] and facilitates this creation.
     */
    public open class Partial(protected val series: List<Entry>) {
        public open fun complete(extraStore: ExtraStore): PieModel = PieModel(series, extraStore)
    }

    public companion object {
        /**
         * Creates a [PieModel].
         */
        public fun build(series: List<Entry>): PieModel = PieModel(series)

        /**
         * Creates a [PieModel].
         */
        public fun build(vararg series: Entry): PieModel = PieModel(series.toList())

        /**
         * Creates a [PieModel].
         */
        public fun build(vararg series: Float): PieModel = PieModel(series.map(::Entry))

        /**
         * Creates a [Partial].
         */
        public fun partial(series: List<Entry>): Partial = Partial(series)
    }
}
