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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.*
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController.Lock
import com.patrykandpatrick.vico.compose.cartesian.marker.Interaction
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.Defaults.CHART_HEIGHT
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlin.math.abs
import kotlinx.coroutines.flow.merge
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
 * @param onViewportMeasured invoked after each measure/draw pass with the current horizontal
 *   viewport geometry. Avoid storing this in Compose state from the callback (it runs every frame).
 * @param onHorizontalScrollDragStarted invoked when a user-driven horizontal scroll drag starts.
 * @param horizontalPointerFlingEnabled whether to run inertial horizontal scrolling after pointer
 *   drag on platforms that add it (desktop and web targets).
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
  onViewportMeasured: ((CartesianViewportSnapshot) -> Unit)? = null,
  onHorizontalScrollDragStarted: (() -> Unit)? = null,
  horizontalPointerFlingEnabled: Boolean = true,
) {
  val mutableRanges = remember { MutableCartesianChartRanges() }
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
        onViewportMeasured = onViewportMeasured,
        onHorizontalScrollDragStarted = onHorizontalScrollDragStarted,
        horizontalPointerFlingEnabled = horizontalPointerFlingEnabled,
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
 * @param onViewportMeasured invoked after each measure/draw pass with the current horizontal
 *   viewport geometry. Avoid storing this in Compose state from the callback (it runs every frame).
 * @param onHorizontalScrollDragStarted invoked when a user-driven horizontal scroll drag starts.
 * @param horizontalPointerFlingEnabled whether to run inertial horizontal scrolling after pointer
 *   drag on platforms that add it (desktop and web targets).
 */
@Composable
public fun CartesianChartHost(
  chart: CartesianChart,
  model: CartesianChartModel,
  modifier: Modifier = Modifier,
  scrollState: VicoScrollState = rememberVicoScrollState(),
  zoomState: VicoZoomState = rememberDefaultVicoZoomState(scrollState.scrollEnabled),
  onViewportMeasured: ((CartesianViewportSnapshot) -> Unit)? = null,
  onHorizontalScrollDragStarted: (() -> Unit)? = null,
  horizontalPointerFlingEnabled: Boolean = true,
) {
  val ranges = remember { MutableCartesianChartRanges() }
  remember(chart, model) {
    ranges.reset()
    chart.updateRanges(ranges, model)
  }
  CartesianChartHostBox(modifier) {
    CartesianChartHostImpl(
      chart,
      model,
      scrollState,
      zoomState,
      ranges.toImmutable(),
      onViewportMeasured = onViewportMeasured,
      onHorizontalScrollDragStarted = onHorizontalScrollDragStarted,
      horizontalPointerFlingEnabled = horizontalPointerFlingEnabled,
    )
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
  onViewportMeasured: ((CartesianViewportSnapshot) -> Unit)? = null,
  onHorizontalScrollDragStarted: (() -> Unit)? = null,
  horizontalPointerFlingEnabled: Boolean = true,
) {
  var markerX by rememberSaveable { mutableStateOf<Double?>(null) }
  var markerSeriesIndex by rememberSaveable { mutableStateOf<Int?>(null) }
  var lastAcceptedInteraction by
    rememberSaveable(saver = Interaction.Saver) { mutableStateOf(null) }
  val measuringContext =
    rememberCartesianMeasuringContext(
      extraStore = extraStore,
      model = model,
      ranges = ranges,
      scrollEnabled = scrollState.scrollEnabled,
      zoomEnabled = scrollState.scrollEnabled && zoomState.zoomEnabled,
      layerPadding =
        remember(chart.layerPadding, model.extraStore) { chart.layerPadding(model.extraStore) },
      markerX = markerX,
      markerSeriesIndex = markerSeriesIndex,
    )

  val coroutineScope = rememberCoroutineScope()
  var lastHandledModel by remember { ValueWrapper(model) }
  val layerDimensions = remember { MutableCartesianLayerDimensions() }
  val scrollInteractionSource = remember { MutableInteractionSource() }

  LaunchedEffect(onHorizontalScrollDragStarted, scrollInteractionSource) {
    if (onHorizontalScrollDragStarted != null) {
      scrollInteractionSource.interactions.collect { interaction ->
        if (interaction is DragInteraction.Start) {
          onHorizontalScrollDragStarted.invoke()
        }
      }
    }
  }

  val onInteraction =
    remember(chart, layerDimensions, scrollState, ranges) {
      if (chart.marker != null) {
        { interaction: Interaction ->
          val x =
            measuringContext.value.pointerPositionToX(
              interaction.point,
              layerDimensions,
              chart.layerBounds,
              scrollState.value,
              ranges,
            )
          val targets =
            chart.getMarkerTargets(
              x,
              measuringContext.value.getVisibleXRange(
                layerDimensions,
                chart.layerBounds,
                scrollState.value,
              ),
            )
          val narrowedTargets: List<CartesianMarker.Target>
          val seriesIndex: Int?
          if (targets.isNotEmpty()) {
            val closestIndex =
              targets.indices.minBy { abs(targets[it].canvasX - interaction.point.x) }
            if (targets.distinctBy { it.canvasX }.size > 1) {
              narrowedTargets = listOf(targets[closestIndex])
              seriesIndex = closestIndex
            } else {
              narrowedTargets = targets
              seriesIndex = null
            }
          } else {
            narrowedTargets = targets
            seriesIndex = null
          }
          if (chart.markerController.shouldAcceptInteraction(interaction, narrowedTargets)) {
            val shouldShow = chart.markerController.shouldShowMarker(interaction, narrowedTargets)
            lastAcceptedInteraction = interaction
            if (shouldShow && narrowedTargets.isNotEmpty()) {
              markerX = narrowedTargets.first().x
              markerSeriesIndex = seriesIndex
            } else {
              markerX = null
              markerSeriesIndex = null
            }
          }
        }
      } else {
        null
      }
    }

  fun onViewportChange() {
    lastAcceptedInteraction
      ?.takeIf { chart.markerController.lock == Lock.Position }
      ?.let { onInteraction?.invoke(it) }
  }

  LaunchedEffect(model) { onViewportChange() }

  LaunchedEffect(scrollState.consumedXDeltas, scrollState.unconsumedXDeltas) {
    merge(scrollState.consumedXDeltas, scrollState.unconsumedXDeltas).collect { onViewportChange() }
  }

  LaunchedEffect(zoomState, scrollState) {
    zoomState.pendingScroll.collect { (scroll, maxValue) ->
      scrollState.scroll(scroll, maxValue)
      onViewportChange()
    }
  }

  DisposableEffect(scrollState) { onDispose { scrollState.clearUpdated() } }

  Canvas(
    modifier =
      Modifier.fillMaxSize()
        .pointerInput(
          scrollState = scrollState,
          scrollInteractionSource = scrollInteractionSource,
          horizontalPointerFlingEnabled = horizontalPointerFlingEnabled,
          consumeMoveEvents = chart.markerController.consumeMoveEvents,
          onInteraction = onInteraction,
          onZoom =
            remember(zoomState, scrollState, coroutineScope) {
              if (zoomState.zoomEnabled) {
                { factor, centroid ->
                  coroutineScope.launch { zoomState.zoom(factor, centroid.x) { scrollState.value } }
                }
              } else {
                null
              }
            },
          longPressEnabled = chart.markerController.acceptsLongPress,
        )
  ) {
    if (size.isEmpty()) return@Canvas
    measuringContext.value.canvasSize = size

    layerDimensions.clear()
    chart.prepare(measuringContext.value, layerDimensions)

    if (chart.layerBounds.isEmpty) return@Canvas

    zoomState.update(measuringContext.value, layerDimensions, chart.layerBounds, scrollState.value)
    scrollState.update(measuringContext.value, chart.layerBounds, layerDimensions)

    onViewportMeasured?.invoke(
      CartesianViewportSnapshot(
        scrollValue = scrollState.value,
        maxScrollValue = scrollState.maxValue,
        fullXRangeStart = measuringContext.value.getFullXRange(layerDimensions).start,
        layoutDirectionMultiplier = measuringContext.value.layoutDirectionMultiplier,
        xSpacing = layerDimensions.xSpacing,
        xStep = measuringContext.value.ranges.xStep,
        layerBoundsWidth = chart.layerBounds.width,
      )
    )

    if (model != lastHandledModel) {
      coroutineScope.launch { scrollState.autoScroll(model, previousModel) }
      lastHandledModel = model
    }

    val drawingContext =
      CartesianDrawingContext(
        measuringContext.value,
        drawContext.canvas,
        layerDimensions,
        chart.layerBounds,
        scrollState.value,
        zoomState.value,
        MutableDrawScope(this),
      )

    chart.draw(drawingContext)
    measuringContext.value.cacheStore.purge()
  }
}

@Composable
private fun CartesianChartHostBox(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
  Box(modifier = modifier.heightIn(max = CHART_HEIGHT.dp).fillMaxWidth(), content = content)
}
