/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.context

import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import kotlin.collections.Map

/**
 * An abstraction layer over [Map] used by [MeasureContext] and [ChartDrawContext] to store and retrieve data.
 *
 * Extras are kept in memory while measuring or drawing is taking place. Afterwards, they are removed.
 */
public interface Extras {

    /**
     * Saves an extra.
     *
     * @param key the extra’s unique identifier.
     * @param value the extra’s value.
     */
    public fun putExtra(key: Any, value: Any)

    /**
     * Checks whether an extra with the given key exists.
     */
    public fun hasExtra(key: Any): Boolean

    /**
     * Retrieves the value of the extra with the given key, unless no such extra exists.
     *
     * @see consumeExtra
     */
    public fun <T> getExtra(key: Any): T

    /**
     * Retrieves the value of the extra with the given key, unless no such extra exists. Once the value of the extra is
     * retrieved, the extra is removed. Use [getExtra] to prevent the extra from being removed.
     *
     * @see getExtra
     */
    public fun <T> consumeExtra(key: Any): T

    /**
     * Operator function for [putExtra].
     *
     * @see putExtra
     */
    public operator fun set(key: Any, value: Any): Unit = putExtra(key, value)

    /**
     * Operator function for [consumeExtra].
     *
     * @see consumeExtra
     */
    public operator fun <T> get(key: Any): T = consumeExtra(key)

    /**
     * Removes all stored extras.
     */
    public fun clearExtras()
}
