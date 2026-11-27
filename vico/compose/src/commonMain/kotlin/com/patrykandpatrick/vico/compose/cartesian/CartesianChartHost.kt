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
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.*
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController.Lock
import com.patrykandpatrick.vico.compose.cartesian.marker.Interaction
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.data.CacheStore
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/**
 * The maximum horizontal distance, in pixels, within which two [CartesianMarker.Target]s are
 * considered to share a position. [CartesianMarker.Target]s for the same _x_ can have marginally
 * different [CartesianMarker.Target.canvasX] values across layers because of floating-point error,
 * so an exact comparison would spuriously treat them as separate positions. This is well below the
 * spacing between genuinely distinct positions (e.g., grouped columns).
 */
private const val MARKER_POSITION_TOLERANCE = 0.5f

/**
 * Narrows [targets] to the one closest to [pointX] when they span more than one horizontal
 * position, returning it together with its index. When all [targets] share a position (within
 * [MARKER_POSITION_TOLERANCE]), they’re returned unchanged with a `null` index. This keeps the
 * tapped column selected in grouped-column charts while preserving every target in, e.g., a column–
 * line combo chart, where the column and line share an _x_.
 */
internal fun narrowMarkerTargets(
  targets: List<CartesianMarker.Target>,
  pointX: Float,
): Pair<List<CartesianMarker.Target>, Int?> {
  if (targets.isEmpty()) return targets to null
  if (targets.maxOf { it.canvasX } - targets.minOf { it.canvasX } <= MARKER_POSITION_TOLERANCE) {
    return targets to null
  }
  val closestIndex = targets.indices.minBy { abs(targets[it].canvasX - pointX) }
  return listOf(targets[closestIndex]) to closestIndex
}

/** The [CoroutineContext] on which [CartesianChartHost] derives charts. Tests replace this. */
internal val LocalCartesianChartBackgroundContext =
  staticCompositionLocalOf<CoroutineContext> { Dispatchers.Default }

/**
 * Displays a [CartesianChart]. The [CartesianChart]’s layers carry the data, so to update the data,
 * pass a new [CartesianChart] (e.g., via [rememberCartesianChart] with new layers).
 *
 * The first [CartesianChart] with layers is drawn in the first frame, as are [CartesianChart]s
 * whose layers’ data is unchanged (e.g., after a theme switch). For other updates, the ranges and
 * drawing models are derived off the main thread; meanwhile, the previous [CartesianChart] remains
 * on screen. If [CartesianChart]s arrive faster than they can be processed, intermediate ones are
 * skipped. A [CartesianChart] whose layers have empty models draws an empty coordinate plane (axes,
 * guidelines, decorations, and so on); see [CartesianLayerRangeProvider] for its ranges. A
 * [CartesianChart] with no layers takes up space but draws nothing.
 *
 * @param chart the [CartesianChart].
 * @param modifier the modifier to be applied to the chart.
 * @param scrollState houses information on the [CartesianChart]’s scroll value. Allows for scroll
 *   customization and programmatic scrolling.
 * @param zoomState houses information on the [CartesianChart]’s zoom factor. Allows for zoom
 *   customization.
 * @param animationSpec the [AnimationSpec] for difference animations: data updates (including the
 *   addition and removal of layers and series) and settings changes that alter what the layers draw
 *   (e.g., a new [CartesianLayerRangeProvider]). Pure style changes apply at once. Set to `null` to
 *   apply all updates without animation.
 * @param initialAnimationSpec the [AnimationSpec] for the reveal animation, which runs when layers
 *   appear in a chart that had nothing to draw (on the first data and whenever data returns after
 *   the chart was emptied). Defaults to [animationSpec]. Set to `null` to skip it. It’s also
 *   skipped in previews and for the first chart shown after the host is restored (e.g., after a
 *   configuration change).
 * @param chartAreaHeight the default height of the coordinate system, to which the heights of the
 *   legend, the marker, and other components are added. Used only when the height isn’t otherwise
 *   constrained (e.g., via [Modifier.height]).
 */
@Composable
public fun CartesianChartHost(
  chart: CartesianChart,
  modifier: Modifier = Modifier,
  scrollState: VicoScrollState = rememberVicoScrollState(),
  zoomState: VicoZoomState = rememberDefaultVicoZoomState(scrollState.scrollEnabled),
  animationSpec: AnimationSpec<Float>? = defaultCartesianDiffAnimationSpec,
  initialAnimationSpec: AnimationSpec<Float>? = animationSpec,
  chartAreaHeight: Dp = Defaults.CARTESIAN_CHART_AREA_HEIGHT.dp,
) {
  val coroutineScope = rememberCoroutineScope()
  val backgroundContext = LocalCartesianChartBackgroundContext.current
  var hasShownData by rememberSaveable { mutableStateOf(false) }
  // The pipeline lives as long as the host: the coroutine scope is stable, and the callbacks below
  // capture this instance. `hasShownData` is `true` here only when the host is restored (e.g.,
  // after
  // a configuration change), and only the pipeline’s first commit skips the initial animation.
  val pipeline = remember {
    CartesianChartPipeline(coroutineScope, backgroundContext).also {
      it.skipInitialAnimation = hasShownData
    }
  }
  var markerX by rememberSaveable { mutableStateOf<Double?>(null) }
  var markerSeriesIndex by rememberSaveable { mutableStateOf<Int?>(null) }
  var lastAcceptedInteraction by
    rememberSaveable(saver = Interaction.Saver) { mutableStateOf(null) }

  // The settings are passed to the pipeline (and applied after composition) rather than assigned
  // here, so a composition that’s never applied changes nothing.
  val settings =
    CartesianChartPipeline.Settings(
      animationSpec,
      initialAnimationSpec,
      LocalInspectionMode.current,
    )
  val commit = pipeline.prepare(chart, settings)
  SideEffect {
    pipeline.apply(commit, settings)
    if (chart.layers.isNotEmpty()) hasShownData = true
  }

  val fontFamilyResolver = LocalFontFamilyResolver.current
  val density = LocalDensity.current
  val layoutDirection = LocalLayoutDirection.current
  val measuringContext = remember {
    MutableCartesianMeasuringContext(
      canvasWidth = 0f,
      fontFamilyResolver = fontFamilyResolver,
      density = density,
      layoutDirection = layoutDirection,
      chartModel = CartesianChartModel(emptyList()),
      ranges = CartesianChartRanges.Empty,
      scrollEnabled = false,
      zoomEnabled = false,
      layerPadding = CartesianLayerPadding(),
      markerX = null,
      markerSeriesIndex = null,
      cacheStore = CacheStore(),
    )
  }
  measuringContext.fontFamilyResolver = fontFamilyResolver
  measuringContext.density = density
  measuringContext.layoutDirection = layoutDirection

  fun MutableCartesianMeasuringContext.update(
    frame: CartesianChartFrame,
    drawingModels: Map<Any, Any>,
  ) {
    chartModel = frame.chart.model
    ranges = frame.ranges
    layerPadding = frame.chart.layerPadding
    this.drawingModels = drawingModels
    scrollEnabled = scrollState.scrollEnabled
    zoomEnabled = scrollState.scrollEnabled && zoomState.zoomEnabled
    this.markerX = markerX
    this.markerSeriesIndex = markerSeriesIndex
  }

  val layerDimensions = remember { MutableCartesianLayerDimensions() }
  val measureLayerDimensions = remember { MutableCartesianLayerDimensions() }
  val markerVisibilityTracker = remember { MarkerVisibilityTracker() }
  val lastHandledModel = remember { ValueWrapper<CartesianChartModel?>(null) }
  val measureExtras =
    remember(measuringContext, chartAreaHeight) {
      { widthPx: Int ->
        val layout = pipeline.layout
        if (layout == null || layout.drawList.isEmpty()) {
          0f
        } else {
          measuringContext.update(layout.frame, layout.drawingModels)
          measuringContext.canvasWidth = widthPx.toFloat()
          measureLayerDimensions.clear()
          layout.frame.chart.getVerticalExtras(
            measuringContext,
            measureLayerDimensions,
            layout.drawList,
          )
        }
      }
    }

  val currentScrollState by rememberUpdatedState(scrollState)
  val onInteraction: (Interaction) -> Boolean = remember {
    { interaction ->
      val frame = pipeline.frame
      val chart = frame?.chart
      // An undrawn chart’s bounds and marker targets aren’t known yet.
      if (
        chart == null ||
          chart.marker == null ||
          !chart.isDrawn ||
          pipeline.display?.drawList.isNullOrEmpty()
      ) {
        false
      } else {
        val scroll = currentScrollState.value
        val x =
          measuringContext.pointerPositionToX(
            interaction.point,
            layerDimensions,
            chart.layerBounds,
            scroll,
            frame.ranges,
          )
        val targets =
          chart.getMarkerTargets(
            x,
            measuringContext.getVisibleXRange(layerDimensions, chart.layerBounds, scroll),
          )
        val (narrowedTargets, seriesIndex) = narrowMarkerTargets(targets, interaction.point.x)
        if (chart.markerController.shouldAcceptInteraction(interaction, narrowedTargets)) {
          val shouldShow = chart.markerController.shouldShowMarker(interaction, narrowedTargets)
          lastAcceptedInteraction = interaction
          if (shouldShow && narrowedTargets.isNotEmpty()) {
            markerX = narrowedTargets.first().x
            markerSeriesIndex = seriesIndex
            // The new selection is positional until it’s resolved while drawing.
            markerVisibilityTracker.selectedTargetKey = null
          } else {
            markerX = null
            markerSeriesIndex = null
          }
          true
        } else {
          false
        }
      }
    }
  }

  fun onViewportChange() {
    val chart = pipeline.frame?.chart?.takeIf { it.isDrawn } ?: return
    lastAcceptedInteraction
      ?.takeIf { chart.markerController.lock == Lock.Position }
      ?.let { onInteraction(it) }
  }

  val committedFrame = pipeline.frame
  val lastReplayedModel = remember { ValueWrapper<CartesianChartModel?>(null) }

  // Observe programmatic zooms and zooms that leave the scroll value unchanged, too.
  LaunchedEffect(scrollState, zoomState) {
    merge(
        scrollState.consumedXDeltas,
        scrollState.unconsumedXDeltas,
        snapshotFlow { zoomState.value },
      )
      .collect { onViewportChange() }
  }

  DisposableEffect(scrollState) {
    scrollState.pendingUpdates = pipeline.pendingUpdates
    onDispose {
      scrollState.clearUpdated()
      if (scrollState.pendingUpdates === pipeline.pendingUpdates) scrollState.pendingUpdates = null
    }
  }

  DisposableEffect(zoomState) {
    zoomState.pendingUpdates = pipeline.pendingUpdates
    onDispose {
      zoomState.clearUpdated()
      if (zoomState.pendingUpdates === pipeline.pendingUpdates) zoomState.pendingUpdates = null
    }
  }

  // Commands waiting for an update that will never be drawn are released.
  DisposableEffect(pipeline) { onDispose { pipeline.pendingUpdates.dispose() } }

  // Bind outside the draw pass, which is skipped when the chart has no area.
  DisposableEffect(zoomState, scrollState) {
    zoomState.setScrollState(scrollState)
    onDispose { zoomState.setScrollState(null) }
  }

  val displayedChart = committedFrame?.chart ?: chart
  ChartHostBox(modifier, chartAreaHeight, measureExtras) {
    Canvas(
      modifier =
        Modifier.fillMaxSize()
          .pointerInput(
            scrollState = scrollState,
            consumeMoveEvents = displayedChart.markerController.consumeMoveEvents,
            onInteraction = if (displayedChart.marker != null) onInteraction else null,
            onZoom =
              remember(zoomState, scrollState, coroutineScope) {
                if (zoomState.zoomEnabled) {
                  { factor, centroid ->
                    // Apply gesture zooms within the pointer event, before the next draw.
                    coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                      zoomState.zoom(factor, centroid.x)
                    }
                  }
                } else {
                  null
                }
              },
            longPressEnabled = displayedChart.markerController.acceptsLongPress,
          )
    ) {
      val display = pipeline.display ?: return@Canvas
      // Once this block is left, the scroll and zoom states reflect `display` as far as they can.
      try {
        if (size.isEmpty()) return@Canvas
        val frame = display.frame
        val chart = frame.chart
        if (display.drawList.isEmpty()) return@Canvas
        measuringContext.update(frame, display.drawingModels)
        measuringContext.canvasWidth = size.width

        layerDimensions.clear()
        chart.prepare(measuringContext, layerDimensions, size, display.drawList)

        if (chart.layerBounds.isEmpty) return@Canvas

        zoomState.update(measuringContext, layerDimensions, chart.layerBounds)
        scrollState.update(measuringContext, chart.layerBounds, layerDimensions)

        val model = chart.model
        val previousModel = lastHandledModel.value
        if (model != previousModel) {
          coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            scrollState.autoScroll(model, previousModel)
          }
          lastHandledModel.value = model
        }

        val drawingContext =
          CartesianDrawingContext(
            measuringContext,
            size,
            drawContext.canvas,
            layerDimensions,
            chart.layerBounds,
            scrollState.value,
            zoomState.value,
            MutableDrawScope(this),
          )

        chart.draw(drawingContext, markerVisibilityTracker, display.drawList)
        measuringContext.cacheStore.purge()
        // A position-locked marker is re-resolved against new data only once the new chart has been
        // drawn, as its marker targets and bounds are only known then.
        if (model != lastReplayedModel.value) {
          val isFirstDraw = lastReplayedModel.value == null
          lastReplayedModel.value = model
          if (!isFirstDraw) onViewportChange()
        }
      } finally {
        pipeline.pendingUpdates.onHandled(display.frame.generation)
      }
    }
  }
}
