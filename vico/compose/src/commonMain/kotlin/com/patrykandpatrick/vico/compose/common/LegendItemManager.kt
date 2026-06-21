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

package com.patrykandpatrick.vico.compose.common

import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.pie.PieChartMeasuringContext

internal class LegendItemManager(
  private val items: AdditionScope<LegendItem>.(ExtraStore) -> Unit
) {
  private val _itemList = mutableListOf<LegendItem>()
  val itemList: List<LegendItem> = _itemList
  private val itemScope = AdditionScope(_itemList::add)
  private var previousExtraStoreHashCode: Int? = null

  fun addItems(context: MeasuringContext) {
    with(context) {
      val resolvedExtraStore =
        when (this) {
          is CartesianMeasuringContext -> model.extraStore
          is PieChartMeasuringContext -> model.extraStore
          else -> error("Unexpected `MeasuringContext` implementation.")
        }
      val extraStoreHashCode = resolvedExtraStore.hashCode()
      if (extraStoreHashCode != previousExtraStoreHashCode) {
        _itemList.clear()
        items(itemScope, resolvedExtraStore)
        previousExtraStoreHashCode = extraStoreHashCode
      }
    }
  }
}
