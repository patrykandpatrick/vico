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
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.sample.views.databinding.BasicColumnChartBinding

@Composable
fun ViewBasicColumnChart(modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  CartesianLayerRangeProvider
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/eji9zq.
      columnSeries { series(5, 6, 5, 2, 11, 8, 5, 2, 15, 11, 8, 13, 12, 10, 2, 7) }
    }
  }
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      BasicColumnChartBinding.inflate(inflater, parent, attachToParent).apply {
        chartView.modelProducer = modelProducer
      }
    },
    modifier,
  )
}
