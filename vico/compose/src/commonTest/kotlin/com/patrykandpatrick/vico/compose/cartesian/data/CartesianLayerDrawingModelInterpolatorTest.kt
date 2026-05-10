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

import com.patrykandpatrick.vico.compose.common.data.CartesianLayerDrawingModelInterpolator
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals

class CartesianLayerDrawingModelInterpolatorTest {
  @Test
  fun `Transform matches series by key`() {
    val interpolator =
      CartesianLayerDrawingModelInterpolator.default<
        LineCartesianLayerDrawingModel.Entry,
        LineCartesianLayerDrawingModel,
      >()
    val oldModel =
      LineCartesianLayerDrawingModel(
        entries =
          listOf(
            mapOf(0.0 to LineCartesianLayerDrawingModel.Entry(0.25f)),
            mapOf(0.0 to LineCartesianLayerDrawingModel.Entry(0.75f)),
          ),
        seriesKeys = listOf("first", "second"),
      )
    val newModel =
      LineCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to LineCartesianLayerDrawingModel.Entry(1f))),
        seriesKeys = listOf("second"),
      )

    interpolator.setModels(oldModel, newModel)
    val transformedModel = runTestCoroutine { interpolator.transform(0.5f) }

    assertEquals(listOf("second"), transformedModel?.seriesKeys)
    assertEquals(0.875f, transformedModel?.single()?.get(0.0)?.y)
  }

  private fun <T> runTestCoroutine(block: suspend () -> T): T {
    var completed = false
    var value: Any? = null
    var failure: Throwable? = null
    block.startCoroutine(
      object : Continuation<T> {
        override val context = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
          result.fold(onSuccess = { value = it }, onFailure = { failure = it })
          completed = true
        }
      }
    )
    failure?.let { throw it }
    check(completed)
    @Suppress("UNCHECKED_CAST")
    return value as T
  }
}
