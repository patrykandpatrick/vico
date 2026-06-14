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

package com.patrykandpatrick.vico.sample.charts.views

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.graphics.ColorUtils
import com.patrykandpatrick.vico.sample.charts.views.databinding.RevealLineChartBinding
import com.patrykandpatrick.vico.views.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.views.cartesian.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.views.cartesian.data.lineModel
import com.patrykandpatrick.vico.views.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.views.common.Fill
import com.patrykandpatrick.vico.views.common.shader.ShaderProvider

@Composable
fun ViewRevealLineChart(modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      lineModel { series(4, 9, 2, 11, 7, 13, 5, 14, 3, 10, 8, 15, 6, 12, 1, 11) }
    }
  }
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      val lineColor = 0xff3287ff.toInt()
      val lineProvider =
        LineCartesianLayer.LineProvider.series(
          LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
            areaFill =
              LineCartesianLayer.AreaFill.single(
                Fill(
                  ShaderProvider.verticalGradient(
                    ColorUtils.setAlphaComponent(lineColor, 102),
                    Color.TRANSPARENT,
                  )
                )
              ),
          )
        )
      RevealLineChartBinding.inflate(inflater, parent, attachToParent).apply {
        with(chartView) {
          chart =
            chart!!.copy(
              (chart!!.layers[0] as LineCartesianLayer).copy(
                lineProvider = lineProvider,
                drawingModelInterpolator = CartesianLayerDrawingModelInterpolator.line(sweep = true),
              )
            )
          this.modelProducer = modelProducer
        }
      }
    },
    modifier,
  )
}
