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

package com.patrykandpatrick.vico.sample.previews.composables.column

import androidx.compose.runtime.Composable
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.sample.previews.annotation.ChartPreview
import com.patrykandpatrick.vico.sample.previews.resource.PreviewSurface
import com.patrykandpatrick.vico.sample.previews.resource.mediumColumnModel
import com.patrykandpatrick.vico.sample.previews.resource.shortColumnModel

@ChartPreview
@Composable
public fun DefaultColumnChart(
  model: CartesianChartModel = shortColumnModel,
  scrollable: Boolean = true,
  initialScroll: Scroll.Absolute = Scroll.Absolute.Start,
  autoScrollCondition: AutoScrollCondition = AutoScrollCondition.Never,
) {
  PreviewSurface {
    CartesianChartHost(
      chart =
        rememberCartesianChart(
          rememberColumnCartesianLayer(),
          startAxis = rememberStartAxis(),
          bottomAxis = rememberBottomAxis(),
        ),
      model = model,
      scrollState =
        rememberVicoScrollState(
          scrollEnabled = scrollable,
          initialScroll = initialScroll,
          autoScrollCondition = autoScrollCondition,
        ),
    )
  }
}

@ChartPreview
@Composable
fun DefaultColumnChartLongScrollable() {
  DefaultColumnChart(model = mediumColumnModel)
}

@ChartPreview
@Composable
fun DefaultColumnChartLongScrollableEnd() {
  DefaultColumnChart(model = mediumColumnModel, initialScroll = Scroll.Absolute.End)
}

@ChartPreview
@Composable
fun DefaultColumnChartLongNonScrollable() {
  DefaultColumnChart(model = mediumColumnModel, scrollable = false)
}
