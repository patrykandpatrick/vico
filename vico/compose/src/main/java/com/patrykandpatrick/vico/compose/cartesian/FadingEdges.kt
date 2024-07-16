/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

import android.animation.TimeInterpolator
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.cartesian.FadingEdges
import com.patrykandpatrick.vico.core.common.Defaults.FADING_EDGE_VISIBILITY_THRESHOLD_DP
import com.patrykandpatrick.vico.core.common.Defaults.FADING_EDGE_WIDTH_DP

/**
 * Creates and remembers a [FadingEdges] instance.
 *
 * @param startEdgeWidth the width of the fade overlay for the start edge (in dp).
 * @param endEdgeWidth the width of the fade overlay for the end edge (in dp).
 * @param visibilityThreshold the scroll distance over which the overlays fade in and out (in dp).
 * @param visibilityEasing used for the fading edges’ fade-in and fade-out animations. This is a
 *   mapping of the degree to which [visibilityThreshold] has been satisfied to the opacity of the
 *   fading edges.
 * @see FadingEdges
 */
@Composable
public fun rememberFadingEdges(
  startEdgeWidth: Dp = FadingEdgesDefaults.edgeWidth,
  endEdgeWidth: Dp = FadingEdgesDefaults.edgeWidth,
  visibilityThreshold: Dp = FadingEdgesDefaults.visibilityThreshold,
  visibilityEasing: Easing = FadingEdgesDefaults.visibilityEasing,
): FadingEdges =
  remember(startEdgeWidth, endEdgeWidth, visibilityThreshold, visibilityEasing) {
    FadingEdges(
      startEdgeWidth.value,
      endEdgeWidth.value,
      visibilityThreshold.value,
      TimeInterpolator(visibilityEasing::transform),
    )
  }

/**
 * Creates and remembers a [FadingEdges] instance.
 *
 * @param edgeWidth the width of the fade overlay.
 * @param visibilityThreshold the scroll distance over which the overlays fade in and out (in dp).
 * @param visibilityEasing used for the fading edges’ fade-in and fade-out animations. This is a
 *   mapping of the degree to which [visibilityThreshold] has been satisfied to the opacity of the
 *   fading edges.
 * @see FadingEdges
 */
@Composable
public fun rememberFadingEdges(
  edgeWidth: Dp = FadingEdgesDefaults.edgeWidth,
  visibilityThreshold: Dp = FadingEdgesDefaults.visibilityThreshold,
  visibilityEasing: Easing = FadingEdgesDefaults.visibilityEasing,
): FadingEdges =
  rememberFadingEdges(
    startEdgeWidth = edgeWidth,
    endEdgeWidth = edgeWidth,
    visibilityThreshold = visibilityThreshold,
    visibilityEasing = visibilityEasing,
  )

private object FadingEdgesDefaults {
  val edgeWidth = FADING_EDGE_WIDTH_DP.dp
  val visibilityThreshold = FADING_EDGE_VISIBILITY_THRESHOLD_DP.dp
  val visibilityEasing = FastOutSlowInEasing
}
