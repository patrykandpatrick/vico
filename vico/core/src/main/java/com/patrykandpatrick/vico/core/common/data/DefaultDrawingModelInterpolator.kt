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

import com.patrykandpatrick.vico.core.common.orZero
import kotlin.math.max
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** The default [DrawingModelInterpolator] implementation. */
@Suppress("UNCHECKED_CAST")
public class DefaultDrawingModelInterpolator<T : DrawingModel.DrawingInfo, R : DrawingModel<T>> :
  DrawingModelInterpolator<T, R> {
  private var transformationMaps = emptyList<Map<Double, TransformationModel<T>>>()
  private var oldDrawingModel: R? = null
  private var newDrawingModel: R? = null

  override fun setModels(old: R?, new: R?) {
    synchronized(this) {
      oldDrawingModel = old
      newDrawingModel = new
      updateTransformationMap()
    }
  }

  override suspend fun transform(fraction: Float): R? =
    newDrawingModel?.transform(
      drawingInfo =
        transformationMaps.mapNotNull { map ->
          map
            .mapNotNull { (x, model) ->
              currentCoroutineContext().ensureActive()
              model.transform(fraction)?.let { drawingInfo -> x to drawingInfo }
            }
            .takeIf { list -> list.isNotEmpty() }
            ?.toMap()
        },
      from = oldDrawingModel,
      fraction = fraction,
    ) as R?

  private fun updateTransformationMap() {
    transformationMaps = buildList {
      repeat(max(oldDrawingModel?.size.orZero, newDrawingModel?.size.orZero)) { index ->
        val map = mutableMapOf<Double, TransformationModel<T>>()
        oldDrawingModel?.getOrNull(index)?.forEach { (x, drawingInfo) ->
          map[x] = TransformationModel(drawingInfo)
        }
        newDrawingModel?.getOrNull(index)?.forEach { (x, drawingInfo) ->
          map[x] = TransformationModel(map[x]?.old, drawingInfo)
        }
        add(map)
      }
    }
  }

  private class TransformationModel<T : DrawingModel.DrawingInfo>(val old: T?, val new: T? = null) {
    fun transform(fraction: Float): T? = new?.transform(old, fraction) as T?
  }
}
