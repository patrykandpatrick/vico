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

package com.patrykandpatrick.vico.sample.previews.composables.line

import androidx.compose.runtime.Composable
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.sample.previews.annotation.ChartPreview
import com.patrykandpatrick.vico.sample.previews.resource.PreviewSurface
import com.patrykandpatrick.vico.sample.previews.resource.mediumLineModel
import com.patrykandpatrick.vico.sample.previews.resource.shortLineModel

@ChartPreview
@Composable
fun DefaultLineChart(
  model: CartesianChartModel = shortLineModel,
  scrollable: Boolean = true,
  initialScroll: Scroll.Absolute = Scroll.Absolute.Start,
) {
  PreviewSurface {
    CartesianChartHost(
      chart =
        rememberCartesianChart(
          rememberLineCartesianLayer(),
          startAxis = VerticalAxis.rememberStart(),
          bottomAxis = HorizontalAxis.rememberBottom(),
        ),
      model = model,
      scrollState = rememberVicoScrollState(scrollable, initialScroll),
    )
  }
}

@ChartPreview
@Composable
fun DefaultLineChartLongScrollable() {
  DefaultLineChart(model = mediumLineModel)
}

@ChartPreview
@Composable
fun DefaultLineChartLongScrollableEnd() {
  DefaultLineChart(model = mediumLineModel, initialScroll = Scroll.Absolute.End)
}

@ChartPreview
@Composable
fun DefaultLineChartLongNonScrollable() {
  DefaultLineChart(model = mediumLineModel, scrollable = false)
}
