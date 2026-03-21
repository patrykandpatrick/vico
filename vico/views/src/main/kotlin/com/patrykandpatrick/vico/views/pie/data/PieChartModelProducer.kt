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

package com.patrykandpatrick.vico.views.pie.data

import com.patrykandpatrick.vico.views.common.data.ExtraStore
import com.patrykandpatrick.vico.views.common.data.MutableExtraStore
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Creates [PieChartModel] instances and handles difference animations. */
public class PieChartModelProducer {
  private var lastPartial: PieChartModel.Partial? = null
  private var lastTransactionExtraStore: MutableExtraStore = MutableExtraStore()
  private val mutex: Mutex = Mutex()
  private val updateReceivers = ConcurrentHashMap<Any, UpdateReceiver>()

  private suspend fun update(
    partial: PieChartModel.Partial?,
    transactionExtraStore: MutableExtraStore,
  ) {
    withContext(Dispatchers.Default) {
      mutex.withLock {
        if (partial == lastPartial && transactionExtraStore == lastTransactionExtraStore)
          return@withLock
        updateReceivers.values.forEach { receiver ->
          receiver.handleUpdate(partial, transactionExtraStore)
        }
        lastPartial = partial
        lastTransactionExtraStore = transactionExtraStore
      }
    }
  }

  private fun getModel(partial: PieChartModel.Partial?, extraStore: ExtraStore): PieChartModel? =
    partial?.complete(extraStore)

  private suspend fun transform(
    key: Any,
    fraction: Float,
    partial: PieChartModel.Partial?,
    transactionExtraStore: ExtraStore,
  ) {
    with(updateReceivers[key] ?: return) {
      withContext(Dispatchers.Default) {
        transform(hostExtraStore, fraction)
        currentCoroutineContext().ensureActive()
        onUpdate(getModel(partial, transactionExtraStore + hostExtraStore.copy()))
      }
    }
  }

  internal suspend fun registerForUpdates(
    key: Any,
    cancelAnimation: suspend () -> Unit,
    startAnimation: (transformModel: suspend (key: Any, fraction: Float) -> Unit) -> Unit,
    prepareForTransformation: (PieChartModel?, MutableExtraStore) -> Unit,
    transform: suspend (MutableExtraStore, Float) -> Unit,
    hostExtraStore: MutableExtraStore,
    onUpdate: (PieChartModel?) -> Unit,
  ) {
    withContext(Dispatchers.Default) {
      val receiver =
        UpdateReceiver(
          cancelAnimation = cancelAnimation,
          startAnimation = startAnimation,
          onUpdate = onUpdate,
          hostExtraStore = hostExtraStore,
          prepareForTransformation = prepareForTransformation,
          transform = transform,
        )
      mutex.withLock {
        updateReceivers[key] = receiver
        receiver.handleUpdate(lastPartial, lastTransactionExtraStore)
      }
    }
  }

  internal fun isRegistered(key: Any): Boolean = updateReceivers.containsKey(key)

  internal fun unregisterFromUpdates(key: Any) {
    updateReceivers.remove(key)
  }

  /** Creates a transaction, runs [block], and performs the update. */
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

    /** Updates auxiliary values. */
    public fun extras(block: (MutableExtraStore) -> Unit) {
      block(extraStore)
    }

    internal suspend fun commit() {
      update(partial, extraStore)
    }
  }

  private inner class UpdateReceiver(
    val cancelAnimation: suspend () -> Unit,
    val startAnimation: (transformModel: suspend (key: Any, fraction: Float) -> Unit) -> Unit,
    val onUpdate: (PieChartModel?) -> Unit,
    val hostExtraStore: MutableExtraStore,
    val prepareForTransformation: (PieChartModel?, MutableExtraStore) -> Unit,
    val transform: suspend (MutableExtraStore, Float) -> Unit,
  ) {
    suspend fun handleUpdate(partial: PieChartModel.Partial?, transactionExtraStore: ExtraStore) {
      cancelAnimation()
      val model = getModel(partial, transactionExtraStore)
      hostExtraStore.clear()
      prepareForTransformation(model, hostExtraStore)
      startAnimation { key, fraction -> transform(key, fraction, partial, transactionExtraStore) }
    }
  }
}
