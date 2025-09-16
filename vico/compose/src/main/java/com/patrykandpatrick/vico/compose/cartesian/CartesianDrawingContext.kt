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

package com.patrykandpatrick.vico.compose.cartesian

import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions

@Composable
internal fun rememberCartesianDrawingContext(
  measuringContext: CartesianMeasuringContext,
  layerDimensions: CartesianLayerDimensions,
  layerBounds: RectF,
  scroll: Float,
  zoom: Float,
) =
  remember(measuringContext, layerDimensions, layerBounds, scroll, zoom) {
    CartesianDrawingContext(measuringContext, layerDimensions, layerBounds, scroll, zoom)
  }
