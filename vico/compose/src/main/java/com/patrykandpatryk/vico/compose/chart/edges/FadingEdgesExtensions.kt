/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.compose.chart.edges

import android.animation.TimeInterpolator
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.core.FADING_EDGES_VISIBILITY_THRESHOLD_DP
import com.patrykandpatryk.vico.core.chart.edges.FadingEdges

/**
 * [FadingEdges] applies a horizontal fade to the edges of the chart area for scrollable charts.
 * This effect indicates that there’s more content beyond a given edge, and the user can scroll to reveal it.
 *
 * @param startFadingEdgeLength the width of the fade overlay for the start edge (in dp).
 * @param endFadingEdgeLength the width of the fade overlay for the end edge (in dp).
 * @param visibilityThreshold the scroll distance over which the overlays fade in and out (in dp).
 * @param fadeEasing used for the fading edges’ fade-in and fade-out animations. This is a mapping of the degree to
 * which [visibilityThreshold] has been satisfied to the opacity of the fading edges.
 */
public fun FadingEdges(
    startFadingEdgeLength: Dp = FadingEdgesDefaults.fadingEdgeLength,
    endFadingEdgeLength: Dp = startFadingEdgeLength,
    visibilityThreshold: Dp = FadingEdgesDefaults.visibilityThreshold,
    fadeEasing: Easing = FadingEdgesDefaults.fadeInterpolator,
): FadingEdges = FadingEdges(
    startFadingEdgeLengthDp = startFadingEdgeLength.value,
    endFadingEdgeLengthDp = endFadingEdgeLength.value,
    visibilityThresholdDp = visibilityThreshold.value,
) { input -> fadeEasing.transform(input) }

/**
 * Creates and remembers a [FadingEdges] instance.
 *
 * @param startFadingEdgeLength the width of the fade overlay for the start edge (in dp).
 * @param endFadingEdgeLength the width of the fade overlay for the end edge (in dp).
 * @param visibilityThreshold the scroll distance over which the overlays fade in and out (in dp).
 * @param fadeEasing used for the fading edges’ fade-in and fade-out animations. This is a mapping of the degree to
 * which [visibilityThreshold] has been satisfied to the opacity of the fading edges.
 *
 * @see FadingEdges
 */
@Composable
public fun rememberFadingEdges(
    startFadingEdgeLength: Dp = FadingEdgesDefaults.fadingEdgeLength,
    endFadingEdgeLength: Dp = startFadingEdgeLength,
    visibilityThreshold: Dp = FadingEdgesDefaults.visibilityThreshold,
    fadeEasing: Easing = FadingEdgesDefaults.fadeInterpolator,
): FadingEdges = remember { FadingEdges() }
    .apply {
        startFadingEdgeLengthDp = startFadingEdgeLength.value
        endFadingEdgeLengthDp = endFadingEdgeLength.value
        visibilityThresholdDp = visibilityThreshold.value
        this.fadeInterpolator = remember(fadeEasing) {
            TimeInterpolator { input -> fadeEasing.transform(input) }
        }
    }

/**
 * Creates and remembers a [FadingEdges] instance.
 *
 * @param fadingEdgesLength the width of the fade overlay.
 * @param visibilityThreshold the scroll distance over which the overlays fade in and out (in dp).
 * @param fadeEasing used for the fading edges’ fade-in and fade-out animations. This is a mapping of the degree to
 * which [visibilityThreshold] has been satisfied to the opacity of the fading edges.
 *
 * @see FadingEdges
 */
@Composable
public fun rememberFadingEdges(
    fadingEdgesLength: Dp = FadingEdgesDefaults.fadingEdgeLength,
    visibilityThreshold: Dp = FadingEdgesDefaults.visibilityThreshold,
    fadeEasing: Easing = FadingEdgesDefaults.fadeInterpolator,
): FadingEdges = rememberFadingEdges(
    startFadingEdgeLength = fadingEdgesLength,
    endFadingEdgeLength = fadingEdgesLength,
    visibilityThreshold = visibilityThreshold,
    fadeEasing = fadeEasing,
)

/**
 * The default values for [FadingEdges].
 */
public object FadingEdgesDefaults {

    /**
     * The width of the fade overlays (in dp).
     */
    public val fadingEdgeLength: Dp = 0.dp

    /**
     * The scroll distance over which the overlays fade in and out (in dp).
     */
    public val visibilityThreshold: Dp = FADING_EDGES_VISIBILITY_THRESHOLD_DP.dp

    /**
     * Used for the fading edges’ fade-in and fade-out animations. This is a mapping of the degree to which the fading
     * edges’ visibility threshold has been satisfied to the opacity of the fading edges.
     */
    public val fadeInterpolator: Easing = FastOutSlowInEasing
}
