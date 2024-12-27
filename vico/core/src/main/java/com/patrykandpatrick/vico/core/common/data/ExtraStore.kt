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

/** Houses auxiliary data. */
@Suppress("UNCHECKED_CAST")
public abstract class ExtraStore internal constructor() {
  /** The underlying [Map]. */
  protected abstract val mapDelegate: Map<Key<*>, Any>

  /** Returns the value associated with the provided key. */
  public open operator fun <T : Any> get(key: Key<T>): T = mapDelegate[key] as T

  /** Returns the value associated with the provided key, or `null` if there’s no such value. */
  public fun <T : Any> getOrNull(key: Key<T>): T? = mapDelegate[key] as? T

  internal abstract fun copy(): ExtraStore

  internal abstract fun copyContentTo(destination: MutableMap<Key<*>, Any>)

  internal abstract operator fun plus(other: ExtraStore): ExtraStore

  override fun equals(other: Any?): Boolean =
    this === other || other is ExtraStore && mapDelegate == other.mapDelegate

  override fun hashCode(): Int = mapDelegate.hashCode()

  /** Used for writing to and reading from [ExtraStore]s. */
  @Suppress("UNUSED") public open class Key<T : Any>

  internal companion object {
    val Empty = MutableExtraStore()
  }
}

/** A [ExtraStore] subclass that allows for data updates. */
public class MutableExtraStore internal constructor(mapDelegate: Map<Key<*>, Any>) : ExtraStore() {
  override val mapDelegate: MutableMap<Key<*>, Any> = mapDelegate.toMutableMap()

  /** Creates an empty [MutableExtraStore]. */
  public constructor() : this(emptyMap())

  /**
   * Saves the provided value to this [MutableExtraStore], associating the value with the given key.
   */
  public operator fun <T : Any> set(key: Key<T>, value: T) {
    mapDelegate[key] = value
  }

  /** Removes the value associated with the provided key. */
  public fun remove(key: Key<*>) {
    mapDelegate.remove(key)
  }

  /** Removes all stored values. */
  public fun clear() {
    mapDelegate.clear()
  }

  override fun copy(): ExtraStore = MutableExtraStore(mapDelegate)

  override fun copyContentTo(destination: MutableMap<Key<*>, Any>) {
    destination.putAll(mapDelegate)
  }

  override operator fun plus(other: ExtraStore): ExtraStore =
    MutableExtraStore(
      buildMap {
        putAll(mapDelegate)
        other.copyContentTo(this)
      }
    )
}
