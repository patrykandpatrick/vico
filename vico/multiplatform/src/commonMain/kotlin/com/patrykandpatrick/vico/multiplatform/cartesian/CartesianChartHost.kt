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

package com.patrykandpatrick.vico.multiplatform.cartesian

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.multiplatform.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.multiplatform.cartesian.data.collectAsState
import com.patrykandpatrick.vico.multiplatform.cartesian.data.component1
import com.patrykandpatrick.vico.multiplatform.cartesian.data.component2
import com.patrykandpatrick.vico.multiplatform.cartesian.data.component3
import com.patrykandpatrick.vico.multiplatform.cartesian.data.component4
import com.patrykandpatrick.vico.multiplatform.cartesian.data.defaultCartesianDiffAnimationSpec
import com.patrykandpatrick.vico.multiplatform.cartesian.data.toImmutable
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.multiplatform.common.Defaults.CHART_HEIGHT
import com.patrykandpatrick.vico.multiplatform.common.Point
import com.patrykandpatrick.vico.multiplatform.common.ValueWrapper
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.getValue
import com.patrykandpatrick.vico.multiplatform.common.setValue
import kotlinx.coroutines.launch

/**
 * Displays a [CartesianChart].
 *
 * @param chart the [CartesianChart].
 * @param modelProducer creates and updates the [CartesianChartModel].
 * @param modifier the modifier to be applied to the chart.
 * @param scrollState houses information on the [CartesianChart]’s scroll value. Allows for scroll
 *   customization and programmatic scrolling.
 * @param zoomState houses information on the [CartesianChart]’s zoom factor. Allows for zoom
 *   customization.
 * @param animationSpec the [AnimationSpec] for difference animations.
 * @param animateIn whether to run an initial animation when the [CartesianChartHost] enters
 *   composition. The animation is skipped for previews.
 * @param placeholder shown when no [CartesianChartModel] is available.
 */
@Composable
public fun CartesianChartHost(
  chart: CartesianChart,
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
  scrollState: VicoScrollState = rememberVicoScrollState(),
  zoomState: VicoZoomState = rememberDefaultVicoZoomState(scrollState.scrollEnabled),
  animationSpec: AnimationSpec<Float>? = defaultCartesianDiffAnimationSpec,
  animateIn: Boolean = true,
  placeholder: @Composable BoxScope.() -> Unit = {},
) {
  val mutableRanges = remember(chart) { MutableCartesianChartRanges() }
  val modelWrapper by modelProducer.collectAsState(chart, animationSpec, animateIn, mutableRanges)
  val (model, previousModel, ranges, extraStore) = modelWrapper

  CartesianChartHostBox(modifier) {
    if (model != null) {
      CartesianChartHostImpl(
        chart,
        model,
        scrollState,
        zoomState,
        ranges,
        previousModel,
        extraStore,
      )
    } else {
      placeholder()
    }
  }
}

/**
 * Displays a [CartesianChart]. This function accepts a [CartesianChartModel]. For dynamic data, use
 * the function overload that accepts a [CartesianChartModelProducer] instance.
 *
 * @param chart the [CartesianChart].
 * @param model the [CartesianChartModel].
 * @param modifier the modifier to be applied to the chart.
 * @param scrollState houses information on the [CartesianChart]’s scroll value. Allows for scroll
 *   customization and programmatic scrolling.
 * @param zoomState houses information on the [CartesianChart]’s zoom factor. Allows for zoom
 *   customization.
 */
@Composable
public fun CartesianChartHost(
  chart: CartesianChart,
  model: CartesianChartModel,
  modifier: Modifier = Modifier,
  scrollState: VicoScrollState = rememberVicoScrollState(),
  zoomState: VicoZoomState = rememberDefaultVicoZoomState(scrollState.scrollEnabled),
) {
  val ranges = remember(chart) { MutableCartesianChartRanges() }
  remember(ranges, chart, model) {
    ranges.reset()
    chart.updateRanges(ranges, model)
  }
  CartesianChartHostBox(modifier) {
    CartesianChartHostImpl(chart, model, scrollState, zoomState, ranges.toImmutable())
  }
}

@Composable
internal fun CartesianChartHostImpl(
  chart: CartesianChart,
  model: CartesianChartModel,
  scrollState: VicoScrollState,
  zoomState: VicoZoomState,
  ranges: CartesianChartRanges,
  previousModel: CartesianChartModel? = null,
  extraStore: ExtraStore = ExtraStore.Empty,
) {
  val pointerPosition = remember { mutableStateOf<Point?>(null) }
  val measuringContext =
    rememberCartesianMeasuringContext(
      extraStore = extraStore,
      model = model,
      ranges = ranges,
      scrollEnabled = scrollState.scrollEnabled,
      zoomEnabled = scrollState.scrollEnabled && zoomState.zoomEnabled,
      layerPadding =
        remember(chart.layerPadding, model.extraStore) { chart.layerPadding(model.extraStore) },
      pointerPosition = pointerPosition.value,
    )

  val coroutineScope = rememberCoroutineScope()
  var previousModelID by remember { ValueWrapper(model.id) }
  val layerDimensions = remember { MutableCartesianLayerDimensions() }

  LaunchedEffect(scrollState.pointerXDeltas) {
    scrollState.pointerXDeltas.collect { delta ->
      pointerPosition.value?.let { point -> pointerPosition.value = point.copy(point.x + delta) }
    }
  }

  DisposableEffect(scrollState) { onDispose { scrollState.clearUpdated() } }

  Canvas(
    modifier =
      Modifier.fillMaxSize()
        .pointerInput(
          scrollState = scrollState,
          onPointerPositionChange =
            remember(chart.marker == null) {
              if (chart.marker != null) pointerPosition.component2() else null
            },
          onZoom =
            remember(zoomState, scrollState, chart, coroutineScope) {
              if (zoomState.zoomEnabled) {
                { factor, centroid ->
                  zoomState.zoom(factor, centroid.x, scrollState.value, chart.layerBounds).let {
                    scroll ->
                    coroutineScope.launch { scrollState.scroll(scroll) }
                  }
                }
              } else {
                null
              }
            },
        )
  ) {
    if (size.isEmpty()) return@Canvas
    measuringContext.canvasSize = size

    layerDimensions.clear()
    chart.prepare(measuringContext, layerDimensions)

    if (chart.layerBounds.isEmpty) return@Canvas

    zoomState.update(measuringContext, layerDimensions, chart.layerBounds)
    scrollState.update(measuringContext, chart.layerBounds, layerDimensions)

    if (model.id != previousModelID) {
      coroutineScope.launch { scrollState.autoScroll(model, previousModel) }
      previousModelID = model.id
    }

    val drawingContext =
      CartesianDrawingContext(
        measuringContext,
        drawContext.canvas,
        layerDimensions,
        chart.layerBounds,
        scrollState.value,
        zoomState.value,
      )

    chart.draw(drawingContext)
    measuringContext.cacheStore.purge()
  }
}

@Composable
private fun CartesianChartHostBox(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
  Box(modifier = modifier.height(CHART_HEIGHT.dp).fillMaxWidth(), content = content)
}
