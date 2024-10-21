/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.cartesian.marker

import androidx.compose.runtime.Immutable

/** Allows for listening to [CartesianMarker] visibility changes. */
@Immutable
public interface CartesianMarkerVisibilityListener {
  /** Called when the specified [CartesianMarker] is shown. */
  public fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {}

  /** Called when the specified [CartesianMarker]’s [CartesianMarker.Target]s change. */
  public fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {}

  /** Called when the specified [CartesianMarker] is hidden. */
  public fun onHidden(marker: CartesianMarker) {}
}
