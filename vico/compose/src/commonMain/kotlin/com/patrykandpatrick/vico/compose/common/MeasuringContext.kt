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

package com.patrykandpatrick.vico.compose.common

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.common.data.CacheStore
import com.patrykandpatrick.vico.compose.common.data.ExtraStore

/** Holds data used for measuring and drawing. */
public interface MeasuringContext {
  /** The [Canvas] width. */
  public val canvasWidth: Float

  /** The [Canvas] size. */
  @Deprecated(
    "With the chart hosts’ `chartAreaHeight` parameter, the canvas height isn’t necessarily " +
      "known by the measuring phase, so `MeasuringContext` exposes only `canvasWidth`. When " +
      "drawing, read the full size from `DrawingContext.canvasSize` instead; if you see this " +
      "warning while drawing, narrow your receiver from `MeasuringContext` to its subtype " +
      "`DrawingContext`. If you need the height while measuring and your chart’s height is fixed " +
      "(e.g., via `Modifier.height`), use that value directly, hoisting it into a constant " +
      "shared by the modifier and your code. Otherwise, don’t depend on the height here. Until " +
      "`canvasSize` is removed, `canvasSize.height` stays exact for a pinned height, and for the " +
      "automatic case, it represents the chart-area height."
  )
  public val canvasSize: Size

  /** The [FontFamily.Resolver]. */
  public val fontFamilyResolver: FontFamily.Resolver

  /** The [Density]. */
  public val density: Density

  /** Houses auxiliary drawing data. */
  public val extraStore: ExtraStore

  /** The number of pixels corresponding to this number of density-independent pixels. */
  public val Dp.pixels: Float
    get() = with(density) { toPx() }

  /** The layout direction. */
  public val layoutDirection: LayoutDirection

  /** Whether [layoutDirection] is [LayoutDirection.Ltr]. */
  public val isLtr: Boolean
    get() = layoutDirection == LayoutDirection.Ltr

  /** Caches drawing data. */
  public val cacheStore: CacheStore

  /** 1 if [isLtr] is true; −1 otherwise. */
  public val layoutDirectionMultiplier: Int
    get() = if (isLtr) 1 else -1
}
