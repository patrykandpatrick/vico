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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.sample.views.databinding.RockMetalRatiosBinding
import java.text.DecimalFormat

private val data =
  mapOf("Ag" to 22378, "Mo" to 4478, "U" to 3624, "Sn" to 2231, "Li" to 1634, "W" to 1081)

private const val Y_DIVISOR = 1000

private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()

private val YDecimalFormat = DecimalFormat("#.##K")

private val StartAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
  YDecimalFormat.format(value / Y_DIVISOR)
}

private val BottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
  context.model.extraStore[BottomAxisLabelKey][x.toInt()]
}

@Composable
fun ViewRockMetalRatios(modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/eji9zq.
      columnSeries { series(data.values) }
      extras { it[BottomAxisLabelKey] = data.keys.toList() }
    }
  }
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      RockMetalRatiosBinding.inflate(inflater, parent, attachToParent).apply {
        with(chartView) {
          chart =
            chart!!.copy(
              startAxis =
                (chart!!.startAxis as VerticalAxis).copy(valueFormatter = StartAxisValueFormatter),
              bottomAxis =
                (chart!!.bottomAxis as HorizontalAxis).copy(
                  valueFormatter = BottomAxisValueFormatter
                ),
            )
          this.modelProducer = modelProducer
        }
      }
    },
    modifier,
  )
}
