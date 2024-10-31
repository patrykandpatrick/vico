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
import com.patrykandpatrick.vico.core.common.data.ExtraStore


@Immutable
public fun interface PaddingProvider {
  public fun getCartesianLayerPadding(
    extraStore: ExtraStore,
  ): CartesianLayerPadding

  public companion object {
    public fun fixed(
      scalableStartDp: Float = 0f,
      scalableEndDp: Float = 0f,
      unscalableStartDp: Float = 0f,
      unscalableEndDp: Float = 0f,
      unscalableTopDp: Float = 0f,
      unscalableBottomDp: Float = 0f,
    ): PaddingProvider = PaddingProvider { _ ->
      CartesianLayerPadding(
        scalableStartDp = scalableStartDp,
        scalableEndDp = scalableEndDp,
        unscalableStartDp = unscalableStartDp,
        unscalableEndDp = unscalableEndDp,
        unscalableTopDp = unscalableTopDp,
        unscalableBottomDp = unscalableBottomDp,
      )
    }
  }
}

/**
 * Stores [CartesianLayer] padding values. [scalableStartDp] and [scalableEndDp] are multiplied by
 * the zoom factor, unlike [unscalableStartDp] and [unscalableEndDp].
 */
@Immutable
public class CartesianLayerPadding(
  internal val scalableStartDp: Float = 0f,
  internal val scalableEndDp: Float = 0f,
  internal val unscalableStartDp: Float = 0f,
  internal val unscalableEndDp: Float = 0f,
  internal val unscalableTopDp: Float = 0f,
  internal val unscalableBottomDp: Float = 0f,
) {
  override fun equals(other: Any?): Boolean =
    this === other ||
      other is CartesianLayerPadding &&
      scalableStartDp == other.scalableStartDp &&
      scalableEndDp == other.scalableEndDp &&
      unscalableStartDp == other.unscalableStartDp &&
      unscalableEndDp == other.unscalableEndDp

  override fun hashCode(): Int {
    var result = scalableStartDp.hashCode()
    result = 31 * result + scalableEndDp.hashCode()
    result = 31 * result + unscalableStartDp.hashCode()
    result = 31 * result + unscalableEndDp.hashCode()
    return result
  }
}
