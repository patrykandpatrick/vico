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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Animation
import com.patrykandpatrick.vico.compose.common.ChartHostBox
import com.patrykandpatrick.vico.compose.common.Defaults
import com.patrykandpatrick.vico.compose.common.MutableDrawScope
import com.patrykandpatrick.vico.compose.common.NEW_PIE_PRODUCER_ERROR_MESSAGE
import com.patrykandpatrick.vico.compose.common.ValueWrapper
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import kotlinx.coroutines.flow.collectLatest

internal val defaultPieDiffAnimationSpec: AnimationSpec<Float> =
  tween(durationMillis = Animation.PIE_DIFF_DURATION)

/**
 * Displays a [PieChart].
 *
 * @param chart the [PieChart].
 * @param modelProducer creates and updates the [PieChartModel].
 * @param modifier the modifier to be applied to the chart.
 * @param animationSpec the [AnimationSpec] for difference animations.
 * @param animateIn whether to run an initial animation when the [PieChartHost] enters composition.
 *   The animation is skipped for previews.
 * @param chartAreaHeight the default diameter of the pie, to which the heights of the legend and
 *   other components are added. Used only when the height isn’t otherwise constrained (e.g., via
 *   [Modifier.height]).
 * @param placeholder shown when no [PieChartModel] is available.
 */
@Composable
public fun PieChartHost(
  chart: PieChart,
  modelProducer: PieChartModelProducer,
  modifier: Modifier = Modifier,
  animationSpec: AnimationSpec<Float>? = defaultPieDiffAnimationSpec,
  animateIn: Boolean = true,
  chartAreaHeight: Dp = Defaults.PIE_CHART_AREA_HEIGHT.dp,
  placeholder: @Composable BoxScope.() -> Unit = {},
) {
  val previousHashCode = remember { ValueWrapper<Int?>(null) }
  val hashCode = modelProducer.hashCode()
  check(previousHashCode.value == null || hashCode == previousHashCode.value) {
    NEW_PIE_PRODUCER_ERROR_MESSAGE
  }
  previousHashCode.value = hashCode
  val isInPreview = LocalInspectionMode.current
  val initialModel = remember { if (isInPreview) modelProducer.getCachedModel() else null }
  var currentModel by remember { mutableStateOf(initialModel) }
  var drawingModel by remember { mutableStateOf(initialModel?.toDrawingModel()) }

  LaunchedEffect(modelProducer, chart.id, animationSpec, animateIn, isInPreview) {
    modelProducer.models.collectLatest { model ->
      chart.drawingModelInterpolator.setModels(drawingModel, model?.toDrawingModel())
      currentModel = model
      if (model == null) {
        drawingModel = null
      } else if (animationSpec != null && !isInPreview && (drawingModel != null || animateIn)) {
        animate(initialValue = 0f, targetValue = 1f, animationSpec = animationSpec) { value, _ ->
          drawingModel = chart.drawingModelInterpolator.transform(value)
        }
      } else {
        drawingModel = chart.drawingModelInterpolator.transform(1f)
      }
    }
  }

  val model = currentModel
  val dm = drawingModel
  if (model == null || dm == null) {
    ChartHostBox(modifier, chartAreaHeight, measureExtras = null) { placeholder() }
  } else {
    PieChartHostImpl(chart, model, dm, modifier, chartAreaHeight)
  }
}

/**
 * Displays a [PieChart].
 *
 * @param chart the [PieChart].
 * @param model the [PieChartModel].
 * @param modifier the modifier to be applied to the chart.
 * @param chartAreaHeight the default diameter of the pie, to which the heights of the legend and
 *   other components are added. Used only when the height isn’t otherwise constrained (e.g., via
 *   [Modifier.height]).
 */
@Composable
public fun PieChartHost(
  chart: PieChart,
  model: PieChartModel,
  modifier: Modifier = Modifier,
  chartAreaHeight: Dp = Defaults.PIE_CHART_AREA_HEIGHT.dp,
) {
  PieChartHostImpl(chart, model, model.toDrawingModel(), modifier, chartAreaHeight)
}

@Composable
internal fun PieChartHostImpl(
  chart: PieChart,
  model: PieChartModel,
  drawingModel: PieChartDrawingModel,
  modifier: Modifier,
  chartAreaHeight: Dp,
) {
  val measuringContext = rememberPieChartMeasuringContext(model, model.extraStore)
  val measureExtras =
    remember(chart, measuringContext, chartAreaHeight) {
      { widthPx: Int ->
        val context = measuringContext.value
        val width = widthPx.toFloat()
        context.canvasWidth = width
        context.canvasSize = Size(width, with(context) { chartAreaHeight.pixels })
        chart.getLegendHeight(context)
      }
    }

  ChartHostBox(modifier, chartAreaHeight, measureExtras) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      if (size.isEmpty()) return@Canvas
      measuringContext.value.canvasWidth = size.width
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
}
