/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.pie.data

import androidx.compose.runtime.Immutable
import com.patrykandpatrick.vico.compose.pie.PieChart

/**
 * Stores a [PieChart]’s data. [PieChartModel]s are immutable values and can be created anywhere,
 * including off the main thread. Use [build] to create instances.
 *
 * Each [Entry] has a [key][Entry.key], which identifies its slice across data updates: a slice
 * whose key disappears animates out, one whose key appears animates in, and the others animate to
 * their new angles. By default, the keys are the entries’ indices, so slices are matched by
 * position; pass a map to [build] to match them by key instead.
 */
@Immutable
public class PieChartModel internal constructor(entries: List<Entry>) {
  /** The entries. */
  public val entries: List<Entry> = entries.toList()

  private val hashCode: Int = this.entries.hashCode()

  /** The sum of all [Entry.value]s. */
  public val sum: Float = entries.sumOf { it.value.toDouble() }.toFloat()

  init {
    val keys = HashSet<Any>()
    this.entries.forEach { entry ->
      require(keys.add(entry.key)) { "Pie entry keys must be unique, but `${entry.key}` repeats." }
    }
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is PieChartModel && hashCode == other.hashCode && entries == other.entries

  override fun hashCode(): Int = hashCode

  override fun toString(): String = "PieChartModel(entries=$entries)"

  /**
   * Represents a single pie entry with the given [value]. [key] identifies the entry’s slice across
   * data updates (see [PieChartModel]); by default, it’s the entry’s index.
   */
  @Immutable
  public class Entry internal constructor(public val value: Float, public val key: Any) {
    init {
      require(value >= 0f) { "Pie entry values must be nonnegative." }
    }

    override fun equals(other: Any?): Boolean =
      this === other || other is Entry && value == other.value && key == other.key

    override fun hashCode(): Int = 31 * value.hashCode() + key.hashCode()

    override fun toString(): String = "Entry(value=$value, key=$key)"
  }

  public companion object {
    /** Creates a [PieChartModel] with one [Entry] per value, keyed by index. */
    public fun build(vararg values: Number): PieChartModel = build(values.asList())

    /** Creates a [PieChartModel] with one [Entry] per value, keyed by index. */
    public fun build(values: Iterable<Number>): PieChartModel =
      PieChartModel(values.mapIndexed { index, value -> Entry(value.toFloat(), index) })

    /**
     * Creates a [PieChartModel] with one [Entry] per map entry, in the map’s iteration order (e.g.,
     * the order passed to `mapOf`). The map keys become the [Entry.key]s, so slices are matched by
     * key across data updates.
     */
    public fun build(values: Map<out Any, Number>): PieChartModel =
      PieChartModel(values.map { (key, value) -> Entry(value.toFloat(), key) })
  }
}
