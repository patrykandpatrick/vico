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

import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/** Creates [PieChartModel]s. */
public class PieChartModelProducer {
  private val modelFlow: MutableStateFlow<PieChartModel?> = MutableStateFlow(null)

  internal val models: StateFlow<PieChartModel?> = modelFlow.asStateFlow()

  /** Creates a [Transaction], invokes [block], and updates the model. */
  public suspend fun runTransaction(block: Transaction.() -> Unit) {
    withContext(Dispatchers.Default) { Transaction().also(block).commit() }
  }

  /** Handles data updates. */
  public inner class Transaction internal constructor() {
    private var partial: PieChartModel.Partial? = null
    private val extraStore: MutableExtraStore = MutableExtraStore()

    internal fun set(partial: PieChartModel.Partial) {
      this.partial = partial
    }

    /** Adds auxiliary values retrievable via [PieChartModel.extraStore]. */
    public fun extras(block: (MutableExtraStore) -> Unit) {
      block(extraStore)
    }

    internal suspend fun commit() {
      modelFlow.value = partial?.complete(extraStore)
    }
  }
}
