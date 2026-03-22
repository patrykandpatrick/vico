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

internal interface PieChartDrawingModelInterpolator {
  public fun setModels(old: PieChartDrawingModel?, new: PieChartDrawingModel?)

  public suspend fun transform(fraction: Float): PieChartDrawingModel?

  companion object
}

internal fun defaultPieChartDrawingModelInterpolator(): PieChartDrawingModelInterpolator =
  DefaultPieChartDrawingModelInterpolator()

private class DefaultPieChartDrawingModelInterpolator : PieChartDrawingModelInterpolator {
  private var oldDrawingModel: PieChartDrawingModel? = null
  private var newDrawingModel: PieChartDrawingModel? = null

  override fun setModels(old: PieChartDrawingModel?, new: PieChartDrawingModel?) {
    oldDrawingModel = old
    newDrawingModel = new
  }

  override suspend fun transform(fraction: Float): PieChartDrawingModel? {
    val newModel = newDrawingModel ?: return null
    return interpolate(oldDrawingModel, newModel, fraction)
  }
}
