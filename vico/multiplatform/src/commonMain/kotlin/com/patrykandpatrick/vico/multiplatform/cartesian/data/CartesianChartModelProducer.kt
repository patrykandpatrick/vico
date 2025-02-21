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

import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.data.MutableExtraStore
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Creates [CartesianChartModel]s and handles difference animations. */
public class CartesianChartModelProducer {
  private var lastPartials = emptyList<CartesianLayerModel.Partial>()
  private var lastTransactionExtraStore = MutableExtraStore()
  private var cachedModel: CartesianChartModel? = null
  private var cachedModelPartialHashCode: Int? = null
  private val mutex = Mutex()
  private val updateReceivers = mutableMapOf<Any, UpdateReceiver>()

  private suspend fun update(
    partials: List<CartesianLayerModel.Partial>,
    transactionExtraStore: MutableExtraStore,
  ) {
    coroutineScope {
      mutex.withLock {
        val immutablePartials = partials.toList()
        if (
          immutablePartials == this@CartesianChartModelProducer.lastPartials &&
            transactionExtraStore == this@CartesianChartModelProducer.lastTransactionExtraStore
        ) {
          return@coroutineScope
        }
        updateReceivers.values
          .map { launch { it.handleUpdate(immutablePartials, transactionExtraStore) } }
          .joinAll()
        lastPartials = immutablePartials
        lastTransactionExtraStore = transactionExtraStore
      }
    }
  }

  private fun getModel(partials: List<CartesianLayerModel.Partial>, extraStore: ExtraStore) =
    if (partials.hashCode() == cachedModelPartialHashCode) {
      cachedModel?.copy(extraStore)
    } else {
      if (partials.isNotEmpty()) {
          CartesianChartModel(partials.map { it.complete(extraStore) }, extraStore)
        } else {
          null
        }
        .also { model ->
          cachedModel = model
          cachedModelPartialHashCode = partials.hashCode()
        }
    }

  private suspend fun transformModel(
    key: Any,
    fraction: Float,
    model: CartesianChartModel?,
    transactionExtraStore: ExtraStore,
    ranges: CartesianChartRanges,
  ) {
    with(updateReceivers[key] ?: return) {
      withContext(getDispatcher()) {
        transform(hostExtraStore, fraction)
        val transformedModel = model?.copy(transactionExtraStore + hostExtraStore.copy())
        currentCoroutineContext().ensureActive()
        onModelCreated(transformedModel, ranges)
      }
    }
  }

  internal suspend fun registerForUpdates(
    key: Any,
    cancelAnimation: suspend () -> Unit,
    startAnimation: (transformModel: suspend (key: Any, fraction: Float) -> Unit) -> Unit,
    prepareForTransformation:
      (CartesianChartModel?, MutableExtraStore, CartesianChartRanges) -> Unit,
    transform: suspend (MutableExtraStore, Float) -> Unit,
    hostExtraStore: MutableExtraStore,
    updateRanges: (CartesianChartModel?) -> CartesianChartRanges,
    onModelCreated: (CartesianChartModel?, CartesianChartRanges) -> Unit,
  ) {
    withContext(getDispatcher()) {
      val receiver =
        UpdateReceiver(
          cancelAnimation,
          startAnimation,
          onModelCreated,
          hostExtraStore,
          prepareForTransformation,
          transform,
          updateRanges,
        )
      mutex.withLock {
        updateReceivers[key] = receiver
        receiver.handleUpdate(lastPartials, lastTransactionExtraStore)
      }
    }
  }

  internal fun isRegistered(key: Any): Boolean = updateReceivers.containsKey(key)

  internal fun unregisterFromUpdates(key: Any) {
    updateReceivers.remove(key)
  }

  /**
   * (1) Creates a [Transaction], (2) invokes [block], and (3) runs a data update, returning once
   * the update is complete. Between steps 2 and 3, if there’s already an update in progress, the
   * current coroutine is suspended until the ongoing update’s completion.
   */
  public suspend fun runTransaction(block: Transaction.() -> Unit) {
    withContext(Dispatchers.Default) { Transaction().also(block).commit() }
  }

  /** Handles data updates. This is used via [runTransaction]. */
  public inner class Transaction internal constructor() {
    private val newPartials = mutableListOf<CartesianLayerModel.Partial>()
    private val newExtraStore = MutableExtraStore()

    /** Adds a [CartesianLayerModel.Partial]. */
    public fun add(partial: CartesianLayerModel.Partial) {
      newPartials.add(partial)
    }

    /**
     * Allows for adding auxiliary values, which can later be retrieved via
     * [CartesianChartModel.extraStore].
     */
    public fun extras(block: (MutableExtraStore) -> Unit) {
      block(newExtraStore)
    }

    internal suspend fun commit() {
      update(newPartials, newExtraStore)
    }
  }

  private inner class UpdateReceiver(
    val cancelAnimation: suspend () -> Unit,
    val startAnimation: (transformModel: suspend (key: Any, fraction: Float) -> Unit) -> Unit,
    val onModelCreated: (CartesianChartModel?, CartesianChartRanges) -> Unit,
    val hostExtraStore: MutableExtraStore,
    val prepareForTransformation:
      (CartesianChartModel?, MutableExtraStore, CartesianChartRanges) -> Unit,
    val transform: suspend (MutableExtraStore, Float) -> Unit,
    val updateRanges: (CartesianChartModel?) -> CartesianChartRanges,
  ) {
    suspend fun handleUpdate(
      partials: List<CartesianLayerModel.Partial>,
      transactionExtraStore: ExtraStore,
    ) {
      cancelAnimation()
      val model = getModel(partials, transactionExtraStore + hostExtraStore)
      val ranges = updateRanges(model)
      prepareForTransformation(model, hostExtraStore, ranges)
      startAnimation { key, fraction ->
        transformModel(key, fraction, model, transactionExtraStore, ranges)
      }
    }
  }

  private suspend fun getDispatcher(): CoroutineDispatcher {
    val context = currentCoroutineContext()
    return if (context[PreviewContextKey] != null) Dispatchers.Unconfined else Dispatchers.Default
  }
}

internal object PreviewContextKey : CoroutineContext.Key<PreviewContext>

internal object PreviewContext : AbstractCoroutineContextElement(PreviewContextKey)
