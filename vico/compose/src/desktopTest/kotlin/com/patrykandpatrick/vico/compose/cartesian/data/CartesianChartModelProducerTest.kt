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

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalInspectionMode
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CartesianChartModelProducerTest {
  @Test
  fun `composed chart keeps receiving model updates after registration`() = runBlocking {
    val producer = CartesianChartModelProducer()
    var rangeUpdates = 0
    withComposition { composition ->
      lateinit var data: State<CartesianChartData>
      composition.setContent {
        val chart =
          rememberCartesianChart(
            rememberColumnCartesianLayer(),
            getXStep = { _, _, _ ->
              rangeUpdates++
              1.0
            },
          )
        data = producer.collectAsState(chart, null, null, MutableCartesianChartRanges())
      }
      for (count in 2..3) {
        producer.runTransaction { columnModel { series(List(count) { 1 }) } }
        assertEquals((count - 1).toDouble(), data.value.model?.width)
      }
    }
    val updatesBeforeDisposal = rangeUpdates
    assertTrue(updatesBeforeDisposal > 0)
    producer.runTransaction { columnModel { series(3, 2, 1, 4) } }
    assertEquals(updatesBeforeDisposal, rangeUpdates)
  }

  @Test
  fun `preview reads the model during the first composition`() = runBlocking {
    val producer = CartesianChartModelProducer()
    producer.runTransaction { columnModel { series(1, 2) } }
    var firstModel: CartesianChartModel? = null
    withComposition { composition ->
      composition.setContent {
        CompositionLocalProvider(LocalInspectionMode provides true) {
          val chart = rememberCartesianChart(rememberColumnCartesianLayer())
          firstModel =
            producer.collectAsState(chart, null, null, MutableCartesianChartRanges()).value.model
        }
      }
      assertEquals(1.0, firstModel?.width)
    }
  }

  @Test
  fun `abandoned preview stops receiving model updates`() = runBlocking {
    val producer = CartesianChartModelProducer()
    producer.runTransaction { columnModel { series(1, 2) } }
    var rangeUpdates = 0
    withComposition { composition ->
      class AbandonPreview : RuntimeException()
      assertFailsWith<AbandonPreview> {
        composition.setContent {
          CompositionLocalProvider(LocalInspectionMode provides true) {
            val chart =
              rememberCartesianChart(
                rememberColumnCartesianLayer(),
                getXStep = { _, _, _ ->
                  rangeUpdates++
                  1.0
                },
              )
            producer.collectAsState(chart, null, null, MutableCartesianChartRanges())
            throw AbandonPreview()
          }
        }
      }
      val updatesBeforeAbandonment = rangeUpdates
      assertTrue(updatesBeforeAbandonment > 0)
      producer.runTransaction { columnModel { series(1, 2, 3) } }
      assertEquals(updatesBeforeAbandonment, rangeUpdates)
    }
  }

  private suspend fun withComposition(block: suspend (Composition) -> Unit) {
    // Run registration and transformation inline so the receiver is installed before setup returns.
    val dispatcher =
      object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) = block.run()
      }
    val recomposer = Recomposer(dispatcher + PreviewContext)
    val applier =
      object : AbstractApplier<Unit>(Unit) {
        override fun insertTopDown(index: Int, instance: Unit) = Unit

        override fun insertBottomUp(index: Int, instance: Unit) = Unit

        override fun remove(index: Int, count: Int) = Unit

        override fun move(from: Int, to: Int, count: Int) = Unit

        override fun onClear() = Unit
      }
    val composition = Composition(applier, recomposer)
    try {
      block(composition)
    } finally {
      composition.dispose()
      recomposer.cancel()
    }
  }

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
