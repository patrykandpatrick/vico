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

import androidx.annotation.WorkerThread
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Creates [CartesianChartModel]s and handles difference animations. */
public class CartesianChartModelProducer private constructor(dispatcher: CoroutineDispatcher) {
  private var partials = emptyList<CartesianLayerModel.Partial>()
  private var extraStore = MutableExtraStore()
  private var cachedModel: CartesianChartModel? = null
  private val updateMutex = Mutex()
  private val registrationMutex = Mutex()
  private val coroutineScope = CoroutineScope(dispatcher)
  private val updateReceivers = ConcurrentHashMap<Any, UpdateReceiver>()

  /** Creates a [CartesianChartModelProducer]. */
  public constructor() : this(Dispatchers.Default)

  private fun tryUpdate(
    partials: List<CartesianLayerModel.Partial>,
    extraStore: MutableExtraStore,
  ): Boolean {
    if (!updateMutex.tryLock()) return false
    val immutablePartials = partials.toList()
    if (immutablePartials == this.partials && extraStore == this.extraStore) {
      updateMutex.unlock()
      return true
    }
    this.partials = immutablePartials
    this.extraStore = extraStore
    cachedModel = null
    val receiverJobs = updateReceivers.values.map { coroutineScope.launch { it.handleUpdate() } }
    coroutineScope.launch {
      receiverJobs.joinAll()
      updateMutex.unlock()
    }
    return true
  }

  private suspend fun update(
    partials: List<CartesianLayerModel.Partial>,
    extraStore: MutableExtraStore,
  ) {
    updateMutex.lock()
    registrationMutex.lock()
    val immutablePartials = partials.toList()
    if (immutablePartials == this.partials && extraStore == this.extraStore) {
      updateMutex.unlock()
      registrationMutex.unlock()
      return
    }
    this.partials = partials.toList()
    this.extraStore = extraStore
    cachedModel = null
    coroutineScope {
      updateReceivers.values.forEach { launch { it.handleUpdate() } }
      registrationMutex.unlock()
    }
    updateMutex.unlock()
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
    chartValues: ChartValues,
  ) {
    with(updateReceivers[key] ?: return) {
      transform(extraStore, fraction)
      val transformedModel =
        model?.copy(this@CartesianChartModelProducer.extraStore + extraStore.copy())
      currentCoroutineContext().ensureActive()
      onModelCreated(transformedModel, chartValues)
    }
  }

  /**
   * Registers an update listener associated with a [key]. [cancelAnimation] and [startAnimation]
   * are called after a data update is requested, with [cancelAnimation] being called before the
   * update starts being processed (at which point [transformModel] should stop being used), and
   * [startAnimation] being called once the update has been processed (at which point it’s safe to
   * use [transformModel]). [updateChartValues] updates the chart’s [MutableChartValues] instance
   * and returns an immutable copy of it. [onModelCreated] is called when a new
   * [CartesianChartModel] has been generated.
   */
  @WorkerThread
  public suspend fun registerForUpdates(
    key: Any,
    cancelAnimation: suspend () -> Unit,
    startAnimation: (transformModel: suspend (key: Any, fraction: Float) -> Unit) -> Unit,
    prepareForTransformation: (CartesianChartModel?, MutableExtraStore, ChartValues) -> Unit,
    transform: suspend (MutableExtraStore, Float) -> Unit,
    extraStore: MutableExtraStore,
    updateChartValues: (CartesianChartModel?) -> ChartValues,
    onModelCreated: (CartesianChartModel?, ChartValues) -> Unit,
  ) {
    val receiver =
      UpdateReceiver(
        cancelAnimation,
        startAnimation,
        onModelCreated,
        extraStore,
        prepareForTransformation,
        transform,
        updateChartValues,
      )
    registrationMutex.withLock {
      updateReceivers[key] = receiver
      receiver.handleUpdate()
    }
  }

  /** Checks if an update listener with the given key is registered. */
  public fun isRegistered(key: Any): Boolean = updateReceivers.containsKey(key)

  /** Unregisters the update listener associated with the given key. */
  public fun unregisterFromUpdates(key: Any) {
    updateReceivers.remove(key)
  }

  /** Creates a [Transaction] instance. */
  @Deprecated("Use the `Transaction` constructor.", ReplaceWith("Transaction()"))
  public fun createTransaction(): Transaction = Transaction()

  /**
   * Creates a [Transaction], runs [block], and calls [Transaction.tryCommit], returning its output.
   * For suspending behavior, use [runTransaction].
   */
  @Suppress("DeprecatedCallableAddReplaceWith")
  @Deprecated(
    "Use `runTransaction`. More information: " +
      "https://patrykandpatrick.com/vico/releases/2.0.0-alpha.22."
  )
  public fun tryRunTransaction(block: Transaction.() -> Unit): Boolean =
    @Suppress("DEPRECATION") Transaction().also(block).tryCommit()

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
     * Allows for adding auxiliary values, which can later be retrieved via
     * [CartesianChartModel.extraStore].
     */
    @Deprecated("Use `extras`.", ReplaceWith("extras(block)"))
    public fun updateExtras(block: (MutableExtraStore) -> Unit) {
      extras(block)
    }

    /**
     * Requests a data update. If the update is accepted, `true` is returned. If the update is
     * rejected, which occurs when there’s already an update in progress, `false` is returned. For
     * suspending behavior, use [commit].
     */
    @Deprecated(
      "Use `commit`. More information: https://patrykandpatrick.com/vico/releases/2.0.0-alpha.22."
    )
    public fun tryCommit(): Boolean = tryUpdate(newPartials, newExtraStore)

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
    val onModelCreated: (CartesianChartModel?, ChartValues) -> Unit,
    val extraStore: MutableExtraStore,
    val prepareForTransformation: (CartesianChartModel?, MutableExtraStore, ChartValues) -> Unit,
    val transform: suspend (MutableExtraStore, Float) -> Unit,
    val updateChartValues: (CartesianChartModel?) -> ChartValues,
  ) {
    suspend fun handleUpdate() {
      cancelAnimation()
      val model = getModel(extraStore)
      val chartValues = updateChartValues(model)
      prepareForTransformation(model, extraStore, chartValues)
      startAnimation { key, fraction -> transformModel(key, fraction, model, chartValues) }
    }
  }

  public companion object {
    /**
     * Creates a [CartesianChartModelProducer], running an initial [Transaction]. [dispatcher] is
     * the [CoroutineDispatcher] to be used for update handling.
     */
    @Deprecated(
      "To create a `CartesianChartModelProducer`, use the constructor. Rather than passing a " +
        "`CoroutineDispatcher`, use `runTransaction` with a `CoroutineContext` that includes the " +
        "`CoroutineDispatcher`. Also use `runTransaction` to run an initial `Transaction`. " +
        "More information: https://patrykandpatrick.com/vico/releases/2.0.0-alpha.22."
    )
    public fun build(
      dispatcher: CoroutineDispatcher = Dispatchers.Default,
      transaction: (Transaction.() -> Unit)? = null,
    ): CartesianChartModelProducer =
      CartesianChartModelProducer(dispatcher).apply {
        if (transaction != null) @Suppress("DEPRECATION") tryRunTransaction(transaction)
      }
  }
}
