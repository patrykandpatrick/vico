/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.multiplatform.common

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.multiplatform.common.data.CacheStore

/** Holds data used for measuring and drawing. */
public interface MeasuringContext {
  /** The [Canvas] size. */
  public val canvasSize: Size

  /** The [FontFamily.Resolver]. */
  public val fontFamilyResolver: FontFamily.Resolver

  /** The [Density]. */
  public val density: Density

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
