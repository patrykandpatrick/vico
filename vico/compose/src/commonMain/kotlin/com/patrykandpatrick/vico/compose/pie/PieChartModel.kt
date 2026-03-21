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

package com.patrykandpatrick.vico.compose.pie

import androidx.compose.runtime.Immutable
import com.patrykandpatrick.vico.compose.common.data.ExtraStore

/** Stores a [PieChart]’s data. */
@Immutable
public class PieChartModel
internal constructor(public val entries: List<Entry>, public val extraStore: ExtraStore) {
  internal val id: Int = 31 * entries.hashCode() + extraStore.hashCode()

  /** The sum of all [Entry.value]s. */
  public val sum: Float = entries.sumOf { it.value.toDouble() }.toFloat()

  /** Creates an immutable copy of this [PieChartModel] with [extraStore]. */
  public fun copy(extraStore: ExtraStore): PieChartModel = PieChartModel(entries, extraStore)

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is PieChartModel && entries == other.entries && extraStore == other.extraStore

  override fun hashCode(): Int = id

  override fun toString(): String = "PieChartModel(entries=$entries, extraStore=$extraStore)"

  /** Represents a single pie entry. */
  @Immutable
  public class Entry(public val value: Float) {
    init {
      require(value >= 0f) { "Pie entry values must be nonnegative." }
    }

    override fun equals(other: Any?): Boolean =
      this === other || other is Entry && value == other.value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = "Entry(value=$value)"
  }

  /** Stores the minimum amount of data required to create a [PieChartModel]. */
  @Immutable
  public class Partial internal constructor(private val entries: List<Entry>) {
    /** Creates a [PieChartModel]. */
    public fun complete(extraStore: ExtraStore): PieChartModel = PieChartModel(entries, extraStore)

    override fun equals(other: Any?): Boolean =
      this === other || other is Partial && entries == other.entries

    override fun hashCode(): Int = entries.hashCode()
  }

  /** Builds [PieChartModel.Partial] instances. */
  public class Builder internal constructor() {
    private var entries: List<Entry> = emptyList()

    /** Sets the entries from [values]. */
    public fun series(vararg values: Number) {
      series(values.asList())
    }

    /** Sets the entries from [values]. */
    public fun series(values: Iterable<Number>) {
      entries = values.map { Entry(it.toFloat()) }
    }

    internal fun build(): Partial = Partial(entries)
  }

  public companion object {
    /** Creates a [PieChartModel]. */
    public fun build(vararg values: Number): PieChartModel =
      PieChartModel(values.map { Entry(it.toFloat()) }, ExtraStore.Empty)

    /** Creates a [Partial]. */
    public fun partial(vararg values: Number): Partial = Partial(values.map { Entry(it.toFloat()) })
  }
}

/** Sets pie data in a [PieChartModelProducer.Transaction]. */
public fun PieChartModelProducer.Transaction.pieSeries(block: PieChartModel.Builder.() -> Unit) {
  set(PieChartModel.Builder().apply(block).build())
}
