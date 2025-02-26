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

package com.patrykandpatrick.vico.multiplatform.cartesian.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore

@Immutable
internal class CartesianChartData(
  val model: CartesianChartModel? = null,
  val previousModel: CartesianChartModel? = null,
  val ranges: CartesianChartRanges = CartesianChartRanges.Empty,
  val extraStore: ExtraStore = ExtraStore.Empty,
)

internal operator fun CartesianChartData.component1(): CartesianChartModel? = model

internal operator fun CartesianChartData.component2(): CartesianChartModel? = previousModel

internal operator fun CartesianChartData.component3(): CartesianChartRanges = ranges

internal operator fun CartesianChartData.component4(): ExtraStore = extraStore

internal class CartesianChartDataState : State<CartesianChartData> {
  private var previousModel: CartesianChartModel? = null

  override var value by mutableStateOf(CartesianChartData())
    private set

  fun set(model: CartesianChartModel?, ranges: CartesianChartRanges, extraStore: ExtraStore) {
    val currentModel = value.model
    if (model?.id != currentModel?.id) previousModel = currentModel
    value = CartesianChartData(model, previousModel, ranges, extraStore)
  }
}
