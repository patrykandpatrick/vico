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

package com.patrykandpatrick.vico.core.pie

import androidx.annotation.WorkerThread
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.common.ExtraStore
import com.patrykandpatrick.vico.core.common.MutableExtraStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/**
 * Creates [PieModel]s and handles difference animations.
 */
public class PieChartModelProducer private constructor(dispatcher: CoroutineDispatcher) {
    private var partial: PieModel.Partial? = null
    private var cachedModel: PieModel? = null
    private val mutex = Mutex()
    private val coroutineScope = CoroutineScope(dispatcher)
    private val updateReceivers = mutableMapOf<Any, UpdateReceiver>()
    private val extraStore = MutableExtraStore()

    private fun trySetPartial(partial: PieModel.Partial): Boolean {
        if (!mutex.tryLock()) return false
        this.partial = partial
        cachedModel = null
        val deferredUpdates = updateReceivers.values.map { coroutineScope.async { it.handleUpdate() } }
        coroutineScope.launch {
            deferredUpdates.awaitAll()
            mutex.unlock()
        }
        return true
    }

    private suspend fun setPartial(partial: PieModel.Partial): Deferred<Unit> {
        mutex.lock()
        this.partial = partial
        cachedModel = null
        val completableDeferred = CompletableDeferred<Unit>()
        val deferredUpdates = updateReceivers.values.map { coroutineScope.async { it.handleUpdate() } }
        coroutineScope.launch {
            deferredUpdates.awaitAll()
            mutex.unlock()
            completableDeferred.complete(Unit)
        }
        return completableDeferred
    }

    private fun getModel(extraStore: ExtraStore? = null) =
        partial?.let { partial ->
            val mergedExtraStore = this.extraStore.let { if (extraStore != null) it + extraStore else it }
            cachedModel?.copy(mergedExtraStore)
                ?: partial.complete(mergedExtraStore).also { cachedModel = it }
        }

    /**
     * Creates an intermediate [PieModel] for difference animations. [fraction] is the balance between the
     * initial and target [PieModel]s.
     */
    public suspend fun transformModel(
        key: Any,
        fraction: Float,
    ) {
        with(updateReceivers[key] ?: return) {
            transform(extraStore, fraction)
            val internalModel = getModel(extraStore.copy())
            currentCoroutineContext().ensureActive()
            onModelCreated(internalModel)
        }
    }

    /**
     * Registers an update listener associated with a [key]. [cancelAnimation] and [startAnimation] are called after a
     * data update is requested, with [cancelAnimation] being called before the update starts being processed (at which
     * point [transformModel] should stop being used), and [startAnimation] being called once the update has been
     * processed (at which point it’s safe to use [transformModel]). [onModelCreated] is called when a new
     * [PieModel] has been generated.
     */
    @WorkerThread
    public fun registerForUpdates(
        key: Any,
        cancelAnimation: () -> Unit,
        startAnimation: (transformModel: suspend (chartKey: Any, fraction: Float) -> Unit) -> Unit,
        prepareForTransformation: (PieModel?, MutableExtraStore) -> Unit,
        transform: suspend (MutableExtraStore, Float) -> Unit,
        extraStore: MutableExtraStore,
        onModelCreated: (PieModel?) -> Unit,
    ) {
        UpdateReceiver(
            cancelAnimation,
            startAnimation,
            onModelCreated,
            extraStore,
            prepareForTransformation,
            transform,
        ).run {
            updateReceivers[key] = this
            handleUpdate()
        }
    }

    /**
     * Checks if an update listener with the given key is registered.
     */
    public fun isRegistered(key: Any): Boolean = updateReceivers.containsKey(key)

    /**
     * Unregisters the update listener associated with the given [key].
     */
    public fun unregisterFromUpdates(key: Any) {
        updateReceivers.remove(key)
    }

    /**
     * Creates a [Transaction] instance.
     */
    public fun createTransaction(): Transaction = Transaction()

    /**
     * Creates a [Transaction], runs [block], and calls [Transaction.tryCommit], returning its output. For suspending
     * behavior, use [runTransaction].
     */
    public fun tryRunTransaction(block: Transaction.() -> Unit): Boolean = createTransaction().also(block).tryCommit()

    /**
     * Creates a [Transaction], runs [block], and calls [Transaction.commit], returning its output.
     */
    public suspend fun runTransaction(block: Transaction.() -> Unit): Deferred<Unit> =
        createTransaction().also(block).commit()

    /**
     * Handles data updates. An initially empty list of [PieModel.Partial]s is created and can be updated via
     * the class’s functions. Each [PieModel.Partial] corresponds to a [CartesianLayer].
     */
    public inner class Transaction internal constructor() {
        private var newPartial: PieModel.Partial? = null

        /**
         * Adds a [PieModel.Partial].
         */
        public fun set(partial: PieModel.Partial) {
            this.newPartial = partial
        }

        /**
         * Allows for adding auxiliary values, which can later be retrieved via [PieModel.extraStore].
         */
        public fun updateExtras(block: (MutableExtraStore) -> Unit) {
            block(extraStore)
        }

        /**
         * Requests a data update. If the update is accepted, `true` is returned. If the update is rejected, which
         * occurs when there’s already an update in progress, or no partial has been set, `false` is returned.
         * For suspending behavior, use [commit].
         */
        public fun tryCommit(): Boolean = newPartial?.let(::trySetPartial) ?: false

        /**
         * Runs a data update. Unlike [tryCommit], this function suspends the current coroutine and waits until an
         * update can be run, meaning the update cannot be rejected. The returned [Deferred] implementation is marked as
         * completed once the update has been processed.
         */
        public suspend fun commit(): Deferred<Unit> = newPartial?.let { setPartial(it) } ?: CompletableDeferred(Unit)
    }

    private inner class UpdateReceiver(
        val cancelAnimation: () -> Unit,
        val startAnimation: (transformModel: suspend (chartKey: Any, fraction: Float) -> Unit) -> Unit,
        val onModelCreated: (PieModel?) -> Unit,
        val extraStore: MutableExtraStore,
        val prepareForTransformation: (PieModel?, MutableExtraStore) -> Unit,
        val transform: suspend (MutableExtraStore, Float) -> Unit,
    ) {
        fun handleUpdate() {
            cancelAnimation()
            prepareForTransformation(getModel(), extraStore)
            startAnimation(::transformModel)
        }
    }

    public companion object {
        /**
         * Creates a [PieChartModelProducer], running an initial [Transaction]. [dispatcher] is the
         * [CoroutineDispatcher] to be used for update handling.
         */
        public fun build(
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
            transaction: Transaction.() -> Unit = {},
        ): PieChartModelProducer = PieChartModelProducer(dispatcher).also { it.tryRunTransaction(transaction) }
    }
}
