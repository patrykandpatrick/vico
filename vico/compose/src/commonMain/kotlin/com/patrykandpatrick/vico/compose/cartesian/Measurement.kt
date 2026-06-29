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
 * A snapshot of the layout data from a single measurement, used by
 * [DataUpdateScrollAnchor.VisibleXRange] to keep the same _x_ coordinates visible when the dataset
 * changes.
 *
 * @property minX the smallest _x_ value in the chart’s data.
 * @property xStep the difference between neighboring _x_ values.
 * @property xSpacing the horizontal distance, in pixels, between neighboring points.
 * @property startPadding the padding, in pixels, before the first point at the chart’s start edge.
 */
internal class Measurement(
  val minX: Double,
  val xStep: Double,
  val xSpacing: Float,
  val startPadding: Float,
)
