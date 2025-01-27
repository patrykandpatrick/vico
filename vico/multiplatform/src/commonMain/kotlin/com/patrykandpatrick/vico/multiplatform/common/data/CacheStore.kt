/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.multiplatform.common.data

/** Caches data. */
public class CacheStore internal constructor() {
  private var map = mutableMapOf<String, Any>()
  private var purgedMap = mutableMapOf<String, Any>()

  /**
   * Retrieves the value associated with the key belonging to the specified namespace and matching
   * the given components. If there’s no such value, `null` is returned.
   */
  public fun <T : Any> getOrNull(keyNamespace: KeyNamespace, vararg keyComponents: Any?): T? {
    val key = keyNamespace.getKey(*keyComponents)
    val value = map[key]
    if (value != null) purgedMap[key] = value
    @Suppress("UNCHECKED_CAST")
    return value as T?
  }

  /** Caches [value]. */
  public operator fun set(keyNamespace: KeyNamespace, vararg keyComponents: Any?, value: Any) {
    val key = keyNamespace.getKey(*keyComponents)
    map[key] = value
    purgedMap[key] = value
  }

  /**
   * Retrieves the value associated with the key belonging to the specified namespace and matching
   * the given components. If there’s no such value, [value] is called, and its result is cached and
   * returned.
   */
  public fun <T : Any> getOrSet(
    keyNamespace: KeyNamespace,
    vararg keyComponents: Any?,
    value: () -> T,
  ): T =
    getOrNull(keyNamespace, *keyComponents)
      ?: value().also { set(keyNamespace, *keyComponents, value = it) }

  internal fun purge() {
    map = purgedMap
    purgedMap = mutableMapOf()
  }

  /** Identifies a key namespace. These namespaces help prevent interscope key collisions. */
  public class KeyNamespace {
    internal fun getKey(vararg components: Any?) =
      components.joinToString(prefix = "${hashCode()}, ")
  }
}
