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

package com.patrykandpatrick.vico.sample.charts

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

private val data =
  listOf(
    3123,
    3115,
    3082,
    3054,
    3091,
    3137,
    3168,
    3195,
    3172,
    3148,
    3201,
    3243,
    3287,
    3310,
    3269,
    3238,
    3196,
    3221,
    3258,
    3302,
    3341,
    3389,
    3427,
    3456,
    3501,
    3481,
    3443,
    3398,
    3362,
    3319,
    3277,
    3241,
    3198,
    3162,
    3119,
    3087,
    3054,
    3092,
    3131,
    3174,
    3218,
    3261,
    3297,
    3334,
    3312,
    3278,
    3243,
    3209,
    3176,
    3142,
    3108,
    3074,
    3041,
    3078,
    3112,
    3149,
    3187,
    3224,
    3261,
    3298,
    3271,
    3236,
    3201,
    3167,
    3203,
    3238,
    3274,
    3311,
    3348,
    3385,
    3362,
    3327,
    3293,
    3259,
    3295,
    3331,
    3368,
    3405,
    3441,
    3418,
    3384,
    3349,
    3315,
    3351,
    3387,
    3423,
    3460,
    3496,
    3473,
    3438,
  )

private val startDate = LocalDate(2025, 4, 1)
private val xValues = data.indices.map { it.toDouble() }

private val DateFormat = LocalDate.Format {
  monthName(MonthNames.ENGLISH_ABBREVIATED)
  char(' ')
  day()
}

private fun niceBase(a: Double, b: Double) = 10.0.pow(floor(log10(max(abs(a), abs(b)))) - 1)

private enum class Period(val label: String, val visibleCount: Int) {
  W1("1W", 7),
  M1("1M", 30),
  M3("3M", Int.MAX_VALUE),
}

@Composable
fun ComposeZoomTransitionLineChart(modifier: Modifier = Modifier) {
  val model = remember { LineCartesianLayerModel.build { series(xValues, data) } }
  var period by remember { mutableStateOf(Period.W1) }
  val lastX = xValues.last()
  val firstX = (lastX - period.visibleCount + 1).coerceAtLeast(xValues.first())
  val visibleData = data.slice(firstX.toInt()..lastX.toInt())
  val minY = visibleData.min().toDouble()
  val maxY = visibleData.max().toDouble()
  val base = niceBase(minY, maxY)
  val scrollState =
    rememberVicoScrollState(scrollEnabled = false, initialScroll = Scroll.Absolute.End)
  val zoomState = rememberVicoZoomState(initialZoom = Zoom.x(Period.W1.visibleCount.toDouble()))

  LaunchedEffect(period) {
    zoomState.animateZoom(
      Zoom.x(period.visibleCount.toDouble()),
      tween(durationMillis = 600),
      bias = 1f,
    )
  }

  Column(modifier) {
    Row(Modifier.padding(horizontal = 16.dp)) {
      Period.entries.forEach { p ->
        FilterChip(
          selected = period == p,
          onClick = { period = p },
          label = { Text(p.label) },
          modifier = Modifier.padding(end = 8.dp),
        )
      }
    }
    CartesianChartHost(
      rememberCartesianChart(
        rememberLineCartesianLayer(
          model,
          // Scales y to the visible period’s data range, rounded to a nice grid step. When the
          // period changes, the chart animates to the new range.
          rangeProvider =
            CartesianLayerRangeProvider.fixed(
              minY = floor(minY / base) * base,
              maxY = ceil(maxY / base) * base,
            ),
        ),
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            // Labels the start and end of the visible period.
            valueFormatter =
              CartesianValueFormatter { context, value, _ ->
                val isEndLabel = value >= lastX - context.ranges.xStep * 0.5
                val x = if (isEndLabel) lastX else firstX
                LocalDate.fromEpochDays(startDate.toEpochDays() + x.toInt()).format(DateFormat)
              },
            itemPlacer = HorizontalAxis.ItemPlacer.extremes(),
          ),
      ),
      scrollState = scrollState,
      zoomState = zoomState,
    )
  }
}
