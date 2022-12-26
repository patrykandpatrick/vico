/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.chart.edges

import android.animation.TimeInterpolator
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.FADING_EDGE_VISIBILITY_THRESHOLD_DP
import com.patrykandpatrick.vico.core.FADING_EDGE_WIDTH_DP
import com.patrykandpatrick.vico.core.chart.edges.FadingEdges

/**
 * [FadingEdges] applies a horizontal fade to the edges of the chart area for scrollable charts.
 * This effect indicates that there’s more content beyond a given edge, and the user can scroll to reveal it.
 *
 * @param startEdgeWidth the width of the fade overlay for the start edge (in dp).
 * @param endEdgeWidth the width of the fade overlay for the end edge (in dp).
 * @param visibilityThreshold the scroll distance over which the overlays fade in and out (in dp).
 * @param visibilityEasing used for the fading edges’ fade-in and fade-out animations. This is a mapping of the degree
 * to which [visibilityThreshold] has been satisfied to the opacity of the fading edges.
 */
public fun FadingEdges(
    startEdgeWidth: Dp = FadingEdgesDefaults.edgeWidth,
    endEdgeWidth: Dp = startEdgeWidth,
    visibilityThreshold: Dp = FadingEdgesDefaults.visibilityThreshold,
    visibilityEasing: Easing = FadingEdgesDefaults.visibilityEasing,
): FadingEdges = FadingEdges(
    startEdgeWidthDp = startEdgeWidth.value,
    endEdgeWidthDp = endEdgeWidth.value,
    visibilityThresholdDp = visibilityThreshold.value,
) { input -> visibilityEasing.transform(input) }

/**
 * Creates and remembers a [FadingEdges] instance.
 *
 * @param startEdgeWidth the width of the fade overlay for the start edge (in dp).
 * @param endEdgeWidth the width of the fade overlay for the end edge (in dp).
 * @param visibilityThreshold the scroll distance over which the overlays fade in and out (in dp).
 * @param visibilityEasing used for the fading edges’ fade-in and fade-out animations. This is a mapping of the degree
 * to which [visibilityThreshold] has been satisfied to the opacity of the fading edges.
 *
 * @see FadingEdges
 */
@Composable
public fun rememberFadingEdges(
    startEdgeWidth: Dp = FadingEdgesDefaults.edgeWidth,
    endEdgeWidth: Dp = startEdgeWidth,
    visibilityThreshold: Dp = FadingEdgesDefaults.visibilityThreshold,
    visibilityEasing: Easing = FadingEdgesDefaults.visibilityEasing,
): FadingEdges = remember { FadingEdges() }
    .apply {
        startEdgeWidthDp = startEdgeWidth.value
        endEdgeWidthDp = endEdgeWidth.value
        visibilityThresholdDp = visibilityThreshold.value
        this.visibilityInterpolator = remember(visibilityEasing) { TimeInterpolator(visibilityEasing::transform) }
    }

/**
 * Creates and remembers a [FadingEdges] instance.
 *
 * @param edgeWidth the width of the fade overlay.
 * @param visibilityThreshold the scroll distance over which the overlays fade in and out (in dp).
 * @param visibilityEasing used for the fading edges’ fade-in and fade-out animations. This is a mapping of the degree
 * to which [visibilityThreshold] has been satisfied to the opacity of the fading edges.
 *
 * @see FadingEdges
 */
@Composable
public fun rememberFadingEdges(
    edgeWidth: Dp = FadingEdgesDefaults.edgeWidth,
    visibilityThreshold: Dp = FadingEdgesDefaults.visibilityThreshold,
    visibilityEasing: Easing = FadingEdgesDefaults.visibilityEasing,
): FadingEdges = rememberFadingEdges(
    startEdgeWidth = edgeWidth,
    endEdgeWidth = edgeWidth,
    visibilityThreshold = visibilityThreshold,
    visibilityEasing = visibilityEasing,
)

/**
 * The default values for [FadingEdges].
 */
public object FadingEdgesDefaults {

    /**
     * The width of the fade overlays (in dp).
     */
    public val edgeWidth: Dp = FADING_EDGE_WIDTH_DP.dp

    /**
     * The scroll distance over which the overlays fade in and out (in dp).
     */
    public val visibilityThreshold: Dp = FADING_EDGE_VISIBILITY_THRESHOLD_DP.dp

    /**
     * Used for the fading edges’ fade-in and fade-out animations. This is a mapping of the degree to which the fading
     * edges’ visibility threshold has been satisfied to the opacity of the fading edges.
     */
    public val visibilityEasing: Easing = FastOutSlowInEasing
}
