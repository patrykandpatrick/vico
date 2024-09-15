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

package com.patrykandpatrick.vico.core.cartesian

import androidx.compose.runtime.Immutable
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer

/**
 * Stores [CartesianLayer] padding values. [scalableStartPaddingDp] and [scalableEndPaddingDp] are
 * multiplied by the zoom factor, unlike [unscalableStartPaddingDp] and [unscalableEndPaddingDp].
 */
@Immutable
public class CartesianLayerPadding(
  internal val scalableStartPaddingDp: Float = 0f,
  internal val scalableEndPaddingDp: Float = 0f,
  internal val unscalableStartPaddingDp: Float = 0f,
  internal val unscalableEndPaddingDp: Float = 0f,
) {
  override fun equals(other: Any?): Boolean =
    this === other ||
      other is CartesianLayerPadding &&
        scalableStartPaddingDp == other.scalableStartPaddingDp &&
        scalableEndPaddingDp == other.scalableEndPaddingDp &&
        unscalableStartPaddingDp == other.unscalableStartPaddingDp &&
        unscalableEndPaddingDp == other.unscalableEndPaddingDp

  override fun hashCode(): Int {
    var result = scalableStartPaddingDp.hashCode()
    result = 31 * result + scalableEndPaddingDp.hashCode()
    result = 31 * result + unscalableStartPaddingDp.hashCode()
    result = 31 * result + unscalableEndPaddingDp.hashCode()
    return result
  }
}
