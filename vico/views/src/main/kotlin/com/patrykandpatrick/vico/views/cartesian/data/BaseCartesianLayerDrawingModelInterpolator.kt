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

package com.patrykandpatrick.vico.views.cartesian.data

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal abstract class BaseCartesianLayerDrawingModelInterpolator<
  T : CartesianLayerDrawingModel.Entry,
  R : CartesianLayerDrawingModel<T>,
> : CartesianLayerDrawingModelInterpolator<T, R> {
  private var transformationMaps = emptyList<Map<Double, TransformationEntry<T>>>()
  protected var oldDrawingModel: R? = null
    private set

  protected var newDrawingModel: R? = null
    private set

  override fun setModels(old: R?, new: R?) {
    synchronized(this) {
      oldDrawingModel = old
      newDrawingModel = new
      updateTransformationMaps()
    }
  }

  override suspend fun transform(fraction: Float): R? {
    val new = newDrawingModel ?: return null
    val old = oldDrawingModel
    return transform(
      old = old,
      new = new,
      entries =
        transformationMaps.mapNotNull { map ->
          map
            .map { (x, entry) ->
              currentCoroutineContext().ensureActive()
              x to transformEntry(oldModel = old, old = entry.old, new = entry.new, fraction)
            }
            .takeIf { entries -> entries.isNotEmpty() }
            ?.toMap()
        },
      fraction = fraction,
    )
  }

  protected abstract fun transform(
    old: R?,
    new: R,
    entries: List<Map<Double, T>>,
    fraction: Float,
  ): R

  protected abstract fun transformEntry(oldModel: R?, old: T?, new: T, fraction: Float): T

  private fun updateTransformationMaps() {
    val oldEntriesByKey =
      oldDrawingModel?.seriesKeys?.zip(oldDrawingModel.orEmpty())?.toMap().orEmpty()
    transformationMaps =
      newDrawingModel
        ?.seriesKeys
        ?.zip(newDrawingModel.orEmpty())
        ?.map { (key, newEntries) ->
          val oldEntries = oldEntriesByKey[key]
          newEntries.mapValues { (x, entry) -> TransformationEntry(oldEntries?.get(x), entry) }
        }
        .orEmpty()
  }

  private class TransformationEntry<T : CartesianLayerDrawingModel.Entry>(val old: T?, val new: T)
}
