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

package com.patrykandpatrick.vico.views.cartesian.data

import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class ToStringTest {
  @Test
  fun `CartesianChartModel toString includes key properties`() {
    val model = LineCartesianLayerModel(listOf(listOf(LineCartesianLayerModel.Entry(0, 1))))
    val chartModel = CartesianChartModel(model)
    val toString = chartModel.toString()

    assertTrue(toString.contains("CartesianChartModel"))
    assertTrue(toString.contains("models="))
    assertTrue(toString.contains("width="))
  }

  @Test
  fun `LineCartesianLayerModel toString includes key properties`() {
    val model = LineCartesianLayerModel(listOf(listOf(LineCartesianLayerModel.Entry(0, 1))))
    val toString = model.toString()

    assertTrue(toString.contains("LineCartesianLayerModel"))
    assertTrue(toString.contains("minX="))
    assertTrue(toString.contains("maxX="))
    assertTrue(toString.contains("minY="))
    assertTrue(toString.contains("maxY="))
  }

  @Test
  fun `LineCartesianLayerModel Entry toString includes coordinates`() {
    val entry = LineCartesianLayerModel.Entry(1.0, 2.0)
    val toString = entry.toString()

    assertTrue(toString.contains("Entry"))
    assertTrue(toString.contains("x=1.0"))
    assertTrue(toString.contains("y=2.0"))
  }

  @Test
  fun `ColumnCartesianLayerModel toString includes key properties`() {
    val model = ColumnCartesianLayerModel(listOf(listOf(ColumnCartesianLayerModel.Entry(0, 1))))
    val toString = model.toString()

    assertTrue(toString.contains("ColumnCartesianLayerModel"))
    assertTrue(toString.contains("minX="))
    assertTrue(toString.contains("maxX="))
    assertTrue(toString.contains("minY="))
    assertTrue(toString.contains("maxY="))
    assertTrue(toString.contains("minAggregateY="))
    assertTrue(toString.contains("maxAggregateY="))
  }

  @Test
  fun `ColumnCartesianLayerModel Entry toString includes coordinates`() {
    val entry = ColumnCartesianLayerModel.Entry(3.0, 4.0)
    val toString = entry.toString()

    assertTrue(toString.contains("Entry"))
    assertTrue(toString.contains("x=3.0"))
    assertTrue(toString.contains("y=4.0"))
  }

  @Test
  fun `CandlestickCartesianLayerModel toString includes key properties`() {
    val model =
      CandlestickCartesianLayerModel.build(
        opening = listOf(1.0),
        closing = listOf(2.0),
        low = listOf(0.5),
        high = listOf(2.5),
      )
    val toString = model.toString()

    assertTrue(toString.contains("CandlestickCartesianLayerModel"))
    assertTrue(toString.contains("minX="))
    assertTrue(toString.contains("maxX="))
    assertTrue(toString.contains("minY="))
    assertTrue(toString.contains("maxY="))
  }

  @Test
  fun `CandlestickCartesianLayerModel Entry toString includes all prices`() {
    val entry =
      CandlestickCartesianLayerModel.Entry(
        x = 0,
        opening = 1.0,
        closing = 2.0,
        low = 0.5,
        high = 2.5,
        absoluteChange = CandlestickCartesianLayerModel.Change.Bullish,
        relativeChange = CandlestickCartesianLayerModel.Change.Neutral,
      )
    val toString = entry.toString()

    assertTrue(toString.contains("Entry"))
    assertTrue(toString.contains("x="))
    assertTrue(toString.contains("opening="))
    assertTrue(toString.contains("closing="))
    assertTrue(toString.contains("low="))
    assertTrue(toString.contains("high="))
    assertTrue(toString.contains("absoluteChange="))
    assertTrue(toString.contains("relativeChange="))
  }

  @Test
  fun `MutableCartesianChartRanges toString includes ranges`() {
    val ranges = MutableCartesianChartRanges()
    ranges.tryUpdate(minX = 0.0, maxX = 10.0, minY = 0.0, maxY = 100.0, axisPosition = null)
    val toString = ranges.toString()

    assertTrue(toString.contains("MutableCartesianChartRanges"))
    assertTrue(toString.contains("minX="))
    assertTrue(toString.contains("maxX="))
    assertTrue(toString.contains("xStep="))
  }

  @Test
  fun `MutableYRange toString includes y range`() {
    val yRange = MutableCartesianChartRanges.MutableYRange(0.0, 100.0)
    val toString = yRange.toString()

    assertTrue(toString.contains("MutableYRange"))
    assertTrue(toString.contains("minY="))
    assertTrue(toString.contains("maxY="))
    assertTrue(toString.contains("length="))
  }
}
