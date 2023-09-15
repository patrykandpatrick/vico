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

package com.patrykandpatrick.vico.core.entry.diff

/**
 * Houses [DrawingModel]s.
 */
@Suppress("UNCHECKED_CAST")
public abstract class DrawingModelStore internal constructor() {
    /**
     * The underlying [Map].
     */
    protected abstract val mapDelegate: Map<Key<*>, DrawingModel<*>>

    /**
     * Used for writing to and reading from [DrawingModelStore]s.
     */
    @Suppress("UNUSED")
    public open class Key<T : DrawingModel<*>>

    /**
     * Returns the value associated with the provided key.
     */
    public open operator fun <T : DrawingModel<*>> get(key: Key<T>): T = mapDelegate[key] as T

    /**
     * Returns the value associated with the provided key, or `null` if there’s no such value.
     */
    public fun <T : DrawingModel<*>> getOrNull(key: Key<T>): T? = mapDelegate[key] as? T

    /**
     * Creates a copy of this [DrawingModelStore].
     */
    public fun copy(): DrawingModelStore = create(mapDelegate)

    public companion object {
        /**
         * Creates a [DrawingModelStore].
         */
        public fun create(mapDelegate: Map<Key<*>, DrawingModel<*>>): DrawingModelStore = object : DrawingModelStore() {
            override val mapDelegate: Map<Key<*>, DrawingModel<*>> = mapDelegate
        }

        /**
         * An empty [DrawingModelStore].
         */
        public val empty: DrawingModelStore = object : DrawingModelStore() {
            override val mapDelegate: Map<Key<*>, DrawingModel<*>> = emptyMap()
        }
    }
}

/**
 * A [DrawingModelStore] subclass that allows for data updates.
 */
public class MutableDrawingModelStore : DrawingModelStore() {
    override val mapDelegate: MutableMap<Key<*>, DrawingModel<*>> = mutableMapOf()

    /**
     * Saves the provided value to this [MutableDrawingModelStore], associating the value with the given key.
     */
    public operator fun <T : DrawingModel<*>> set(key: Key<T>, value: T) {
        mapDelegate[key] = value
    }
}
