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
 * Defines what happens to a [CartesianChart]’s scroll value when its data changes (e.g., when points
 * are added or removed).
 */
public enum class DataUpdateScrollAnchor {
  /**
   * Keeps the raw scroll value (the number of pixels from the chart’s start edge). When data is
   * added on the left—e.g., older history is prepended—the start edge shifts, so the viewport jumps
   * to different points. This is Vico’s original behavior.
   */
  Start,

  /**
   * Keeps the same _x_ coordinates visible across data updates. When data is added on the left—e.g.,
   * older history is prepended—the scroll value is offset by the inserted width so that the points
   * that were on-screen stay in place, and the chart doesn’t jump. The adjustment is applied during
   * measurement, before the chart is drawn, so there’s no flicker. (Automatic scrolling, configured
   * via [VicoScrollState]’s `autoScroll` and `autoScrollCondition`, still takes effect afterward.)
   */
  VisibleXRange,
}
