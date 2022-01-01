/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.context

/**
 * An abstraction layer over [kotlin.collections.Map], used to store and retrieve data for:
 * - Measuring operation, used within [pl.patrykgoworowski.vico.core.context.MeasureContext].
 * - Draw operation, used within [pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext].
 *
 * Extras will be available downstream the measuring, or drawing operation. Once the operation is complete,
 * the data will be lost.
 *
 * Once the data for given key is retrieved, it is being cleared from underlying map.
 */
public interface Extras {

    /**
     * Adds a [value] to local store.
     * @param key Used to retrieve given [value] from the store.
     * @param value Can be retrieved from the store using the [key].
     */
    public fun putExtra(key: Any, value: Any)

    /**
     * Check whether local store contains a value for given [key].
     */
    public fun hasExtra(key: Any): Boolean

    /**
     * Retrieves the stored value for the [key], if it exists and wasn’t already consumed with [consumeExtra].
     * @see consumeExtra
     */
    public fun <T> getExtra(key: Any): T

    /**
     * Retrieves the stored value for the [key], if it exists and wasn’t already consumed.
     * The value can be read only once, and will be cleared after this function is called.
     * For non-consumable value retrieval use [getExtra].
     * @see getExtra
     */
    public fun <T> consumeExtra(key: Any): T

    /**
     * Convenience operator fun for [putExtra].
     * @see putExtra
     */
    public operator fun set(key: Any, value: Any): Unit = putExtra(key, value)

    /**
     * Convenience operator fun for [consumeExtra].
     * @see [consumeExtra]
     */
    public operator fun <T> get(key: Any): T = consumeExtra(key)

    /**
     * Removes all stored extras.
     */
    public fun clearExtras()
}
