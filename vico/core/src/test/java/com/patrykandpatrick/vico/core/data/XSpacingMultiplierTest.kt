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

package com.patrykandpatrick.vico.core.data

import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.MutableChartValues
import com.patrykandpatrick.vico.core.cartesian.data.getXSpacingMultiplier
import java.util.stream.Stream
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

public class XSpacingMultiplierTest {

  @ParameterizedTest
  @MethodSource("getXWithValidXStep")
  public fun `Valid xStep produces the correct xStepMultiplier`(x: List<Number>, xStep: Float) {
    checkXSpacingMultiplier(x, xStep)
  }

  @ParameterizedTest()
  @MethodSource("getXWithInvalidXStep")
  public fun `Invalid xStep throws an exception on xStepMultiplier calculation`(
    x: List<Number>,
    xStep: Float,
  ) {
    assertThrows<IllegalStateException> { checkXSpacingMultiplier(x, xStep) }
  }

  private fun checkXSpacingMultiplier(xCollection: List<Number>, xStep: Float) {
    val chartValues = MutableChartValues()
    val columnCartesianModel =
      ColumnCartesianLayerModel.build { series(xCollection, xCollection.map { 1f }) }
    val model = CartesianChartModel(columnCartesianModel)
    chartValues.update(xStep, model)

    chartValues.tryUpdate(
      columnCartesianModel.minX,
      columnCartesianModel.maxX,
      columnCartesianModel.minY,
      columnCartesianModel.maxY,
      null,
    )
    xCollection.forEach { chartValues.getXSpacingMultiplier(it.toFloat()) }
  }

  private companion object {
    @JvmStatic
    private fun getXWithValidXStep(): Stream<Arguments> =
      Stream.of(
        Arguments.of(listOf(1.35, 1.9, 2.59), 0.01f),
        Arguments.of(listOf(0.1, 0.2), 0.1f),
        Arguments.of(listOf(0.000001f, 0.000002f), 0.000001f),
        Arguments.of(listOf(1000f, 0.000002f), 1000f),
      )

    @JvmStatic
    private fun getXWithInvalidXStep(): Stream<Arguments> =
      Stream.of(
        Arguments.of(listOf(1.35, 1.9, 2.59), 0.1f),
        Arguments.of(listOf(0.1, 0.2), 1f),
        Arguments.of(listOf(0.000001f, 0.000002f), 10f),
        Arguments.of(listOf(1000f, 0.000002f), 10000f),
      )
  }
}
