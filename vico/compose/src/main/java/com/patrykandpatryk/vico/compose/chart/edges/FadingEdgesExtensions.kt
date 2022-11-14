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
import com.patrykandpatryk.vico.core.FULL_FADE_SCROLL_THRESHOLD_DP
import com.patrykandpatryk.vico.core.chart.edges.FadingEdges

/**
 * [FadingEdges] applies a horizontal fade for scrollable content inside of a chart.
 * A faded edge indicates a possibility to scroll towards the edge to reveal more content.
 *
 * @param startFadingEdgeLength the length in dp unit of a start edge.
 * @param endFadingEdgeLength the length in dp unit of an end edge.
 * @param fullFadeThreshold the amount of scroll in dp unit needed to make the fade fully visible.
 * @param fadeEasing interpolates a fade transition of fading edges.
 */
public fun FadingEdges(
    startFadingEdgeLength: Dp = FadingEdgesDefaults.fadingEdgeLength,
    endFadingEdgeLength: Dp = startFadingEdgeLength,
    fullFadeThreshold: Dp = FadingEdgesDefaults.fullFadeThreshold,
    fadeEasing: Easing = FadingEdgesDefaults.fadeInterpolator,
): FadingEdges = FadingEdges(
    startFadingEdgeLengthDp = startFadingEdgeLength.value,
    endFadingEdgeLengthDp = endFadingEdgeLength.value,
    fullFadeThresholdDp = fullFadeThreshold.value,
) { input -> fadeEasing.transform(input) }

/**
 * Creates and remember [FadingEdges].
 *
 * @param startFadingEdgeLength the length in dp unit of a start edge.
 * @param endFadingEdgeLength the length in dp unit of an end edge.
 * @param fullFadeThreshold the amount of scroll in dp unit needed to make the fade fully visible.
 * @param fadeEasing interpolates a fade transition of fading edges.
 *
 * @see FadingEdges
 */
@Composable
public fun rememberFadingEdges(
    startFadingEdgeLength: Dp = FadingEdgesDefaults.fadingEdgeLength,
    endFadingEdgeLength: Dp = startFadingEdgeLength,
    fullFadeThreshold: Dp = FadingEdgesDefaults.fullFadeThreshold,
    fadeEasing: Easing = FadingEdgesDefaults.fadeInterpolator,
): FadingEdges = remember { FadingEdges() }
    .apply {
        startFadingEdgeLengthDp = startFadingEdgeLength.value
        endFadingEdgeLengthDp = endFadingEdgeLength.value
        fullFadeThresholdDp = fullFadeThreshold.value
        this.fadeInterpolator = remember(fadeEasing) {
            TimeInterpolator { input -> fadeEasing.transform(input) }
        }
    }

/**
 * Creates and remember [FadingEdges].
 *
 * @param fadingEdgesLength the length in dp unit of horizontal edges.
 * @param fullFadeThreshold the amount of scroll in dp unit needed to make the fade fully visible.
 * @param fadeEasing interpolates a fade transition of fading edges.
 *
 * @see FadingEdges
 */
@Composable
public fun rememberFadingEdges(
    fadingEdgesLength: Dp = FadingEdgesDefaults.fadingEdgeLength,
    fullFadeThreshold: Dp = FadingEdgesDefaults.fullFadeThreshold,
    fadeEasing: Easing = FadingEdgesDefaults.fadeInterpolator,
): FadingEdges = rememberFadingEdges(
    startFadingEdgeLength = fadingEdgesLength,
    endFadingEdgeLength = fadingEdgesLength,
    fullFadeThreshold = fullFadeThreshold,
    fadeEasing = fadeEasing,
)

/**
 * The default values for [FadingEdges].
 */
public object FadingEdgesDefaults {

    /**
     * The length in dp unit of an edge.
     */
    public val fadingEdgeLength: Dp = 0.dp

    /**
     * The amount of scroll in dp unit needed to make the fade fully visible.
     */
    public val fullFadeThreshold: Dp = FULL_FADE_SCROLL_THRESHOLD_DP.dp

    /**
     * Interpolates a fade transition of fading edges.
     */
    public val fadeInterpolator: Easing = FastOutSlowInEasing
}
