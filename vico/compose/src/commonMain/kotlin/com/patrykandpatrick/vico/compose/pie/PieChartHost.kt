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

@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.patrykandpatrick.vico.compose.pie

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Animation
import com.patrykandpatrick.vico.compose.common.Defaults.CHART_HEIGHT
import com.patrykandpatrick.vico.compose.common.MutableDrawScope
import com.patrykandpatrick.vico.compose.common.NEW_PIE_PRODUCER_ERROR_MESSAGE
import com.patrykandpatrick.vico.compose.common.ValueWrapper
import kotlinx.coroutines.flow.collectLatest

internal val defaultPieDiffAnimationSpec: AnimationSpec<Float> =
  tween(durationMillis = Animation.PIE_DIFF_DURATION)

/** Displays a [PieChart]. */
@Composable
public fun PieChartHost(
  chart: PieChart,
  modelProducer: PieChartModelProducer,
  modifier: Modifier = Modifier,
  animationSpec: AnimationSpec<Float>? = defaultPieDiffAnimationSpec,
  animateIn: Boolean = true,
  placeholder: @Composable BoxScope.() -> Unit = {},
) {
  val previousHashCode = remember { ValueWrapper<Int?>(null) }
  val hashCode = modelProducer.hashCode()
  check(previousHashCode.value == null || hashCode == previousHashCode.value) {
    NEW_PIE_PRODUCER_ERROR_MESSAGE
  }
  previousHashCode.value = hashCode
  var currentModel by remember { mutableStateOf<PieChartModel?>(null) }
  var currentDrawingModel by remember { mutableStateOf<PieChartDrawingModel?>(null) }
  var animationFraction by remember { mutableFloatStateOf(if (animateIn) 0f else 1f) }
  val isInPreview = LocalInspectionMode.current

  LaunchedEffect(modelProducer, chart.id, animationSpec, animateIn, isInPreview) {
    modelProducer.models.collectLatest { model ->
      val oldDrawingModel = currentDrawingModel
      currentModel = model
      chart.prepareForTransformation(oldDrawingModel, model)
      if (model == null) {
        currentDrawingModel = null
        animationFraction = 0f
      } else if (animationSpec != null && !isInPreview && (oldDrawingModel != null || animateIn)) {
        animationFraction = 0f
        currentDrawingModel = chart.transform(animationFraction)
        animate(initialValue = 0f, targetValue = 1f, animationSpec = animationSpec) { value, _ ->
          animationFraction = value
        }
      } else {
        animationFraction = 1f
        currentDrawingModel = chart.transform(animationFraction)
      }
    }
  }

  LaunchedEffect(chart.id, currentModel?.id, animationFraction) {
    currentDrawingModel =
      if (currentModel == null) {
        null
      } else {
        chart.transform(animationFraction)
      }
  }

  PieChartHostBox(modifier) {
    val model = currentModel
    val drawingModel = currentDrawingModel
    if (model == null || drawingModel == null) {
      placeholder()
    } else {
      PieChartHostImpl(chart, model, drawingModel)
    }
  }
}

/** Displays a [PieChart]. */
@Composable
public fun PieChartHost(chart: PieChart, model: PieChartModel, modifier: Modifier = Modifier) {
  PieChartHostBox(modifier) { PieChartHostImpl(chart, model, model.toDrawingModel()) }
}

@Composable
internal fun PieChartHostImpl(
  chart: PieChart,
  model: PieChartModel,
  drawingModel: PieChartDrawingModel,
) {
  val measuringContext = rememberPieChartMeasuringContext(model, model.extraStore)

  Canvas(modifier = Modifier.fillMaxSize()) {
    if (size.isEmpty()) return@Canvas
    measuringContext.value.canvasSize = size
    val mutableDrawScope = MutableDrawScope(this)
    val legendHeight = chart.getLegendHeight(measuringContext.value)
    val chartBounds = Rect(0f, 0f, size.width, size.height - legendHeight)
    if (chartBounds.isEmpty) return@Canvas
    chart.bounds = chartBounds
    chart.legend?.setBounds(0f, chartBounds.bottom, size.width, chartBounds.bottom + legendHeight)
    val drawingContext =
      PieChartDrawingContext(
        measuringContext.value,
        drawContext.canvas,
        chartBounds,
        mutableDrawScope,
      )
    chart.draw(drawingContext, drawingModel)
    measuringContext.value.cacheStore.purge()
  }
}

@Composable
private fun PieChartHostBox(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
  Box(modifier = modifier.heightIn(max = CHART_HEIGHT.dp).fillMaxWidth(), content = content)
}
