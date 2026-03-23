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

package com.patrykandpatrick.vico.sample.charts.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberLiveTimelineChartState
import com.patrykandpatrick.vico.compose.cartesian.LiveTimelineCartesianChartHost
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Fixed base so the demo is reproducible; “now” advances from here. */
private const val SIM_BASE_EPOCH_MS = 1_700_000_000_000L

private const val TICK_MS: Long = 450L

/**
 * Simulated live time-series line chart (epoch-ms _x_, _x_ ≤ simulated `now`).
 *
 * Unbounded in-memory history is intentional for this demo; running it for many hours can lead to
 * high memory use (OOM) or slow frames. The sample caps the simulated point count below.
 */
@Composable
private fun ComposeLiveTimeSeriesChart(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  var nowMs by remember { mutableLongStateOf(SIM_BASE_EPOCH_MS) }
  var visibleWindowMs by remember { mutableLongStateOf(120_000L) }
  var tick by remember { mutableIntStateOf(0) }

  val timelineState =
    rememberLiveTimelineChartState(
      visibleDurationMillis = visibleWindowMs,
      nowProvider = { nowMs },
    )

  val maxPoints = 400
  LaunchedEffect(modelProducer) {
    while (isActive) {
      delay(TICK_MS)
      tick++
      nowMs = SIM_BASE_EPOCH_MS + tick * TICK_MS
      val fromTick = (tick - maxPoints).coerceAtLeast(0)
      val xs = (fromTick..tick).map { SIM_BASE_EPOCH_MS + it * TICK_MS }
      val ys = xs.mapIndexed { i, _ -> sin((fromTick + i) * 0.07) * 12 }
      modelProducer.runTransaction { lineSeries { series(xs, ys) } }
    }
  }

  Column(modifier = modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Button(
        onClick = {
          timelineState.followController.pauseFollow =
            !timelineState.followController.pauseFollow
        }
      ) {
        Text(
          if (timelineState.followController.pauseFollow) "Resume follow" else "Pause follow"
        )
      }
      Button(onClick = { timelineState.followController.goLive() }) { Text("Go live") }
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text("Window:", modifier = Modifier.padding(top = 10.dp))
      Button(onClick = { visibleWindowMs = 60_000L }) { Text("1 min") }
      Button(onClick = { visibleWindowMs = 120_000L }) { Text("2 min") }
      Button(onClick = { visibleWindowMs = 600_000L }) { Text("10 min") }
    }
    LiveTimelineCartesianChartHost(
      chart =
        rememberCartesianChart(
          rememberLineCartesianLayer(),
          startAxis = VerticalAxis.rememberStart(),
          bottomAxis = HorizontalAxis.rememberBottom(),
        ),
      modelProducer = modelProducer,
      state = timelineState,
      modifier = Modifier.height(220.dp).fillMaxWidth(),
    )
  }
}

@Composable
fun ComposeLiveTimeSeriesChart(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  ComposeLiveTimeSeriesChart(modelProducer, modifier)
}

@Composable
@Preview
private fun ComposeLiveTimeSeriesChartPreview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  runBlocking?.invoke {
    modelProducer.runTransaction {
      lineSeries {
        series(
          listOf(SIM_BASE_EPOCH_MS, SIM_BASE_EPOCH_MS + TICK_MS),
          listOf(0.0, 1.0),
        )
      }
    }
  }
  PreviewBox { ComposeLiveTimeSeriesChart(modelProducer) }
}
