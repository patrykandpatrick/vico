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

package com.patrykandpatrick.vico.core.cartesian.data

import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Creates [CartesianChartModel]s and handles difference animations. */
public class CartesianChartModelProducer {
  private var partials = emptyList<CartesianLayerModel.Partial>()
  private var extraStore = MutableExtraStore()
  private var cachedModel: CartesianChartModel? = null
  private val updateMutex = Mutex()
  private val registrationMutex = Mutex()
  private val updateReceivers = ConcurrentHashMap<Any, UpdateReceiver>()

  private suspend fun update(
    partials: List<CartesianLayerModel.Partial>,
    extraStore: MutableExtraStore,
  ) {
    withContext(Dispatchers.Default) {
      updateMutex.lock()
      registrationMutex.lock()
      val immutablePartials = partials.toList()
      if (
        immutablePartials == this@CartesianChartModelProducer.partials &&
          extraStore == this@CartesianChartModelProducer.extraStore
      ) {
        updateMutex.unlock()
        registrationMutex.unlock()
        return@withContext
      }
      this@CartesianChartModelProducer.partials = partials.toList()
      this@CartesianChartModelProducer.extraStore = extraStore
      cachedModel = null
      coroutineScope {
        updateReceivers.values.forEach { launch { it.handleUpdate() } }
        registrationMutex.unlock()
      }
      updateMutex.unlock()
    }
  }

  private fun getModel(extraStore: ExtraStore) =
    if (partials.isEmpty()) {
      null
    } else {
      val mergedExtraStore = this.extraStore + extraStore
      cachedModel?.copy(mergedExtraStore)
        ?: CartesianChartModel(partials.map { it.complete(mergedExtraStore) }, mergedExtraStore)
          .also { cachedModel = it }
    }

  private suspend fun transformModel(
    key: Any,
    fraction: Float,
    model: CartesianChartModel?,
    ranges: CartesianChartRanges,
  ) {
    with(updateReceivers[key] ?: return) {
      withContext(Dispatchers.Default) {
        transform(extraStore, fraction)
        val transformedModel =
          model?.copy(this@CartesianChartModelProducer.extraStore + extraStore.copy())
        currentCoroutineContext().ensureActive()
        onModelCreated(transformedModel, ranges)
      }
    }
  }

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public suspend fun registerForUpdates(
    key: Any,
    cancelAnimation: suspend () -> Unit,
    startAnimation: (transformModel: suspend (key: Any, fraction: Float) -> Unit) -> Unit,
    prepareForTransformation:
      (CartesianChartModel?, MutableExtraStore, CartesianChartRanges) -> Unit,
    transform: suspend (MutableExtraStore, Float) -> Unit,
    extraStore: MutableExtraStore,
    updateRanges: (CartesianChartModel?) -> CartesianChartRanges,
    onModelCreated: (CartesianChartModel?, CartesianChartRanges) -> Unit,
  ) {
    withContext(Dispatchers.Default) {
      val receiver =
        UpdateReceiver(
          cancelAnimation,
          startAnimation,
          onModelCreated,
          extraStore,
          prepareForTransformation,
          transform,
          updateRanges,
        )
      registrationMutex.withLock {
        updateReceivers[key] = receiver
        receiver.handleUpdate()
      }
    }
  }

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public fun isRegistered(key: Any): Boolean = updateReceivers.containsKey(key)

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public fun unregisterFromUpdates(key: Any) {
    updateReceivers.remove(key)
  }

  public fun createTransaction(): Transaction = Transaction()

  /** Creates a [Transaction], runs [block], and calls [Transaction.commit]. */
  public suspend fun runTransaction(block: Transaction.() -> Unit) {
    Transaction().also(block).commit()
  }

  /**
   * Handles data updates. An initially empty list of [CartesianLayerModel.Partial]s is created and
   * can be updated via the class’s functions. Each [CartesianLayerModel.Partial] corresponds to a
   * [CartesianLayer].
   */
  public inner class Transaction {
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

    /**
     * Runs a data update, returning once the update is complete. If there’s already an update in
     * progress, the current coroutine is first suspended until the ongoing update’s completion. An
     * update is complete once a new [CartesianChartModel] has been generated, and the
     * [CartesianChart] hosts have been notified.
     */
    public suspend fun commit() {
      update(newPartials, newExtraStore)
    }
  }

  private inner class UpdateReceiver(
    val cancelAnimation: suspend () -> Unit,
    val startAnimation: (transformModel: suspend (key: Any, fraction: Float) -> Unit) -> Unit,
    val onModelCreated: (CartesianChartModel?, CartesianChartRanges) -> Unit,
    val extraStore: MutableExtraStore,
    val prepareForTransformation:
      (CartesianChartModel?, MutableExtraStore, CartesianChartRanges) -> Unit,
    val transform: suspend (MutableExtraStore, Float) -> Unit,
    val updateRanges: (CartesianChartModel?) -> CartesianChartRanges,
  ) {
    suspend fun handleUpdate() {
      cancelAnimation()
      val model = getModel(extraStore)
      val ranges = updateRanges(model)
      prepareForTransformation(model, extraStore, ranges)
      startAnimation { key, fraction -> transformModel(key, fraction, model, ranges) }
    }
  }
}
