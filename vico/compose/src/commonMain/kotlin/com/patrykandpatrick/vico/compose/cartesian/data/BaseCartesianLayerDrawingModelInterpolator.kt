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

package com.patrykandpatrick.vico.compose.cartesian.data

import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerDrawingModelInterpolator.Transition

/**
 * Pairs the entries of two [CartesianLayerDrawingModel]s by series key and _x_-value and
 * interpolates each pair via [transformEntry]. Series that are only in the old drawing model are
 * exiting: they’re kept (at their former positions in the series order) and animated as the reverse
 * of an entering series, and they’re absent from the final frame.
 */
internal abstract class BaseCartesianLayerDrawingModelInterpolator<
  T : CartesianLayerDrawingModel.Entry,
  R : CartesianLayerDrawingModel<T>,
> : CartesianLayerDrawingModelInterpolator<R> {
  override fun createTransition(old: R?, new: R): Transition<R> {
    val oldEntriesByKey = old?.seriesKeys?.zip(old)?.toMap().orEmpty()
    val keys = mergeKeys(old?.seriesKeys.orEmpty(), new.seriesKeys)
    val newEntriesByKey = new.seriesKeys.zip(new).toMap()
    val series = keys.map { key ->
      val newEntries = newEntriesByKey[key]
      val oldEntries = oldEntriesByKey[key]
      if (newEntries != null) {
        SeriesTransition(newEntries.mapValues { (x, entry) -> oldEntries?.get(x) to entry }, false)
      } else {
        SeriesTransition(oldEntries!!.mapValues { (_, entry) -> null to entry }, true)
      }
    }
    val newIndices = new.seriesKeys.withIndex().associate { (index, key) -> key to index }
    val oldIndices =
      old?.seriesKeys?.withIndex()?.associate { (index, key) -> key to index }.orEmpty()
    // Exiting series keep the layer they came from, which styles and lays them out.
    val sources = keys.map { key ->
      newIndices[key]?.let(new::getSeriesSource)
        ?: oldIndices[key]?.let { old?.getSeriesSource(it) }
    }
    return Transition { fraction ->
      if (fraction == 1f) {
        new
      } else {
        transform(old, new, keys, series.map { it.interpolate(old, fraction) }, sources, fraction)
      }
    }
  }

  private inner class SeriesTransition(
    val pairs: Map<Double, Pair<T?, T>>,
    val isExiting: Boolean,
  ) {
    fun interpolate(oldModel: R?, fraction: Float): Map<Double, T> =
      if (isExiting) {
        pairs.mapValues { (_, pair) -> transformEntry(oldModel, null, pair.second, 1 - fraction) }
      } else {
        pairs.mapValues { (_, pair) -> transformEntry(oldModel, pair.first, pair.second, fraction) }
      }
  }

  /**
   * Creates an intermediate drawing model with the given series. [seriesKeys] includes the keys of
   * any exiting series. [seriesSources] holds the series’ source layers (see
   * [CartesianLayerDrawingModel.seriesSources]).
   */
  protected abstract fun transform(
    old: R?,
    new: R,
    seriesKeys: List<Any>,
    entries: List<Map<Double, T>>,
    seriesSources: List<Any?>,
    fraction: Float,
  ): R

  protected abstract fun transformEntry(oldModel: R?, old: T?, new: T, fraction: Float): T
}

/**
 * Merges [old] and [new], keeping the order of [new] and inserting each key that’s only in [old]
 * after its predecessor in [old] (or at the start).
 */
internal fun mergeKeys(old: List<Any>, new: List<Any>): List<Any> {
  val newKeys = new.toSet()
  if (old.all { it in newKeys }) return new
  val result = new.toMutableList()
  var insertionIndex = 0
  old.forEach { key ->
    val index = result.indexOf(key)
    if (index != -1) {
      insertionIndex = index + 1
    } else {
      result.add(insertionIndex, key)
      insertionIndex++
    }
  }
  return result
}
