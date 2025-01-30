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
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.candlestickSeries
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.sample.views.databinding.GoldPricesBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ceil
import kotlin.math.floor

private const val Y_STEP = 10.0

private const val MS_IN_H = 3.6e+6

private val RangeProvider =
  object : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) =
      Y_STEP * floor(minY / Y_STEP)

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
      Y_STEP * ceil(maxY / Y_STEP)
  }

private val StartAxisValueFormatter = CartesianValueFormatter.decimal(DecimalFormat("$#,###"))

private val StartAxisItemPlacer = VerticalAxis.ItemPlacer.step({ Y_STEP })

private val BottomAxisValueFormatter =
  object : CartesianValueFormatter {
    private val dateFormat =
      SimpleDateFormat("h a", Locale.US).apply { timeZone = TimeZone.getTimeZone("GMT") }

    override fun format(
      context: CartesianMeasuringContext,
      value: Double,
      verticalAxisPosition: Axis.Position.Vertical?,
    ) = dateFormat.format(value * MS_IN_H)
  }

@Composable
fun ViewGoldPrices(modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/y3c4gz.
      candlestickSeries(x, opening, closing, low, high)
    }
  }
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      GoldPricesBinding.inflate(inflater, parent, attachToParent).apply {
        with(chartView) {
          chart =
            chart!!.copy(
              (chart!!.layers[0] as CandlestickCartesianLayer).copy(rangeProvider = RangeProvider),
              startAxis =
                (chart!!.startAxis as VerticalAxis).copy(
                  valueFormatter = StartAxisValueFormatter,
                  itemPlacer = StartAxisItemPlacer,
                ),
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

private val x = (0..16).toList() + (18..23).toList()

private val opening =
  listOf<Number>(
    2634.899902,
    2635.300049,
    2630.899902,
    2628.800049,
    2623.600098,
    2624.600098,
    2623.100098,
    2629.399902,
    2635.100098,
    2618.100098,
    2623.699951,
    2613.699951,
    2612.199951,
    2618.699951,
    2619.100098,
    2620.300049,
    2621.800049,
    2620,
    2620.199951,
    2620.899902,
    2620.699951,
    2619.399902,
    2616.5,
  )

private val closing =
  listOf<Number>(
    2635.399902,
    2631.199951,
    2628.899902,
    2623.600098,
    2624.899902,
    2623.100098,
    2629.5,
    2635.100098,
    2618.300049,
    2623.699951,
    2613.600098,
    2612,
    2618.399902,
    2619,
    2620.300049,
    2621.899902,
    2620,
    2620.199951,
    2620.899902,
    2620.699951,
    2619.399902,
    2616.600098,
    2619.100098,
  )

private val low =
  listOf<Number>(
    2632,
    2630.199951,
    2627.600098,
    2621.5,
    2623.199951,
    2623.100098,
    2621.300049,
    2628.600098,
    2618,
    2616.800049,
    2611.899902,
    2608.399902,
    2612.199951,
    2616.300049,
    2616.5,
    2619.699951,
    2619.699951,
    2617.800049,
    2618.600098,
    2619.399902,
    2619.100098,
    2615.5,
    2616.300049,
  )

private val high =
  listOf<Number>(
    2636.5,
    2636.5,
    2631.899902,
    2629.600098,
    2629.699951,
    2626.899902,
    2631.699951,
    2636.199951,
    2636.899902,
    2626.800049,
    2623.899902,
    2615.699951,
    2618.899902,
    2619.699951,
    2621.699951,
    2623.199951,
    2622.100098,
    2620.899902,
    2621.800049,
    2624,
    2622.100098,
    2619.600098,
    2619.399902,
  )
