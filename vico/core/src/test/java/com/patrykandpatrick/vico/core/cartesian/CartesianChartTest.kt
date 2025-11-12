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

package com.patrykandpatrick.vico.core.cartesian

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Xfermode
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Point
import com.patrykandpatrick.vico.core.common.component.LineComponent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkConstructor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

public class CartesianChartTest {

  @BeforeEach
  public fun setUp() {
    MockKAnnotations.init(this, relaxUnitFun = true)
    mockkConstructor(Paint::class)
    every { anyConstructed<Paint>().setColor(any()) } returns Unit
    every { anyConstructed<Paint>().setXfermode(any()) } returns Xfermode()
    every { anyConstructed<Paint>().style = any() } returns Unit
    every { anyConstructed<Paint>().strokeCap = any() } returns Unit
    mockkConstructor(RectF::class)
    every { anyConstructed<RectF>() == any() } returns true
    every { anyConstructed<RectF>().hashCode() } returns 0
  }

  @Test
  public fun `Given two the same CartesianChart instances are created, when they are compared, then they are NOT equal`() {
    val chart1 = getCartesianChart()
    val chart2 = getCartesianChart()

    Assertions.assertNotSame(chart1, chart2)
    Assertions.assertNotSame(chart1.hashCode(), chart2.hashCode())
  }

  @Test
  public fun `Given CartesianChart is copied without any changes, when it is compared to the original, then they are equal`() {
    val chart = getCartesianChart()

    val copiedChart = chart.copy()

    Assertions.assertEquals(chart, copiedChart)
    Assertions.assertEquals(chart.hashCode(), copiedChart.hashCode())
  }

  @Test
  public fun `Given CartesianChart is copied with changes, when it is compared to the original, then they are NOT equal`() {
    val chart = getCartesianChart()

    val copiedChart = chart.copy(layerPadding = { CartesianLayerPadding(10f, 10f, 10f, 10f) })

    Assertions.assertNotEquals(chart, copiedChart)
    Assertions.assertNotEquals(chart.hashCode(), copiedChart.hashCode())
  }

  @Test
  public fun `Given marker targets from multiple layers at the same x position with slightly different canvasX values, when getMarkerTargets is called, then all targets are returned`() {
    // Create a custom chart that exposes markerTargets for testing
    val chart =
      object :
        CartesianChart(
          LineCartesianLayer(
            lineProvider =
              LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill.Black))
              )
          ),
          ColumnCartesianLayer(
            columnProvider = ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill.Black))
          ),
          marker =
            object : CartesianMarker {
              override fun drawOverLayers(
                context: CartesianDrawingContext,
                targets: List<CartesianMarker.Target>,
              ) {}
            },
        ) {
        @Suppress("UNCHECKED_CAST")
        fun setTestMarkerTargets(targets: Map<Double, List<CartesianMarker.Target>>) {
          (markerTargets as MutableMap<Double, MutableList<CartesianMarker.Target>>).clear()
          targets.forEach { (x, targetList) ->
            (markerTargets as MutableMap<Double, MutableList<CartesianMarker.Target>>)
              .getOrPut(x) { mutableListOf() }
              .addAll(targetList)
          }
        }
      }

    // Create test targets from different layers at the same x position
    // with slightly different canvasX values (simulating floating point precision differences)
    val lineTarget =
      object : LineCartesianLayerMarkerTarget {
        override val x: Double = 5.0
        override val canvasX: Float = 100.0f
        override val points: List<LineCartesianLayerMarkerTarget.Point> = emptyList()
      }

    val columnTarget =
      object : ColumnCartesianLayerMarkerTarget {
        override val x: Double = 5.0
        override val canvasX: Float = 100.0001f // Slightly different canvasX
        override val columns: List<ColumnCartesianLayerMarkerTarget.Column> = emptyList()
      }

    // Set up the marker targets
    chart.setTestMarkerTargets(mapOf(5.0 to listOf(lineTarget, columnTarget)))

    // Get marker targets at a pointer position near the targets
    val result = chart.getMarkerTargets(Point(100.0f, 50.0f))

    // Both targets should be returned
    Assertions.assertEquals(2, result.size)
    Assertions.assertTrue(result.any { it is LineCartesianLayerMarkerTarget })
    Assertions.assertTrue(result.any { it is ColumnCartesianLayerMarkerTarget })
  }

  @Test
  public fun `Given marker targets from multiple layers at the same x position with significantly different canvasX values, when getMarkerTargets is called, then only closest targets are returned`() {
    // Create a custom chart that exposes markerTargets for testing
    val chart =
      object :
        CartesianChart(
          LineCartesianLayer(
            lineProvider =
              LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill.Black))
              )
          ),
          ColumnCartesianLayer(
            columnProvider = ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill.Black))
          ),
          marker =
            object : CartesianMarker {
              override fun drawOverLayers(
                context: CartesianDrawingContext,
                targets: List<CartesianMarker.Target>,
              ) {}
            },
        ) {
        @Suppress("UNCHECKED_CAST")
        fun setTestMarkerTargets(targets: Map<Double, List<CartesianMarker.Target>>) {
          (markerTargets as MutableMap<Double, MutableList<CartesianMarker.Target>>).clear()
          targets.forEach { (x, targetList) ->
            (markerTargets as MutableMap<Double, MutableList<CartesianMarker.Target>>)
              .getOrPut(x) { mutableListOf() }
              .addAll(targetList)
          }
        }
      }

    // Create test targets from different layers at the same x position
    // with significantly different canvasX values (beyond tolerance)
    val lineTarget =
      object : LineCartesianLayerMarkerTarget {
        override val x: Double = 5.0
        override val canvasX: Float = 100.0f
        override val points: List<LineCartesianLayerMarkerTarget.Point> = emptyList()
      }

    val columnTarget =
      object : ColumnCartesianLayerMarkerTarget {
        override val x: Double = 5.0
        override val canvasX: Float = 105.0f // Significantly different canvasX
        override val columns: List<ColumnCartesianLayerMarkerTarget.Column> = emptyList()
      }

    // Set up the marker targets
    chart.setTestMarkerTargets(mapOf(5.0 to listOf(lineTarget, columnTarget)))

    // Get marker targets at a pointer position near the lineTarget
    val result = chart.getMarkerTargets(Point(100.0f, 50.0f))

    // Only the lineTarget should be returned (it's closer)
    Assertions.assertEquals(1, result.size)
    Assertions.assertTrue(result.any { it is LineCartesianLayerMarkerTarget })
    Assertions.assertFalse(result.any { it is ColumnCartesianLayerMarkerTarget })
  }

  private companion object {
    fun getCartesianChart(): CartesianChart =
      CartesianChart(
        LineCartesianLayer(
          lineProvider =
            LineCartesianLayer.LineProvider.series(
              LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill.Black))
            )
        ),
        ColumnCartesianLayer(
          columnProvider = ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill.Black))
        ),
        startAxis = VerticalAxis.start(LineComponent(Fill.Black)),
        bottomAxis = HorizontalAxis.bottom(LineComponent(Fill.Black)),
        markerVisibilityListener = object : CartesianMarkerVisibilityListener {},
        fadingEdges = FadingEdges(),
      )
  }
}
