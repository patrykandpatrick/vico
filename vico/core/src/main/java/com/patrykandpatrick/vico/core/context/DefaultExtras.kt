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

private const val INITIAL_CAPACITY = 8

/**
 * The default implementation of [Extras].
 * @see Extras
 */
@Suppress("UNCHECKED_CAST")
public class DefaultExtras : Extras {

    private val extrasMap: HashMap<Any, Any> = HashMap(INITIAL_CAPACITY)

    public override fun putExtra(key: Any, value: Any) {
        extrasMap[key] = value
    }

    public override fun hasExtra(key: Any): Boolean = extrasMap.containsKey(key)

    override fun <T> getExtra(key: Any): T = extrasMap[key] as T

    public override fun <T> consumeExtra(key: Any): T = getExtra<T>(key).also {
        extrasMap.remove(key)
    }

    override fun clearExtras() {
        extrasMap.clear()
    }
}
