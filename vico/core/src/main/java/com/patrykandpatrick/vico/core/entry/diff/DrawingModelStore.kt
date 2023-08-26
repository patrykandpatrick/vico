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

@file:Suppress("UNCHECKED_CAST")

package com.patrykandpatrick.vico.core.entry.diff

public abstract class DrawingModelStore internal constructor() {

    protected abstract val mapDelegate: Map<Key<*>, DrawingModel<*>>

    public open class Key<T : DrawingInfo>

    public open operator fun <T : DrawingInfo> get(key: Key<T>): DrawingModel<T>? =
        mapDelegate[key] as? DrawingModel<T>

    public companion object Empty : DrawingModelStore() {
        override val mapDelegate: Map<Key<*>, DrawingModel<*>> = emptyMap()
    }
}

public class MutableDrawingModelStore : DrawingModelStore() {

    override val mapDelegate: MutableMap<Key<*>, DrawingModel<*>> = mutableMapOf()

    public operator fun <T : DrawingInfo> set(key: Key<T>, value: DrawingModel<T>) {
        mapDelegate[key] = value
    }
}
