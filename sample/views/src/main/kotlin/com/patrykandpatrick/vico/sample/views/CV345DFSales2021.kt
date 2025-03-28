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

package com.patrykandpatrick.vico.sample.views

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.sample.views.databinding.Cv345dfSales2021Binding

private data class Point(val x: Int, val y: Int)

private val data: List<List<Point>> =
  listOf(
    listOf(
      Point(13, 225),
      Point(17, 200),
      Point(18, 221),
      Point(32, 270),
      Point(34, 246),
      Point(38, 205),
      Point(40, 215),
    ),
    listOf(Point(17, 230)),
  )

private var colourInt: Int = 0

private val MarkerValueFormatter =
  object : DefaultCartesianMarker.ValueFormatter {
    private fun SpannableStringBuilder.append(y: Double) {
      append(
        y.toInt().toString(),
        ForegroundColorSpan(colourInt),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
      )
    }

    override fun format(
      context: CartesianDrawingContext,
      targets: List<CartesianMarker.Target>,
    ): CharSequence =
      SpannableStringBuilder().apply {
        targets.forEachIndexed { index, target ->
          (target as LineCartesianLayerMarkerTarget).points.forEachIndexed { index, point ->
            append(point.entry.y)
            if (index != target.points.lastIndex) append(", ")
          }
          if (index != targets.lastIndex) append(", ")
        }
      }
  }

@Composable
fun ViewCV345DFSales2021(modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/vmml6t.
      lineSeries {
        data.forEach { points -> series(x = points.map { it.x }, y = points.map { it.y }) }
      }
    }
  }
  val context = LocalContext.current
  colourInt = ContextCompat.getColor(context, R.color.cv345df_sales_2021_line_1_color)
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      Cv345dfSales2021Binding.inflate(inflater, parent, attachToParent).apply {
        with(chartView) {
          chart = chart!!.copy(marker = getMarker(context, MarkerValueFormatter))
          this.modelProducer = modelProducer
        }
      }
    },
    modifier,
  )
}
