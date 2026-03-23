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

/**
 * Immutable snapshot of horizontal viewport geometry for a [CartesianChart], suitable for mapping
 * between scroll position and the chart’s _x_ domain (for example epoch milliseconds on a time
 * axis).
 *
 * @param scrollValue current scroll offset in pixels.
 * @param maxScrollValue maximum scroll offset in pixels.
 * @param fullXRangeStart start of the chart’s full horizontal _x_ range (including layer padding),
 *   matching the chart’s internal “full range” used for scrolling.
 * @param layoutDirectionMultiplier `1` for LTR layouts and `−1` for RTL layouts.
 * @param xSpacing horizontal distance in pixels between consecutive major _x_ steps.
 * @param xStep difference in _x_ between neighboring major steps ([CartesianChartRanges.xStep]).
 * @param layerBoundsWidth width in pixels of the chart layer area.
 */
public data class CartesianViewportSnapshot(
  public val scrollValue: Float,
  public val maxScrollValue: Float,
  public val fullXRangeStart: Double,
  public val layoutDirectionMultiplier: Int,
  public val xSpacing: Float,
  public val xStep: Double,
  public val layerBoundsWidth: Float,
)
