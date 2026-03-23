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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.defaultCartesianDiffAnimationSpec

/** Holds scroll, zoom, and follow state for a live timeline chart. */
public data class LiveTimelineChartState(
  public val scrollState: VicoScrollState,
  public val zoomState: VicoZoomState,
  public val followController: LiveTimelineFollowController,
)

internal class CartesianViewportSnapshotHolder {
  var snapshot: CartesianViewportSnapshot? = null
}

/**
 * Remembers a [LiveTimelineChartState] for [LiveTimelineCartesianChartHost].
 *
 * Pinch-zoom is disabled; the visible time window is driven by [visibleDurationMillis] (in the same
 * unit as chart _x_, typically milliseconds) via [Zoom.x].
 *
 * @param minVisibleDurationMillis narrowest window (largest zoom) allowed.
 * @param maxVisibleDurationMillis widest window (smallest zoom) allowed.
 */
@Composable
public fun rememberLiveTimelineChartState(
  visibleDurationMillis: Long,
  nowProvider: () -> Long = defaultLiveTimelineNowProvider(),
  followConfig: LiveTimelineFollowConfig = LiveTimelineFollowConfig.Default,
  minVisibleDurationMillis: Double = 10_000.0,
  maxVisibleDurationMillis: Double = 86_400_000.0 * 7.0,
  scrollState: VicoScrollState =
    rememberVicoScrollState(autoScrollCondition = AutoScrollCondition.Never),
): LiveTimelineChartState {
  val zoomState =
    rememberVicoZoomState(
      zoomEnabled = false,
      initialZoom = Zoom.x(visibleDurationMillis.toDouble()),
      minZoom = Zoom.x(maxVisibleDurationMillis),
      maxZoom = Zoom.x(minVisibleDurationMillis),
    )
  val followController =
    remember { LiveTimelineFollowController(nowProvider = nowProvider, config = followConfig) }
  SideEffect {
    followController.nowProvider = nowProvider
    followController.config = followConfig
  }
  LaunchedEffect(visibleDurationMillis) {
    zoomState.zoom(Zoom.x(visibleDurationMillis.toDouble()))
  }
  return LiveTimelineChartState(
    scrollState = scrollState,
    zoomState = zoomState,
    followController = followController,
  )
}

/**
 * Displays a Cartesian line chart whose horizontal axis is time-like (for example epoch
 * milliseconds), keeping the current instant from [LiveTimelineChartState.followController]’s
 * [LiveTimelineFollowController.nowProvider] centered while [LiveTimelineFollowMode.Following] is
 * active. User horizontal drag switches to browsing without fling (including on desktop and web).
 */
@Composable
public fun LiveTimelineCartesianChartHost(
  chart: CartesianChart,
  modelProducer: CartesianChartModelProducer,
  state: LiveTimelineChartState,
  modifier: Modifier = Modifier,
  animationSpec: AnimationSpec<Float>? = defaultCartesianDiffAnimationSpec,
  animateIn: Boolean = true,
  placeholder: @Composable BoxScope.() -> Unit = {},
) {
  val viewportHolder = remember { CartesianViewportSnapshotHolder() }
  LaunchedEffect(state.scrollState, state.followController) {
    while (true) {
      withFrameNanos { }
      val snap = viewportHolder.snapshot ?: continue
      when (val result = state.followController.onFrame(snap, state.scrollState.value)) {
        is LiveTimelineFollowFrameResult.NoOp -> Unit
        is LiveTimelineFollowFrameResult.ScrollToAbsolutePixels ->
          state.scrollState.scroll(Scroll.Absolute.pixels(result.pixels))
      }
    }
  }
  CartesianChartHost(
    chart = chart,
    modelProducer = modelProducer,
    modifier = modifier,
    scrollState = state.scrollState,
    zoomState = state.zoomState,
    animationSpec = animationSpec,
    animateIn = animateIn,
    placeholder = placeholder,
    onViewportMeasured = { viewportHolder.snapshot = it },
    onHorizontalScrollDragStarted = { state.followController.notifyUserPanStarted() },
    horizontalPointerFlingEnabled = false,
  )
}

/**
 * Convenience overload that calls [rememberLiveTimelineChartState] for the given
 * [visibleDurationMillis].
 */
@Composable
public fun LiveTimelineCartesianChartHost(
  chart: CartesianChart,
  modelProducer: CartesianChartModelProducer,
  visibleDurationMillis: Long,
  modifier: Modifier = Modifier,
  nowProvider: () -> Long = defaultLiveTimelineNowProvider(),
  followConfig: LiveTimelineFollowConfig = LiveTimelineFollowConfig.Default,
  animationSpec: AnimationSpec<Float>? = defaultCartesianDiffAnimationSpec,
  animateIn: Boolean = true,
  placeholder: @Composable BoxScope.() -> Unit = {},
) {
  val state =
    rememberLiveTimelineChartState(
      visibleDurationMillis = visibleDurationMillis,
      nowProvider = nowProvider,
      followConfig = followConfig,
    )
  LiveTimelineCartesianChartHost(
    chart = chart,
    modelProducer = modelProducer,
    state = state,
    modifier = modifier,
    animationSpec = animationSpec,
    animateIn = animateIn,
    placeholder = placeholder,
  )
}

/** Overload that accepts a static [CartesianChartModel]. */
@Composable
public fun LiveTimelineCartesianChartHost(
  chart: CartesianChart,
  model: CartesianChartModel,
  state: LiveTimelineChartState,
  modifier: Modifier = Modifier,
) {
  val viewportHolder = remember { CartesianViewportSnapshotHolder() }
  LaunchedEffect(state.scrollState, state.followController) {
    while (true) {
      withFrameNanos { }
      val snap = viewportHolder.snapshot ?: continue
      when (val result = state.followController.onFrame(snap, state.scrollState.value)) {
        is LiveTimelineFollowFrameResult.NoOp -> Unit
        is LiveTimelineFollowFrameResult.ScrollToAbsolutePixels ->
          state.scrollState.scroll(Scroll.Absolute.pixels(result.pixels))
      }
    }
  }
  CartesianChartHost(
    chart = chart,
    model = model,
    modifier = modifier,
    scrollState = state.scrollState,
    zoomState = state.zoomState,
    onViewportMeasured = { viewportHolder.snapshot = it },
    onHorizontalScrollDragStarted = { state.followController.notifyUserPanStarted() },
    horizontalPointerFlingEnabled = false,
  )
}
