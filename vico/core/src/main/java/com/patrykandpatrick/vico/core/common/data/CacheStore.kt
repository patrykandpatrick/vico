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

package com.patrykandpatrick.vico.core.common.data

/** Caches data. */
public class CacheStore {
  private var map = mutableMapOf<String, Any>()
  private var purgedMap = mutableMapOf<String, Any>()

  /**
   * Retrieves the value associated with the key belonging to the specified namespace and matching
   * the given components. If there’s no such value, [value] is called, and its result is cached and
   * returned.
   */
  public fun <T : Any> getOrSet(
    keyNamespace: KeyNamespace,
    vararg keyComponents: Any?,
    value: () -> T,
  ): T {
    val key = keyNamespace.getKey(*keyComponents)
    return (@Suppress("UNCHECKED_CAST") (map[key]?.also { purgedMap[key] = it } as T?))
      ?: value().also { newValue ->
        map[key] = newValue
        purgedMap[key] = newValue
      }
  }

  /**
   * Removes all values that were added before the last call to this function and haven’t been read
   * since the last call. When called for the first time on a given [CacheStore], this function
   * doesn’t remove any values.
   */
  public fun purge() {
    map = purgedMap
    purgedMap = mutableMapOf()
  }

  /** Identifies a key namespace. These namespaces help prevent interscope key collisions. */
  public class KeyNamespace {
    internal fun getKey(vararg components: Any?) =
      components.joinToString(prefix = "${hashCode()}, ")
  }
}
