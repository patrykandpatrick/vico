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

import com.patrykandpatrick.vico.compose.cartesian.data.mergeKeys
import com.patrykandpatrick.vico.compose.common.lerp
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel

/**
 * Houses pie drawing information: one [SliceInfo] per slice, in drawing order. While slices are
 * exiting, their [SliceInfo]s are in [slices] at their former positions, though their keys are no
 * longer in the [PieChartModel].
 */
internal class PieChartDrawingModel(val slices: List<SliceInfo>) {
  /**
   * Returns the frame at [fraction] of the transition from [from] to this [PieChartDrawingModel].
   * Slices are matched by key. Slices only in [from] exit (the reverse of entering: their sweeps
   * shrink to zero, and they fade out), keeping their positions among their neighbors. At
   * [fraction] 1, this [PieChartDrawingModel] is returned, so exiting slices are dropped.
   */
  internal fun transform(from: PieChartDrawingModel?, fraction: Float): PieChartDrawingModel {
    if (fraction == 1f) return this
    val oldSlices = from?.slices.orEmpty().associateBy { it.key }
    val newSlices = slices.associateBy { it.key }
    val keys = mergeKeys(from?.slices.orEmpty().map { it.key }, slices.map { it.key })
    return PieChartDrawingModel(
      keys.map { key ->
        newSlices[key]?.transform(oldSlices[key], fraction)
          ?: oldSlices.getValue(key).exit(fraction)
      },
    )
  }

  /** Replaces the styles of the slices that are in [chart]’s model with [chart]’s. */
  internal fun withStylesFrom(chart: PieChart): PieChartDrawingModel {
    val entries = chart.model.entries.withIndex().associateBy { it.value.key }
    return PieChartDrawingModel(
      slices.map { info ->
        val entry = entries[info.key] ?: return@map info
        info.copy(
          entry = entry.value,
          index = entry.index,
          slice = chart.sliceProvider.getSlice(entry.value, entry.index),
        )
      },
    )
  }

  /**
   * Houses a slice’s drawing information. [entry], [index], and [slice] are what the slice
   * represents and its style; they’re used to draw the slice while it’s exiting, when it’s no
   * longer in the [PieChartModel] (and the [PieChart.SliceProvider] isn’t asked about it).
   */
  internal class SliceInfo(
    val degrees: Float,
    val sliceOpacity: Float = 1f,
    val labelOpacity: Float = 1f,
    val key: Any = 0,
    val entry: PieChartModel.Entry? = null,
    val index: Int = 0,
    val slice: PieChart.Slice? = null,
  ) {
    /** Interpolates from [from] (or from nothing, for an entering slice) to this [SliceInfo]. */
    internal fun transform(from: SliceInfo?, fraction: Float): SliceInfo {
      val targetOpacity = if (degrees == 0f) 0f else 1f
      return copy(
        degrees = (from?.degrees ?: 0f).lerp(degrees, fraction),
        sliceOpacity = (from?.sliceOpacity ?: 0f).lerp(targetOpacity, fraction),
        labelOpacity = (from?.labelOpacity ?: 0f).lerp(targetOpacity, fraction),
      )
    }

    /** Returns this exiting [SliceInfo] at [fraction] of its exit, from its current state. */
    internal fun exit(fraction: Float): SliceInfo =
      copy(
        degrees = degrees.lerp(0f, fraction),
        sliceOpacity = sliceOpacity.lerp(0f, fraction),
        labelOpacity = labelOpacity.lerp(0f, fraction),
      )

    internal fun copy(
      degrees: Float = this.degrees,
      sliceOpacity: Float = this.sliceOpacity,
      labelOpacity: Float = this.labelOpacity,
      entry: PieChartModel.Entry? = this.entry,
      index: Int = this.index,
      slice: PieChart.Slice? = this.slice,
    ): SliceInfo = SliceInfo(degrees, sliceOpacity, labelOpacity, key, entry, index, slice)

    override fun equals(other: Any?): Boolean =
      this === other ||
        other is SliceInfo &&
          degrees == other.degrees &&
          sliceOpacity == other.sliceOpacity &&
          labelOpacity == other.labelOpacity &&
          key == other.key &&
          entry == other.entry &&
          index == other.index &&
          slice == other.slice

    override fun hashCode(): Int {
      var result = degrees.hashCode()
      result = 31 * result + sliceOpacity.hashCode()
      result = 31 * result + labelOpacity.hashCode()
      result = 31 * result + key.hashCode()
      result = 31 * result + entry.hashCode()
      result = 31 * result + index
      result = 31 * result + slice.hashCode()
      return result
    }

    override fun toString(): String =
      "SliceInfo(key=$key, degrees=$degrees, sliceOpacity=$sliceOpacity, labelOpacity=$labelOpacity)"
  }

  override fun equals(other: Any?): Boolean =
    this === other || other is PieChartDrawingModel && slices == other.slices

  override fun hashCode(): Int = slices.hashCode()

  override fun toString(): String = "PieChartDrawingModel(slices=$slices)"
}

/**
 * Creates the drawing model for this [PieChart]’s model, resolving each slice’s style via its
 * [PieChart.SliceProvider].
 */
internal fun PieChart.createDrawingModel(): PieChartDrawingModel =
  PieChartDrawingModel(
    model.entries.mapIndexed { index, entry ->
      val degrees = if (model.sum == 0f) 0f else entry.value / model.sum * 360f
      val opacity = if (degrees == 0f) 0f else 1f
      PieChartDrawingModel.SliceInfo(
        degrees = degrees,
        sliceOpacity = opacity,
        labelOpacity = opacity,
        key = entry.key,
        entry = entry,
        index = index,
        slice = sliceProvider.getSlice(entry, index),
      )
    },
  )
