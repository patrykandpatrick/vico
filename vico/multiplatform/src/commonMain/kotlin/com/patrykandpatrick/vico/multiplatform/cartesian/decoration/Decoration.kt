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

package com.patrykandpatrick.vico.multiplatform.cartesian.decoration

import androidx.compose.runtime.Immutable
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayer

/**
 * A [Decoration] presents additional information on a [CartesianChart].
 *
 * @see [HorizontalBox]
 * @see [HorizontalLine]
 */
@Immutable
public interface Decoration {
  /** Draws content under the [CartesianLayer]s. */
  public fun drawUnderLayers(context: CartesianDrawingContext) {}

  /** Draws content over the [CartesianLayer]s. */
  public fun drawOverLayers(context: CartesianDrawingContext) {}
}
