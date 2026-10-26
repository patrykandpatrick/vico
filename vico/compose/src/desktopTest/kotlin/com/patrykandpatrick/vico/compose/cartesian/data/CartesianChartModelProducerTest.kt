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

import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CartesianChartModelProducerTest {
  @Test
  fun `unregistering a chart while an update is in progress doesn't throw`() = runBlocking {
    val producer = CartesianChartModelProducer()
    val firstKey = Any()
    val secondKey = Any()
    producer.register(firstKey)
    producer.register(secondKey)
    var isUpdateInProgress = false
    // `update` launches one coroutine per receiver while iterating over them, so this unregisters
    // `secondKey` mid-iteration, as a chart leaving the composition on the main thread would.
    val dispatcher = HookDispatcher {
      if (isUpdateInProgress) producer.unregisterFromUpdates(secondKey)
    }
    withContext(dispatcher) {
      isUpdateInProgress = true
      producer.Transaction().apply { lineModel { series(1, 2, 3) } }.commit()
    }
    assertFalse(producer.isRegistered(secondKey))
  }

  private suspend fun CartesianChartModelProducer.register(key: Any) {
    registerForUpdates(
      key = key,
      restoredModel = null,
      cancelAnimation = {},
      startAnimation = {},
      prepareForTransformation = { _, _, _ -> },
      transform = { _, _ -> },
      hostExtraStore = MutableExtraStore(),
      updateRanges = { MutableCartesianChartRanges() },
      onUpdate = { _, _, _ -> },
    )
  }

  private class HookDispatcher(private val onDispatch: () -> Unit) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
      onDispatch()
      Dispatchers.Default.dispatch(context, block)
    }
  }
}
