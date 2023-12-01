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

import com.patrykandpatrick.vico.core.chart.layer.CartesianLayer
import com.patrykandpatrick.vico.core.extension.gcdWith
import com.patrykandpatrick.vico.core.model.drawing.DrawingModel
import kotlin.math.abs

/**
 * Stores a [CartesianLayer]’s data.
 */
public interface CartesianLayerModel {
    /**
     * Identifies this [CartesianLayerModel].
     */
    public val id: Int

    /**
     * The minimum _x_ value.
     */
    public val minX: Float

    /**
     * The maximum _x_ value.
     */
    public val maxX: Float

    /**
     * The minimum _y_ value.
     */
    public val minY: Float

    /**
     * The maximum _y_ value.
     */
    public val maxY: Float

    /**
     * The greatest common divisor of the _x_ values’ differences.
     */
    public val xDeltaGcd: Float

    /**
     * Stores auxiliary data, including [DrawingModel]s.
     */
    public val extraStore: ExtraStore

    /**
     * Creates a copy of this [CartesianLayerModel] with the given [ExtraStore].
     */
    public fun copy(extraStore: ExtraStore): CartesianLayerModel

    /**
     * Represents a single entity in a [CartesianLayerModel].
     */
    public interface Entry {
        /**
         * The _x_ coordinate.
         */
        public val x: Float
    }

    /**
     * Stores the minimum amount of data required to create a [CartesianLayerModel] and facilitates this creation.
     */
    public interface Partial {
        /**
         * Creates a full [CartesianLayerModel] with the given [ExtraStore] from this [Partial].
         */
        public fun complete(extraStore: ExtraStore = ExtraStore.empty): CartesianLayerModel
    }
}

internal fun Iterable<CartesianLayerModel.Entry>.getXDeltaGcd() =
    zipWithNext { firstEntry, secondEntry -> abs(secondEntry.x - firstEntry.x) }
        .fold<Float, Float?>(null) { gcd, delta -> gcd?.gcdWith(delta) ?: delta }
        ?: 1f

internal fun <T : CartesianLayerModel.Entry> Iterable<T>.forEachInIndexed(
    range: ClosedFloatingPointRange<Float>,
    action: (Int, T) -> Unit,
) {
    var index = 0
    forEach { if (it.x in range) action(index++, it) }
}

internal fun <T : CartesianLayerModel.Entry> List<T>.forEachInIndexed(
    range: ClosedFloatingPointRange<Float>,
    action: (Int, T, T?) -> Unit,
) {
    forEachInIndexed(range) { index, entry ->
        action(index, entry, getOrNull(index + 1)?.takeIf { it.x in range })
    }
}
